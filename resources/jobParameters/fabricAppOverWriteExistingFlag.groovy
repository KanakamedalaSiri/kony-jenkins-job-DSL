def getParameters(args) {
    return {
        parameterSeparatorDefinition {
            name('IMPORT_OPTIONS_HEADER')
            separatorStyle('')
            sectionHeader(args.fabricImportOptionsParamsSeparatorDesc)
            sectionHeaderStyle('')
        }
        booleanParam('OVERWRITE_EXISTING_APP_VERSION', false, 'Overwrite existing Foundry app, if same app version already exist in Foundry Console.')
        
    }
}