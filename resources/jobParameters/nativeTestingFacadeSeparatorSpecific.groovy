def getParameters(args) {
    return {
        parameterSeparatorDefinition {
            name('NATIVE_TESTING_HEADER')
            separatorStyle('')
            sectionHeader(args.nativeTestingParamsSeparatorDesc)
            sectionHeaderStyle('')
        }
    }
}