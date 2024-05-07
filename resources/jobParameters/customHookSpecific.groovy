def getParameters(args) {
    return {
        stringParam('HOOK_NAME',args.HOOK_NAME, "Name of your CustomHook")
        choiceParam('BUILD_STEP', getDefaultBuildStep(args), 'Select one of the following phases where you want to inject CustomHook.')
        stringParam('HOOK_CHANNEL', getDefaultHookChannel(args), 'Select one of the following hook channel step where you want to inject CustomHook.')
        choiceParam('BUILD_ACTION', getDefaultBuildAction(args), 'Type of hook you want to run.')
        stringParam('SCRIPT_ARGUMENTS',args.SCRIPT_ARGUMENTS, "Specify targets, goal or arguments for Hook. These args will be passed to CustomHook script. <br>For Ant - pass args like -DProjectName=ABC <br>For Maven - Specify goals like clean install <br>")
        stringParam("BUILD_SCRIPT",args.BUILD_SCRIPT, description = 'Upload CustomHook project zip. It must contain Ant or Maven script at root location.\n Currently only Ant and Maven hooks are supported by Volt MX AppFactory.\n')
    }
}

def getDefaultBuildStep(args){
    if(args.BUILD_STEP == "PRE_BUILD_STEP"){
        return ['PRE_BUILD_STEP', 'POST_BUILD_STEP', 'POST_TEST_STEP']
    }
    else if(args.BUILD_STEP == "POST_BUILD_STEP"){
        return ['POST_BUILD_STEP', 'PRE_BUILD_STEP', 'POST_TEST_STEP']
    }
    else if(args.BUILD_STEP == "POST_TEST_STEP"){
        return ['POST_TEST_STEP', 'PRE_BUILD_STEP', 'POST_BUILD_STEP']
    }
    else if(args.BUILD_STEP == "POST_DEPLOY_STEP"){
        return ['POST_DEPLOY_STEP', 'PRE_BUILD_STEP', 'POST_BUILD_STEP']
    }
    else{
        return []
    }
}

def getDefaultHookChannel(args){
    return args.HOOK_CHANNEL
}

def getDefaultBuildAction(args){
    if(args.BUILD_ACTION == "Execute Ant"){
        return ['Execute Ant', 'Execute Maven']
    }
    else if(args.BUILD_ACTION == "Execute Maven"){
        return ['Execute Maven', 'Execute Ant']
    }
    else {
        return []
    }
}