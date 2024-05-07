def getParameters(args) {
    return {
        parameterSeparatorDefinition {
            name('PROJECT_SOURCE_CODE_HEADER')
            separatorStyle('')
            sectionHeader(args.flywayScmSeparatorDesc)
            sectionHeaderStyle('')
        }
        stringParam('SCM_URL', '', 'Provide the URL to the repository where the source code of your app is being stored and versioned. If you are using any type of Git server as your SCM system, your URL will typically look like this: https://git-vendor.com/temenos/foo-app.git, Important:If you are used to looking at your source code repository through your web browser, be careful not to mistake the URL in your browsers address bar with the URL of your Git repository.')
        choiceParam('SCM_SERVER_TYPE', ['GitHub', 'Bitbucket', 'AWS CodeCommit', 'Others'], args.scmVendorParamDesc)
        stringParam('SCM_BRANCH', '', args.projectSourceCodeBranchParamDesc)
        credentialsParam('SCM_CREDENTIALS') {
            type('com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl')
            required()
            defaultValue('')
            description(args.flywayScmCredentialsParamDesc)
        }
    }
}