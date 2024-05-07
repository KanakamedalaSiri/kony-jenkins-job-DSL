def getParameters(args) {
    return {
        parameterSeparatorDefinition {
            name('EXPORT_OPTIONS_HEADER')
            separatorStyle('')
            sectionHeader(args.fabricExportOptionsParamsSeparatorDesc)
            sectionHeaderStyle('')
        }
        booleanParam('OVERWRITE_EXISTING_SCM_BRANCH', false, 'Force push to given branch. <br/>Note: This will overwrite the app content ' +
                'that exist in the given branch with the latest exported Foundry app content.')
    }
}