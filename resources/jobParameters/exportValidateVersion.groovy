def getParameters(args) {
    return {
        parameterSeparatorDefinition {
            name('EXPORT_OPTIONS_HEADER')
            separatorStyle('')
            sectionHeader(args.fabricExportOptionsParamsSeparatorDesc)
            sectionHeaderStyle('')
        }
        booleanParam('VALIDATE_VERSION', false, 'Check this box if you want AppFactory to validate the app version from the foundry console matches the one in the SCM branch. ' +
                'If these two versions do not match AppFactory will not commit the changes. ' +
                'This is useful if you are keeping separate long-lived branches for different versions of your Foundry App. ')
        validatingStringParameterDefinition {
            name('FABRIC_APP_VERSION')
            defaultValue('')
            regex('^$|^[1-9]{1,3}\\.[0-9]{1,2}$')
            failedValidationMessage('Please enter valid App Version as specified in parameter description!')
            description("The version of the Foundry application to be exported.\n" +
                    "Note: Please choose an existing version from the Foundry console.\n" +
                    "The version should be in the format major.minor, " +
                    "where major and minor are numeric, and major is between 1 and 999, and  minor is between 0 and 99 -e.g.: 1.0 or 999.99.");
        }
    }
}