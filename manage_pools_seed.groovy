import exceptions.AppfactoryAbortException
import jenkins.model.Jenkins
import com.cloudbees.hudson.plugins.folder.Folder
import org.jenkinsci.plugins.configfiles.custom.CustomConfig
import org.jenkinsci.plugins.configfiles.json.JsonConfig
import org.jenkinsci.plugins.configfiles.folder.FolderConfigFileProperty
import helper.ansi_color_helper
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

/* Fetch job name, by default JOB_NAME variable contains full path to the job */
projectRootFolderName = JOB_NAME.split('/')[0]
deviceListVar = getBinding().getVariables()['DEVICES_LIST']
deviceFilterVar = getBinding().getVariables()['DEVICE_FILTER']
def poolNameVar = getBinding().getVariables()['POOL_NAME']
/*
    Workaround to remove escaping from device names in the list returned by DEVICES_LIST parameter in case it is available in job parameters,
    by default Jenkins string build parameter add escaping to the provided string.
*/
devicesConfiguration = deviceListVar ? "${deviceListVar?.replaceAll(/\\"/, '"')}" : (deviceFilterVar) ? deviceFilterVar.split(';')[0]: null
poolName = (poolNameVar) ?: (deviceFilterVar) ? deviceFilterVar.split(';')[1]: null
/* Store pool name to delete */
poolsToDelete = (AVAILABLE_TEST_POOLS) ? AVAILABLE_TEST_POOLS.trim().tokenize(',') : null

if (deviceFilterVar && !poolsToDelete) {
    def configJson = new JsonSlurper().parseText(devicesConfiguration)
    isNewPool = configJson['isNewPool']
    configJson.remove('isNewPool')
    devicesConfiguration = JsonOutput.toJson(configJson)
}

/* Main body of the scripts */
def mainFlow() {
    /* Setting build description based on the type of operation */
    buildOperation = poolName ? 'Created' : 'Deleted'
    buildDescription = poolName ? poolName : AVAILABLE_TEST_POOLS
    def build = hudson.model.Executor.currentExecutor().currentExecutable

    def folderObject = getFolderObject(projectRootFolderName)
    def folderConfigFilesObject = getConfigPropertyObject(folderObject)
    def availableConfigs = getAvailableConfigs(folderConfigFilesObject)


    /*
        Check provided parameters, because of this job has two purposes: creating and deleting device pools,
        we need stick to one operation at a time.
     */
    if (poolName && devicesConfiguration && !poolsToDelete) {
        createConfig(build, poolName, devicesConfiguration, folderConfigFilesObject, availableConfigs)
    } else if (!poolName && !devicesConfiguration && poolsToDelete) {
        poolsToDelete.each { poolName ->
            removeConfig(build, poolName, folderConfigFilesObject, availableConfigs)
        }
    } else {
        throw new AppfactoryAbortException('Please provide parameters either for Creating a new pool or Deleting an existing pool!')
    }
}

/*
    To be able to store devices configuration for the test with Config File Provider,
    we need to get Folder object where we want to store devices configuration first.
 */
def getFolderObject(folderName) {
    def folderObject = null

    try {
        folderObject = Jenkins.instance.getItemByFullName("${folderName}", Folder)
    } catch (Exception e) {
        throw new AppfactoryAbortException("Failed to get folder object: ${e.message()}")
    }

    folderObject
}

/* Get Config File Provider property in provided project Folder for storing devices configuration first. */
def getConfigPropertyObject(folderObject) {
    def folderConfigFilesObject = null
    def folderProperties

    try {
        folderProperties = folderObject.getProperties()

        folderProperties.each { property ->
            if (property instanceof FolderConfigFileProperty) {
                folderConfigFilesObject = property
            }
        }
    } catch (Exception e) {
        throw new AppfactoryAbortException("Failed to get folder object properties: ${e.message()}")
    }

    folderConfigFilesObject
}

/* Get all device pools that been created before, for remove step */
def getAvailableConfigs(folderConfigFilesObject) {
    def availableConfigs = null

    if (folderConfigFilesObject) {
        availableConfigs = folderConfigFilesObject.getConfigs()
    }

    availableConfigs
}

/* Create Config File object of CustomConfig type for provided device list */
def createConfig(build, poolName, devicesConfiguration, folderConfigFilesObject, availableConfigs) {
    def unique = true
    def creationDate = new Date().format("yyyyMMdd_HH-mm-ss-SSS", TimeZone.getTimeZone('UTC'))
    def newConfigComments = "This config created at ${creationDate} for pool ${poolName}"

    if (availableConfigs) {
        unique = (availableConfigs.find { config -> config.id == poolName }) ? false : true
    }

    if (unique) {
        if (deviceListVar){
            folderConfigFilesObject.save(new CustomConfig(poolName, poolName, newConfigComments, devicesConfiguration))
        } else {
            folderConfigFilesObject.save(new JsonConfig(poolName, poolName, newConfigComments, devicesConfiguration))
        }
        println ansi_color_helper.decorateMessage("Pool ${poolName} has been created successfully", 'INFO')
        build.description = 'Pool Created: ' + poolName
    } else {
        if (deviceListVar) {
            throw new AppfactoryAbortException("Please provide unique ID for devices pool, ${poolName} already exists!")
        }
        else if(isNewPool){
            throw new AppfactoryAbortException("${poolName} already exists! Please provide a different name or edit the existing pool to update it.")
        }
        else {
            folderConfigFilesObject.save(new JsonConfig(poolName, poolName, newConfigComments, devicesConfiguration))
            println ansi_color_helper.decorateMessage("Pool ${poolName} has been updated successfully", 'INFO')
            build.description = 'Pool Updated: ' + poolName
        }
    }
}

/* Remove already existing device pool */
def removeConfig(build, configID, folderConfigFilesObject, availableConfigs) {
    def poolExists
    try {
        if (availableConfigs) {
            poolExists = (availableConfigs.find { config -> config.id == configID }) ? true : false
        }

        if (poolExists) {
            folderConfigFilesObject.remove(configID)
            println ansi_color_helper.decorateMessage("Pool has been removed successfully", 'INFO')
        } else {
            println ansi_color_helper.decorateMessage("Pool with ${configID} ID was not found", 'WARN')
        }
        build.description = 'Pool Deleted: ' + buildDescription
    } catch (Exception e) {
        throw new RuntimeException(ansi_color_helper.decorateMessage("Failed to remove pool ${configID}", 'ERROR'), e)
    }
}

mainFlow()