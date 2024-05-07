def getParameters(args) {
    return {
        parameterSeparatorDefinition {
            name('FABRIC_EXPORT_HEADER')
            separatorStyle('')
            sectionHeader(args.fabricExportParamsSeparatorDesc)
            sectionHeaderStyle('')
        }
        credentialsParam('EXPORT_FABRIC_CREDENTIALS_ID') {
            type('com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl')
            required()
            defaultValue('')
            description('Volt MX Foundry username and password for export.')
        }
    }
}
