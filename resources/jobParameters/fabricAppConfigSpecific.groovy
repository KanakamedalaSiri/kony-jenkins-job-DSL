def getParameters(args) {
    return {
        credentialsParam('FABRIC_APP_CONFIG') {
            type('com.kony.AppFactory.Jenkins.credentials.impl.MobileFabricAppTriplet')
            defaultValue('')
            description('The configuration that App Factory will use in order to point your Iris (client) application to connect to the instance of its corresponding Foundry (server) application. This configuration includes the hosting type, cloud account, environment name and Foundry app name.')
        }
    }
}
