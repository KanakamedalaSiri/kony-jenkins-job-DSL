def getParameters(args) {

    def suffixForArtifactUrl = '<br>Note: Specified URL should be public.<br>For <em>Jasmine test framework</em> run, the binary of the URL must be built in <code>Test</code> build mode.' +
            '<br>For all other automation frameworks (Ex: TestNG) run, the binary of the URL most likely built in <code>Release</code> or <code>Release-protected</code> build mode.'

    return {
        parameterSeparatorDefinition {
            name('NATIVE_HEADER')
            separatorStyle('')
            sectionHeader(args.nativeRunTestsSpecificDesc)
            sectionHeaderStyle('')
        }
        stringParam('ANDROID_UNIVERSAL_NATIVE_BINARY_URL', '',
                'Location of the Android universal binary. ' + suffixForArtifactUrl)
        stringParam('ANDROID_MOBILE_NATIVE_BINARY_URL', '',
                'Location of the Android mobile binary. ' + suffixForArtifactUrl)
        stringParam('ANDROID_TABLET_NATIVE_BINARY_URL', '',
                'Location of the Android tablet binary. ' + suffixForArtifactUrl)
        stringParam('IOS_UNIVERSAL_NATIVE_BINARY_URL', '',
                'Location of the iOS universal binary. ' + suffixForArtifactUrl)
        stringParam('IOS_MOBILE_NATIVE_BINARY_URL', '',
                'Location of the iOS mobile binary. ' + suffixForArtifactUrl)
        stringParam('IOS_TABLET_NATIVE_BINARY_URL', '',
                'Location of the iOS tablet binary. ' + suffixForArtifactUrl)
        stringParam('NATIVE_TESTS_URL', '', 'Location of the test binary.' +
                '<br> Notes: The test binary (Appium Java Test Package) should be ' +
                'compatible with Device Farm and must be in .zip format, and the specified URL should be public.')
    }
}
