def getParameters(args) {
    return {
        booleanParam('OVERRIDE_TEST_DATA', false, 'Check the box to replace the original test data set with the custom data set when running the test cases.')
        booleanParam('RERUN_FAILED_TESTS', false, 'Select this checkbox if you want to re-run the failed tests one more time.')
    }
}