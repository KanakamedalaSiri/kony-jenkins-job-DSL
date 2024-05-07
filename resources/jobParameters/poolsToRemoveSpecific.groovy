def getParameters(args) {
    return {
        it / 'properties' / 'hudson.model.ParametersDefinitionProperty' / 'parameterDefinitions' <<
                'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                    'name'('AVAILABLE_TEST_POOLS')
                    'description'(args.poolsToRemoveParamDesc)
                    /* Workaround to expose root folder of the project for fetching available pools (custom configs)
                    Default currentProject object that is available in groovy scripts for extended choice parameter returns
                    null object for jobs that been created in subfolders, because in projectName property that is used for
                    getting project object it stores job name instead of job path.
                    That is why we are adding variable bindings with projectName variable that set to projectRootFolderName
                    during project structure creation.
                 */
                    'bindings'("projectName=${args.projectName}")
                    'groovyClasspath'()
                    'multiSelectDelimiter'(',')
                    'visibleItemCount'(1)
                    'type'('PT_SINGLE_SELECT')
                    'groovyScript'(args.showAvailablePoolsScript)
                }
    }
}



