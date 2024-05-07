def getParameters(args) {
    return {
        credentialsParam('EXPORT_FABRIC_APP_CONFIG') {
            type('com.kony.AppFactory.Jenkins.credentials.impl.MobileFabricAppTriplet')
            defaultValue('')
            description('Foundry app configuration details like app name, account id or OnPrem console, identity urls to export the Foundry application.')
        }
    }
}
