package resources.jobParameters

def getParameters(args) {
    return {
        booleanParam('IGNORE_JARS', true, "Check this box if you have any JAR files inside the exported <em>Foundry</em> application and " +
                "want them to be ignored while check-in the app content to source control system.<br>" + "<br>" +
                "<strong>Note:</strong> This is generally a good idea if you have the Java source code for those JAR dependencies stored " +
                "in source control then no need of tracking JAR binaries again in the source control system. Opt out this check box if you " +
                " really sure to check-in JAR files also along with <em>Foundry</em> application source.")
    }
}