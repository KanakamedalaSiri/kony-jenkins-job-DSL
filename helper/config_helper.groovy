package helper

import hudson.FilePath
import hudson.model.Run
import hudson.model.TaskListener
import hudson.model.Cause
import hudson.model.Executor
import jenkins.model.Jenkins
import org.jenkinsci.lib.configprovider.ConfigProvider
import org.jenkinsci.lib.configprovider.model.Config
import org.jenkinsci.plugins.configfiles.custom.CustomConfig
import org.jenkinsci.plugins.configfiles.folder.FolderConfigFileProperty

/* This class is to define ConfigFile for CustomHooks at each project, and fetch ConfigFile content */

class config_helper {
    def userID = getBuildUser()

    /* Create Config File for customhooks with list of hooks uploaded */
    def createConfigFile(folderName, fileId, content) {
        def folderObject = getFolderObject(folderName)
        FolderConfigFileProperty folderConfigFilesObject = getConfigPropertyObject(folderObject)
        Collection<Config> availableConfigs = getAvailableConfigs(folderConfigFilesObject)
        createConfig(fileId, content, folderConfigFilesObject, availableConfigs)
        println " [" + userID + "] newContent = " + content
        println " [" + userID + "] updated CustomHook config JSON file successfully!!"
    }

    /* Retrieve customhooks config file content, to manipulate further with default params using updateCustomHook job */
    def getOlderContent(folderName, fileId){
        def folderObject = getFolderObject(folderName)
        FolderConfigFileProperty folderConfigFilesObject = getConfigPropertyObject(folderObject)
        Collection<Config> availableConfigs = getAvailableConfigs(folderConfigFilesObject)
        def olderContent = getConfigFileContent(fileId, availableConfigs);
        println " [" + userID + "] Updating CustomHook config JSON content in project ${folderName}"
        println " [" + userID + "] olderContent = " + olderContent
        return olderContent
    }

    /*
    To be able to store each hook job with default params with Config File Provider,
    we need to get Folder object where we want to store hooks jobs list first.
    */
    String getConfigFileContent(String configFileName, Collection<Config> availableConfigs) throws IOException {
        String olderContent = "";
        for(Config config : availableConfigs){
            Run<?, ?> build = null;
            FilePath workspace= null;
            ConfigProvider provider = config.getDescriptor();
            List<String> tempFiles = new ArrayList<>();
            tempFiles.add("dummy");
            if((config.name).equals(configFileName)){
                olderContent = config.getDescriptor().supplyContent(config, build, workspace, TaskListener.NULL, tempFiles);
            }
        }
        return olderContent;
    }

    /*
    To be able to store each hook job with default params with Config File Provider,
    we need to get Folder object where we want to store hooks jobs list first.
    */
    def getFolderObject(folderName) {
        def folderObject = null
        folderObject = Jenkins.instance.getItemByFullName(folderName);
        folderObject
    }

    /* Get Config File Provider property in provided project Folder for storing hooks list along with params */
    def getConfigPropertyObject(folderObject) {
        def folderConfigFilesObject = null
        def folderProperties = folderObject.getProperties()
        folderProperties.each { property ->
            if (property instanceof FolderConfigFileProperty) {
                folderConfigFilesObject = property
            }
        }
        folderConfigFilesObject
    }

    /* Get all hooks list that been created before, for running them in channel build pipeline */
    def getAvailableConfigs(folderConfigFilesObject) {
        def availableConfigs = null
        if (folderConfigFilesObject) {
            availableConfigs = folderConfigFilesObject.getConfigs()
        }
        availableConfigs
    }

    /* Create Config File object of CustomConfig type for provided hooks list */
    def createConfig(configFileName, content, folderConfigFilesObject, availableConfigs) {
        def creationDate = new Date().format("yyyyMMdd_HH-mm-ss-SSS")
        def newConfigComments = "This config created at ${creationDate} for hook ${configFileName}"

        folderConfigFilesObject.save(new CustomConfig(configFileName, configFileName, newConfigComments, content))
        println " [" + userID + "] CustomHook config file for project ${configFileName} has been created/updated successfully"
    }

    def getBuildUser() {
        /* Get the currentBuild instance */
        def currentBuild = Executor.currentExecutor().currentExecutable
        /* Return user ID who triggered the build, for setting it in the system print logs */
        return currentBuild.getCause(Cause.UserIdCause).getUserId()
    }
}