def getParameters(args) {
    return {
        it / 'properties' / 'hudson.model.ParametersDefinitionProperty' / 'parameterDefinitions' <<
                'com.kony.AppFactory.Jenkins.parameters.cron.CronParameterDefinition' {
                    'name'('CRON_SCHEDULE')
                    'defaultValue'('')
                    'description'(args.cronScheduleParamDesc)
                    'required'(args.isRequired)
                }
    }
}
