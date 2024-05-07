def getParameters(args) {
    return {
        stringParam('MVN_TEST_OPTIONS', '', "Specify maven options for your tests. These args will be passed to cucumber tests. <br>Pass args like -DgroupId=ABC <br>Specify goals like clean install <br>")
        stringParam('TEST_ZIP_PACKAGE', '', '\n' +
                'Public url to download test zip. Zip must contain Ant or Maven script at root location. <br> For example, a sample zip, \'fabricTests.zip\' when extracted directly gives pom.xml in current directory only. \n' +
                '<br>')
        stringParam('TEST_REPORT_DIRS', '', "Specify paths to reports relative to pom.xml for tracking.<br> For example, \'target/cucumber/cucumber.json\' <br> If not specified default reports will be tracked. <br>")
    }
}