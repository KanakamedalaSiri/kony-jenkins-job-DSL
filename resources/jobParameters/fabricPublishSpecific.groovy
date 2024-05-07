def getParameters(args) {
    return {
        booleanParam('SET_DEFAULT_VERSION', false, 'Select this checkbox if you want to make the version specified at FABRIC_APP_VERSION field as default version for your app.')
    }
}