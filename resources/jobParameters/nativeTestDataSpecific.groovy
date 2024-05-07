def getParameters(args) {
    return {
        stringParam('NATIVE_CUSTOM_DATA_PATH', '', "Provide the directory path to the custom test data, for executing the current test cases in Native channel." + "<br>" +
            "<strong>Note: </strong> The directory path should be specifically selected to <em>Jasmine</em> <code>testresources</code> in the respective native channel's <em>Iris<sup>TM</sup></em> project source." +
            " -eg: If your custom test data files are located at the below path for Native channel test:" + "<br>" +
            "<code><irisProject>/testresources/Jasmine/Mobile/customTestData/DevData/Datafile.js</code>" + " then, test data dir path will be: <code>customTestData/DevData</code>")
        
        
    }
}