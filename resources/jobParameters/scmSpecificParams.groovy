def getParameters(args) {
    return {
        parameterSeparatorDefinition {
            name('PROJECT_SOURCE_CODE_HEADER')
            separatorStyle('')
            sectionHeader(args.scmParamsSeparatorDesc)
            sectionHeaderStyle('')
        }
        stringParam('PROJECT_SOURCE_CODE_BRANCH', '', args.projectSourceCodeBranchParamDesc)
    }
}