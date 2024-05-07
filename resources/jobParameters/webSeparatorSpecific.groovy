def getParameters(args) {
    return {
        if (args.isFacadeJob)
            booleanParam('RESPONSIVE_WEB', false, 'Select the box if your build is for Responsive Web Application.')
        else {
            parameterSeparatorDefinition {
                name('WEB_HEADER')
                separatorStyle('')
                sectionHeader(args.webParamsSeparatorDesc)
                sectionHeaderStyle('')
            }
        }
        validatingStringParameterDefinition {
            name('WEB_APP_VERSION')
            defaultValue('')
            regex('^\\s*$|^[0-9]+\\.[0-9]+\\.[0-9]+$')
            failedValidationMessage('Please enter valid Web App Version (For Example : 1.0.1)')
            description('Version of the WEB application. It is the value that you generally enter in the Build UI mode at "Project Settings -> Application -> Version".\n' + '<br>\n' + 'For Example : 1.0.1')
        }
        booleanParam('PUBLISH_WEB_APP', false, args.publishToFabricParamDesc)
        booleanParam('LEGACY_WEB', false, 'With Iris V8 SP2 and over, web applications are built and packaged into a lightweight <code>.zip</code> format. ' +
                'Checking this box will build the web application using the older legacy framework and package it into a <code>.war</code> format.' + '<br>' +
                '<br>This flag is equivalent to the setting found in Iris under Project Settings -> Responsive Web -> General Settings -> Enable Desktop Web (Legacy)' + '<br>' +
                '<br><strong>Note: If you are using a Iris version prior to V8 SP2, keep this box checked.')
        parameterSeparatorDefinition {
            name('WEB_PROTECTION_HEADER')
            separatorStyle('')
            sectionHeader(args.webProtectionParamsSeparatorDesc)
            sectionHeaderStyle('')
        }
        credentialsParam('OBFUSCATION_PROPERTIES') {
            type('com.kony.AppFactory.Jenkins.credentials.impl.WebProtectionKeysImpl')
            required()
            defaultValue('')
            description("The contents of the securejs.properties file, used to invoke API calls on your behalf in order to obfuscate your web application.")
        }
        choiceParam('PROTECTION_LEVEL', ['BASIC', 'MODERATE', 'CUSTOM'], 'Set the level of protection you wish your web application to have. The possible values are <code>BASIC</code>, <code>MODERATE</code> and <code>CUSTOM</code>. If you choose a <code>CUSTOM</code> level, provide the path to configure it in the <code>CUSTOM_PROTECTION_PATH</code> field below.')
        validatingStringParameterDefinition {
            name('EXCLUDE_LIST_PATH')
            defaultValue('')
            regex('^\\s*$|^.*(\\.txt)$')
            failedValidationMessage('Please enter valid exclude list file as specified in parameter description (For Example: exclude_files_for_protection.txt)')
            description("The path to the plain text document in '.txt' format which contains a list of files that must be excluded from the obfuscation process. The path must be relative to the root of the repository and should not contains spaces in file name., For example: 'custombuild/excludelist.txt'. Read more <a href='https://opensource.hcltechsw.com/volt-mx-docs/docs/documentation/Foundry/voltmx_appfactory_user_guide/Content/BuildingAnApp.html' target=\"_blank\"> here.</a>")
        }
        validatingStringParameterDefinition {
            name('CUSTOM_PROTECTION_PATH')
            defaultValue('')
            regex('^\\s*$|^.*(\\.json)$')
            failedValidationMessage('Please enter valid JSON file as specified in parameter description (For Example: blue_print_config.json')
            description("If you have set the PROTECTION_LEVEL to CUSTOM , specify the path to a JSON file containing your desired protection blueprint and should not contains spaces in file name. The path must be relative to the root of the repository , For Example: custombuild/blueprint.json.");
        }
    }
}
