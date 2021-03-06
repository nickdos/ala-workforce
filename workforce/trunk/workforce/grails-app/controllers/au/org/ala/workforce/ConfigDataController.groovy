package au.org.ala.workforce

class ConfigDataController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        cache false
        redirect(action: "edit", params: params)
    }

    def edit = {
        cache false
        def configInstance = ConfigData.get(1)
        if (!configInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'config.label', default: 'ConfigData'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [configInstance: configInstance]
        }
    }

    def update = {
        cache false
        def configInstance = ConfigData.get(1)
        if (configInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (configInstance.version > version) {

                    configInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'config.label', default: 'ConfigData')] as Object[], "Another user has updated this ConfigData while you were editing")
                    render(view: "edit", model: [configInstance: configInstance])
                    return
                }
            }
            configInstance.properties = params
            if (!configInstance.hasErrors() && configInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'config.label', default: 'ConfigData'), ''])}"
                redirect(action: "edit", id: configInstance.id)
            }
            else {
                render(view: "edit", model: [configInstance: configInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'config.label', default: 'ConfigData'), params.id])}"
            redirect(action: "edit")
        }
    }


}