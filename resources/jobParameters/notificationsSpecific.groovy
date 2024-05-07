def getParameters(args) {
    return {
        parameterSeparatorDefinition {
            name('NOTIFICATIONS_HEADER')
            separatorStyle('')
            sectionHeader(args.notificationsParamsSeparatorDesc)
            sectionHeaderStyle('')
        }
        validatingStringParameterDefinition {
            name('RECIPIENTS_LIST')
            defaultValue("")
            regex('^\\s|^[^;]*$')
            failedValidationMessage("Invalid Email-ID!! Semicolon(';') is not allowed.")
            description('A comma (,) separated list of the e-mail addresses that must receive notifications on the result of this job.')
        }
    }
}