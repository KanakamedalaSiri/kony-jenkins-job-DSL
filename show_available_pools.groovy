import jenkins.model.Jenkins
import com.cloudbees.hudson.plugins.folder.Folder
import org.jenkinsci.plugins.configfiles.folder.FolderConfigFileProperty

/*
    Main body of the scripts.
    Been agreed to store all device pools (CustomConfig objects of Config File Plugin) under project root folder.
    Method below fetch all CustomConfig objects from Config File property of the folder and
    displays them as drop-down list to the user.
*/
def showAvailablePools() {
    def folderObject = getFolderObject(projectName)
    def folderConfigFilesObject = getConfigPropertyObject(folderObject)
    def availableConfigs = (getAvailableConfigs(folderConfigFilesObject)) ? [' '] +
            getAvailableConfigs(folderConfigFilesObject).collect { conf -> conf.name } : []

    availableConfigs
}

/* Get Folder object where devices list been stored */
def getFolderObject(folderName) {
    return Jenkins.instance.getItemByFullName(folderName, Folder)
}

/* Get Config File property from Folder object */
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

/* Get all Config files "device pools" from Config File object */
def getAvailableConfigs(folderConfigFilesObject) {
    def availableConfigs = []

    if (folderConfigFilesObject) {
        availableConfigs = folderConfigFilesObject.getConfigs()
    }

    availableConfigs
}

showAvailablePools()