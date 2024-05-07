def getParameters(args) {
    return {
        parameterSeparatorDefinition {
            name('WEB_HEADER')
            separatorStyle('')
            sectionHeader(args.webParamsSeparatorDesc)
            sectionHeaderStyle('')
        }
        stringParam('WEB_APP_VERSION', '', 'Version of the WEB(DesktopWeb & SPA) application. It is the value that you generally enter in the Build UI mode at "Project Settings -> Application -> Version".\n' + '<br>\n' +'For Example : 1.0.1')
        booleanParam('PUBLISH_FABRIC_APP', false, args.publishSPAOrDWToFabricParamDesc)
        booleanParam('FORCE_WEB_APP_BUILD_COMPATIBILITY_MODE',false,'If this checkbox is selected, the web app package will be built using the older(war) extension. It is the value that you generally enter in the Build UI mode at "Project Settings -> Application -> Force Web App Build Compatibility Mode".\n' +'<br/>\n'+  '<b>Note:</b> Newer extension(zip) is only supported with V8 service pack 2 on Volt MX Cloud. If your app is using a lower set of plugins, please keep the checkbox enabled.')
    }
}