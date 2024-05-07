def getParameters(args) {
    return {
        if (args.isFacadeJob) {
            booleanParam('IOS', false, 'Select the checkbox if your build is for iOS specific application')
        }
        else {
            parameterSeparatorDefinition {
                name('IOS_HEADER')
                separatorStyle('')
                sectionHeader(args.iosParamsSeparatorDesc)
                sectionHeaderStyle('')
            }
        }
        if(args.isFacadeJob)
            booleanParam('IOS_UNIVERSAL_NATIVE', false, 'Select the checkbox if you want to build iOS universal app and fill all required parameters below.')
        stringParam('IOS_UNIVERSAL_APP_ID', '', 'A bundle ID for Universal application(Mobile and Tablet), that is used to provision devices and by the operating system when the app is distributed to customers. It is the value you generally enter in build UI mode at "Project Settings -> Native -> iPhone/iPad/Watch -> Bundle Identifier". <br> For Example : com.quantumappfactory.KitchenSink')
        if(args.isFacadeJob)
            booleanParam('IOS_MOBILE_NATIVE', false, 'Select the checkbox if you want to build iOS app for Mobile and fill all the required parameters below.')
        stringParam('IOS_MOBILE_APP_ID', '', 'A bundle ID for mobile application, that is used to provision devices and by the operating system when the app is distributed to customers. It is the value you generally enter in build UI mode at "Project Settings -> Native -> iPhone/iPad/Watch -> Bundle Identifier". <br> For Example : com.quantumappfactory.KitchenSink')
        if(args.isFacadeJob)
            booleanParam('IOS_TABLET_NATIVE', false, 'Select the checkbox if you want to build iOS app for Tablet and fill all the required parameters below.')
        stringParam('IOS_TABLET_APP_ID', '', 'A bundle ID for tablet application, that is used to provision devices and by the operating system when the app is distributed to customers. It is the value you generally enter in build UI mode at "Project Settings -> Native -> iPhone/iPad/Watch -> Bundle Identifier". <br> For Example : com.quantumappfactory.KitchenSink')
        validatingStringParameterDefinition {
            name('IOS_APP_VERSION')
            defaultValue('')
            regex('^\\s*$|^[0-9]+\\.[0-9]+\\.[0-9]+$')
            failedValidationMessage('Please enter valid App Version (For Example : 1.0.1)')
            description('Version of the iOS application. This setting is available at "Project Settings -> Application -> Version" in Iris. <br> For Example : 1.0.1')
        }
        validatingStringParameterDefinition {
            name('IOS_BUNDLE_VERSION')
            defaultValue('')
            regex('^\\s*$|^[0-9]+\\.[0-9]+\\.[0-9]+$')
            failedValidationMessage('Please enter valid Bundle Version (For Example : 1.0.1)')
            description('App internal build version. This value is used only to determine whether one version is more recent than another, with higher numbers indicating more recent versions. This setting is available at "Project Settings -> Native -> iPhone/iPad/Watch -> Bundle Version" in Iris. <br> For Example : 1.0.1')
        }
        choiceParam('IOS_DISTRIBUTION_TYPE', ['development', 'appstore', 'enterprise', 'adhoc'], args.appleDeveloperProfileTypeParamDesc)
        booleanParam('APPLE_WATCH_EXTENSION', false, 'Select the checkbox if you want to build iOS app with Watch extension')
    }
}
