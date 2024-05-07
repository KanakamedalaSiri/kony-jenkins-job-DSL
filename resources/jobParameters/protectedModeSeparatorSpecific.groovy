def getParameters(args) {
	return {
		parameterSeparatorDefinition {
			name('PROTECTED_BUILD_HEADER')
			separatorStyle('')
			sectionHeader(args.protectedBuildParamsSeparatorDesc)
			sectionHeaderStyle('')
		}
		credentialsParam('PROTECTED_KEYS') {
			type('com.kony.AppFactory.Jenkins.credentials.impl.ProtectedModeTriplet')
			defaultValue('')
			description('The public key, private key and fin key to enable additional security with Iris app. You can skip this input build parameter if your build mode is other than release-protected.')
		}
	}
}