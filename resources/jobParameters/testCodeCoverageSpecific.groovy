def getParameters(args) {
    return {
        booleanParam('ENABLE_CODE_COVERAGE', false, "Check this box if you want <em>App Factory</em> to run <a href='https://en.wikipedia.org/wiki/Code_coverage'>code coverage</a> analysis. The result will be a measure of how much of your project's code is exercised by your test scripts. It is <i>strongly</i> recommended to always analyse code coverage when running test scripts, in order to help detect test omissions and to better understand which areas in the project may require additional test scripts.")
    }
}
