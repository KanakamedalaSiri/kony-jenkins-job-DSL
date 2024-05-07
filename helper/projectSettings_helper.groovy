package helper

import jenkins.model.Jenkins
import com.cloudbees.hudson.plugins.folder.AbstractFolder

class projectSettings_helper {

    /**
     * Will fetch the project settings properties from the project root folder config.
     */
    def getAppFactoryProjectSettings(projectName) {
        /* As the DSL scripts are executed with the ClassLoader for the Job DSL plugin.
         * In Jenkins each plugin gets it's own ClassLoader and it can only see the Jenkins core classes and the classes of plugins that it depends on.
         * Because the Job DSL plugin does not define a dependency to the Volt MX CustomView plugin, the scripts cannot use the classes from that plugin.
         * So using the Groovy's dynamic typing and getDescriptor method as a workaround to identify and fetch the required projectSettings property.
         */
        def projSettingsProperty
        if(projectName) {
            def projSettingsDesc = Jenkins.instance.getDescriptor('com.kony.appfactory.project.settings.ProjectSettingsProperty')
            def folderObj = Jenkins.get().getItemByFullName(projectName, AbstractFolder.class)
            def FolderProperties = folderObj?.getProperties()
            FolderProperties?.each { property ->
                if (property.getClass().toString() == projSettingsDesc.getKlass().toString()) {
                    projSettingsProperty = property
                }
            }
        }
        return projSettingsProperty?.getProjectSettings()
    }

    /**
     * Will retrieve the corresponding triggered service source control settings
     */
    def getScmProjectSettings(projectName, triggeredServiceName) {
        def scmSettingsMap
        def projectSettings = getAppFactoryProjectSettings(projectName)
        if (projectSettings) {
            if (triggeredServiceName == 'Iris') {
                scmSettingsMap = projectSettings.getVisualizerSettings()?.getSourceControl()?.toMap()
            } else if (triggeredServiceName == 'Foundry') {
                scmSettingsMap = projectSettings.getFabricSettings()?.getSourceControl()?.toMap()
            } else if (triggeredServiceName == 'Microservices') {
                scmSettingsMap = projectSettings.getMicroserviceSettings()?.getSourceControl()?.toMap()
            }
        }
        return scmSettingsMap
    }

    /**
     * Check new Project settings available (>v9.1) for a given project.
     * @param projectType
     */
    def getProjectVersion(projectName) {
        /* ProjectSettings might have exist in V9.1 where there is no DSL version tag attached to the settings object
         * So, returning 'null' incase for 9.1 and 9.1 below versions.
         */
        return (getAppFactoryProjectSettings(projectName)?.getProjectDSLVersion())
    }
}
