package util

/**
 * This helper class help us in reducing replication of code in dsl scripts
 *
 * */
class build_utils {

    /**
     * Function which allows to run shell command
     *
     * @params command : shell command
     * @return map : successOutput, errorOutput and exitCode
     */
    static executeProcess(command) {
        /* Initialize StringBuilds to capture STD Output  */
        def successOutput = new StringBuilder()
        def errorOutput = new StringBuilder()
        def exitValue = -1;

        try {
            /**
             * Execute process in separate thread
             * @documentation: http://docs.groovy-lang.org/2.3.5/html/api/org/codehaus/groovy/runtime/ProcessGroovyMethods.html
             */
            def process = command.execute()
            process.consumeProcessOutput(successOutput, errorOutput)

            /* Wait for process to finish */
            process.waitFor()

            exitValue = process.exitValue()

            [successOutput: successOutput, errorOutput: errorOutput, exitValue: exitValue]
        }
        catch (Exception e) {
            [successOutput: successOutput, errorOutput: "Error occurred while running command \n ${e.getMessage()}", exitValue: exitValue]
        }
    }
}