import exceptions.AppfactoryAbortException
import hudson.model.*
import helper.validation_helper
import helper.projectSettings_helper
import helper.ansi_color_helper
import jenkins.model.Jenkins

/* Names of the folders for Schedulers and Watchers */
scheduledJobsFolderName = 'Schedulers'
scmTriggeredJobsFolderName = 'Watchers'

projectSettingsHelper = new projectSettings_helper()
validationHelper = new validation_helper()
/* Get timestamp to generate unique auto-triggered job name */
timestamp = new Date().format("yyyyMMdd_HH-mm-ss-SSS", TimeZone.getTimeZone('UTC'))
/* Get build object to query build parameters of the createTrigger job */
Build build = Executor.currentExecutor().currentExecutable
/* Trigger job parameters */
triggeredServiceName = getServiceForTriggeredSeedJob(JOB_BASE_NAME)
/* Current Appfactory project name */
projectName = JOB_NAME.substring(0, JOB_NAME.indexOf('/'))
isProjectSettingsExists = (projectSettingsHelper?.getProjectVersion(projectName)) ? true : false
isNewFabricJobStructure = false
if(triggeredServiceName == 'Foundry')
    isNewFabricJobStructure = (getBinding().getVariables()['PROJECT_SOURCE_CODE_URL_FOR_FABRIC'] || isProjectSettingsExists) ? true : false

jobType = (CRON_SCHEDULE) ? 'Scheduler' : 'Watcher'

/* We need these variables for Watcher service only.*/
if(jobType == 'Watcher') {
    if (triggeredServiceName == 'Database') {
        scmUrl = SCM_URL
        scmServerType = SCM_SERVER_TYPE
        scmBranch = SCM_BRANCH
        scmCredentiaslId = SCM_CREDENTIALS
    }
    else if (isProjectSettingsExists) {
        scmSettingsMap = projectSettingsHelper.getScmProjectSettings(projectName, triggeredServiceName)
        validationHelper.validateProjectSettingsScmParameters(scmSettingsMap)
        scmUrl = scmSettingsMap["PROJECT_SOURCE_CODE_URL"]
        scmServerType = scmSettingsMap["PROJECT_SOURCE_CODE_SERVER_TYPE"]
        scmCredentiaslId = scmSettingsMap["PROJECT_SOURCE_CODE_REPOSITORY_CREDENTIALS_ID"]
        scmBranch = (triggeredServiceName == 'Iris') ? PROJECT_SOURCE_CODE_BRANCH : SCM_BRANCH
    }
     else {
        scmUrl = (triggeredServiceName == 'Iris' || triggeredServiceName == 'Microservices') ? PROJECT_SOURCE_CODE_URL : PROJECT_SOURCE_CODE_URL_FOR_FABRIC
        scmServerType = (triggeredServiceName == 'Iris' || triggeredServiceName == 'Microservices') ? PROJECT_SOURCE_CODE_SERVER_TYPE : PROJECT_SOURCE_CODE_SERVER_TYPE_FOR_FABRIC
        scmBranch = (triggeredServiceName == 'Iris') ? PROJECT_SOURCE_CODE_BRANCH : SCM_BRANCH
        scmCredentiaslId = (triggeredServiceName == 'Iris' || triggeredServiceName == 'Microservices') ? PROJECT_SOURCE_CODE_REPOSITORY_CREDENTIALS_ID : SCM_CREDENTIALS
    }
    if(!(scmUrl && scmServerType && scmBranch && scmCredentiaslId)){
        throw new AppfactoryAbortException("One or more required Source Control related properties are having null values. Please check the project properties.")
    }
}
jobFolder = (CRON_SCHEDULE) ? scheduledJobsFolderName : scmTriggeredJobsFolderName
jobSufix = (CRON_SCHEDULE) ? CRON_SCHEDULE : scmBranch
jobBuildParameters = getBuildParameters(build)
jobDescription = generateJobDescription(jobBuildParameters)
jobPath = JOB_NAME - JOB_BASE_NAME + jobFolder + '/' + jobType + '_' + timestamp
/* Node to run on label */
nodeLabel = SEED_JOB.assignedLabelString ?: 'preparation'
/* Get build parameters for triggered(Facade) job */
triggeredJobName = getTriggeredJobName(JOB_BASE_NAME)
triggerActionName = getTriggerActionName(JOB_BASE_NAME)
triggerDisplayName = "${triggerActionName}_${jobType}_${jobSufix}"
triggeredJobParameters = getTriggeredJobParameters(jobBuildParameters)
triggeredJobParametersMap = getTriggeredJobParametersMap(jobBuildParameters)
sameNameTriggerList = getSameCronExpressionTriggerList(triggerDisplayName)
validationHelper.validateParameters(JOB_BASE_NAME, triggeredJobParametersMap)
validationHelper.checkForSameCronExpressionTriggerWithSameParam(sameNameTriggerList, triggeredJobParametersMap)

/* Create auto-triggered job */
job("${jobPath}") {
    /* Set retention policies from seeder job */
    logRotator(setJobRetentionPolicyConfig())
    /* Set job display name */
    displayName("${triggerActionName}_${jobType}_${jobSufix}")
    /* Restring job to build on same slave as seeder job */
    label(nodeLabel)
    /* Set job description (table with provided parameters */
    description(jobDescription)
    /* Config scm block for different trigger types */
    if(jobType == "Watcher") {
        scm {
            git {
                remote {
                    url(scmUrl)
                    credentials(scmCredentiaslId)
                }
                branches(scmBranch)
            }
        }
    }
    /* Set trigger depending on PROJECT_SOURCE_CODE_SERVER_TYPE parameter */
    triggers(generateTrigger())
    steps {
        triggerBuilder {
            configs {
                blockableBuildTriggerConfig {
                    block {
                        buildStepFailureThreshold('FAILURE')
                        unstableThreshold('UNSTABLE')
                        failureThreshold('FAILURE')
                    }
                    projects(triggeredJobName)
                    configs {
                        predefinedBuildParameters {
                            properties(triggeredJobParameters)
                            /* textParamValueOnNewLine is made as a mandatory field for trigger jobs in latest version of Jenkins. So we need to explicitly mention it and set the default value. */
                            textParamValueOnNewLine(false)
                        }
                    }
                }
            }
        }
    }
}

/* Get triggered job name depending on seed job name */
def getTriggeredJobName(seedJobName) {
    def triggeredJobName

    switch (seedJobName) {
        case 'createTest':
            triggeredJobName = "${JOB_NAME - JOB_BASE_NAME}runTests"
            break
        case 'createTrigger':
            triggeredJobName = "${JOB_NAME - JOB_BASE_NAME - 'Triggers/'}Builds/buildIrisApp"
            break
        case 'createExportTrigger':
            if(isNewFabricJobStructure){
                triggeredJobName = "${JOB_NAME - JOB_BASE_NAME - 'Triggers/'}Builds/FabricTasks/Export"
            } else {
                triggeredJobName = "${JOB_NAME - JOB_BASE_NAME - 'Triggers/'}Export"
            }
            break
        case 'createImportTrigger':
            if(isNewFabricJobStructure) {
                triggeredJobName = "${JOB_NAME - JOB_BASE_NAME - 'Triggers/'}Builds/FabricTasks/Import"
            } else {
                triggeredJobName = "${JOB_NAME - JOB_BASE_NAME - 'Triggers/'}Import"
            }
            break
        case 'createMigrationTrigger':
            if(isNewFabricJobStructure) {
                triggeredJobName = "${JOB_NAME - JOB_BASE_NAME - 'Triggers/'}Builds/FabricTasks/Migrate"
            } else {
                triggeredJobName = "${JOB_NAME - JOB_BASE_NAME - 'Triggers/'}Migrate"
            }
            break
        case 'createFabricAppTrigger':
            triggeredJobName = "${JOB_NAME - JOB_BASE_NAME - 'Triggers/'}Builds/buildFabricApp"
            break
        case 'createFlywayTrigger':
            triggeredJobName = "${JOB_NAME - JOB_BASE_NAME - 'Triggers/'}Flyway"
            break
        case 'createConfigureMSTrigger':
            triggeredJobName = "${JOB_NAME - JOB_BASE_NAME - 'Triggers/'}ConfigureMS"
            break
            
        default:
            triggeredJobName = ''
            break
    }

    if (triggeredJobName) {
        return triggeredJobName
    } else {
        throw new AppfactoryAbortException("Could not get matching trigger action name for triggered seed job: ${seedJobName}!")
    }
}

/* Generate build parameters for triggered(Facade) job */
def getTriggeredJobParameters(parameters) {
    def triggeredJobParameters = []

    parameters.each { parameter ->
        triggeredJobParameters.add("${parameter.name}=${parameter.value}")
    }

    triggeredJobParameters.join('\n')
}

/**
 * Generate build parameters for triggered job
 *
 * @param parameters in format [[name: parameterName, value: parameterValue]]
 * @return parameter in format [parameterName:parameterValue]
 * */
def getTriggeredJobParametersMap(parameters) {
    def triggeredJobParameters = [:]

    parameters.each { parameter ->
        triggeredJobParameters.put(parameter.name, parameter.value);
    }

    triggeredJobParameters
}

/* Generate job description (build parameters table) */
def generateJobDescription(parameters) {
    def tableBody = ''

    parameters.each {
        tableBody += "\n            <tr><td>${it.name}</td><td>${it.value.toString()}</td></tr>"
    }

    def jobDescription = """\
    <h2>Job Parameters</h2>
    <table class="job-config-settings">
        <thead>
            <tr>
                <th>Parameter</th>
                <th>Value</th>
            </tr>
        </thead>
        <tbody>${tableBody}
        </tbody>
    </table>
    """.stripIndent()

    jobDescription
}

/* Get all build parameters for current run */
def getBuildParameters(build) {
    /* Return all not null build parameters and exclude 'CRON_SCHEDULE' */
    build.getAction(ParametersAction).parameters.findResults {
        (it.name != 'CRON_SCHEDULE' && it.value) || (it.value instanceof Boolean) ? it : null
    }
}

/* Set trigger depending on input parameters */
def generateTrigger() {
    def trigger

    /* If CRON_SCHEDULE build parameter not empty */
    if (CRON_SCHEDULE) {
        /* Set cron trigger */
        trigger = {
            cron {
                spec(CRON_SCHEDULE)
            }
        }
    /* Else set generic SCM trigger */
    } else {
        trigger = getTriggerType()
    }

    trigger
}

/* Get trigger type */
def getTriggerType() {
    def triggerType

    switch (scmServerType) {
        case 'GitHub':
            triggerType = {
                gitHubPushTrigger()
            }
            break
        case 'Bitbucket':
            triggerType = {
                bitbucketPush()
                pollSCM {
                    scmpoll_spec('')
                    ignorePostCommitHooks(false)
                }
            }
            break
        case 'AWS CodeCommit':
            triggerType = {
                pollSCM {
                    scmpoll_spec('H/5 * * * *')
                    ignorePostCommitHooks(false)
                }
            }
            break
        default:
            triggerType = {
                genericTrigger {
                    regexpFilterExpression('')
                    regexpFilterText('')
                }
            }
            break
    }

    triggerType
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

/* Get the trigger action name based on trigger seed job. */
def getTriggerActionName(seedJobName) {
     def triggerActionMap = [
         createTest:'runTests',
         createTrigger:'buildIrisApp',
         createExportTrigger:'Export',
         createImportTrigger:'Import',
         createMigrationTrigger:'Migrate',
         createFabricAppTrigger:'buildFabricApp',
         createFlywayTrigger:'Flyway',
         createConfigureMSTrigger:'Microservices'
         ]
     def triggerActionName = triggerActionMap.get(seedJobName)
     if (triggerActionName) {
         return triggerActionName
     } else {
         throw new AppfactoryAbortException("Could not get matching trigger action name!")
     }
}

/* Get the list of same cron expression existing schedulers */
def getSameCronExpressionTriggerList(String createTriggerName) {
    def sameNameTriggerList = []
    triggerPath = JOB_NAME - JOB_BASE_NAME + jobFolder
    Jenkins.instance.getItemByFullName(triggerPath).allJobs
        .findAll { it instanceof AbstractProject }
        .each {
            if(it.displayName == createTriggerName) {
                sameNameTriggerList.add(it.fullName)
            }
        }
        return sameNameTriggerList
}

/* Get the service name based on trigger seed job. */
def getServiceForTriggeredSeedJob (seedJobName) {
    def triggerServiceMap = [
        createTest : 'Iris',
        createTrigger : 'Iris',
        createExportTrigger:'Foundry',
        createImportTrigger:'Foundry',
        createMigrationTrigger:'Foundry',
        createFabricAppTrigger:'Foundry',
        createFlywayTrigger:'Database',
        createConfigureMSTrigger:'Microservices'
        ]
    def serviceName = triggerServiceMap.get(seedJobName)
    if (serviceName) {
        return serviceName
    } else {
        throw new AppfactoryAbortException("Could not get matching service name for triggered seed job: ${seedJobName}!")
    }
}
