def getParameters(args) {
    return {
        parameterSeparatorDefinition {
            name('FABRIC_IMPORT_HEADER')
            separatorStyle('')
            sectionHeader(args.fabricImportParamsSeparatorDesc)
            sectionHeaderStyle('')
        }
        credentialsParam('IMPORT_FABRIC_CREDENTIALS_ID') {
            type('com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl')
            required()
            defaultValue('')
            description('Volt MX Foundry username and password for import.')
        }
    }
}
