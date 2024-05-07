def getParameters(args) {
    return {
        activeChoiceParam('IMPORT_FABRIC_APP_VERSION') {
            description('Select whether you want the version of the Foundry application to be taken from the Meta.json file as it is stored in source control, or taken from the FABRIC_APP_CONFIG credential selected in App Factory, or if you\'d like to type it manually.')
            choiceType('SINGLE_SELECT')
            groovyScript {
                script("['PICK_FROM_FABRIC_APP_META_JSON':'Pick it from the Meta.json file from source control', 'PICK_FROM_FABRIC_APP_CONFIG':'Pick it from the App Config credential', 'Other':'Type it in']")
            }
        }
        validatingStringParameterDefinition {
            name('FABRIC_APP_VERSION')
            defaultValue('1.0')
            regex('^[1-9]{1,3}\\.[0-9]{1,2}$')
            failedValidationMessage('Please enter valid App Version as specified in parameter description!')
            description("The version of the Foundry application to be imported -If you have chosen to type it in manually.\n" +
                "Note: The version should be in the format major.minor, " +
                "where major and minor are numeric, and major is between 1 and 999, and  minor is between 0 and 99 -e.g.: 1.0 or 999.99.");
        }
        booleanParam('PUBLISH', false, "Check this box if you want <em>App Factory</em> to publish your <em>Foundry</em> application to the <em>Foundry</em> environment specified by <code>FABRIC_APP_CONFIG.</code>" + "<br>" +
        "<br>If left unchecked, <em>App Factory</em> will check out your <em>Foundry</em> application from source control, bundle it, archive it, and import it, but it will not publish it to any environment. You will then have to publish it manually from the <em>Foundry</em> Console.<br>"+ "<br>" +
        "<strong>Note:</strong> In order to publish the <em>Foundry</em> application to an environment, you must first import it into its corresponding workspace.")

    }
}
