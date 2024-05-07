def getParameters(args) {
    return {
        credentialsParam('IMPORT_FABRIC_APP_CONFIG') {
            type('com.kony.AppFactory.Jenkins.credentials.impl.MobileFabricAppTriplet')
            defaultValue('')
            description('Foundry app configuration details like app name, account id or OnPrem console, identity urls to import the Foundry application. Foundry Environment details from this config will be used to publish the imported app.')
        }
    }
}
