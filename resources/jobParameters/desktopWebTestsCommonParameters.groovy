def getParameters(args) {
    return {
        stringParam('WEB_TEST_PLAN', 'testPlan.js', 'Enter the relative path of test plan that you would like to execute. This path should be relative to the "testresources/Jasmine/Desktop/Test Plans" folder. If no value is provided, the default plan (testPlan.js) will be executed.<br><b>Note: </b>This parameter is only valid if Jasmine is selected as TEST_FRAMEWORK.')
        choiceParam('AVAILABLE_BROWSERS', ['CHROME'], 'Using this field, you can select the browser on which you want to run the Web tests')
        choiceParam('SCREEN_RESOLUTION', ['1024x768', '1920x1080', '1366x768', '1440x900', '1536x864', '1680x1050', '1280x720'], 'Please select the screen resolution for Web Test')
        stringParam('RUN_WEB_TESTS_ARGUMENTS', '', 'The arguments that you want to pass in a maven command to the Web tests.\n' + '<br>\n' + 'For Example: If you pass "-Dsurefire.suiteXmlFiles=resources/Testng.xml", this will trigger the tests present in resources/Testng.xml file. If you do not pass any TestNG file name, we will take Testng.xml as default file.')
    }
}
