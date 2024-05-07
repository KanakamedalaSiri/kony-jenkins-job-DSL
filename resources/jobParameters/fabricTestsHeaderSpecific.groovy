def getParameters(args) {
    return {
        parameterSeparatorDefinition {
            name('FABRIC_TESTS_HEADER')
            separatorStyle('')
            sectionHeader(args.fabricTestsParamsSeparatorDesc)
            sectionHeaderStyle('')
        }
    }
}