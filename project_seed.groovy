import exceptions.AppfactoryAbortException
import jenkins.model.Jenkins
import hudson.model.Cause
import hudson.model.Executor
import com.cloudbees.hudson.plugins.folder.Folder

/* Create an instance of GroovyShell class to be able to fetch User who triggered the build */
groovyShell = new GroovyShell()
/* Get the currentBuild instance */
currentBuild = Executor.currentExecutor().currentExecutable
/* Get user ID which triggered the build, for setting it as an owner of the project */
user = currentBuild.getCause(Cause.UserIdCause)
projectName = (PROJECT_NAME)?.capitalize()
projectType = PROJECT_TYPE
isOlderFabricJobsExists = false
/* Check if project (root folder) already exists */
project = isProjectExists(projectName)
services = setSeviceTypes()
facadeJobName = 'buildIrisApp'
fabricFacadeJobName = 'buildFabricApp'
fabricTaskFolderName = "FabricTasks"
buildsFolderName = 'Builds'
channelsFolderName = 'Channels'
testAutomationChannels = 'Channels'

/* Node to run on label */
restrictSeedJobsToRunOn = SEED_JOB.assignedLabelString ?: 'preparation'
/* Get properties for seed jobs (createTrigger and createTest) */
scriptsCheckoutFolder = "."
showAvailablePoolsScriptPath = getPath([scriptsCheckoutFolder, 'show_available_pools.groovy'])
triggersScriptPath = getPath([scriptsCheckoutFolder, 'triggers_seed.groovy'])
managePoolsScriptPath = getPath([scriptsCheckoutFolder, 'manage_pools_seed.groovy'])
customHookScriptPath = getPath([scriptsCheckoutFolder, 'custom_hook.groovy'])
/* Get path to resources folder */
resourcesFolder = getPath([scriptsCheckoutFolder, 'resources'])

/* Load corresponding project description as per project type */
projectDesc = (projectType == "Volt MX Project") ? loadDescription('project/Iris/Builds/buildIrisApp', 'desc') :
        loadDescription('project/Microservices', 'desc')

/* Load job/parameter descriptions */
projectDesc = loadDescription('project/Iris/Builds/buildIrisApp', 'desc')
scmParamsSeparatorDesc = loadDescription('project/Iris/Builds/buildIrisApp', 'SCM_HEADER')
projectSourceCodeBranchParamDesc = loadDescription('project/Iris/Builds/buildIrisApp',
        'PROJECT_SOURCE_CODE_BRANCH')
fabricProjectSourceCodeBranchParamDesc = loadDescription('project/Foundry',
        'FABRIC_PROJECT_SOURCE_CODE_BRANCH')
buildModeParamDesc = loadDescription('project/Iris/Builds/buildIrisApp', 'BUILD_MODE')
konyParamsSeparatorDesc = loadDescription('project/Iris/Builds/buildIrisApp', 'KONY_HEADER')
channelsHeaderDesc = loadDescription('project/Iris/Builds/buildIrisApp', 'TARGETS_HEADER')
androidParamsSeparatorDesc = loadDescription('project/Iris/Builds/buildIrisApp', 'ANDROID_HEADER')
iosParamsSeparatorDesc = loadDescription('project/Iris/Builds/buildIrisApp', 'IOS_HEADER')
appleDeveloperProfileTypeParamDesc = loadDescription('project/Iris/Builds/buildIrisApp',
        'IOS_DISTRIBUTION_TYPE')
nativeTestingParamsSeparatorDesc = loadDescription('project/Iris/Builds/buildIrisApp', 'NATIVE_TESTING_HEADER')
webTestingParamsSeparatorDesc = loadDescription('project/Iris/Builds/buildIrisApp', 'WEB_TESTING_HEADER')
createEditPoolParamsSeperatorDesc = loadDescription('project/Iris/Builds/buildIrisApp', 'CREATE_EDIT_POOL_HEADER')
availableTestPoolsParamDesc = loadDescription('project/Iris/Builds/buildIrisApp', 'AVAILABLE_TEST_POOLS')
/* Using 'poolsToRemoveParamDesc' for AVAILABLE_TEST_POOLS description in Manage_pool job as different header and description are required. */
poolsToRemoveParamDesc = loadDescription('project/Iris/Builds/buildIrisApp', 'POOLS_TO_REMOVE')
removePoolParamsSeparatorDesc = loadDescription('project/Iris/Builds/buildIrisApp', 'REMOVE_POOL_HEADER')
fabricCredentialsIDParamDesc = loadDescription('project/Iris/Builds/buildIrisApp', 'FABRIC_CREDENTIALS_ID')
webParamsSeparatorDesc = loadDescription('project/Iris/Builds/buildIrisApp', 'WEB_HEADER')
fabricScmParamsSeparatorDesc = loadDescription('project/Foundry', 'SCM_HEADER')
fabricKonyParamsSeparatorDesc = loadDescription('project/Foundry', 'KONY_HEADER')
fabricImportOptionsParamsSeparatorDesc = loadDescription('project/Foundry', 'IMPORT_OPTIONS_HEADER')
cronScheduleParamDesc = loadDescription('project/Iris/Triggers/createTrigger', 'CRON_SCHEDULE')
fabricCronScheduleParamDesc = loadDescription('project/Iris/Triggers/createTrigger', 'CRON_SCHEDULE_FABRIC')
customHookFacadeSpecificDesc = loadDescription('project/Iris/Builds/buildIrisApp', 'CUSTOM_HOOK_FACADE_HEADER')
desktopWebTestsSpecificDesc = loadDescription('project/Iris/Tests/runTests', 'DESKTOP_WEB_TESTS_HEADER')
nativeRunTestsSpecificDesc = loadDescription('project/Iris/Tests/runTests', 'NATIVE_HEADER')
notificationsParamsSeparatorDesc = loadDescription('project/Iris/Builds/buildIrisApp', 'NOTIFICATIONS_HEADER')
publishToFabricParamDesc = loadDescription('project/Iris/Builds/buildIrisApp', 'PUBLISH_TO_FABRIC')
protectedBuildParamsSeparatorDesc = loadDescription('project/Iris/Builds/buildIrisApp', 'PROTECTED_BUILD_HEADER')
testFrameworkParamsSeparatorDesc = loadDescription('project/Iris/Builds/buildIrisApp', 'TEST_FRAMEWORK_HEADER')
webProtectionParamsSeparatorDesc = loadDescription('project/Iris/Builds/buildIrisApp', 'WEB_PROTECTION_HEADER')
/* Database related Param Descriptions */
flywayScmSeparatorDesc = loadDescription('project/Database', 'SCM_HEADER')
flywaySpecificSeparatorDesc = loadDescription('project/Database', 'FLYWAY_HEADER')
scmVendorParamDesc = loadDescription('project/Database', 'SCM_VENDOR_TYPE')
flywayScmCredentialsParamDesc = loadDescription('project/Database', 'SCM_CREDENTIALS')

/* Microservices related Param Descriptions */
microServicesProjectSourceCodeBranchParamDesc = loadDescription('project/Microservices', 'SCM_BRANCH')
microServicesScmSeparatorDesc = loadDescription('project/Microservices', 'SCM_HEADER')
microServicesSpecificSeparatorDesc = loadDescription('project/Microservices', 'MICROSERVICE_HEADER')
microServicesJoltFilesParamDesc = loadDescription('project/Microservices', 'JOLT_FILES')
microServicesPolicyFilesParamDesc = loadDescription('project/Microservices', 'POLICY_FILES')

/* Scan related Param Descriptions */
scanScmParamsSeparatorDesc = loadDescription('project/Scans', 'SCM_HEADER')
scanProjectSourceCodeBranchParamDesc = loadDescription('project/Scans', 'SCM_BRANCH')

/* Load job parameters */
channelsHeaderSpecific = parseJobParameters('channelsHeaderSpecific', [channelsHeaderDesc: channelsHeaderDesc])
desktopWebTestsSpecificParameters = parseJobParameters('desktopWebTestsSpecific', [desktopWebTestsSpecificDesc: desktopWebTestsSpecificDesc])
desktopWebTestsCommonParameters = parseJobParameters('desktopWebTestsCommonParameters')
/* Separated SCM specific parameters which will be used for checking out source code */
scmSpecificParameters = parseJobParameters('scmSpecificParams', [scmParamsSeparatorDesc: scmParamsSeparatorDesc, projectSourceCodeBranchParamDesc: projectSourceCodeBranchParamDesc])

notificationsSpecificParameters = parseJobParameters('notificationsSpecific', [notificationsParamsSeparatorDesc: notificationsParamsSeparatorDesc])
visualizerSpecificBuildParameters = parseJobParameters('visualizerSpecific',
        [konyParamsSeparatorDesc: konyParamsSeparatorDesc,
         buildModeParamDesc: buildModeParamDesc,
         fabricCredentialsIDParamDesc: fabricCredentialsIDParamDesc])
facadeAndroidSpecificBuildParameters = parseJobParameters('androidSpecific',
        [androidParamsSeparatorDesc: androidParamsSeparatorDesc,
         isFacadeJob: true])
androidSpecificBuildParameters = parseJobParameters('androidSpecific',
        [androidParamsSeparatorDesc: androidParamsSeparatorDesc,
         isFacadeJob: false])
facadeIosSpecificBuildParameters = parseJobParameters('iosSpecific',
        [iosParamsSeparatorDesc            : iosParamsSeparatorDesc,
         appleDeveloperProfileTypeParamDesc: appleDeveloperProfileTypeParamDesc, isFacadeJob: true])
iosSpecificBuildParameters = parseJobParameters('iosSpecific',
        [iosParamsSeparatorDesc            : iosParamsSeparatorDesc,
         appleDeveloperProfileTypeParamDesc: appleDeveloperProfileTypeParamDesc, isFacadeJob: false])
facadeDesktopWebSpecificBuildParameters = parseJobParameters('webSeparatorSpecific',
        [webParamsSeparatorDesc: webParamsSeparatorDesc,
         webProtectionParamsSeparatorDesc: webProtectionParamsSeparatorDesc,
         publishToFabricParamDesc: publishToFabricParamDesc, isFacadeJob: true])
webSeparatorSpecificBuildParameters = parseJobParameters('webSeparatorSpecific',
        [webParamsSeparatorDesc: webParamsSeparatorDesc,
         webProtectionParamsSeparatorDesc: webProtectionParamsSeparatorDesc,
         publishToFabricParamDesc: publishToFabricParamDesc, isFacadeJob: false])
channelSpecificBuildParameters = parseJobParameters('channelSpecific')
fabricPublishBuildParameters = parseJobParameters('fabricPublishSpecific')
testAutomationBuildParameters = parseJobParameters('testAutomation', [nativeRunTestsSpecificDesc: nativeRunTestsSpecificDesc])
availablePoolsParameter = parseJobParameters('deviceFarmSpecific',
        [showAvailablePoolsScript: readFileFromWorkspace(showAvailablePoolsScriptPath),
         projectName: projectName,
         availableTestPoolsParamDesc: availableTestPoolsParamDesc,
         nativeTestingParamsSeparatorDesc: nativeTestingParamsSeparatorDesc])
availablePoolsParameterForRunTests = parseJobParameters('deviceFarmSpecific',
        [showAvailablePoolsScript: readFileFromWorkspace(showAvailablePoolsScriptPath),
	  projectName: projectName,
         availableTestPoolsParamDesc: availableTestPoolsParamDesc])

testEnvironmentParameters = parseJobParameters('testEnvironmentSpecific')
nativeTestingParameters = parseJobParameters('nativeTestingFacadeSpecific')
runNativeTestsFacadeParameters = parseJobParameters('runNativeTestsFacadeSpecific')
nativeTestDataParameters = parseJobParameters('nativeTestDataSpecific')
webTestDataParameters = parseJobParameters('webTestDataSpecific')
nativeTestingSeparatorSpecificParameters = parseJobParameters('nativeTestingFacadeSeparatorSpecific', [nativeTestingParamsSeparatorDesc: nativeTestingParamsSeparatorDesc])
customHookParam = parseJobParameters('createCustomHookSpecific')
updateCustomHookParam = parseJobParameters('updateCustomHookSpecific')
disableCustomHookParameter = parseJobParameters('customHookFacadeSpecific', [customHookFacadeSpecificDesc: customHookFacadeSpecificDesc])
disableDesktopWebTestsParameter = parseJobParameters('desktopWebTestsVisualizerSpecific', [webTestingParamsSeparatorDesc: webTestingParamsSeparatorDesc])
protectedModeSpecificBuildParameters = parseJobParameters('protectedModeSeparatorSpecific',
		[protectedBuildParamsSeparatorDesc: protectedBuildParamsSeparatorDesc])

//Foundry Import/Export/Publish/Migrate Scheduler seed job settings
fabricExportOptionsParamsSeparatorDesc = loadDescription('project/Foundry', 'EXPORT_OPTIONS_HEADER')
fabricExportParamsSeparatorDesc = loadDescription('project/Foundry', 'FABRIC_EXPORT_HEADER')
fabricImportParamsSeparatorDesc = loadDescription('project/Foundry', 'FABRIC_IMPORT_HEADER')
fabricExportServiceConfigPathParamsDesc = loadDescription('project/Foundry', 'EXPORT_SERVICE_CONFIG_PATH')
buildFabricAppServiceConfigPathParamsDesc = loadDescription('project/Foundry', 'FABRIC_APP_SERVICE_CONFIG_PATH')
fabricCommonScmBuildParameters = parseJobParameters('fabricCommonScm', [scmParamsSeparatorDesc: fabricScmParamsSeparatorDesc, projectSourceCodeBranchParamDesc: fabricProjectSourceCodeBranchParamDesc])
exportValidateVersionParameters = parseJobParameters('exportValidateVersion', [fabricExportOptionsParamsSeparatorDesc: fabricExportOptionsParamsSeparatorDesc])
scmOverWriteExistingFlagParameters = parseJobParameters('scmOverWriteExistingFlag', [fabricExportOptionsParamsSeparatorDesc: fabricExportOptionsParamsSeparatorDesc])
fabricAppOverWriteExistingFlagParameters = parseJobParameters('fabricAppOverWriteExistingFlag', [fabricImportOptionsParamsSeparatorDesc: fabricImportOptionsParamsSeparatorDesc])
fabricCommitOptionsParamsSeparatorDesc = loadDescription('project/Foundry', 'COMMIT_OPTIONS_HEADER')
commitSpecificParameters = parseJobParameters('commitSpecific', [fabricCommitOptionsParamsSeparatorDesc: fabricCommitOptionsParamsSeparatorDesc])
fabricParamsSeparatorDesc = parseJobParameters('fabricParamsSeparator', [fabricKonyParamsSeparatorDesc: fabricKonyParamsSeparatorDesc])
fabricCredentialsParameters = parseJobParameters('fabricCredentialsSpecific')
exportFabricCredentialsParameters = parseJobParameters('exportFabricCredentialsSpecific', [fabricExportParamsSeparatorDesc: fabricExportParamsSeparatorDesc])
importFabricCredentialsParameters = parseJobParameters('importFabricCredentialsSpecific', [fabricImportParamsSeparatorDesc: fabricImportParamsSeparatorDesc])
fabricPublishFlagParameters = parseJobParameters('fabricPublishFlag')
fabricAppConfigParameter = parseJobParameters('fabricAppConfigSpecific')
fabricAppConfigExportParameter = parseJobParameters('fabricAppConfigExportSpecific')
fabricAppConfigImportParameter = parseJobParameters('fabricAppConfigImportSpecific')
ignoreJarAssetsFlagParameter = parseJobParameters('ignoreJarAssetsFlag')

/* Foundry app build parameter */
fabricMavenBuildParamsSeparatorDesc = loadDescription('project/Foundry', 'FABRIC_MAVEN_BUILD_HEADER')
fabricMavenBuildSpecificParameters = parseJobParameters ('fabricMavenBuildSpecific', [fabricMavenBuildParamsSeparatorDesc: fabricMavenBuildParamsSeparatorDesc])
fabricBuildImportFlagSpecificParameter = parseJobParameters ('fabricBuildImportFlagSpecific')
fabricBuildImportAndPublishSpecificParameters = parseJobParameters('fabricBuildImportAndPublishSpecific')
fabricBuildCronScheduleParamDesc = loadDescription('project/Foundry/Triggers', 'CRON_SCHEDULE_FABRIC_BUILD')
fabricServiceConfigPathParameter = parseJobParameters('fabricServiceConfigPathSpecific', [fabricServiceConfigPathParamsDesc: buildFabricAppServiceConfigPathParamsDesc])
fabricServiceConfigPathExportParameter = parseJobParameters('fabricServiceConfigPathSpecific', [fabricServiceConfigPathParamsDesc: fabricExportServiceConfigPathParamsDesc])

/* Foundry tests build parameter */
fabricTestsParamsSeparatorDesc = loadDescription('project/Foundry/Tests/runFabricTests', 'FABRIC_TESTS_HEADER')
fabricTestsSeparatorSpecific = parseJobParameters('fabricTestsHeaderSpecific', [fabricTestsParamsSeparatorDesc: fabricTestsParamsSeparatorDesc])
/* Kept for future scope. */
fabricTestsFrameworkSpecific = parseJobParameters('fabricTestsFrameworkSpecific')
runFabricTestsFacadeSpecific = parseJobParameters('runFabricTestsFacadeSpecific')
fabricRunTestsParameter = parseJobParameters('fabricCucumberSpecific')

/* Using 'poolsToRemoveParamDesc' for AVAILABLE_TEST_POOLS as parameter serves different purpose in managepool job than in buildIrisApp job. */
poolsToRemoveParameter = parseJobParameters('poolsToRemoveSpecific',
        [showAvailablePoolsScript: readFileFromWorkspace(showAvailablePoolsScriptPath),
         projectName: projectName,
         poolsToRemoveParamDesc: poolsToRemoveParamDesc])
deviceFilterParameter = parseJobParameters('deviceFilterSpecific', [projectName: projectName])
testFrameworkSeparatorSpecificParameter = parseJobParameters('testFrameworkSeparatorSpecific', [testFrameworkParamsSeparatorDesc: testFrameworkParamsSeparatorDesc])
testFrameworkTypeSpecificParameter = parseJobParameters('testFrameworkSpecific')
testCodeCoverageSpecificParameter = parseJobParameters('testCodeCoverageSpecific')
jasmineTestSpecificParameter = parseJobParameters('jasmineTestSpecific')

/* Scan related Params */
scanCommonScmBuildParameters = parseJobParameters('fabricCommonScm', [scmParamsSeparatorDesc: scanScmParamsSeparatorDesc, 
                                                                      projectSourceCodeBranchParamDesc: scanProjectSourceCodeBranchParamDesc])
/* Flyway job related params */
flywayScmSpecificParameters = parseJobParameters('flywayScmSpecific', [flywayScmSeparatorDesc: flywayScmSeparatorDesc,
                                                                       scmVendorParamDesc: scmVendorParamDesc,
                                                                       projectSourceCodeBranchParamDesc: projectSourceCodeBranchParamDesc,
                                                                       flywayScmCredentialsParamDesc: flywayScmCredentialsParamDesc])
flywayJobSpecificParameters = parseJobParameters('flywayJobSpecific', [flywaySpecificSeparatorDesc: flywaySpecificSeparatorDesc])
configureMSJobSpecificParameters = parseJobParameters('configureMSJobSpecific', [microServicesProjectSourceCodeBranchParamDesc: microServicesProjectSourceCodeBranchParamDesc, microServicesScmSeparatorDesc: microServicesScmSeparatorDesc, microServicesSpecificSeparatorDesc: microServicesSpecificSeparatorDesc,
                                                                                 microServicesJoltFilesParamDesc: microServicesJoltFilesParamDesc, microServicesPolicyFilesParamDesc: microServicesPolicyFilesParamDesc])
/* Create project root folder */
if (!project) {
    /*
        The 'configure' step only works when updating an already existing folder so we have to call 'folder' twice.
        Create root folder first.
     */
    folder("$projectName")

    /* Update created folder */
    folder("$projectName") {
        configure { folder ->
            /* Set Config File property to fix problem with failing scripts in drop-down list
                of AVAILABLE_TEST_POOLS build parameter.
             */
            folder / 'properties' / 'org.jenkinsci.plugins.configfiles.folder.FolderConfigFileProperty' {
                configs(class: 'sorted-set') {
                    comparator(class: 'org.jenkinsci.plugins.configfiles.folder.FolderConfigFileProperty$1')
                }
            }

            /* Set Ownership property to make user that triggers the job as an owner of the project */
            folder / 'properties' / 'org.jenkinsci.plugins.ownership.model.folders.FolderOwnershipProperty' / 'ownership' {
                primaryOwnerId(user.getUserId())
                ownershipEnabled('true')
                coownersIds(class: "sorted-set")
            }

            /* Set Appfactory Project Settings default structure*/
            folder / 'properties' / 'com.kony.appfactory.project.settings.ProjectSettingsProperty' {
                'projSettings' {
                    projectDSLVersion(JOB_DSL_SCRIPTS_BRANCH)
                    visualizerSettings {
                        sourceControl{}
                        android{}
                        ios{}
                        internationalization{}
                        scans {
                            sonar{}
                        }
                        models{}
                        notification{}
                    }
                    fabricSettings {
                        sourceControl{}
                        maven{}
                        notification{}
                    }
                    microserviceSettings{
                        sourceControl{}
                        notification{}
                    }
                }
            }
        }
        description("An App Factory project for $projectName" + projectDesc)
    }
} else{

    /* Avoid overriding of Microservice Project with Volt MX Project or vice versa */
    def isProjectTypeMismatch = false
    if (projectType == "Volt MX Project") {
        microserviceFullPath = projectName + "/Microservices"
        isProjectTypeMismatch = isServiceExists(microserviceFullPath)
    } else if (projectType == "Volt MX Microservice Project") {
        visualizerServiceFullPath = projectName + "/Iris"
        fabricServiceFullPath = projectName + "/Foundry"
        isProjectTypeMismatch = (isServiceExists(visualizerServiceFullPath) || isServiceExists(fabricServiceFullPath))
    }

    if(isProjectTypeMismatch){
        throw new AppfactoryAbortException("\n'$projectName' project already exists with other project type. Please choose different name for your project Or choose appropriate value for PROJECT_TYPE")
    }
}

/* Create project structure as per service selected */
services.each { service ->
    switch (service) {
        case 'Iris':
                cronScheduleVizParam = parseJobParameters('triggersSpecific', [cronScheduleParamDesc: cronScheduleParamDesc, isRequired: 'false'])
                /* Create service folder */
                folder("${projectName}/${service}")

                /* Create sub-folders */
                createVisualizerProjectStructure(service)
            break
        case 'Foundry':
                cronScheduleFabricParam = parseJobParameters('triggersSpecific', [cronScheduleParamDesc: fabricCronScheduleParamDesc, isRequired: 'true'])
                cronScheduleParamWithWatcherSupport = parseJobParameters('triggersSpecific', [cronScheduleParamDesc: fabricBuildCronScheduleParamDesc, isRequired: 'false'])
                /* Create service folder */
                folder("${projectName}/${service}")
                
                def fabricFolder = Jenkins.instance.getItemByFullName("${projectName}/${service}")
                def fabricTaskFolder = Jenkins.instance.getItemByFullName("${projectName}/${service}/${buildsFolderName}/${fabricTaskFolderName}")
                if(fabricFolder != null && fabricTaskFolder == null)
                    isOlderFabricJobsExists = true
                    
                /* Create sub-folders */
                createFabricProjectStructure(service)
            break
        case 'Quality':
                /* Create service folder */
                folder("${projectName}/${service}")
                
                /* Create sub-folders */
                createQualityChecksStructure(service)
            break
        case 'Database':
                cronScheduleVizParam = parseJobParameters('triggersSpecific', [cronScheduleParamDesc: cronScheduleParamDesc, isRequired: 'false'])
                /* Create service folder */
                folder("${projectName}/${service}")

                /* Create sub-folders */
                createDatabaseServiceStructure(service)
            break
        case 'Microservices':
                cronScheduleVizParam = parseJobParameters('triggersSpecific', [cronScheduleParamDesc: cronScheduleParamDesc, isRequired: 'false'])
                /* Create service folder */
                folder("${projectName}/${service}")

                /* Create sub-folders */
                createMicroservicesJobStructure(service)
            break
        default:
            throw new AppfactoryAbortException("$service service not supported!")
            break
    }
}

/* Once everything been created, set build description */
setBuildDescription(currentBuild, "<div id=\"build-description\"><p>Project: $projectName</p></div>")

/* Load job parameters */
def parseJobParameters(jobParametersFileName, Map args = [:]) {
    def jobParametersFileExtension = '.groovy'
    def jobParametersFilePath = getPath([resourcesFolder, 'jobParameters', jobParametersFileName])

    return groovyShell.parse(
            readFileFromWorkspace(jobParametersFilePath + jobParametersFileExtension)
    ).getParameters(args)
}

/* Load job/parameter description */
def loadDescription(filePath, fileName) {
    def descriptionFileExtension = '.html'
    def descriptionFilePath = getPath([resourcesFolder, filePath, fileName])

    return readFileFromWorkspace(descriptionFilePath + descriptionFileExtension)
}

/* Set build description */
def setBuildDescription(currentBuild, buildDescription) {
    currentBuild.description = buildDescription
}

/* Check if project already exists */
def isProjectExists(projectName) {
    return (projectName) ? Jenkins.instance.getItemByFullName(projectName, Folder) : null
}

/* Check if the provided service already exists */
def isServiceExists(serviceName) {
    return (serviceName) ? Jenkins.instance.getItemByFullName(serviceName, Folder) : null
}

/* Create Foundry project structure */
def createFabricProjectStructure(service) {

    def fabricBasePath = getPath([projectName, service])
    def customHookViewName = "CustomHooks"
    def customHookFolderPath= getPath([fabricBasePath, customHookViewName])
    def fabricBuildsFolderPath = getPath([fabricBasePath, buildsFolderName])
    def fabricTasksFolderPath = getPath([fabricBuildsFolderPath, fabricTaskFolderName])
    def fabricFacadeJobPath = getPath([fabricBuildsFolderPath, fabricFacadeJobName])
    
    /* All the script for creating fabric builds facade params */
    def fabricFacadeBuildParameters = disableCustomHookParameter <<
            fabricRunTestsParameter <<
            runFabricTestsFacadeSpecific <<
            fabricTestsSeparatorSpecific <<
            fabricServiceConfigPathParameter <<
            fabricBuildImportAndPublishSpecificParameters <<
            fabricCredentialsParameters <<
            fabricBuildImportFlagSpecificParameter <<
            fabricAppConfigParameter <<
            fabricParamsSeparatorDesc <<
            fabricMavenBuildSpecificParameters

    /* Create builds folder */
    folder("$fabricBuildsFolderPath")
    
    /* Note: If user rebuilding the same project, CustomHook folder might have already exist, lets keep the same folder so
     * that we will get previous hooks that are configured specific to the project.
     */
     if (!(Jenkins.instance.getItemByFullName(customHookFolderPath, Folder))) {
         createCustomHooksFolderStructure(service, customHookFolderPath, customHookViewName)
     }
     
    /* Create facade job */
    pipelineJob("$fabricFacadeJobPath") {
        description("<br>Pipeline Job for building a Foundry app with Java assets and optionally imports and publishes the app to Foundry environment")
        logRotator(setJobRetentionPolicyConfig())
        environmentVariables(setJobEnvVars())
        parameters(fabricFacadeBuildParameters << fabricCommonScmBuildParameters)
        definition {
            cps {
                script('import com.kony.appfactory.fabric.Facade\n\nnew Facade(this).createPipeline()')
                sandbox()
            }
        }
    }
    
    /* Create structure for fabric tasks folder and their jobs */
    generateFabricProjectSubFolders(fabricTasksFolderPath)
    
    /* Create structure for fabric triggers jobs */
    createFabricTriggersFolderStructure(fabricBasePath)

    /* Create structure for fabric tests and trigger jobs */
    createFabricTestsFolderStructure(fabricBasePath)
}

/* Create Foundry project structure */
def createQualityChecksStructure(service) {
    def basePath = getPath([projectName, service])
    def jobList = ['SonarQube']

    /* Create jobs */
    jobList.each { job ->
        def buildParameters
        switch(job) {
            case 'SonarQube':
                buildParameters = scanCommonScmBuildParameters
                pipelineScript = 'import com.kony.appfactory.qualitychecks.CodeScanners\n\nnew CodeScanners(this).runSonarScan()'
                break
            default:
                throw new AppfactoryAbortException("$job quality scan is not supported.")
                break
        }

        pipelineJob("${projectName}/${service}/${job}") {
            logRotator(setJobRetentionPolicyConfig())
            environmentVariables(setJobEnvVars())
            parameters(buildParameters)
            definition {
                cps {
                    script(pipelineScript)
                    sandbox()
                }
            }
        }
    }
    
}

def createMicroservicesJobStructure(service){
    def basePath = getPath([projectName, service])
    def jobList = ['ConfigureMS']
    def jobParameters

    /* Create jobs */
    jobList.each { job ->
        switch(job) {
            case 'ConfigureMS':
                jobParameters = configureMSJobSpecificParameters
                pipelineScript = 'import com.kony.appfactory.microservices.ConfigureMS \n\nnew ConfigureMS(this).runConfigureMSPipeline()'
                break
            default:
                throw new AppfactoryAbortException("$job Configuration Microservice is not supported.")
                break
        }

        createPipelineJob(service, job, jobParameters, pipelineScript)
    }
    createMicroservicesTriggersFolderStructures(basePath)

}

/* Create Database service structure */
def createDatabaseServiceStructure(service) {
    def basePath = getPath([projectName, service])
    def jobList = ['Flyway']
    def jobParameters

    /* Create jobs */
    jobList.each { job ->
        switch(job) {
            case 'Flyway':
                jobParameters = notificationsSpecificParameters << flywayJobSpecificParameters << flywayScmSpecificParameters
                pipelineScript = 'import com.kony.appfactory.database.Flyway\n\nnew Flyway(this).runFlywayPipeline()'
                break
            default:
                throw new AppfactoryAbortException("$job database service is not supported.")
                break
        }

        createPipelineJob(service, job, jobParameters, pipelineScript)
    }
    createDatabaseTriggersFolderStructures(basePath)

}

/* Creates pipeline job */
private void createPipelineJob(service, job, jobParameters, pipelineScript) {
    pipelineJob("${projectName}/${service}/${job}") {
        logRotator(setJobRetentionPolicyConfig())
        environmentVariables(setJobEnvVars())
        parameters(jobParameters)
        definition {
            cps {
                script(pipelineScript)
                sandbox()
            }
        }
    }
}

def getJobConfiguration(job){
    def jobConfiguration = [:]
    switch (job) {
        case 'createFlywayTrigger':
            jobConfiguration.jobParameters = notificationsSpecificParameters << flywayJobSpecificParameters << flywayScmSpecificParameters
            jobConfiguration.pipelineScript = 'import com.kony.appfactory.database.Flyway\n\nnew Flyway(this).runFlywayPipeline()'
            jobConfiguration.description = '<br/>Creates trigger job for Flyway.'
            break;
        case 'createConfigureMSTrigger':
            jobConfiguration.jobParameters = configureMSJobSpecificParameters
            jobConfiguration.pipelineScript = 'import com.kony.appfactory.microservices \n\nnew ConfigureMS(this).runConfigureMSPipeline()'
            jobConfiguration.description = '<br/>Creates trigger job for Configuration Microservice.'
            break;
    }
    jobConfiguration
}

/* Creates Microservice Triggers folder structures */
def createMicroservicesTriggersFolderStructures(basePath){
    def microServicesTriggersFolderPath = getPath([basePath, 'Triggers'])
    def microServicesTriggersSeedJobName = 'createConfigureMSTrigger'
    def microServicesSchedulerJobsFolderName = 'Schedulers'
    def microServicesTriggersWatcherJobsFolderName = 'Watchers'

    folder("${microServicesTriggersFolderPath}")
    folder("${microServicesTriggersFolderPath}/${microServicesSchedulerJobsFolderName}")
    folder("${microServicesTriggersFolderPath}/${microServicesTriggersWatcherJobsFolderName}")
    def microServicesJobBuildConfiguration = getJobConfiguration(microServicesTriggersSeedJobName)
    createTriggerSeedJob(microServicesTriggersFolderPath, microServicesTriggersSeedJobName, microServicesJobBuildConfiguration)
}


/* Creates Database Triggers folder structures */
def createDatabaseTriggersFolderStructures(basePath){
    def databaseTriggersFolderPath = getPath([basePath, 'Triggers'])
    def databaseTriggersSeedJobName = 'createFlywayTrigger'
    def databaseTriggersSchedulerJobsFolderName = 'Schedulers'
    def databaseTriggersWatcherJobsFolderName = 'Watchers'

    folder("${databaseTriggersFolderPath}")
    folder("${databaseTriggersFolderPath}/${databaseTriggersSchedulerJobsFolderName}")
    folder("${databaseTriggersFolderPath}/${databaseTriggersWatcherJobsFolderName}")
    def flywayJobBuildConfiguration = getJobConfiguration(databaseTriggersSeedJobName)
    createTriggerSeedJob(databaseTriggersFolderPath, databaseTriggersSeedJobName, flywayJobBuildConfiguration)
}

/* Create Triggers Seed job for Database or Microservice Jobs */
def createTriggerSeedJob(baseFolder, seedJobName, seedjobConfiguration){
    job("${baseFolder}/${seedJobName}") {
        description(seedjobConfiguration.description)
        configure cronScheduleVizParam
        configure { project ->
            project / scm(class: 'hudson.plugins.filesystem_scm.FSSCM') {
              path '/var/lib/jenkins/libraries/kony-jenkins-job-DSL'
              clearWorkspace 'true'
              copyHidden 'true'
              filterEnabled 'false'
              includeFilter 'false'
              filters ''
            }
        }
        logRotator(setJobRetentionPolicyConfig())
        wrappers {
            colorizeOutput()
        }
        label(restrictSeedJobsToRunOn)
        environmentVariables(setJobEnvVars())

        parameters(seedjobConfiguration.jobParameters)

        steps {
            dsl {
                external(triggersScriptPath)
            }
        }

        publishers {
            wsCleanup {
                includePattern('kony-jenkins-job-DSL*/**')
                deleteDirectories(true)
            }
        }
    }
}

/* Create Auto-Triggered Jobs structure */
def createTriggerSeed(jobPath, seedJobName, seedJobBuildParameters) {
    /* Create seed for Auto-Triggered Jobs */
    job("${jobPath}/${seedJobName}") {
	  configure cronScheduleVizParam
      configure { project ->
        project / scm(class: 'hudson.plugins.filesystem_scm.FSSCM') {
          path '/var/lib/jenkins/libraries/kony-jenkins-job-DSL'
          clearWorkspace 'true'
          copyHidden 'true'
          filterEnabled 'false'
          includeFilter 'false'
          filters ''
        }
      }
        logRotator(setJobRetentionPolicyConfig())
        wrappers {
            colorizeOutput()
        }
        label(restrictSeedJobsToRunOn)
        switch(seedJobName) {
            case 'createTest':
                parameters(seedJobBuildParameters <<
                        runNativeTestsFacadeParameters <<
                        jasmineTestSpecificParameter <<
                        testFrameworkTypeSpecificParameter <<
                        testFrameworkSeparatorSpecificParameter <<
                        scmSpecificParameters)
                parameters(nativeTestingParameters <<
                        nativeTestDataParameters <<
                        testEnvironmentParameters)
                configure availablePoolsParameter
                parameters(disableCustomHookParameter <<
                        desktopWebTestsCommonParameters <<
                        webTestDataParameters <<
                        desktopWebTestsSpecificParameters <<
                        disableDesktopWebTestsParameter)
                break
            case 'createTrigger':
                parameters(nativeTestingSeparatorSpecificParameters <<
                    jasmineTestSpecificParameter <<
                    testFrameworkTypeSpecificParameter <<
                    testFrameworkSeparatorSpecificParameter <<
                    seedJobBuildParameters <<
                    scmSpecificParameters)
                parameters(nativeTestingParameters << nativeTestDataParameters << testEnvironmentParameters << runNativeTestsFacadeParameters)
                configure availablePoolsParameter
                parameters(desktopWebTestsCommonParameters <<
                    webTestDataParameters <<
                    disableDesktopWebTestsParameter)
                break
        }
        steps {
            dsl {
                external(triggersScriptPath)
            }
        }

        publishers {
            wsCleanup {
                includePattern('kony-jenkins-job-DSL*/**')
                deleteDirectories(true)
            }
        }
    }
}

def createBuildTriggersFolderStructure(basePath, facadeJobBuildParameters) {
    def baseFolder = getPath([basePath, 'Triggers'])
    def seedJobName = 'createTrigger'
    def scheduledJobsFolderName = 'Schedulers'
    def scmTriggeredJobsFolderName = 'Watchers'

    folder("${baseFolder}")
    folder("${baseFolder}/${scheduledJobsFolderName}")
    folder("${baseFolder}/${scmTriggeredJobsFolderName}")

    /* Create seed for build job triggers */
    createTriggerSeed(baseFolder, seedJobName, facadeJobBuildParameters)
}

/* Create Pool Manager Job */

def createPoolManager(jobPath) {
    def jobName = 'managePool'
    def removePoolSeparator = {
        parameterSeparatorDefinition {
        name('REMOVE_POOL_HEADER')
        separatorStyle('')
        sectionHeader(removePoolParamsSeparatorDesc)
        sectionHeaderStyle('')
    }}
    def createEditPoolSeperator = {
        parameterSeparatorDefinition {
            name('CREATE_EDIT_POOL_HEADER')
            separatorStyle('')
            sectionHeader(createEditPoolParamsSeperatorDesc)
            sectionHeaderStyle('')
        }
    }
    job("${jobPath}/${jobName}") {
        logRotator(setJobRetentionPolicyConfig())
        /* Ansi colorized output on console logs */
        wrappers {
            colorizeOutput()
        }
        label(restrictSeedJobsToRunOn)
        properties {
            rebuild {
                rebuildDisabled(true)
            }
        }
        parameters(createEditPoolSeperator)
        configure deviceFilterParameter
        configure { project ->
            project / scm(class: 'hudson.plugins.filesystem_scm.FSSCM') {
              path '/var/lib/jenkins/libraries/kony-jenkins-job-DSL'
              clearWorkspace 'true'
              copyHidden 'true'
              filterEnabled 'false'
              includeFilter 'false'
              filters ''
            }
        }
        parameters(removePoolSeparator)
        configure poolsToRemoveParameter
        steps {
            dsl {
                external(managePoolsScriptPath)
            }
        }

        publishers {
            wsCleanup {
                includePattern('kony-jenkins-job-DSL*/**')
                deleteDirectories(true)
            }
        }

    }
}

/* Create Auto-Triggered Jobs structure */
def createTestTriggersFolderStructure(basePath) {
    def baseFolder = getPath([basePath, 'Tests'])
    def seedJobName = 'createTest'
    def testAutomationJobName = 'runTests'
    def scheduledJobsFolderName = 'Schedulers'
    def scmTriggeredJobsFolderName = 'Watchers'
    def channels = ['runWebTests', 'runNativeTests']
    def testsFolderPath = getPath([basePath, 'Tests'])
    def testsChannelFolderPath = getPath([testsFolderPath, testAutomationChannels])

    folder("${baseFolder}")
    /* Create a folder 'Channels' under 'Tests' */
    folder("${baseFolder}/${testAutomationChannels}")
    folder("${baseFolder}/${scheduledJobsFolderName}")
    folder("${baseFolder}/${scmTriggeredJobsFolderName}")

    /* Create test automation job */
    pipelineJob("${baseFolder}/${testAutomationJobName}") {
        logRotator(setJobRetentionPolicyConfig())
        environmentVariables(setJobEnvVars())
        parameters(testAutomationBuildParameters <<
                runNativeTestsFacadeParameters <<
                jasmineTestSpecificParameter <<
                testFrameworkTypeSpecificParameter <<
                testFrameworkSeparatorSpecificParameter <<
                scmSpecificParameters)
        parameters(nativeTestingParameters << nativeTestDataParameters << testEnvironmentParameters)
        configure availablePoolsParameterForRunTests
        parameters(disableCustomHookParameter <<
                desktopWebTestsCommonParameters <<
                webTestDataParameters <<
                desktopWebTestsSpecificParameters <<
                disableDesktopWebTestsParameter)
        properties {
            copyArtifactPermissionProperty {
                projectNames('/*')
            }
        }
        definition {
            cps {
                script('import com.kony.appfactory.visualizer.TestAutomation\n' +
                        '\nnew TestAutomation(this).createPipeline()')
                sandbox()
            }
        }
    }

    /* Create seed for test automation job triggers */
    createTriggerSeed(baseFolder, seedJobName, testAutomationBuildParameters)

    /* Create pool manager job */
    createPoolManager(baseFolder)

    /* Create pipeline job to build child jobs(runNativeTests and runWebTests) under 'Channels' folder of Tests */
    channels.each { channel ->
        def jobPath = getPath([testsChannelFolderPath, channel])
        if (!jobPath) {
            throw new AppfactoryAbortException('Job path can\'t be null!')
        }
        def jobConfiguration = getChannelJobConfiguration(channel)
        if (!jobConfiguration) {
            throw new AppfactoryAbortException('Channel type not found!')
        }

        /* Getting previous job path for a given job name, if exists (runWebTests -> runDesktopWebTests) */
        def previousJobPath =  getPreviousJobNamePath(testsChannelFolderPath, channel)

        /* Create pipeline job to build specific channel */
        pipelineJob(jobPath) {
            /* Renames jobs matching the regular expression to the name of this job before the configuration is updated.
               The regular expression needs to match the full name of the job, i.e. with folders included.
               This can be useful to keep the build history. */
            previousNames(previousJobPath)
            logRotator(setJobRetentionPolicyConfig())
            environmentVariables(setJobEnvVars())
            parameters(jobConfiguration.buildParameters)
            /* This is because we want the AVAILABLE_TEST_POOLS parameter in the Native section and NOT at the end of all parameters*/
            if(channel == "runNativeTests") {
                configure availablePoolsParameter
                parameters(nativeTestingParameters << testEnvironmentParameters)
                parameters(jobConfiguration.hooksAndNotifications)
            }
            properties {
                copyArtifactPermissionProperty {
                    projectNames('/*')
                }
            }
            definition {
                cps {
                    script(jobConfiguration.pipelineScript)
                    sandbox()
                }
            }
        }
    }
}

/* Create the Custom Hooks folder structure */
def createCustomHooksFolderStructure(service, customHookFolderPath, customHookViewName) {

    /* Create customHooks folder and set default view for the folder to CustomHookView defined in Volt MX customview plugin.
    * Setting the customHookView as primary view by updating config.xml of CustomHooks folder.
    */
    folder(customHookFolderPath) {
        primaryView(customHookViewName)
        configure { folder ->
            folder.remove(folder / views)
            folder / 'properties' / 'org.jenkinsci.plugins.configfiles.folder.FolderConfigFileProperty' {
                'configs'(class: 'sorted-set') {
                    'comparator'(class: 'org.jenkinsci.plugins.configfiles.folder.FolderConfigFileProperty$1')
                }
            }
            folder / 'folderViews'(class: 'com.cloudbees.hudson.plugins.folder.views.DefaultFolderViewHolder') {
                'views' {
                    'com.kony.appfactory.customview.views.SectionedView' {
                        owner(class: 'com.cloudbees.hudson.plugins.folder.Folder', reference: '../../../..')
                        name(customHookViewName)
                        filterExecutors("false")
                        filterQueue("false")
                        properties(class: 'hudson.model.View$PropertyList')
                        'sections' {
                            'com.kony.appfactory.customview.CustomHookView' {
                                jobNames {
                                    comparator(class: "hudson.util.CaseInsensitiveComparator")
                                }
                                name(projectName)
                                buildtype(service)
                                width("FULL")
                                alignment("CENTER")
                                hookList("")
                                text("")
                                style("NONE")
                            }
                        }
                    }
                }

                primaryView(customHookViewName)
                tabBar(class: "hudson.views.DefaultViewsTabBar")
            }
        }
    }

    /* Create FreeStyle Job _createCustomnHook under CustomHooks folder */
    job(customHookFolderPath + "/" + "_createCustomHook"){
        logRotator(setJobRetentionPolicyConfig())
        description("This job is used for uploading CustomHook script at required build stage with default params")
        environmentVariables(setJobEnvVarsForCreateCustomHook())
        parameters(customHookParam)
        properties {
            rebuild {
                rebuildDisabled(true)
            }
        }
        label("master")
        wrappers {
            colorizeOutput()
            preBuildCleanup()
        }
        configure { project ->
            project / scm(class: 'hudson.plugins.filesystem_scm.FSSCM') {
              path '/var/lib/jenkins/libraries/kony-jenkins-job-DSL'
              clearWorkspace 'true'
              copyHidden 'true'
              filterEnabled 'false'
              includeFilter 'false'
              filters ''
            }
        }
        steps {
            dsl {
                external('./${DSL_SCRIPT_NAME}')
            }
        }
        publishers {
            wsCleanup {
                includePattern('kony-jenkins-job-DSL*/**')
                deleteDirectories(true)
            }
        }

    }

    /* Create FreeStyle Job _updateCustomnHook under CustomHooks folder */
    job(customHookFolderPath + "/" + "_updateCustomHook"){
        logRotator(setJobRetentionPolicyConfig())
        description("This job is used for updating existing CustomHook job params and CustomHook script")
        label("master")
        wrappers {
            colorizeOutput()
            preBuildCleanup()
        }
        environmentVariables(setJobEnvVarsForUpdateCustomHook())
        parameters(updateCustomHookParam)
        properties {
            rebuild {
                rebuildDisabled(true)
            }
        }
        configure { project ->
            project / scm(class: 'hudson.plugins.filesystem_scm.FSSCM') {
              path '/var/lib/jenkins/libraries/kony-jenkins-job-DSL'
              clearWorkspace 'true'
              copyHidden 'true'
              filterEnabled 'false'
              includeFilter 'false'
              filters ''
            }
        }
        steps {
            dsl {
                external('./${DSL_SCRIPT_NAME}')
            }
        }
        publishers {
            wsCleanup {
                includePattern('kony-jenkins-job-DSL*/**')
                deleteDirectories(true)
            }
        }
    }

    /* Create CustomHooks Build Stage folder */
    folder(customHookFolderPath+"/"+"PRE_BUILD")
    folder(customHookFolderPath+"/"+"POST_BUILD")
    if(service.equalsIgnoreCase('Foundry')) {
        folder(customHookFolderPath+"/"+"POST_DEPLOY")
    } else {
        folder(customHookFolderPath+"/"+"POST_TEST")
    }

}

/* Create Iris project structure */
def createVisualizerProjectStructure(service) {
    def customHookViewName = "CustomHooks"
    def basePath = getPath([projectName, service])
    def buildsFolderPath = getPath([basePath, buildsFolderName])
    def channelsFolderPath = getPath([buildsFolderPath, channelsFolderName])
    def facadeJobPath = getPath([buildsFolderPath, facadeJobName])
    def customHookFolderPath= getPath([basePath, customHookViewName])

    def facadeBuildParameters = disableCustomHookParameter <<
            facadeDesktopWebSpecificBuildParameters <<
            facadeIosSpecificBuildParameters <<
            facadeAndroidSpecificBuildParameters <<
            channelsHeaderSpecific <<
            protectedModeSpecificBuildParameters <<
            visualizerSpecificBuildParameters

    /* Create builds folder */
    folder(buildsFolderPath)

    /* Note: If user rebuilding the same project, CustomHook folder might have already exist, lets keep the same folder so
    * that we will get previous hooks that are configured specific to the project.
    */
    if (!(Jenkins.instance.getItemByFullName(customHookFolderPath, Folder))) {
        createCustomHooksFolderStructure(service, customHookFolderPath, customHookViewName)
    }
    
    /* Create facade job */
    pipelineJob(facadeJobPath) {
        logRotator(setJobRetentionPolicyConfig())
        environmentVariables(setJobEnvVars())
        parameters(facadeBuildParameters << scmSpecificParameters)
        parameters(jasmineTestSpecificParameter << testFrameworkTypeSpecificParameter << testFrameworkSeparatorSpecificParameter)
        parameters(testCodeCoverageSpecificParameter)
        parameters(nativeTestingSeparatorSpecificParameters)
        parameters(nativeTestingParameters << nativeTestDataParameters << testEnvironmentParameters << runNativeTestsFacadeParameters)
        properties {
            copyArtifactPermissionProperty {
                projectNames('/*')
            }
        }
        configure availablePoolsParameter
        definition {
            cps {
                script('import com.kony.appfactory.visualizer.Facade\n\nnew Facade(this).createPipeline()')
                sandbox()
            }
        }
        parameters(desktopWebTestsCommonParameters <<
            webTestDataParameters <<
            disableDesktopWebTestsParameter)
    }
    
    /* Create structure for channel build jobs */
    generateVisualizerProjectSubFolders(channelsFolderPath)
    /* Create structure for auto-triggered jobs */
    createBuildTriggersFolderStructure(basePath, facadeBuildParameters)
    /* Create structure for test jobs */
    createTestTriggersFolderStructure(basePath)
}

/* Create project folders and build jobs
* @param rootFolderName Folder name in which we want to create subfolders
* @param isBuildsFoldersGeneration indicates whether this sub-folder generation is for Build folder or for Tests folder
* */

/* Create project folders and build jobs */
def generateVisualizerProjectSubFolders(rootFolderName) {
    /* APPFACT-312 disable Windows channels
    def channels = ['buildAndroid', 'buildIos', 'buildWindows', 'buildSpa']
     */
    def channels = ['buildAndroid', 'buildIos', 'buildResponsiveWeb']


    /* Create channels folder */
    folder(rootFolderName)

    channels.each { channel ->
        def jobPath = getPath([rootFolderName, channel])
        if (!jobPath) {
            throw new AppfactoryAbortException('Job path can\'t be null!')
        }
        def jobConfiguration = getChannelJobConfiguration(channel)
        if (!jobConfiguration) {
            throw new AppfactoryAbortException('Channel type not found!')
        }

        /* Getting previous job path for a given job name, if exists (buildResponsiveWeb -> buildDesktopWeb) */
        def previousJobPath =  getPreviousJobNamePath(rootFolderName, channel)

        /* Create pipeline job to build specific channel */
        pipelineJob(jobPath) {
            /* Renames jobs matching the regular expression to the name of this job before the configuration is updated.
               The regular expression needs to match the full name of the job, i.e. with folders included.
               This can be useful to keep the build history. */
            previousNames(previousJobPath)
            logRotator(setJobRetentionPolicyConfig())
            environmentVariables(setJobEnvVars())
            parameters(jobConfiguration.buildParameters)
            properties {
                copyArtifactPermissionProperty {
                    projectNames('/*')
                }
            }
            definition {
                cps {
                    script(jobConfiguration.pipelineScript)
                    sandbox()
                }
            }
        }
    }
}

/* returns the previous job path for a given job name */
def getPreviousJobNamePath(rootFolderName, jobName) {
    def previousJobName = null
    def previousJobPath = null

    switch (jobName) {
        case 'buildResponsiveWeb':
            previousJobName = "buildDesktopWeb"
            break
        case 'runWebTests':
            previousJobName = "runDesktopWebTests"
            break
        default:
            break
    }

    if (previousJobName) {
        previousJobPath = getPath([rootFolderName, previousJobName])
    }

    previousJobPath
}

/* Get channel job configuration depending on channel name */
def getChannelJobConfiguration(channelName) {
    def jobConfiguration = [:]

    switch (channelName) {
        case 'buildAndroid':
            jobConfiguration.buildParameters = disableCustomHookParameter <<
                    channelSpecificBuildParameters <<
                    androidSpecificBuildParameters <<
                    protectedModeSpecificBuildParameters <<
                    visualizerSpecificBuildParameters <<
                    scmSpecificParameters

            jobConfiguration.pipelineScript = 'import com.kony.appfactory.visualizer.channels.AndroidChannel\n' +
                    '\nnew AndroidChannel(this).createPipeline()'
            break
        case 'buildIos':
            jobConfiguration.buildParameters = disableCustomHookParameter <<
                    channelSpecificBuildParameters <<
                    iosSpecificBuildParameters <<
                    protectedModeSpecificBuildParameters <<
                    visualizerSpecificBuildParameters <<
                    scmSpecificParameters

            jobConfiguration.pipelineScript = 'import com.kony.appfactory.visualizer.channels.IosChannel\n' +
                    '\nnew IosChannel(this).createPipeline()'
            break
        case 'buildWindows':
            def windowsSpecificBuildParameters = parseJobParameters(
                    'windowsSpecific', [channelOs: ['Windows8', 'Windows10']]
            )
            jobConfiguration.buildParameters = disableCustomHookParameter <<
                    channelSpecificBuildParameters <<
                    windowsSpecificBuildParameters <<
                    visualizerSpecificBuildParameters <<
                    scmSpecificParameters

            jobConfiguration.pipelineScript = 'import com.kony.appfactory.visualizer.channels.WindowsChannel\n' +
                    '\nnew WindowsChannel(this).createPipeline()'
            break
        case 'buildResponsiveWeb':
                    jobConfiguration.buildParameters = disableCustomHookParameter<<
                    webSeparatorSpecificBuildParameters <<
                    protectedModeSpecificBuildParameters <<
                    visualizerSpecificBuildParameters <<
                    scmSpecificParameters

            jobConfiguration.pipelineScript = 'import com.kony.appfactory.visualizer.channels.DesktopWebChannel\n' +
                    '\nnew DesktopWebChannel(this).createPipeline()'
            break
        case 'runWebTests':
            jobConfiguration.buildParameters = disableCustomHookParameter <<
                    desktopWebTestsCommonParameters <<
                    desktopWebTestsSpecificParameters <<
                    testFrameworkTypeSpecificParameter <<
                    testFrameworkSeparatorSpecificParameter <<
                    scmSpecificParameters

            jobConfiguration.pipelineScript = 'import com.kony.appfactory.tests.channels.DesktopWebTests\n' +
                    '\nnew DesktopWebTests(this).createPipeline()'
            break
        case 'runNativeTests':
            jobConfiguration.buildParameters = testAutomationBuildParameters <<
                    testFrameworkTypeSpecificParameter <<
                    testFrameworkSeparatorSpecificParameter <<
                    scmSpecificParameters
            jobConfiguration.hooksAndNotifications = disableCustomHookParameter

            jobConfiguration.pipelineScript = 'import com.kony.appfactory.tests.channels.NativeAWSDeviceFarmTests\n' +
                    '\nnew NativeAWSDeviceFarmTests(this).createPipeline()'
            break
        default:
            break
    }

    jobConfiguration
}

/* Collect environment variables for Iris, Foundry and Flyway jobs */
def setJobEnvVars() {
    def result = [:]

    result.put('PROJECT_NAME', projectName)
    result
}

/* Collect environment variables for CustomHooks under a project */
def getCommonJobEnvForCustomHook() {
    def result = [:]
    
    result.put('PROJECT_NAME', projectName)
    result.put('JOB_DSL_SCRIPTS_BRANCH','${JOB_DSL_SCRIPTS_BRANCH}')
    result
}

/* Collect environment variables for _createCustomHook under a project */
def setJobEnvVarsForCreateCustomHook() {
    def result = getCommonJobEnvForCustomHook()
    result.put('DSL_SCRIPT_NAME','custom_hook.groovy')
    result
}

/* Collect environment variables for _updateCustomHook under a project */
def setJobEnvVarsForUpdateCustomHook() {
    def result = getCommonJobEnvForCustomHook()
    result.put('DSL_SCRIPT_NAME','update_custom_hooks.groovy')
    result
}

/* Closure for constructing path */
def getPath(pathList) {
    pathList.join('/')
}

/* Manages jobs retention policies. */
def setJobRetentionPolicyConfig() {
    return {
        /* If specified, only up to this number of build records are kept. */
        numToKeep((SEED_JOB.logRotator?.numToKeep) ?: 10)
        /* If specified, build records are only kept up to this number of days. */
        daysToKeep((SEED_JOB.logRotator?.daysToKeep) ?: 90)
        /* If specified, only up to this number of builds have their artifacts retained. */
        artifactNumToKeep((SEED_JOB.logRotator?.artifactNumToKeep) ?: 8)
        /*
            If specified, artifacts from builds older than this number of days will be deleted, but the logs, history,
            reports, etc for the build will be kept.
        */
        artifactDaysToKeep((SEED_JOB.logRotator?.artifactDaysToKeep) ?: 30)
    }
}

/* Creates Foundry triggers folder structure */
def createFabricTriggersFolderStructure(basePath) {
    def fabricBaseFolder = getPath([basePath, 'Triggers'])
    def fabricSeedJobNames = ['createFabricAppTrigger', 'createImportTrigger', 'createExportTrigger', 'createMigrationTrigger']
    def fabricScheduledJobsFolderName = 'Schedulers'
    def scmTriggeredJobsFolderName = 'Watchers'

    folder("${fabricBaseFolder}")
    folder("${fabricBaseFolder}/${fabricScheduledJobsFolderName}")
    folder("${fabricBaseFolder}/${scmTriggeredJobsFolderName}")

    /* Create seed job for fabric build triggers */
    fabricSeedJobNames.each { fabricSeedJobName ->
        def fabricJobBuildConfiguration = getFabricJobsConfiguration(fabricSeedJobName)
        createFabricTriggerSeedJob(fabricBaseFolder, fabricSeedJobName, fabricJobBuildConfiguration)
    }
}

/* Get the fabric build configuration based on fabric job */
def getFabricJobsConfiguration(fabricSeedJobName) {
    def fabricJobConfiguration = [:]
    switch(fabricSeedJobName) {
        case 'createFabricAppTrigger':
        
            fabricJobConfiguration.buildParameters = disableCustomHookParameter <<
                fabricRunTestsParameter <<
                runFabricTestsFacadeSpecific <<
                fabricTestsSeparatorSpecific <<
                fabricServiceConfigPathParameter <<
                fabricBuildImportAndPublishSpecificParameters <<
                fabricCredentialsParameters <<
                fabricBuildImportFlagSpecificParameter <<
                fabricAppConfigParameter <<
                fabricParamsSeparatorDesc <<
                fabricMavenBuildSpecificParameters <<
                fabricCommonScmBuildParameters

            fabricJobConfiguration.pipelineScript = 'import com.kony.appfactory.fabric.Facade\n\nnew Facade(this).createPipeline()'
            fabricJobConfiguration.description = '<br>Creates a scheduler job for building a Foundry app with Java assets and optionally imports and publishes the app to Foundry environment'
            break
            
        case 'createImportTrigger':
            fabricJobConfiguration.buildParameters = fabricPublishBuildParameters <<
                fabricPublishFlagParameters <<
                fabricAppOverWriteExistingFlagParameters <<
                fabricAppConfigParameter <<
                fabricCredentialsParameters <<
                fabricParamsSeparatorDesc <<
                fabricCommonScmBuildParameters
                
            fabricJobConfiguration.pipelineScript = 'import com.kony.appfactory.fabric.Fabric\n\nnew Fabric(this).importApp()'
            fabricJobConfiguration.description = '<br><p style="color:red"><b>*** The createImportTrigger job is deprecated.' +
                    ' *** <br><br>We recommend to use the new createFabricAppTrigger Job available at Foundry/Triggers folder' +
                    ' to get similar functionality.<br><br> Note:<br> &nbsp;&nbsp; You can opt out CLEAN_JAVA_ASSETS and' +
                    ' BUILD_JAVA_ASSETS options while configuring the new trigger job to achieve the same functionality' +
                    ' that this trigger job is doing.</b></p>'
            break
            
        case 'createExportTrigger':
            fabricJobConfiguration.buildParameters = commitSpecificParameters <<
                ignoreJarAssetsFlagParameter <<
                fabricServiceConfigPathExportParameter <<
                exportValidateVersionParameters <<
                fabricAppConfigParameter <<
                fabricCredentialsParameters <<
                fabricParamsSeparatorDesc <<
                fabricCommonScmBuildParameters
                
            fabricJobConfiguration.pipelineScript = 'import com.kony.appfactory.fabric.Fabric\n\nnew Fabric(this).exportApp()'
            fabricJobConfiguration.description = '<br>Creates a scheduler job for exporting a Foundry App from source control system to Foundry environment'
            break
        
        case 'createMigrationTrigger':
            fabricJobConfiguration.buildParameters = fabricPublishBuildParameters <<
                fabricPublishFlagParameters <<
                fabricAppOverWriteExistingFlagParameters <<
                fabricAppConfigImportParameter <<
                importFabricCredentialsParameters <<
                commitSpecificParameters <<
                scmOverWriteExistingFlagParameters <<
                fabricAppConfigExportParameter <<
                exportFabricCredentialsParameters <<
                fabricCommonScmBuildParameters
                
            fabricJobConfiguration.pipelineScript = 'import com.kony.appfactory.fabric.Fabric\n\nnew Fabric(this).migrateApp()'
            fabricJobConfiguration.description = '<br><p style="color:red"><b>*** The createMigrationTrigger job is deprecated. *** <br><br>\n' +
                    '\n' +
                    'We recommend to use the following approach for achieving the similar functionality.<br><br>\n' +
                    '\n' +
                    '&nbsp;&nbsp; 1. Use the createExportTrigger job available at Foundry/Triggers for exporting an app' +
                    ' from source environment to SCM GIT repo. You can opt out IGNORE_JARS option to check-in jar assets' +
                    ' available in the Foundry app to SCM repository. Create separate scheduler for export activity.<br>\n' +
                    '&nbsp;&nbsp; 2. Use new createFabricAppTrigger Job available at Foundry/Builds folder to import the' +
                    ' app to destination environment from SCM GIT repo. You can opt out CLEAN_JAVA_ASSETS and' +
                    ' BUILD_JAVA_ASSETS options while configuring the trigger job to import the entire app with java' +
                    ' assets include to destination environment. Create separate scheduler for import activity ensuring' +
                    ' the createExportTrigger job is executed prior to this job schedule.<br>'
            break

        case 'Export':
            fabricJobConfiguration.buildParameters = commitSpecificParameters <<
                    fabricServiceConfigPathExportParameter <<
                    ignoreJarAssetsFlagParameter <<
                    exportValidateVersionParameters <<
                    fabricAppConfigParameter <<
                    fabricCredentialsParameters <<
                    fabricParamsSeparatorDesc <<
                    fabricCommonScmBuildParameters

            fabricJobConfiguration.pipelineScript = 'import com.kony.appfactory.fabric.Fabric\n\nnew Fabric(this).exportApp()'
            fabricJobConfiguration.description = "<br>Pipeline job for exporting a Foundry App from source control system to Foundry environment"
            break

        case 'Import':
            fabricJobConfiguration.buildParameters = fabricPublishBuildParameters <<
                    fabricPublishFlagParameters <<
                    fabricAppOverWriteExistingFlagParameters <<
                    fabricAppConfigParameter <<
                    fabricCredentialsParameters <<
                    fabricParamsSeparatorDesc <<
                    fabricCommonScmBuildParameters

            fabricJobConfiguration.pipelineScript = 'import com.kony.appfactory.fabric.Fabric\n\nnew Fabric(this).importApp()'
            fabricJobConfiguration.description = '<br><p style="color:red"><b>*** The Import job is deprecated. *** <br><br>' +
                    'We recommend to use the new buildFabricApp Job available at Foundry/Builds folder to get similar functionality.' +
                    '<br><br> Note:<br> &nbsp;&nbsp; You can opt out CLEAN_JAVA_ASSETS and BUILD_JAVA_ASSETS options while' +
                    ' triggering the build on new job for achieving the same functionality that this Import job is doing.' +
                    '</b></p>'
            break

        case 'Publish':
            fabricJobConfiguration.buildParameters = fabricPublishBuildParameters <<
                    fabricAppConfigParameter <<
                    fabricCredentialsParameters <<
                    fabricParamsSeparatorDesc

            fabricJobConfiguration.pipelineScript = 'import com.kony.appfactory.fabric.Fabric\n\nnew Fabric(this).publishApp()'
            fabricJobConfiguration.description = '<br><p style="color:red"><b>*** The Publish job is deprecated. *** <br><br>' +
                    'We recommend to use the new buildFabricApp Job available at Foundry/Builds folder to get similar functionality.' +
                    '<br><br>Note:<br> &nbsp;&nbsp; You can opt in IMPORT and PUBLISH options while triggering the build on' +
                    ' new job for achieving the same functionality that this Publish job is doing.</b></p>'
            break

        case 'Migrate':
            fabricJobConfiguration.buildParameters = fabricPublishBuildParameters <<
                    fabricPublishFlagParameters <<
                    fabricAppOverWriteExistingFlagParameters <<
                    fabricAppConfigImportParameter <<
                    importFabricCredentialsParameters <<
                    commitSpecificParameters <<
                    scmOverWriteExistingFlagParameters <<
                    fabricAppConfigExportParameter <<
                    exportFabricCredentialsParameters <<
                    fabricCommonScmBuildParameters

            fabricJobConfiguration.pipelineScript = 'import com.kony.appfactory.fabric.Fabric\n\nnew Fabric(this).migrateApp()'
            fabricJobConfiguration.description = '<br><p style="color:red"><b>*** The Migrate job is deprecated. *** <br><br>\n' +
                    '\n' +
                    'We recommend to use the following approach for achieving the similar functionality.<br><br>\n' +
                    '\n' +
                    '&nbsp;&nbsp; 1. Use the Export job available at Foundry/Builds/FabricTask for exporting an app' +
                    ' from source environment to SCM GIT repo. You can opt out IGNORE_JARS option to check-in jar' +
                    ' assets available in the Foundry app to SCM repository.<br>\n' +
                    '&nbsp;&nbsp; 2. Use new buildFabricApp Job available at Foundry/Builds folder to import the app' +
                    ' from SCM GIT repository to destination environment. You can opt out CLEAN_JAVA_ASSETS and' +
                    ' BUILD_JAVA_ASSETS options while triggering the build on new job to import the entire app with' +
                    ' java assets included.<br>'
            break

        default:
            throw new AppfactoryAbortException("$fabricSeedJobName job type not supported")
            break
    }
    
    fabricJobConfiguration
}

/* Create the fabric seed job based on trigger job */
def createFabricTriggerSeedJob(fabricBaseFolder, fabricSeedJobName, fabricJobBuildConfiguration) {
    job("${fabricBaseFolder}/${fabricSeedJobName}") {
        description(fabricJobBuildConfiguration.description)
        if(fabricSeedJobName == 'createFabricAppTrigger' || fabricSeedJobName == 'createFabricTest') {
            configure cronScheduleParamWithWatcherSupport
        } else {
            configure cronScheduleFabricParam
        }
        configure { project ->
            project / scm(class: 'hudson.plugins.filesystem_scm.FSSCM') {
              path '/var/lib/jenkins/libraries/kony-jenkins-job-DSL'
              clearWorkspace 'true'
              copyHidden 'true'
              filterEnabled 'false'
              includeFilter 'false'
              filters ''
            }
        }
        logRotator(setJobRetentionPolicyConfig())
        wrappers {
            colorizeOutput()
        }
        label(restrictSeedJobsToRunOn)
        
        parameters(fabricJobBuildConfiguration.buildParameters)
        
        steps {
            dsl {
                external(triggersScriptPath)
            }
        }

        publishers {
            wsCleanup {
                includePattern('kony-jenkins-job-DSL*/**')
                deleteDirectories(true)
            }
        }
    }
}

def generateFabricProjectSubFolders(fabricTasksFolderPath) {
    
    /* Create fabric tasks folder */
    folder("$fabricTasksFolderPath")
        
    /* Check old fabric jobs exist then move to new Foundry Path: <projectName>/Foundry/Builds/fabricTasks folder */
    if(!isOlderFabricJobsExists) {
        /* Skip creating Foundry jobs at "<projectName>/Foundry/Builds/FabricTasks" folder if already existing at "<projectName>/Foundry" path */
        def jobList = ['Export', 'Import', 'Publish', 'Migrate']
        jobList.each { job ->
            def fabricJobBuildConfiguration = getFabricJobsConfiguration(job)
            pipelineJob("${fabricTasksFolderPath}/${job}") {
                description(fabricJobBuildConfiguration.description)
                logRotator(setJobRetentionPolicyConfig())
                environmentVariables(setJobEnvVars())
                parameters(fabricJobBuildConfiguration.buildParameters)
                definition {
                    cps {
                        script(fabricJobBuildConfiguration.pipelineScript)
                        sandbox()
                    }
                }
            }
        }
    }
}
def createFabricTestsFolderStructure(fabricBasePath) {
    def testAutomationJobName = 'runFabricTests'
    def fabricTestsFolderPath = getPath([fabricBasePath, 'Tests'])
    /* Create fabric tests folder */
    folder(fabricTestsFolderPath)
    /* Create test automation job */
    pipelineJob("${fabricTestsFolderPath}/${testAutomationJobName}") {
        logRotator(setJobRetentionPolicyConfig())
        environmentVariables(setJobEnvVars())
        parameters(fabricRunTestsParameter <<
                fabricTestsSeparatorSpecific <<
                fabricCommonScmBuildParameters)
        properties {
            copyArtifactPermissionProperty {
                projectNames('/*')
            }
        }
        definition {
            cps {
                script('import com.kony.appfactory.fabricTests.FacadeTests\n' +
                        '\nnew FacadeTests(this).createPipeline()')
                sandbox()
            }
        }
    }
    createFabricTestsTriggersFolderStructure(fabricTestsFolderPath)
}

def createFabricTestsTriggersFolderStructure(fabricTestsFolderPath) {
    def seedJobName = 'createFabricTest'
    def scheduledJobsFolderName = 'Schedulers'
    def scmTriggeredJobsFolderName = 'Watchers'
    folder("${fabricTestsFolderPath}/${scheduledJobsFolderName}")
    folder("${fabricTestsFolderPath}/${scmTriggeredJobsFolderName}")
    def fabricJobConfiguration = [:]
    fabricJobConfiguration.buildParameters = fabricRunTestsParameter <<
            fabricTestsSeparatorSpecific <<
            fabricCommonScmBuildParameters

    fabricJobConfiguration.pipelineScript = 'import com.kony.appfactory.fabricTests.FacadeTests\n\nnew FacadeTests(this).createPipeline()'
    fabricJobConfiguration.description = '<br>Creates a scheduler job to run API tests for Foundry apps.'
    createFabricTriggerSeedJob(fabricTestsFolderPath, seedJobName, fabricJobConfiguration)

}

/* Collect service type selected at Create AppFactory project */
def setSeviceTypes() {
    def servicesList = []
    if(projectType == "Volt MX Project")
        servicesList.addAll(['Iris', 'Foundry', 'Quality', 'Database'])
    else
        servicesList.add('Microservices')

    servicesList
}

/* Check root folder path for Iris and Foundry projects are in valid format */
def checkRootFolderPathIsValidForProject(serviceRootFolder) {
    if (serviceRootFolder && serviceRootFolder.matches(/.*\s+.*/)) {
        throw new AppfactoryAbortException("$serviceRootFolder path must not contain spaces!")
    }
}
