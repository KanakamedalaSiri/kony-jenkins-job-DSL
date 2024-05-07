def getParameters(args) {
    return {
        stringParam('FABRIC_DIR', '', "In most cases the <em>Foundry</em> project is placed at the root of the source code repository. " +
                "If this is your case, then you <strong>must</strong> leave this field <strong>blank</strong>." + "<br>" +
                "<br>However, if your <em>Foundry</em> project resides in a sub-folder of your source code repository -typically along with other projects in what is commonly referred to as a monorepo approach- " +
                "you may use this field to specify the path ot it, relative to the root of the source code repository."+ "<br>" +
                "<br><strong>Note: </strong>The Foundry directory in your repository will be the one that contains the <code>Apps</code> or <code>Services</code> directory, "+
                "as it results from exporting an app or service from Foundry and decompressing it -e.g.: If your <code>Apps</code> directory resides in <code>path/to/FooApp/Apps</code>, then the Foundry directory is <code>path/to/FooApp</code>.")
    }
}




