def getParameters(args) {
    return {
        stringParam('SERVICE_CONFIG_PATH', '', "The path to the <a href='https://opensource.hcltechsw.com/volt-mx-docs/docs/documentation/Foundry/voltmx_foundry_user_guide/Content/ServiceConfigProfile.html'> service configuration</a> JSON file, relative to the root of the source code repository. " + "<br>" +
                "<br>We recommend placing the service config files in a folder named <code>configuration</code>, but custom locations are supported. We also recommend naming each file after the environment it is meant for, and spaces in the file name <i>must</i> be avoided -e.g. " +
                "If your environment is called <code>My Test</code>, then the following would all be valid paths to the config file: <code>configuration/my-test.json</code>, <code>configuration/my_test.JSON</code> or <code>configuration/MyTest.json</code>." + "<br> <br>" +
                args.fabricServiceConfigPathParamsDesc + "<br>" +
                "<br><strong>Note: </strong>This is only applicable for Foundry Console versions 9.2.0 and above.")
    }
}

