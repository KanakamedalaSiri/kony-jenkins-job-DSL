def getParameters(args) {
    return {
        if (args.isFacadeJob)
            booleanParam('ANDROID', false, 'Select the checkbox if your build is for Android specific application')
        else {
            parameterSeparatorDefinition {
                name('ANDROID_HEADER')
                separatorStyle('')
                sectionHeader(args.androidParamsSeparatorDesc)
                sectionHeaderStyle('')
            }
        }
        if(args.isFacadeJob)
            booleanParam('ANDROID_UNIVERSAL_NATIVE', false, 'Select the checkbox if you want to build Android universal app and then fill all required parameters below.')
        stringParam('ANDROID_UNIVERSAL_APP_ID', '', 'Please provide the Universal app (Mobile and Tablet) unique application ID, to identify your app on the device and in Google Play Store. It is the value you generally enter in build UI mode at "Project Settings -> Native -> Android -> Package Name". <br> For Example : com.quantumappfactory.KitchenSink')
        if(args.isFacadeJob)
            booleanParam('ANDROID_MOBILE_NATIVE', false, 'Select the checkbox if you want to build Android app for Mobile and fill all the required parameters below.')
        stringParam('ANDROID_MOBILE_APP_ID', '', 'Please provide mobile app unique application ID, to identify your app on the device and in Google Play Store. It is the value you generally enter in build UI mode at "Project Settings -> Native -> Android -> Package Name". <br> For Example : com.quantumappfactory.KitchenSink')
        if(args.isFacadeJob)
            booleanParam('ANDROID_TABLET_NATIVE', false, 'Select the checkbox if you want to build Android app for Tablet and fill all the required parameters below.')
        stringParam('ANDROID_TABLET_APP_ID', '', 'Please provide tablet app unique application ID, to identify your app on the device and in Google Play Store. It is the value you generally enter in build UI mode at "Project Settings -> Native -> Android -> Package Name". <br> For Example : com.quantumappfactory.KitchenSink')
        validatingStringParameterDefinition {
            name('ANDROID_APP_VERSION')
            defaultValue('')
            regex('^\\s*$|^[0-9]+\\.[0-9]+\\.[0-9]+$')
            failedValidationMessage('Please enter valid App Version (For Example : 1.0.1)')
            description('Version of the Android application. This setting is available at "Project Settings -> Application -> Version" in Iris. <br> For Example : 1.0.1')
        }
        validatingStringParameterDefinition {
            name('ANDROID_VERSION_CODE')
            defaultValue('')
            regex('^\\s*$|^[1-9][0-9]*$')
            failedValidationMessage('Please enter valid Android Version Code (For Example : An integer value such as : 1)')
            description('App internal version number. This value is used only to determine whether one version is more recent than another, with higher numbers indicating more recent versions. This setting is available at "Project Settings -> Native -> Android -> Version Code" in Iris. <br> For Example : An integer value such as : 1')
        }
        booleanParam('ANDROID_APP_BUNDLE', false, 'Select the checkbox to build the Android binary in Android App Bundle (AAB) format for store submission.')
        booleanParam('SUPPORT_x86_DEVICES', false, 'Select the checkbox to generate binary (apk) for x86 architecture along with ARM architecture. This setting is available at "Project Settings -> Native -> Android -> Support x86 Devices" in Iris.')

    }
}