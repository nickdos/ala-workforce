package au.org.ala.workforce

import grails.converters.deep.JSON
import static AnswerDataType.*
import java.text.NumberFormat
import java.text.ParseException
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * OO representation of a question.
 *
 * Created by markew
 * Date: Dec 14, 2010
 * Time: 8:50:48 AM
 */
class QuestionModel {

    int qset                        // a unique int that identifies the question qset
    int questionNumber              // the ordinal for this level of question
    int level                       // the level this question has in the hierarchy
    String guid                     // a unique identifier of the question - independent of question set or question hierarchy
    String label                    // type of displayed label, eg none, 3, b), iii)
    String qtext                    // the question text
    String subtext                  // sub-heading to the question text
    String shorttext                // abbreviated question text for answers report
    String aggregationText          // question label for aggregation report
    QuestionType qtype              // question type, eg rank, pick, range, matrix, group, none
    Object qdata                    // json string describing data - format depends on the qType
    String instruction              // optional instructions
    String instructionPosition = 'bottom'
                                    // optional positioning for instructions eg "top", default is "bottom"
    AnswerType atype = AnswerType.text
                                    // the type of the answer widget, eg range, radio, percent, text, rank, boolean, none
    AnswerDataType datatype = AnswerDataType.text
                                    // the data type of the answer, eg bool, number, text, percent, numberRange
    String alabel                   // text that labels the answer widget - may be units eg 'hours per week'
    Object adata                    // data for a answer , eg a pick list - may be a reference to an external list eg states of australia
    String displayHint              // suggested form of display, eg dropdown, radio, checkbox
    String layoutHint               // directs layout of child questions
    boolean required                // the answer may not be blank
    String requiredIf               // the answer may not be blank if the condition is met
    List validation                 // list of cross-question validations
    String onchangeAction           // js function to call when answer value is changed
    String dependentOn              // the entire question can be dependent on the answer to another question - this holds the path and the condition

    QuestionModel owner            // reverse link

    List<QuestionModel> questions = [] // sub questions

    Object aggregations             // data aggregation properties

    /* transient values */
    boolean disabled = false        // can be set based on the answer to a contingent question
    String answerValueStr           // any answer that may be supplied for validation - this usually holds the answer supplied
                                    //  by a user. It is held here during validation and error feedback
    String errorMessage = ""        // any error that results from validation

    QuestionModel() {}

    QuestionModel(record) {

        //println "creating questionModel for q${record.level1}_${record.level2}_${record.level3}"
        
        // determine level from the values of level1, level2 and level 3
        if (record.level3 > 0) {
            this.level = 3
        } else if (record.level2 > 0) {
            this.level = 2
        } else {
            this.level = 1
        }

        // the number of this question is the value at its level
        this.questionNumber = record."level${this.level}"

        // JSON objects
        if (record.adata) {
            this.adata = JSON.parse(record.adata)
        }
        if (record.qdata) {
            this.qdata = JSON.parse(record.qdata)
        }
        if (record.validation) {
            this.validation = record.validation.tokenize(' ')
        }
        if (record.aggregation) {
            this.aggregations = JSON.parse(record.aggregation)
        }

        //println "loading props for ${record.level1}_${record.level2}_${record.level3}"
        
        // other properties
        ['atype','qtype','label','qtext','shorttext','instruction','alabel','displayHint','layoutHint','datatype',
                'subtext','required','requiredIf','instructionPosition','guid','qset','onchangeAction','dependentOn'].each {
            if (record."${it}") {
                this."${it}" = record."${it}"
            }
        }
    }

    boolean isAnswerTrue() {
        return datatype == AnswerDataType.bool && answerValueStr in ['on','yes','true']
    }

    boolean isDisabled() {
        return disabled || owner?.isDisabled()
    }

    /**
     * Override the dynamic setter to filter the values being set.
     *
     * @param answer
     * @return
     */
    public void setAnswerValueStr(String answer) {
        /* This handles the case where a boolean checkbox on a parent question acts as a 'gatekeeper' to the
         * rest of the question. We don't want to store any entered or default values from the sub-questions
         * if the parent checkbox is not ticked.
         */
//        if (owner && owner.atype == AnswerType.bool && !owner.isAnswerTrue()) {
//            // do not set the answer as it has no meaning
//            return
//        }

        /* This handles the case where selecting other in the previous sibling question acts as a 'gatekeeper' to the
         * 'If other please specify' text. We don't want to store any entered or default values from the 'if other'
         * questions if the other option is not selected.
         *
         * Implemented by reversing the 'requiredIf' link.
        if (requiredIf) {
            def rif = parseCondition(requiredIf)
            def onlyIf = getQuestionFromPath(rif.path)
            switch (rif.comparator) {
                case "=":
                    if (onlyIf && onlyIf.answerValueStr != rif.value) {
                        // do not set the answer as they have not selected the gatekeeper option
                        return
                    }
                    break
                case "=~":
                    if (onlyIf && !(onlyIf.answerValueStr =~ rif.value)) {
                        // do not set the answer as they have not selected the gatekeeper option
                        return
                    }
                    break
                case "groovyTruth":
                    if (onlyIf && !onlyIf?.answerValueStr) {
                        // do not set the answer as they have not answered the gatekeeper option
                        return
                    }
                    break
            }
        }
         */

        /* This handles case where textarea contains only whitespace
         */
        if (this.atype == AnswerType.textarea && answer && answer.trim() == '') {
            return
        }

        // the default action is to set the property
        this.answerValueStr = answer
    }

    QuestionModel previousSibling() {
        if (owner && questionNumber > 1) {
            return owner.questions[questionNumber - 2]
        }
        return null
    }

    def validate = {params ->
        def valid = true
        clearErrors()
        //println "validating ${ident()} atype=${atype} datatype=${datatype} requiredIf=${requiredIf}"
        if (atype != AnswerType.none) {
            //if (answerValueStr) {println "answer is ${answerValueStr}"}
            if (answerValueStr) {
                // validate my answer
                switch (datatype) {
                    case bool:
                        // only needs to have a value
                        valid = answerValueStr
                        break
                    case text:
                        // don't allow 'other' text if 'other' is not selected
                        if (badOtherText()) {
                            valid = false
                            if (this.owner && this.owner.atype == AnswerType.bool) {
                                errorMessage = "You have provided additional text but not selected 'Yes'."
                            } else {
                                errorMessage = "You have provided 'if other' text but not selected the 'other' option."
                            }
                        }
                        else {
                            // otherwise only needs to have a value
                            valid = answerValueStr
                        }
                        break
                    case rank:
                        // must be 1) a number, 2) within range
                        // note that this is unlikely to be called as the ranking-group validation on the
                        // parent question happens first
                        if (answerValueStr.isInteger()) {
                            def val = NumberFormat.getIntegerInstance().parse(answerValueStr)
                            int max = this.owner.qdata?.max
                            if (val < 1 || val > max) {
                                valid = false
                                errorMessage = "Rank value must be between 1 and ${max}. Value is ${val}"
                            }
                        }
                        else {
                            valid = false
                            errorMessage = "${answerValueStr} is not a valid integer"
                        }
                        break
                    case [number, integer]:
                        // remove any commas
                        answerValueStr = answerValueStr.findAll {it != ','}.join()

                        // must be a number
                        if (answerValueStr.isNumber()) {
                            def val = NumberFormat.getInstance().parse(answerValueStr)
                            // if it's also a percent, it must be in range
                            if (atype == AnswerType.percent && (val < 0 || val > 100)) {
                                valid = false
                                errorMessage = "A percentage must be between 0 and 100. Value is ${val}"
                            }
                            // check if it's an integer if the dataType integer
                            if (datatype == integer && !answerValueStr.isInteger()) {
                                valid = false
                                errorMessage = "Answer must be a whole number"
                            }
                            if (adata instanceof JSONObject && adata?.has('min')) {
                                def min = adata.min
                                if (val < min) {
                                    valid = false
                                    errorMessage = "Number must not be less than ${min}"
                                }
                            }
                            else {
                                // default to a min of 0
                                if (val < 0) {
                                    valid = false
                                    errorMessage = "Number cannot be less than 0"
                                }
                            }
                            if (adata instanceof JSONObject && adata?.has('max')) {
                                def max = adata.max
                                if (val > max) {
                                    valid = false
                                    errorMessage = "Number must not be greater than ${max}"
                                }
                            }
                            else if (datatype == integer) {
                                if (val > 2000000000) {
                                    valid = false
                                    errorMessage = "Number must not be greater than 2 billion"
                                }
                            }

                            // perform any validations between fields
                            if (this.owner.qdata instanceof JSONObject && this.owner.qdata.has('validation')) {
                                def validations = this.owner.qdata.validation
                                if (validations instanceof String) {
                                    validations = [validations]
                                }
                                def thisColumn = getThisColumnNumber()
                                def thisRow = getThisRowNumber()
                                if (isRowSubjectToValidation(thisRow)) {
                                    for (validation in validations) {
                                        Map<String,String> validationDetails = getValidationDetails(validation)
                                        if (validationDetails['subjectColumn'] as int == thisColumn) {
                                            switch (validationDetails['validation']) {
                                                case 'greaterThanOrEqual':
                                                    int objectColumn = validationDetails['objectColumn'] as int
                                                    def columnValue = getColumnValue(objectColumn).replaceAll(',', '')
                                                    if (columnValue.isNumber()) {
                                                        if ((val as int) < (columnValue as int)) {
                                                            valid = false
                                                            def rowLabel = adata.row
                                                            errorMessage = "Number for ${rowLabel} must not be less than that in column '${this.owner.qdata.cols[objectColumn - 1]}'"
                                                        }
                                                    }
                                                    break
                                                default:
                                                    break
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            valid = false
                            errorMessage = "${answerValueStr} is not a valid number"
                        }
                        break
                    case numberRange:
                        def numbers = answerValueStr.tokenize('-')
                        if (numbers.size() == 2) {
                            try {
                                NumberFormat.getInstance().parse(numbers[0] as String)
                                NumberFormat.getInstance().parse(numbers[1] as String)
                            } catch (ParseException e) {
                                valid = false
                                errorMessage = "A number range must contain two valid numbers"  // not a message that a user should see
                            }
                        }
                        else if (answerValueStr[-1] == '-') {
                            try {
                                NumberFormat.getInstance().parse(numbers[0] as String)
                            } catch (ParseException e) {
                                valid = false
                                errorMessage = "A number range must contain at least one valid number"  // not a message that a user should see
                            }
                        }
                        else if (answerValueStr == adata.alt) {
                            valid = true
                        }
                        else {
                            valid = false
                            errorMessage = "A number range must be in the form nnn-mmm"  // not a message that a user should see
                        }
                        break
                    default:
                        valid = false
                        errorMessage = "${datatype} is not a known datatype"
                }
            } else {
                if (required) {
                    //println "answer for ${ident()} is not valid because no value was entered and it is required"
                    valid = false
                    errorMessage = "An answer is required"
                }
                if (requiredIf) {
                    def rif = parseCondition(requiredIf)
                    switch (rif.comparator) {
                        case "=":
                            if (getQuestionFromPath(rif.path)?.answerValueStr == rif.value) {
                                valid = false
                                errorMessage = "An answer is required if you select '${rif.label ?: rif.value}'"
                            }
                            break
                        case "=~":
                            if (getQuestionFromPath(rif.path)?.answerValueStr?.toLowerCase() =~ rif.value?.toLowerCase()) {
                                valid = false
                                errorMessage = "An answer is required if you select '${rif.label ?: rif.value}'"
                            }
                            break
                        case "groovyTruth":
                            if (getQuestionFromPath(rif.path)?.answerValueStr) {
                                valid = false
                                errorMessage = "An answer is required if you specify '${rif.label ?: rif.value}'"
                            }
                            break
                        case "enabled":
                            if (params?.disabledState == 'enabled') {
                                valid = false
                                errorMessage = "An answer is required"
                            }
                            break
                    }
                }
            }
        }

        // add errors to a map
        Map<String, String> errors = new HashMap<String, String>()
        if (!valid) {
            errors.put ident(), errorMessage
            println "Error in ${ident()}: ${errorMessage}"
        }
        // check sub-questions
        questions.each {
            errors += it.validate(params)
        }
        // check inter-question validations if all sub-questions are valid
        if (!errors) { errors += validateGlobalConstraints() }
        return errors
    }

    boolean isRowSubjectToValidation(int row) {
        if (owner.qdata instanceof JSONObject && owner.qdata.has('validationRange')) {
            def range = owner.qdata.validationRange
            if (row >= (range.start as int) && row <= (range.finish as int)) {
                return true
            } else {
                return false
            }
        } else {
            return true
        }
    }

    /**
     * Determine the row number (starting from 1) for this field in a matrix question
     * @return The row number or 0 if not applicable
     */
    int getThisRowNumber() {
        int result = 0
        if (adata instanceof JSONObject && adata?.has('row')) {
            def rowLabel = adata.row
            if (owner.qdata instanceof JSONObject && owner.qdata.has('rows')) {
                def rows = owner.qdata.rows
                result = rows.findIndexOf { it == rowLabel } + 1
            }
        }
        return result
    }

    /**
     * Determine the column number (starting from 1) for this field in a matrix question
     * @return The column number or 0 if not applicable
     */
    int getThisColumnNumber() {
        int result = 0
        if (adata instanceof JSONObject && adata?.has('col')) {
            def columnLabel = adata.col
            if (owner.qdata instanceof JSONObject && owner.qdata.has('cols')) {
                def columns = owner.qdata.cols
                result = columns.findIndexOf { it == columnLabel } + 1
            }
        }
        return result
    }

    /**
     * Get the value for the answer at the specified column in this row of questions
     * @param columnNumber
     * @return The value of the answer
     */
    String getColumnValue(int columnNumber) {
        def columnLabel = owner.qdata.cols[columnNumber - 1]
        def rowLabel = adata.row
        def sibling = owner.questions.find { it.adata.col == columnLabel && it.adata.row == rowLabel}
        if (sibling) {
            return sibling.answerValueStr ?: ''
        } else {
            return ''
        }
    }

    /**
     * Pull apart an inter-field validation specification
     * @param spec validation specification
     * @return Map of the component parts
     */
    Map<String, String> getValidationDetails(String spec) {
        def bits = spec.tokenize('[,]')
        String validation = bits[0]
        String subjectColumn = bits[1]
        String objectColumn = bits[2]
        return ['validation':validation, 'subjectColumn':subjectColumn, 'objectColumn':objectColumn]
    }

    def validateGlobalConstraints() {
        if (!validation) {
            return [:]
        }
        Map<String,String> errors = new HashMap<String, String>()
        validation.each {
            switch (it) {
                case 'percent-total-lessThanOrEqual-100':
                    // all child questions of type percent must sum to <= 100
                    def sum = 0
                    // iterate max of 2 levels
                    questions.each { r1 ->
                        if (r1.atype == AnswerType.percent && r1.answerValueStr && !r1.errorMessage) {
                            sum += r1.answerValueStr as int
                        }
                        r1.questions.each { r2 ->
                            if (r2.atype == AnswerType.percent && r2.answerValueStr && !r1.errorMessage) {
                                sum += r2.answerValueStr as int
                            }
                        }
                    }
                    // check max
                    if (sum > 100) {
                        errorMessage = "Percentages can not add to more than 100%"
                        errors.put ident(), errorMessage
                        println "Error in ${ident()}: ${errorMessage}"
                    }
                    break
                case 'percent-total-greaterThan-0':
                    // all child questions of type percent must sum to <= 100
                    def sum = 0
                    // iterate max of 2 levels
                    questions.each { r1 ->
                        if (r1.atype == AnswerType.percent && r1.answerValueStr && !r1.errorMessage) {
                            sum += r1.answerValueStr as int
                        }
                        r1.questions.each { r2 ->
                            if (r2.atype == AnswerType.percent && r2.answerValueStr && !r1.errorMessage) {
                                sum += r2.answerValueStr as int
                            }
                        }
                    }
                    // check min
                    if (sum <= 0) {
                        errorMessage = "You must enter a percentage (greater than 0) in at least one field"
                        errors.put ident(), errorMessage
                        println "Error in ${ident()}: ${errorMessage}"
                    }
                    break
                case 'minimum-1-answer':
                    // at least one child question must have an answer
                    boolean atLeastOneAnswer = false
                    // iterate max of 2 levels
                    questions.each { r1 ->
                        if (r1.answerValueStr && r1.atype != AnswerType.calculate) {
                            atLeastOneAnswer = true
                        }
                        r1.questions.each { r2 ->
                            if (r2.answerValueStr && r2.atype != AnswerType.calculate) {
                                atLeastOneAnswer = true
                            }
                        }
                    }
                    if (!atLeastOneAnswer) {
                        errorMessage = "You must enter a answer in at least one field"
                        errors.put ident(), errorMessage
                        println "Error in ${ident()}: ${errorMessage}"
                    }

                    break
                case 'pseudo-radio':
                    int onCheckboxes = 0
                    // iterate next level
                    questions.each { q ->
                        if (q.datatype == AnswerDataType.bool && q.answerValueStr in ['on','yes']) {
                            onCheckboxes++
                        }
                    }
                    if (onCheckboxes == 0) {
                        errorMessage = "You must check one answer"
                        errors.put ident(), errorMessage
                        println "Error in ${ident()}: ${errorMessage}"
                    }
                    else if (onCheckboxes > 1) {
                        errorMessage = "You may only check one answer"
                        errors.put ident(), errorMessage
                        println "Error in ${ident()}: ${errorMessage}"
                    }
                    break
                case 'ranking-group':
                    // set of immediate sub-questions of type rank must contain each of the values 1..maxRequired exactly once
                    def maxRequired = qdata.maxRequired ?: qdata.max
                    def answerSet = []
                    boolean valid = true
                    questions.each {
                        if (it.atype == AnswerType.rank && it.answerValueStr) {
                            if (it.answerValueStr.isInteger()) {
                                def val = NumberFormat.getIntegerInstance().parse(it.answerValueStr)
                                answerSet << val
                                if (val < 1 || val > maxRequired) {
                                    valid = false
                                    errorMessage = "Rank value must be between 1 and ${maxRequired}. Value is ${val}"
                                    errors.put ident(), errorMessage
                                }
                            }
                            else {
                                valid = false
                                errorMessage = "${it.answerValueStr} is not an integer."
                                errors.put ident(), errorMessage
                            }
                        }
                    }
                    if (valid) {
                        def reason = ''
                        def offendingRank
                        (1..maxRequired).each { rank ->
                            def occurs = answerSet.findAll {it == rank}.size()
                            if (occurs != 1) {
                                reason = "The rank ${rank} appears ${occurs} times"
                                offendingRank = rank as String
                                valid = false
                            }
                        }
                        if (!valid) {
                            errorMessage = "Answers must contain the numbers 1 to ${maxRequired} exactly once each. (${reason})"
                            questions.each {
                                if (it.answerValueStr == offendingRank) {
                                    it.errorMessage = errorMessage
                                }
                            }
                            errors.put ident(), errorMessage
                            println "Error in ${ident()}: ${errorMessage}"
                        }
                    }
                    break
                case ~/column-total-greaterThanOrEqual-priorQuestion-column-total.*/:
                    def tokens = it.tokenize('[,]')
                    QuestionModel thisTotalQuestion = getTotalQuestion(this, tokens[1])
                    QuestionModel previousTotalQuestion = getTotalQuestion(this.previousSibling(), tokens[2])
                    def thisTotal = thisTotalQuestion.answerValueStr
                    def previousTotal = previousTotalQuestion.answerValueStr
                    if (thisTotal && previousTotal && ((thisTotal as int) < (previousTotal as int))) {
                        thisTotalQuestion.errorMessage = "Total should be greater than or equal to corresponding total above"
                        errors.put thisTotalQuestion.ident(), thisTotalQuestion.errorMessage
                    }
                    break
            }
        }
        return errors
    }

    QuestionModel getTotalQuestion(QuestionModel q, String col) {
        def result = null
        if (q.qdata instanceof JSONObject && q.qdata.has('cols')) {
            def colLabel = q.qdata.cols[(col as int) - 1]
            for (QuestionModel sq : q.questions) {
                if (sq.adata instanceof JSONObject && sq.adata?.has('row')) {
                    if (sq.adata.col == colLabel && sq.adata.row == 'Total') {
                        result = sq
                        break
                    }
                }
            }
        }
        return result
    }

    void clearErrors() {
        errorMessage = ""
    }

    /**
     * Detects when text in a 'if other please specify' field has been supplied
     * when the 'other' option is not selected.
     * @return
     */
    boolean badOtherText() {
        if (requiredIf) {
            def rif = parseCondition(requiredIf)
            def onlyIf = getQuestionFromPath(rif.path)
            switch (rif.comparator) {
                case "=":
                    if (onlyIf && onlyIf.answerValueStr != rif.value) {
                        return true
                    }
                    break
                case "=~":
                    if (onlyIf && !(onlyIf.answerValueStr =~ rif.value)) {
                        return true
                    }
                    break
                case "groovyTruth":
                    if (onlyIf) {
                        if (!onlyIf.answerValueStr) {
                            return true
                        } else if (onlyIf.answerValueStr.isNumber() && (onlyIf.answerValueStr as int == 0)) {
                            return true
                        }
                    }
                    break
            }
        }
        return false
    }

    static Map parseCondition(String condition) {
        // special cases
        if (condition == 'enabled') {
            return [comparator: condition]
        }
        // extract the optional label
        def optionalLabel = ""
        def opt = condition.tokenize('|')
        if (opt.size() > 1) {
            optionalLabel = opt[1]
        }
        def comparison = '='
        def bits = opt[0].tokenize('=')
        def conditionPath = bits[0]
        def conditionValue = bits[1]
        if (conditionValue.size() > 0 && conditionValue[0] == '~') {
            // comparison is 'contains'
            comparison = '=~'
            conditionValue = conditionValue[1..-1]
        }
        if (conditionValue == '*') {
            // value is anything (wildcard)
            comparison = 'groovyTruth'
        }
        /*println "evaluating 'requiredIf': path = ${conditionPath} testvalue = ${conditionValue} " +
                "answer = ${getQuestionFromPath(conditionPath)?.answerValueStr}"*/
        [label: optionalLabel, path: conditionPath, value: conditionValue, comparator: comparison]
    }

    /**
     * Path is a relative way to address other answers in the same level 1 question.
     *
     * .. is the parent question
     * ../n is the nth question that is sibling to this one (ie in the parent of this)
     * ../../n is the nth question in the grandparent of this
     * ../n/m is the mth sub-question in the nth question of the parent of this
     *
     * n and m are 1-based
     *
     * @param path
     * @return a QuestionModel
     */
    def getQuestionFromPath(path) {
        if (!path) return null
        def bits = path.tokenize('/')
        // the first bit must be '..'
        if (bits[0] != '..' || !owner) {
            return null
        }
        def question = owner
        if (bits.size() == 1) {
            // must be ..
            return question
        }
        if (bits[1] == '..') {
            // the form is ../../n
            int n = (bits[2] as int) - 1
            // check that there is a parent and that they have n children
            if (question.owner && question.owner?.questions?.size() > n) {
                return question.owner.questions[n]
            }
        } else if (bits.size() == 2) {
            // the form is ../n
            int n = (bits[1] as int) - 1
            if (question.questions?.size() > n) {
                return question.questions[n]
            }
        } else if (bits.size() > 2) {
            // the form is ../n/m
            int n = (bits[1] as int) - 1
            int m = (bits[2] as int) - 1
            if (question.questions?.size() > n) {
                question = question.questions[n]
                if (question.questions?.size() > m) {
                    return question.questions[m]
                }
            }
        }
        return null
    }

    String ident() {
        switch (level) {
            case 1:
                return "q" + questionNumber
            case 2:
                return "q" + owner.questionNumber + "_" + questionNumber
            case 3:
                return "q" + owner.owner.questionNumber + "_" + owner.questionNumber + "_" + questionNumber
            default:
                return "error"
        } 
    }

    int level1() {
        switch (level) {
            case 1:
                return questionNumber
            case 2:
                return owner.questionNumber
            case 3:
                return owner.owner.questionNumber
            default:
                return -1
        }
    }

    int level2() {
        switch (level) {
            case 1:
                return 0
            case 2:
                return questionNumber
            case 3:
                return owner.questionNumber
            default:
                return -1
        }
    }

    int level3() {
        switch (level) {
            case 1:
                return 0
            case 2:
                return 0
            case 3:
                return questionNumber
            default:
                return -1
        }
    }

    /**
     * Returns the number of 'rows' required to layout this question.
     * @return positive integer
     */
    int calculateDisplayRows() {
        // hack for now
        int rows = questions.size()
        if (qtext) rows++
        if (instruction && instructionPosition != 'top') rows++
        return rows;
    }

    /**
     * Save the answer to the database.
     *
     * Need to save when:
     *  1) no previous answer and this answer has a value
     *  2) previous answer is different to this answer (even if this answer is blank)
     *
     * @return true if answer actually written
     */
    def saveAnswer(userId) {
        def saveAnswer = true
        // get all answers for the question for this user
        def answers = Answer.findAllByUserIdAndGuid(userId, guid, [sort:'lastUpdated',order:'desc'])
        if (answers) {
            def answer = answers[0] // the most recent
            //println "Question ${ident()}: comparing ${answer.answerValue} to ${answerValueStr}"
            saveAnswer = answer.answerValue != answerValueStr
            // explicit check for blank answers
            if (answer.answerValue == null && answerValueStr == "") {
                // would result in the same answer, so don't save
                saveAnswer = false
            }
        }
        else {
            // no existing answer so only save if there is a value
            saveAnswer = answerValueStr
        }
        if (saveAnswer) {
            Answer a = new Answer(guid: guid, userId: userId, setId: qset, answerValue: answerValueStr)
            return a.save()
        }
        return saveAnswer
    }

    /**
     * Save the answer and the answers of all sub-questions to the database.
     * 
     * @param userId
     * @return
     */
    def saveAllAnswers(userId) {
        saveAnswer(userId)
        questions.each {
            it.saveAllAnswers userId
        }
    }

    int estimateHeight() {
        int height = 0
        if (qtext) height++
        if (instruction) height++
        questions.each {height += it.estimateHeight()}

        return height
    }

    def String toString() {
        return ident() + ":\n" +
                "text=${qtext}\n" +
                "qtype=${qtype}\n" +
                "qdata=${qdata}\n" +
                "instruction=${instruction}\n" +
                "atype=${atype}\n" +
                "datatype=${datatype}\n" +
                "alabel=${alabel}\n" +
                "adata=${adata}\n" +
                "displayHint=${displayHint}\n" +
                "required=${required}\n" +
                "requiredIf=${requiredIf}\n" +
                "errorMessage=${errorMessage}\n" +
                "value=${answerValueStr}\n"
    }

    /**
     * Reverses the ident transform.
     * @param ident
     * @return Returns the question number for each level as a list [level1, level2, level3]
     */
    static List parseIdent(String ident) {
        def strs = ident?.tokenize('_')
        // TODO: needs validity checking and an INVALID_IDENT exception
        String s1 = strs[0]
        String s2 = "0"
        String s3 = "0"
        s1 = s1[1..-1] // dump the q
        if (strs.size() > 1) {
            s2 = strs[1]
        }
        if (strs.size() > 2) {
            s3 = strs[2]
        }
        try {
            def nf = NumberFormat.getInstance()
            def l1 = nf.parse(s1)
            def l2 = nf.parse(s2)
            def l3 = nf.parse(s3)
            return [l1,l2,l3]
        } catch (ParseException e) {
            return []
        }
    }
}
