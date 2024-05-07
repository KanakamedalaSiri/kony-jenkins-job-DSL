import com.cloudbees.hudson.plugins.folder.Folder
import exceptions.AppfactoryAbortException
import jenkins.model.*
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import helper.config_helper
import helper.ansi_color_helper
import util.build_utils

/* This class is to create pipeline job with the name of Hook_Name under Iris/CustomHooks/<build_stage>/ folder.
*
* Here creation includes three main sections
* 1) upload hook to s3
* 2) create pipeline
* 3) update configs with new hook details
*
* Rollback mechanism
* 1) upload hook fails, throw exception
* 2) create pipeline fails, revert changes from 2) and 1)
* 3) update config fails, revert changes from 2) and 1)
* */


/* parameters */
nameOfHook = HOOK_NAME
buildStep = BUILD_STEP
hookChannel = HOOK_CHANNEL
buildAction = BUILD_ACTION
scriptArguments = SCRIPT_ARGUMENTS.replace('\"','\'')
propagateBuildStatus = String.valueOf(PROPAGATE_BUILD_STATUS)
projectName = binding.variables.PROJECT_NAME
isVizHook = true
/* Check whether Cloud Account ID is present or not. If present, assign it to a variable 'cloudAccountId' else fail the build. */
try {
    cloudAccountId = Jenkins.instance.getSecurityRealm().getAccountId()
}
catch (Exception e) {
    throw new AppfactoryAbortException('Unable to find the Cloud Account ID!!')
}

/* Additional Information about project */
workspace = binding.variables.WORKSPACE
s3BucketName = binding.variables.S3_BUCKET_NAME
s3BucketRegion = binding.variables.S3_BUCKET_REGION

customhooksConfigFolder = JOB_NAME - JOB_BASE_NAME
artifactUrl = "https://${s3BucketName}.s3.amazonaws.com/$cloudAccountId/$projectName/CustomHooks/$buildStep/$nameOfHook/1/Hook.zip";

// We will make the flag to false since the custom hooks are to be created in the fabric pipeline
if(JOB_NAME.contains('Foundry')) {
    isVizHook = false
    artifactUrl = "https://${s3BucketName}.s3.amazonaws.com/$cloudAccountId/$projectName/CustomHooks/Foundry/$buildStep/$nameOfHook/1/Hook.zip";
}

/* Names of the folders for each stage */
preBuildFolderName = 'PRE_BUILD'
postBuildFolderName = 'POST_BUILD'
postTestFolderName = 'POST_TEST'
postDeployFolderName = 'POST_DEPLOY'

groovyShell = new GroovyShell()
hookFolderName = getCustomHookFolderName(buildStep)

/* Check Number of Number in stage. If  equals 5, user should allow to create more */
def buildStepFolderObject = Jenkins.instance.getItemByFullName("$customhooksConfigFolder/$hookFolderName/")
def customHookLimit = binding.variables.CUSTOMHOOKS_MAX_COUNT.toInteger()
if(buildStepFolderObject.getAllJobs().size().toInteger() >= customHookLimit) {
    throw new AppfactoryAbortException("Exceeded number of CustomHooks limit!!. Number of CustomHooks can't be more than $customHookLimit per Hook Point" +
                    "(Pre-Build/Post-Build/Post-Test/Post-Deploy).")
}

/* Due to bug in Build Description Setter, adding build description using Jenkins native APIs */
def build = hudson.model.Executor.currentExecutor().currentExecutable
build.description = HOOK_NAME + '-' + BUILD_STEP

/* Stage 1 : Validating Parameters */
validateBuildParameters()

/* Stage 2: upload hook Files */
uploadHook()

/* Stage 3: create pipeline */
jobDescription = "$nameOfHook : extending existing pipeline"
jobPath = JOB_NAME - JOB_BASE_NAME + hookFolderName + '/' + nameOfHook

resourcesFolder = getPath([".", 'resources'])
hookParameters = parseJobParameters('customHookSpecific', [
        'HOOK_NAME': "$nameOfHook",
        'BUILD_STEP': "$BUILD_STEP",
        'HOOK_CHANNEL': "$hookChannel",
        'BUILD_ACTION':"$BUILD_ACTION",
        'BUILD_SCRIPT':"$artifactUrl",
        'SCRIPT_ARGUMENTS':"$scriptArguments"])

try {
    /* Create Custom Hook job */
    pipelineJob("${jobPath}") {
        /* Set retention policies from seeder job */
        logRotator(setJobRetentionPolicyConfig())
        /* Set job display name */
        displayName("$nameOfHook")
        /* Set project name environment variable */
        environmentVariables(setJobEnvVars())
        /* Set job description (table with provided parameters */
        description(jobDescription)
        /* Set parameters to Custom Hook job */
        parameters(hookParameters)
        /* need to disable rebuild button for CustomHooks*/
        properties {
            rebuild {
                rebuildDisabled(true)
            }
        }
        definition {
            cps {
                script('import com.kony.appfactory.visualizer.customhooks.CustomHook\nnew CustomHook(this).processPipeline()')
                sandbox()
            }
        }
        disabled()
    }
}
catch (Exception e){
    println(ansi_color_helper.decorateMessage(e.getMessage(), 'WARN'))
    rollbackUpload()
    throw new AppfactoryAbortException('\nFailed to create pipeline.\n')
}

/* Stage4: Update config Files */
try {
    def config = new config_helper()
    def jsonBuilder = new JsonBuilder()
    def newHookJson = """
{
   "propagateBuildStatus":"$propagateBuildStatus",
   "index":"0",
   "hookUrl":"$artifactUrl",
   "parameter":{
      "BUILD_STEP":"$buildStep",
      "HOOK_CHANNEL": "$hookChannel",
      "BUILD_ACTION":"$buildAction",
      "SCRIPT_ARGUMENTS":"$scriptArguments"
   },
   "hookName":"$nameOfHook",
   "status":"disabled"
}"""

    def newHookInJson = new JsonSlurper().parseText(newHookJson)
    def newBuilder = new JsonBuilder(newHookInJson)

/* Collect Older JSON from Project Folder Config */
    def olderContent = config.getOlderContent(customhooksConfigFolder, projectName);

/* Update Older JSON with new Hook JSON */
    if (olderContent) {
        def olderContentInJson = new JsonSlurper().parseText(olderContent)
        def oldBuilder = new JsonBuilder(olderContentInJson)

        /* Update the index of newHook */
        newBuilder.content.index = olderContentInJson[hookFolderName].size().toString()

        /* Append newHook JSON */
        oldBuilder.content."$hookFolderName" << newHookInJson
        config.createConfigFile(customhooksConfigFolder, projectName, oldBuilder.toPrettyString())

    } else {
        /* Update New Hook JSON with basic Skeleton */
        def basicContent = """{
        "PRE_BUILD":[],
        "POST_BUILD":[],
        "POST_TEST":[],
        "POST_DEPLOY":[]
    }"""
        def basicContentInJson = new JsonSlurper().parseText(basicContent)
        def basicBuilder = new JsonBuilder(basicContentInJson)

        basicBuilder.content."$hookFolderName" << newHookInJson
        config.createConfigFile(customhooksConfigFolder, projectName, basicBuilder.toPrettyString())
    }
}
catch (Exception e){
    println(ansi_color_helper.decorateMessage(e.getMessage(), 'WARN'))
    rollbackUpload()
    //rollbackPipeline()
    throw new AppfactoryAbortException('\nFailed to update config files.\n')
}

/* ----------------  Utility functions ------------------ */
def rollbackPipeline(){
    try {
        def job = Jenkins.instance.getItemByFullName(jobPath)
        job.delete();
    }
    catch (Exception e){
        println(ansi_color_helper.decorateMessage(e.getMessage(), 'WARN'))
    }
}

def rollbackUpload(){
    def uploadCmd
    if(isVizHook) {
        uploadCmd = "aws --region $s3BucketRegion s3 rm  s3://$s3BucketName/$cloudAccountId/$projectName/CustomHooks/$buildStep/$nameOfHook/1/Hook.zip"
    } else {
        uploadCmd = "aws --region $s3BucketRegion s3 rm  s3://$s3BucketName/$cloudAccountId/$projectName/CustomHooks/Foundry/$buildStep/$nameOfHook/1/Hook.zip"
    }
    uploadCmd.execute()
    println(ansi_color_helper.decorateMessage('Hook unpublished successfully.', 'INFO'))
}

/* get Custom Hook Folder Name based on build Step type */
def getCustomHookFolderName(buildStep){
    switch (buildStep) {
        case 'PRE_BUILD_STEP':
            customHookFolder = preBuildFolderName
            break
        case 'POST_BUILD_STEP':
            customHookFolder = postBuildFolderName
            break
        case 'POST_TEST_STEP':
            customHookFolder = postTestFolderName
            break
        case 'POST_DEPLOY_STEP':
            customHookFolder = postDeployFolderName
            break
        default:
            customHookFolder = ''
            break
    }

    if (customHookFolder) {
        return customHookFolder
    } else {
        throw new AppfactoryAbortException("Not able to find custom hook of type $buildStep")
    }

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

def parseJobParameters(jobParametersFileName, Map args = [:]) {
    def jobParametersFileExtension = '.groovy'
    def jobParametersFilePath = getPath([resourcesFolder, 'jobParameters', jobParametersFileName])

    return groovyShell.parse(
            readFileFromWorkspace(jobParametersFilePath + jobParametersFileExtension)
    ).getParameters(args)
}

def getPath(pathList) {
    pathList.join('/')
}

/* Validate all required parameters for the CustomHook name job to run */
def validateBuildParameters(){
    checkParameter("HOOK_NAME", HOOK_NAME)
    checkParameter("BUILD_STEP", BUILD_STEP)
    checkParameter("HOOK_CHANNEL", HOOK_CHANNEL)
    checkParameter("BUILD_ACTION", BUILD_ACTION)
    checkParameter("HOOK_ARCHIVE_FILE", HOOK_ARCHIVE_FILE)
    checkDuplicationOfProject(HOOK_NAME, BUILD_STEP)

    /* Validation added for file extension*/
    if ("${HOOK_ARCHIVE_FILE}") {
        def successOutput = new StringBuilder(), errorOutput = new StringBuilder()
        def proc = "unzip -l $workspace/HOOK_ARCHIVE_FILE".execute()

        proc.consumeProcessOutput(successOutput, errorOutput)
        proc.waitForOrKill(1000)

        if (errorOutput) {
            throw new AppfactoryAbortException('Uploaded file isn\'t a valid zip file.')
        }
    }
}

def checkParameter(parameterName, value){
    if(!value){
        throw new AppfactoryAbortException("$parameterName is mandatory paramater. It cannot be empty or null")
    }
}

/* Validate if there is any job already exists for CustomHook name, if so throw out an exception */
def checkDuplicationOfProject(hookName, buildStep){
    def buildType = isVizHook ? "Iris" : "Foundry"
    Jenkins instance = Jenkins.instance
    Folder buildFolder = instance
            .getItem(projectName)
            .getItem(buildType)
            .getItem("CustomHooks")
            .getItem(buildStep - "_STEP")
    buildFolder.getAllJobs().each{ job->
        if(job.getDisplayName() == hookName){
            throw new AppfactoryAbortException("$hookName named Custom hook aleady exist in $buildStep")
        }
    }
}

/* Set environment variables for the CustomHook job */
def setJobEnvVars(){
    def result = [:]
    result.put('PROJECT_NAME', projectName)
    result
}


/* Execute S3 upload command to copy Hook.zip file to S3 bucket from workspace to project folder's CustomHooks folder */
def uploadHook() {
    def uploadCmd
    /* Upload new hook zip to S3 by renaming HOOK_ARCHIVE_FILE to Hook.zip */
    def renameArtifacts = "mv $workspace/HOOK_ARCHIVE_FILE $workspace/Hook.zip"
    def renameArtifactsOutput = build_utils.executeProcess(renameArtifacts);
    if (renameArtifactsOutput.errorOutput) {
        throw new AppfactoryAbortException("Failed to Rename Hook. \n $renameArtifactsOutput.errorOutput")
    }

    if(isVizHook) {
        uploadCmd = "aws --region $s3BucketRegion s3 cp $workspace/Hook.zip s3://${s3BucketName}/$cloudAccountId/$projectName/CustomHooks/$buildStep/$nameOfHook/1/"
    } else {
        uploadCmd = "aws --region $s3BucketRegion s3 cp $workspace/Hook.zip s3://${s3BucketName}/$cloudAccountId/$projectName/CustomHooks/Foundry/$buildStep/$nameOfHook/1/"
    }
    def uploadCmdOutput = build_utils.executeProcess(uploadCmd)

    if (uploadCmdOutput.errorOutput) {
        throw new AppfactoryAbortException("Failed to publish Hook. \n $uploadCmdOutput.errorOutput")
    } else {
        println(ansi_color_helper.decorateMessage('Hook archive published successfully.', 'INFO'))
    }
}
