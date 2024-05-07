def getParameters(args) {
    return {
        parameterSeparatorDefinition {
            name('KONY_HEADER')
            separatorStyle('')
            sectionHeader(args.konyParamsSeparatorDesc)
            sectionHeaderStyle('')
        }
        choiceParam('BUILD_MODE', ['debug', 'test', 'release', 'release-protected'], args.buildModeParamDesc)
        credentialsParam('FABRIC_CREDENTIALS_ID') {
            type('com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl')
            required()
            defaultValue('')
            description(args.fabricCredentialsIDParamDesc)
        }
        credentialsParam('FABRIC_APP_CONFIG') {
            type('com.kony.AppFactory.Jenkins.credentials.impl.MobileFabricAppTriplet')
            defaultValue('')
            description('The configurations to bind your client application to a specific Foundry runtime. Typically a Foundry application will be published to several environments -e.g. Development, QA, Production, etc. This parameter tells App Factory to which of those it should link the client apps built from the Iris project.')
        }
    }
}
