package helper

import exceptions.AppfactoryAbortException
import java.util.List
import java.util.Map
import jenkins.model.Jenkins

class validation_helper {

    /**
     * Validates DSL job parameters
     *
     * @param seedJobName
     * @param buildParameters
     * @throws AppfactoryAbortException when validation fails.
     * */
    def validateParameters(seedJobName, buildParameters){

        def scmParameters

        def nativeAppBinaryUrlParameters
        def nativeTestBinaryUrlParameter

        def desktopWebPublishedAppUrlParameters

        def poolNameParameter

        switch (seedJobName) {
            case 'createTest':
                /* Filter all Iris binary url build parameters */
                nativeAppBinaryUrlParameters = getParametersByPartialName(buildParameters, 'NATIVE_BINARY_URL')
                // desktopwebappURl might be WEB_APP_URL or FABRIC_APP_URL build parameter (in dsl projects < 9.3, it was FABRIC_APP_URL). So collecting the exact parameter.
                desktopWebPublishedAppUrlParameters = getParametersByPartialName(buildParameters, '_APP_URL')

                /* Filter testNG test binaries build parameter. This check is needed to maintain backward compatibility */
                def nativeTestsUrl = buildParameters.containsKey('NATIVE_TESTS_URL') ? 'NATIVE_TESTS_URL' : 'TESTS_BINARY_URL'
                
                nativeTestBinaryUrlParameter = getParametersByPartialName(buildParameters, nativeTestsUrl)

                /* Filter pool name build parameter */
                poolNameParameter = getParametersByPartialName(buildParameters, 'AVAILABLE_TEST_POOLS')
                /* Filter all SCM build parameters */
                scmParameters = getParametersByPartialName(buildParameters, 'PROJECT_SOURCE_CODE')

                // ---- Test Binary Specific -----

                /* Validate if both the source are provided for Test binary */
                if (scmParameters && nativeTestBinaryUrlParameter) {
                    throw new AppfactoryAbortException('Please provide only one option for the source of test scripts: GIT or TESTS_URL')
                }

                /* Validation if none of the options provided for Test Binary */
                if (!nativeTestBinaryUrlParameter && !scmParameters) {
                    throw new AppfactoryAbortException('Please provide at least one source of test binaries')
                }

                /* scm Test binary parameters been provided without nativeAppBinaryUrlParameters and desktopWebPublishedAppUrlParameters */
                if (scmParameters && (!nativeAppBinaryUrlParameters && !desktopWebPublishedAppUrlParameters)) {
                    throw new AppfactoryAbortException("Please provide Application binary URL along with test source'.")
                }

                if(nativeTestBinaryUrlParameter && !nativeAppBinaryUrlParameters){
                    throw new AppfactoryAbortException("Please provide both 'Application binary' and 'Test binary' for Native tests.")
                }

                // ---- Iris Application Specific Validations -----
                /* Check if at least one application binaries parameter been provided */
                if (!nativeAppBinaryUrlParameters && !desktopWebPublishedAppUrlParameters) {
                    throw new AppfactoryAbortException("Please provide at least one of application binary URLs")
                }

                /* Check if application binaries parameter been provided, validate urls*/
                (!nativeAppBinaryUrlParameters) ?:
                        validateApplicationBinariesURLs(nativeTestBinaryUrlParameter << nativeAppBinaryUrlParameters)
                (!desktopWebPublishedAppUrlParameters) ?:
                        validateApplicationBinariesURLs(desktopWebPublishedAppUrlParameters)

                // ---- Universal Application Specific Validations -----
                /*
                    Restrict the user to run tests either with Universal build binary or with normal native test binaries,
                    fail the build if both options are provided.
                */
                if (buildParameters.ANDROID_UNIVERSAL_NATIVE_BINARY_URL && (buildParameters.ANDROID_MOBILE_NATIVE_BINARY_URL || buildParameters.ANDROID_TABLET_NATIVE_BINARY_URL)) {
                    throw new AppfactoryAbortException('Sorry, You can\'t run test for Android Universal binary along with Android Mobile/Tablet')
                }
                if (buildParameters.IOS_UNIVERSAL_NATIVE_BINARY_URL && (buildParameters.IOS_MOBILE_NATIVE_BINARY_URL || buildParameters.IOS_TABLET_NATIVE_BINARY_URL)) {
                    throw new AppfactoryAbortException('Sorry, You can\'t run test for iOS Universal binary along with iOS Mobile/Tablet')
                }

                // ---- DevicePool Specific -----
                /* Fail build if nativeAppBinaryUrlParameters been provided without test pool */
                if (!poolNameParameter && nativeAppBinaryUrlParameters) {
                    throw new AppfactoryAbortException('Please provide pool to test provided app binaries.')
                }

                break
            case 'createTrigger':
                break
            default:
                break
        }
    }

    /**
     * Validate application binaries URLs
     *
     * @param appBinaryUrlParameters
     * @throw DSLExceptions in case URL isn't valid
     */
    def validateApplicationBinariesURLs(appBinaryUrlParameters) {
        for (parameter in appBinaryUrlParameters) {
            if (parameter.value.contains('//') && isValidUrl(parameter.value))
                parameter.value = parameter.value.replace(" ", "%20")
            else
                throw new AppfactoryAbortException("Build parameter ${parameter.key} value is not valid URL!")
        }
    }

    /**
     * Validates provided URL.
     *
     * @param urlString URL to validate.
     * @return validation result (true or false).
     */
    def isValidUrl(urlString) {
        try {
            urlString.replace(" ", "%20").toURL().toURI()
            return true
        } catch (Exception exception) {
            return false
        }
    }

    /**
     * Collect all the parameters containing partialParameterName which are neither empty nor null.
     *
     * @param buildParameters
     * @param partialParameterName
     * @return list of Parameter matching the partialParameterName
     * */
    def getParametersByPartialName(buildParameters, partialParameterName) {
        return buildParameters.findAll {
            it.key.contains(partialParameterName) && it.value
        }
    }
    
    /**
     * Getting the triggered job parameters map and iterating over same name schedulers list and comparing with
     * each existing schedulers with new trigger job parameters. If AppFactory user is trying to create a trigger
     * with same cron expression and params will fail the creation of such trigger.
     *
     * @param sameCronExpressionTriggerList
     * @param triggeredJobParametersMap
     * @return if true it fails the creation of duplicate trigger else allow to continue
     **/
    def checkForSameCronExpressionTriggerWithSameParam(List<String> sameCronExpressionTriggerList, Map<String,String> triggeredJobParametersMap) {
        sameCronExpressionTriggerList.each { sameCronExpressionTrigger ->
            def folder = Jenkins.instance.getItemByFullName(sameCronExpressionTrigger)
            folder.buildersList.each{
                it.getConfigs().each{ c ->
                    for(p in c.configs) {
                        boolean areAlike = true
                        String parameterList = p.getProperties()
                        def existingTriggerWithSameCron = [:]
                        parameterList.eachLine{ param ->
                            def existingTriggerParamName = param.split("=")[0]
                            def existingTriggerParamValue = param.split("=")[1]
                            existingTriggerWithSameCron.put(existingTriggerParamName, existingTriggerParamValue)
                        }
                        boolean areKeySetsAlike = existingTriggerWithSameCron.keySet().equals(triggeredJobParametersMap.keySet())

                        if (areKeySetsAlike) {
                            for (paramKey in existingTriggerWithSameCron.keySet()){
                                if (existingTriggerWithSameCron.get(paramKey) != triggeredJobParametersMap.get(paramKey).toString()) {
                                    areAlike = false
                                    break
                                }
                            }
                            if(areAlike) {
                                throw new AppfactoryAbortException("Looks like you have already a Tigger job ($sameCronExpressionTrigger) with same build settings. createTrigger with same cron expression and with same parameters is not allowed!!")
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Validate project settings source control parameters
     * @param scmSettingsMap - map with source control settings values
     */
    def validateProjectSettingsScmParameters(scmSettingsMap) {
        def emptyParams = []
        def requiredScmParamsMap = ["PROJECT_SOURCE_CODE_URL": 'Repository URL', 'PROJECT_SOURCE_CODE_REPOSITORY_CREDENTIALS_ID': "SCM Credentials", "PROJECT_SOURCE_CODE_SERVER_TYPE": "SCM Vendor"]
        requiredScmParamsMap?.keySet()?.each { param ->
            if (!scmSettingsMap[param])
                emptyParams.add(requiredScmParamsMap[param])
        }
        /* If there are empty parameters */
        if (emptyParams) {
            String message = 'Project Settings parameter' + ((emptyParams.size() > 1) ? "'s" : '')
            String requiredParamsErrorMessage = [emptyParams.join(', '), message, "can't be null!"].join(' ')
            throw new AppfactoryAbortException(requiredParamsErrorMessage)
        }
    }
}
