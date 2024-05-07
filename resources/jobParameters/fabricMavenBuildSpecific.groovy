def getParameters(args) {
    return {
        parameterSeparatorDefinition {
            name('FABRIC_MAVEN_BUILD_HEADER')
            separatorStyle('')
            sectionHeader(args.fabricMavenBuildParamsSeparatorDesc)
            sectionHeaderStyle('')
        }
        booleanParam('CLEAN_JAVA_ASSETS', true, "Check this box if you have any JAR files stored along your <em>Foundry</em> application in source control " +
                "and want <em>App Factory</em> to remove them before importing your integrations into <em>Foundry</em>.<br>" + "<br>" +
                "<strong>Note:</strong> This is generally a good idea if you have the Java source code for those dependencies stored in source control and also check the <code>BUILD_JAVA_ASSETS</code> box for <em>App Factory</em> to build and bundle the latest version of those.")
        booleanParam('BUILD_JAVA_ASSETS', false, "Check this box if you have written any custom code for your <em>Foundry</em> integrations using Java, and want <em>App Factory</em> to build and import those dependencies from source control." +
                "If selected, App Factory will check out your Java source code, compile it, package it and bundle it with your <em>Foundry</em> application.<br>" + "<br>" +
                "<strong>Note:</strong> At the moment, Maven 3 is the only supported Java build mechanism.")
        stringParam('JAVA_PROJECTS_DIR', '', "If you have written any custom Java code for your <em>Foundry</em> integrations, use this field to specify the path to the parent directory containing all the Java projects you have to build." +
                " -e.g. If your <em>Foundry</em> application depends on two Java projects which are stored in subdirectories <code>foo/bar/Project1</code> and <code>foo/bar/Project2</code>, then the correct value for this field is <code>foo/bar</code>." + "<br>" +
                "<br><strong>Note:</strong> This path may also be used to point to Git submodules.")
        validatingStringParameterDefinition {
            name('MVN_GOALS_AND_OPTIONS')
            defaultValue('clean package')
            regex('^(?!\\s.*$)(?!mvn|MVN.*$).*$')
            failedValidationMessage("Please type maven goals & options without maven keyword 'mvn' or 'MVN' and leading spaces!")
            description("If you have written any custom Java code for your Foundry integrations, " +
                    "use this field to specify the Maven goals you wish to execute in order to build them, as well as any other command line options for the 'mvn' command. "+
                    "The default goals are clean package. \n" +
                    "Note: Use this if you want to invoke Maven with a different set of goals and/or maven profiles and/or additional arguments using the '-D' option" +
                    "-e.g.: clean package -DskipTests=true -Darg1=foo1 -Darg2=foo2")
        }
    }
}
