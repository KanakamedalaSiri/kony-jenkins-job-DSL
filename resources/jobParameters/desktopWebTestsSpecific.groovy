def getParameters(args) {
    return {
        parameterSeparatorDefinition {
            name('DESKTOPWEB_HEADER')
            separatorStyle('')
            sectionHeader(args.desktopWebTestsSpecificDesc)
            sectionHeaderStyle('')
        }
        stringParam('WEB_APP_URL', '', "App URL where the Web App WAR is published on Foundry." + "<br>" +
            "<strong>Note: </strong> The WAR deployed should be built-in <code>Test</code> mode by using <em>Iris<sup>TM</sup></em> version 9.3.0.0 and above, if you want to run test with Jasmine test framework.")
    }
}

