def getParameters(args) {
    return {
        parameterSeparatorDefinition {
            name('WEB_TESTING_HEADER')
            separatorStyle('')
            sectionHeader(args.webTestingParamsSeparatorDesc)
            sectionHeaderStyle('')
        }
        booleanParam('RUN_WEB_TESTS', defaultValue = false, 'Select this checkbox if you want to execute Web tests.')
    }
}
