def getParameters(args) {
    return {
        booleanParam('IMPORT', false, "Check this box if you want <em>App Factory</em> to import your <em>Foundry</em> application into the Foundry account or workspace specified by <code>FABRIC_APP_CONFIG</code>." + "<br>"+
                "<br>If left unchecked, <em>App Factory</em> will check out your <em>Foundry</em> application from source control, bundle it and archive it, but it will not import it into any workspace. " +
                "You will then have to download the bundle from the archive and import it manually into <em>Foundry</em>." + "<br>" + "<br>" +
                "<strong>Note:</strong> In order to publish the <em>Foundry</em> application to an environment, you must first import it into its corresponding workspace.")
    }
}
