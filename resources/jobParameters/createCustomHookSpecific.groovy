def getParameters(args) {
    return {
        validatingStringParameterDefinition {
            name('HOOK_NAME')
            defaultValue('')
            regex('^[a-zA-Z][A-Za-z0-9]{3,16}$')
            failedValidationMessage('Please enter valid hook name.')
            description('The name of your CustomHook must start with a letter, only contain letters and numbers and should be between 4 and 17 characters.')
        }

        choiceParameter {
            name('BUILD_STEP')
            randomName('')
            choiceType('PT_SINGLE_SELECT')
            description('Select one of the following phases where you want to inject CustomHook.')
            filterable(false)
            /* filterLength is made as a mandatory field in latest version of Jenkins. So we need to explicitly mention it and set the default value. */
            filterLength(1)
            script {
                groovyScript {
                    script {
                        script('return ["PRE_BUILD_STEP","POST_BUILD_STEP","POST_TEST_STEP"]')
                        sandbox(false)
                    }
                    fallbackScript {
                        script('[\'Error while getting Build Stages list\']')
                        sandbox(true)
                    }
                }
            }
        }

        cascadeChoiceParameter {
            name('HOOK_CHANNEL')
            randomName('')
            choiceType('PT_SINGLE_SELECT')
            description('Select one of the following hook channel where you want to inject CustomHook.')
            filterable(false)
            /* filterLength is made as a mandatory field in latest version of Jenkins. So we need to explicitly mention it and set the default value. */
            filterLength(1)
            script {
                groovyScript {
                    script {
                        script(getReactiveChoiceScript())
                        sandbox(false)
                    }
                    fallbackScript {
                        script('[\'Error while getting channel pipeline build steps list\']')
                        sandbox(true)
                    }
                }
            }
            referencedParameters('BUILD_STEP')

        }

        choiceParam('BUILD_ACTION', ['Execute Ant','Execute Maven'], 'Type of hook you want to run.\n' +
                '<br>')
        fileParam('HOOK_ARCHIVE_FILE', '\n' +
                'Upload CustomHook project zip. It must contain Ant or Maven script at root location.\n' +
                '\n' +
                'Currently only Ant and Maven hooks are supported by Volt MX AppFactory. \n' +
                '<br>')
        stringParam('SCRIPT_ARGUMENTS','', "Specify targets, goal or arguments for Hook. These args will be passed to CustomHook script. <br>For Ant - pass args like -DProjectName=ABC <br>For Maven - Specify goals like clean install <br>")
        booleanParam('PROPAGATE_BUILD_STATUS', defaultValue = false, 'Fail the entire build if the hook execution fails.')
    }
}

def getReactiveChoiceScript(){
    return '''
    if (BUILD_STEP.equals("PRE_BUILD_STEP")){
        return ["ALL","ANDROID_UNIVERSAL_STAGE",
        "ANDROID_MOBILE_STAGE",
        "ANDROID_TABLET_STAGE",
        "IOS_UNIVERSAL_STAGE",
        "IOS_MOBILE_STAGE",
        "IOS_TABLET_STAGE",
        "IOS_UNIVERSAL_IPA_STAGE",
        "IOS_MOBILE_IPA_STAGE",
        "IOS_TABLET_IPA_STAGE",
        "RESPONSIVE_WEB_STAGE"
        ]
    }
    else if(BUILD_STEP.equals("POST_BUILD_STEP")){
        return ["ALL","ANDROID_UNIVERSAL_STAGE",
        "ANDROID_MOBILE_STAGE",
        "ANDROID_TABLET_STAGE",
        "IOS_UNIVERSAL_STAGE",
        "IOS_MOBILE_STAGE",
        "IOS_TABLET_STAGE",
        "RESPONSIVE_WEB_STAGE"
        ]
    }
    else {
        return ["ALL", "ANDROID_UNIVERSAL_STAGE", "ANDROID_MOBILE_STAGE", "ANDROID_TABLET_STAGE", "IOS_UNIVERSAL_STAGE", "IOS_MOBILE_STAGE", "IOS_TABLET_STAGE", "RESPONSIVE_WEB_STAGE"]
    }
    '''
}

