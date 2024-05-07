def getParameters(args) {
    return {
        parameterSeparatorDefinition {
            name('CUSTOM_HOOKS_HEADER')
            separatorStyle('')
            sectionHeader(args.customHookFacadeSpecificDesc)
            sectionHeaderStyle('')
        }
        booleanParam('RUN_CUSTOM_HOOKS', defaultValue = true, 'Check this box if you want <em>App Factory</em> to run any previously configured custom steps in your build process.')
    }
}