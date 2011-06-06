package au.org.ala.workforce

class ReportController {

    def modelLoaderService

    /**
     * Display questions that have answers.
     *
     */
    def answers = {

        def questionList = []

        def setId = params.set as int ?: 1
        def qset = QuestionSet.findBySetId(setId)

        def loggedInUserId = request.userPrincipal.attributes.userid as int
        def userId = params.id as int ?: loggedInUserId
        if (userId != loggedInUserId && !request.isUserInRole('ROLE_ABRS_ADMIN')) {
            userId = loggedInUserId
        }
        def user = User.findByUserid(userId)

        def questions = Question.findAllByLevel2AndQset(0, setId)

        def year = DateUtil.getYear(params.year)
        def answers = Answer.getAnswers(setId, userId, year)
        questions.each {
            questionList <<  modelLoaderService.loadQuestionWithAnswer(setId, it.level1, answers)
        }

        assert questionList

        // render the page
        [qset: qset, questions: questionList, user: user]
    }

    
    /**
     * Display a single question with answers.
     *
     */
    def singleQuestionAnswer = {

        def questionId = params.qid.toInteger() ?: 1;
        def setId = params.set.toInteger() ?: 1
        def qset = QuestionSet.findBySetId(setId)
        def loggedInUserId = request.userPrincipal.attributes.userid as int
        def userId = params.id as int ?: loggedInUserId
        if (userId != loggedInUserId && !request.isUserInRole('ROLE_ABRS_ADMIN')) {
            userId = loggedInUserId
        }
        def user = User.findByUserid(userId)
        def year = DateUtil.getYear(params.year)
        def answers = Answer.getAnswers(setId, userId, year)
        def question = modelLoaderService.loadQuestionWithAnswer(setId, questionId, answers)

        // render the page
        render(view:'answer', model:[qset: qset, question: question, user: user])
    }

}
