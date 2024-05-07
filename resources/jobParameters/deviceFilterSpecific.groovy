def getParameters(args) {
    return {
        it / 'properties' / 'hudson.model.ParametersDefinitionProperty' / 'parameterDefinitions' <<
                'com.kony.AppFactory.Jenkins.parameters.deviceFilter.DeviceFilterDefinition' {
                    'name'('DEVICE_FILTER')
                    'description'('Device pool parameter containing filters for device selection.')
                    'projectName'(args.projectName)
                }
    }
}