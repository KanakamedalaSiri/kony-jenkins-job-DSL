def getParameters(args) {
    return {
        choiceParam('TEST_ENVIRONMENT', ['Standard', 'Custom'], 'Select the environment to run the tests')
    }
}
