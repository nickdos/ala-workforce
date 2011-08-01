package au.org.ala.workforce

class AdminController {

    def modelLoaderService

    def dashboard = {
        cache false

        def setId = params.set.toInteger() ?: QuestionModel.CURRENT_PERSONAL_SURVEY
        def qset = QuestionSet.findBySetId(setId)
        def currentYear = new GregorianCalendar().get(Calendar.YEAR).toString()
        def numberStarted = Answer.numberStartedForYear(setId, currentYear)
        def numberCompleted = Event.numberCompletedForYear(setId, currentYear)
        def total
        switch (setId) {
            case QuestionModel.CURRENT_INSTITUTIONAL_SURVEY: total = Institution.countBySetId(setId); break
            default: total = ((numberCompleted/100 as int) + 1) * 100
        }

        // render the page
        [qset: qset, year: currentYear, started: numberStarted, completed: numberCompleted, total: total]
    }

    def index = {
        cache false
        
        render(view:'../index', model:[admin:true])
    }

    /**
     * Returns the userid attribute of the current authenticated user.
     * @return userid
     */
    int userId() {
        return request.getUserPrincipal().attributes.userid as int
    }
}
