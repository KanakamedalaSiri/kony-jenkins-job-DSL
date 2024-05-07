def getParameters(args) {
    return {
        credentialsParam('FABRIC_CREDENTIALS') {
            type('com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl')
            required()
            defaultValue('')
            description('The credentials that App Factory will use in order to import and publish your Foundry application into\n' +
                    'the account and environment specified by FABRIC_APP_CONFIG.\n' +
                    'Notice that Foundry password expires periodically, so you will need to update this credential whenever that happens.')
        }
    }
}
