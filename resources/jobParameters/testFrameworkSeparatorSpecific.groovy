def getParameters(args) {
    return {
        parameterSeparatorDefinition {
            name('TEST_FRAMEWORK_HEADER')
            separatorStyle('')
            sectionHeader(args.testFrameworkParamsSeparatorDesc)
            sectionHeaderStyle('')
        }
    }
}
