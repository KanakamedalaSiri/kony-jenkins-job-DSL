def getParameters(args) {
    return {
        choiceParameter {
            name('TEST_FRAMEWORK')
            randomName('')
            choiceType('PT_RADIO')
            description('Choose one of the Test Frameworks in which tests have been written for testing the application.')
            filterable(false)
            /* filterLength is made as a mandatory field in latest version of Jenkins. So we need to explicitly mention it and set the default value. */
            filterLength(1)
            script {
                groovyScript {
                    script {
                        script('return ["TestNG:selected","Jasmine"]')
                        sandbox(false)
                    }
                    fallbackScript {
                        script('[\'Error while getting Test Framework type\']')
                        sandbox(true)
                    }
                }
            }
        }
    }
}

