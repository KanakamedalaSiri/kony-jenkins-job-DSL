/*
    Code in this file is just a POC for renaming existing project, and has not been tested yet.
    It doesn't solves problem with stored per job environment variables. But next step for improvements could be moving
    all configurations (env vars and maybe other config settings) to
    Config File Plugin property under root folder project,
    to be able to backup them during the project structure updates and store them in one place, in pipeline scripts all
    values from Config File properties could be easily fetched with
    configFileProvider([configFile('ID')]) {} step.
 */
import hudson.model.Executor

currentBuild = Executor.currentExecutor().currentExecutable
projectName = (PROJECT_NAME)?.capitalize()
newProjectName = (NEW_PROJECT_NAME)?.capitalize()
project = isProjectExists(projectName)

if (newProjectName) {
    if (projectName != newProjectName) {
        project.renameTo(newProjectName)
        setBuildDescription(currentBuild, "<h3>Renamed project: $projectName -> $newProjectName</h3>")
    }
}