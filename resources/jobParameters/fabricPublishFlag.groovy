def getParameters(args) {
    return {
        booleanParam('ENABLE_PUBLISH', false, 'Publish application after successful import.')
    }
}