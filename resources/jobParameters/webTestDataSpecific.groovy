def getParameters(args) {
    return {
        stringParam('WEB_CUSTOM_DATA_PATH', '', "Provide the directory path to the custom test data, for executing the current test cases in web channel." + "<br>" +
            "<strong>Note: </strong> The directory path should be specifically selected to <em>Jasmine</em> <code>testresources</code> in the respective web channel's <em>Iris<sup>TM</sup></em> project source." +
            " -eg: If your custom test data files are located at the below path for Web channel test:" + "<br>" +
            "<code><irisProject>/testresources/Jasmine/Desktop/customTestData/DevData/Datafile.js</code>" + " then, test data dir path will be: <code>customTestData/DevData</code>")
    }
}