import exceptions.AppfactoryAbortException
import hudson.model.*
import jenkins.model.*
import groovy.json.JsonSlurper
import helper.config_helper
import helper.ansi_color_helper
import util.build_utils
import groovy.json.JsonBuilder
import com.cloudbees.hudson.plugins.folder.Folder

/* This class is to update existing CustomHook pipeline jobs with default values in parameters and upload new script to S3 *

* Here creation includes three main sections
* 1) upload hook to s3
* 2) update parameters
* 3) update configs with new hook details
*
* Rollback mechanism
* 1) upload hook fails, throw exception and upload backup once again
* 2) update config fails revert 1)
* */

/* Some local variables */
def config = new config_helper()

/* Gather Parameters */
buildStep = BUILD_STEP
hookChannel = HOOK_CHANNEL
nameOfHook = OLD_HOOK_NAME
hookFolderName = buildStep - "_STEP"
propagateBuildStatus = String.valueOf(PROPAGATE_BUILD_STATUS)
newNameOfHook = HOOK_NAME
buildAction = BUILD_ACTION
scriptArguments = SCRIPT_ARGUMENTS.replace('\"','\'')
projectName = binding.variables.PROJECT_NAME
urlOfHook = ""
isVizHook = true

/* Check whether Cloud Account ID is present or not. If present, assign it to a variable 'cloudAccountId' else fail the build. */
try {
    cloudAccountId = Jenkins.instance.getSecurityRealm().getAccountId()
}
catch (Exception e) {
    throw new AppfactoryAbortException('Unable to find the Cloud Account ID!!')
}

/* Additional Information about build */
workspace = binding.variables.WORKSPACE
s3BucketName = binding.variables.S3_BUCKET_NAME
s3BucketRegion = binding.variables.S3_BUCKET_REGION
customHookName = isHookRenamed(nameOfHook, newNameOfHook) ?  newNameOfHook :  nameOfHook

customhooksConfigFolder = JOB_NAME - JOB_BASE_NAME
buildType = "Iris"

// We will make the flag to false since the custom hooks are to be created in the fabric pipeline
if(JOB_NAME.contains('Foundry')) {
    isVizHook = false
    buildType = "Foundry"
}

/* Due to bug in Build Description Setter, adding build description using Jenkins native APIs */
def build = hudson.model.Executor.currentExecutor().currentExecutable
build.description = HOOK_NAME + '-' + BUILD_STEP

/* --------- Validate Parameters from hook ---------*/
validateBuildParameters()

/* --------- Upload hook zip to S3 -----------------*/
if ("${HOOK_ARCHIVE_FILE}") {
    uploadHook()
}

/* --------- Update config files ------------------ */
try {
    /* Collect Older JSON from Project Folder Config */
    def olderContent = config.getOlderContent(customhooksConfigFolder, projectName);
    def olderContentInJson = new JsonSlurper().parseText(olderContent)
    def oldBuilder = new JsonBuilder(olderContentInJson)

    /* get index of hook and update in new JSON */
    def index = getIndexOfHookFromJson(nameOfHook, olderContentInJson[hookFolderName])

    /* URL of Hook */
    if ("${HOOK_ARCHIVE_FILE}") {
        if(isVizHook) {
            urlOfHook = "https://${s3BucketName}.s3.amazonaws.com/$cloudAccountId/$projectName/CustomHooks/$buildStep/$customHookName/1/Hook.zip";
        } else {
            urlOfHook = "https://${s3BucketName}.s3.amazonaws.com/$cloudAccountId/$projectName/CustomHooks/Foundry/$buildStep/$customHookName/1/Hook.zip";
        }
    } else {
        /* get this from config file */
        out.println("No hook zip uploaded, so older zip will be used");
        urlOfHook = getHookUrlFromConfigFileJson(nameOfHook, olderContentInJson[hookFolderName])
    }

    /* update index in new Hook Json */
    /* Prepare new hook json */
    def newHookJson = initializeNewJson(customHookName, urlOfHook, buildStep, hookChannel, buildAction, scriptArguments, propagateBuildStatus)
    def newHookInJson = new JsonSlurper().parseText(newHookJson)
    def newBuilder = new JsonBuilder(newHookInJson)

    if(index == null){
        throw new AppfactoryAbortException("Hook named " + nameOfHook + " is not found in CustomHook config file [JSON]")
    } else {
        newBuilder.content.index = index
    }

    /* remove object from config Json */
    def objectIndex = removeOldHookPropertiesFromJson(nameOfHook, olderContentInJson[hookFolderName])
    olderContentInJson[hookFolderName].remove(objectIndex);

    /* add new object to config json */
    oldBuilder.content."$hookFolderName" << newHookInJson
    config.createConfigFile(customhooksConfigFolder, projectName, oldBuilder.toPrettyString())
}
catch (Exception e){
    println(e.getMessage())
    throw new AppfactoryAbortException("\nException occurred while updating config files\n")
}


/* --------- Update parameters ---------------------*/
updateHookJobs()

/* --------------------- utility functions --------------------*/

/* @Params {hookName} {jsonContent}
   @return index of hook in Json */
def getIndexOfHookFromJson(hookName, hooksList){
    def index
    hooksList.each{ hooks->
        if(hooks.hookName == hookName){
            out.println(ansi_color_helper.decorateMessage(
                    "Hook named " + hookName + " is found at index " + hooks.index, 'INFO'))
            index = hooks.index
            return index
        }
    }
    return index;
}

def getHookUrlFromConfigFileJson(hookName, hooksList){
    def hookUrl
    hooksList.each{ hooks->
        if(hooks.hookName == hookName){
            hookUrl = hooks.hookUrl
        }
    }
    return hookUrl;
}

/* @Params {hookName} ${JsonContent}
   @return updatedObject */
def removeOldHookPropertiesFromJson(hookName, hookObjectList){
    def indexOfObjectToDelete
    int index =0
    hookObjectList.each{ hooks->
        if(hooks.hookName == hookName){
            indexOfObjectToDelete = index;
        }
        index = index + 1;
    }
    indexOfObjectToDelete
}

/* update Job default params of Hook */
def updateHookJobs(){
    /* update below hook parameters
    *   BUILD_STEP
    *   HOOK_NAME
    *   BUILD_ACTION
    *   SCRIPT_ARGUMENTS
    *   BUILD_SCRIPT
    * */

    /* Parameter Descriptions */
    def hookNameDesc = "The name of your new Custom Hook\n It must start with a letter, only contain letters and numbers and be between 4 and 17 characters long."
    def buildStepDesc = "Select one of the following phases where you want to inject custom hook.<br>"
    def hookChannelDesc = "Select one of the following hook channel where you want to inject custom hook.<br>"
    def buildActionDesc = "Type of hook you want to run <br>"
    def buildScriptDesc = "Upload CustomHook.zip file that has custom hook logic. Refer Link for the Json Format"
    def scriptArgumentsDesc = "Specify targets, goal or arguments for Hook. These args will be passed to CustomHook script. <br>For Ant - pass args like -DProjectName=ABC <br>For Maven - Specify goals like clean install <br>"

    Jenkins instance = Jenkins.instance
    Folder buildFolder = instance
            .getItem(projectName)
            .getItem(buildType)
            .getItem("CustomHooks")
            .getItem(hookFolderName)

    buildFolder.getAllJobs().each {
        if (it.getDisplayName() == nameOfHook) {
            /* Helper for choice parameters */
            String[] buildStepChoices = ['PRE_BUILD', 'POST_BUILD', 'POST_TEST']
            String[] hookChannelChoices = ['ANDROID_MOBILE_STAGE', 'ANDROID_TABLET_STAGE','IOS_MOBILE_STAGE','IOS_TABLET_STAGE', 'IOS_MOBILE_IPA_STAGE', 'IOS_TABLET_IPA_STAGE']

            def defaultBuildStepParam = new StringParameterDefinition("defaultBuildStepChoice", buildStep)
            def defaultHookChannelParam = new StringParameterDefinition("defaultHookChannelChoice", hookChannel)

            String[] buildActionChoices = ['Execute Ant', 'Execute Maven']
            def defaultBuildActionParam = new StringParameterDefinition("defaultBuildActionChoice", buildAction)

            /* Parameters definitions */
            def hookNameParam = new StringParameterDefinition("HOOK_NAME", customHookName, hookNameDesc)
            def buildActionParam = new ChoiceParameterDefinition("BUILD_ACTION", buildActionChoices, buildActionDesc).copyWithDefaultValue(defaultBuildActionParam.getDefaultParameterValue())
            def scriptArgumentParam = new StringParameterDefinition("SCRIPT_ARGUMENTS", scriptArguments, scriptArgumentsDesc);
            def buildScriptParam = new StringParameterDefinition("BUILD_SCRIPT", urlOfHook, buildScriptDesc)
            def buildStepParam = new ChoiceParameterDefinition('BUILD_STEP', buildStepChoices, buildStepDesc).copyWithDefaultValue(defaultBuildStepParam.getDefaultParameterValue())
            def hookChannelParam = new ChoiceParameterDefinition('HOOK_CHANNEL', hookChannelChoices, hookChannelDesc).copyWithDefaultValue(defaultHookChannelParam.getDefaultParameterValue())

            it.removeProperty(ParametersDefinitionProperty.class)
            it.addProperty(new ParametersDefinitionProperty([hookNameParam, buildStepParam, hookChannelParam, buildActionParam, scriptArgumentParam, buildScriptParam]))

            if(isHookRenamed(nameOfHook, newNameOfHook)){
                ((Job)it).renameTo(customHookName);
            }
            out.println(ansi_color_helper.decorateMessage("Hook is updated successfully!!", 'INFO'))
        }
    }
}

/**
 * @param nameOfHook
 * @param newNameOfHook
 * @return is hook renamed
 */
def isHookRenamed(nameOfHook, newNameOfHook){
    if(nameOfHook != newNameOfHook && newNameOfHook){
        return true
    }
    else{
        return false
    }
}


/** Initialize new hook Json object
 *  @param customHookName
 *  @param urlOfHook
 *  @param buildStep
 *  @param buildAction
 *  @param scriptArguments
 *  @param propagateBuildStatus
 *  @return new Json object string
 */
def initializeNewJson(String customHookName, String urlOfHook, String buildStep, String hookChannel, String buildAction, String scriptArguments,  String propagateBuildStatus){
    return """
        {
           "propagateBuildStatus":"$propagateBuildStatus",
           "index":0,
           "hookUrl":"$urlOfHook",
           "parameter":{
              "BUILD_STEP":"$buildStep",
              "HOOK_CHANNEL":"$hookChannel",
              "BUILD_ACTION":"$buildAction",
              "SCRIPT_ARGUMENTS":"$scriptArguments"
           },
           "hookName":"$customHookName",
           "status":"enabled"
        }
    """
}

/* Validate all required parameters for the CustomHook name job to run */
def validateBuildParameters(){
    if ("${HOOK_ARCHIVE_FILE}") {
        def successOutput = new StringBuilder(), errorOutput = new StringBuilder()
        def proc = "unzip -l $workspace/HOOK_ARCHIVE_FILE".execute()

        proc.consumeProcessOutput(successOutput, errorOutput)
        proc.waitForOrKill(1000)

        if (errorOutput) {
            throw new AppfactoryAbortException("Uploaded file isn't a valid zip file.")
        }
    }

    checkParameter("BUILD_ACTION", BUILD_ACTION)
    !isHookRenamed(nameOfHook, newNameOfHook)?: checkDuplicationOfProject(newNameOfHook, buildStep)
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
            throw new AppfactoryAbortException("Renaming failed. $hookName named Custom hook aleady exist in $buildStep")
        }
    }
}


/* Execute S3 upload command to copy Hook.zip file to S3 bucket from workspace to project folder's CustomHooks folder */

def uploadHook() {
    def uploadCmd
    /*Take backup of previous hook zip*/
    takeHookZipSnapshot()

    /* Upload new hook zip to S3 by renaming HOOK_ARCHIVE_FILE to Hook.zip */
    def renameArtifacts = "mv $workspace/HOOK_ARCHIVE_FILE $workspace/Hook.zip"
    def renameArtifactsOutput = build_utils.executeProcess(renameArtifacts);
    if (renameArtifactsOutput.errorOutput) {
        throw new AppfactoryAbortException("Failed to Rename Hook. \n $renameArtifactsOutput.errorOutput")
    }

    if(isVizHook) {
        uploadCmd = "aws --region $s3BucketRegion s3 cp $workspace/Hook.zip s3://${s3BucketName}/$cloudAccountId/$projectName/CustomHooks/$buildStep/$customHookName/1/"
    } else {
        uploadCmd = "aws --region $s3BucketRegion s3 cp $workspace/Hook.zip s3://${s3BucketName}/$cloudAccountId/$projectName/CustomHooks/Foundry/$buildStep/$customHookName/1/"
    }
    
    def uploadCmdOutput = build_utils.executeProcess(uploadCmd)

    if (uploadCmdOutput.errorOutput) {
        throw new AppfactoryAbortException("Failed to publish Hook. \n $uploadCmdOutput.errorOutput")
    } else {
        println(ansi_color_helper.decorateMessage('Hook archive published successfully.', 'INFO'))
    }
}

def takeHookZipSnapshot() {
    try {
        def takeBackupCmd
        if(isVizHook) {
            takeBackupCmd = "aws --region $s3BucketRegion s3 cp s3://${s3BucketName}/$cloudAccountId/$projectName/CustomHooks/$buildStep/$customHookName/1/Hook.zip $workspace/Hookbak.zip"
        } else {
            takeBackupCmd = "aws --region $s3BucketRegion s3 cp s3://${s3BucketName}/$cloudAccountId/$projectName/CustomHooks/Foundry/$buildStep/$customHookName/1/Hook.zip $workspace/Hookbak.zip"
        }
        
        takeBackupCmd.execute()
    }
    catch (Exception e){
        println(e.getMessage())
        throw new AppfactoryAbortException("\nException occurred while capturing snapshot of hook.\n\n")
    }
}
