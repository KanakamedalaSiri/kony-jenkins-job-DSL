def getParameters(args) {
    return {
        parameterSeparatorDefinition {
            name('PROJECT_SOURCE_CODE_HEADER')
            separatorStyle('')
            sectionHeader(args.microServicesScmSeparatorDesc)
            sectionHeaderStyle('')
        }
        stringParam('SCM_BRANCH', '', args.microServicesProjectSourceCodeBranchParamDesc)
        parameterSeparatorDefinition {
            name('MICROSERVICE_HEADER')
            separatorStyle('')
            sectionHeader(args.microServicesSpecificSeparatorDesc)
            sectionHeaderStyle('')
        }
        validatingStringParameterDefinition {
            name('MICROSERVICE_BASE_URL')
            defaultValue('')
            regex('^http(s)?:(//)(.*/ms-genericconfig-api/api/)(v(\\d+\\.)(\\d+\\.)(\\*|\\d+))(/)?$')
            failedValidationMessage("Invalid Microservice Base URL !! The Microservice base url must be in the form of given " +
                    "format http(s)://<hostname>:<port>/ms-genericconfig-api/api/<api-version>/ ")
            description('The base url of generic config microservice API. ' +
                     'For Example : http://localhost:7366/ms-genericconfig-api/api/v2.0.0/, http://54.163.100.180:7006/ms-genericconfig-api/api/v1.0.0 etc. ' +
                     'Note: Make sure the generic microservice link is accessible from AppFactory.')
        }
        booleanParam('DEPLOY_JOLT_FILES', false, args.microServicesJoltFilesParamDesc)
        validatingStringParameterDefinition {
            name('GROUP_ID')
            defaultValue('')
            regex('^\\s*|(.*\\.jolt)$')
            failedValidationMessage("Invalid Group ID!! The Group ID must be in the form of given format <temn.msf.name>.jolt")
            description('The ID of the group which specifies the type of microservice configuration you want to deploy.' +
                    ' For Example : PaymentOrder.jolt, Party.jolt')
        }
        stringParam('JOLT_FILES_DIR', 'configurations/extensions', 'Use this field to specify the path to the parent directory (relative to root of your project) containing the Jolt configuration files that you want to deploy. ' +
                'This directory is referenced to find all Jolt extension files mentioned in the next build parameter JOLT_FILES_LIST. If no file list provided in the JOLT_FILES_LIST build parameter, all json extension files present in this directory are deployed.' +
                '<br>Default value is "configurations/extensions", it is the path relative to the root of your project, which is containing the jolt files.')
        validatingStringParameterDefinition {
            name('JOLT_FILES_LIST')
            defaultValue("")
            regex('^[^*/]*$')
            failedValidationMessage("This parameter does not accept sub-directories, file paths and Wildcard entries.")
            description('Comma-separated list of Jolt file names (relative to JOLT_FILES_DIR) to be deployed recursively for configuring your microservice. ' +
                    'For Example : PAYMENT_ORDEREvent_PaymentOrderExtn.json, ARRANGEMENTEvent_ArrangementExtn.json etc.,' +
                    'Note: Ignore this filed to deploy all json extension files from the directory mentioned in the above JOLT_FILES_DIR build parameter. ' +
                    'Sub-directories or file paths are not allowed. For Example: folder2/, folder1/ARRANGEMENTEvent_ArrangementExtn.json etc.,')
        }
        booleanParam('DEPLOY_POLICY_FILES', false, args.microServicesPolicyFilesParamDesc)
        stringParam('POLICY_FILES_DIR', 'configurations/policy', 'Use this field to specify the path to the parent directory (relative to root of your project) containing the Policy configuration files that you want to deploy. ' +
                'This directory is referenced to find all policy extension files mentioned in the next build parameter POLICY_FILES_LIST. If no file list provided in the POLICY_FILES_LIST build parameter, all xml extension files present in this directory are deployed.' +
                '<br>Default value is "configurations/policy", it is the path relative to the root of your project, which is containing the policy files.')
        validatingStringParameterDefinition {
            name('POLICY_FILES_LIST')
            defaultValue("")
            regex('^[^*/]*$')
            failedValidationMessage("This parameter does not accept sub-directories, file paths and Wildcard entries.")
            description('Comma-separated list of Policy file names (relative to POLICY_FILES_DIR) to be deployed recursively for configuring your microservice.' +
                    'For Example : holdings-pdp-config.xml, party-pdf-config.xml etc.,' +
                    'Note: Ignore this filed to deploy all xml extension files from the directory mentioned in the above POLICY_FILES_DIR build parameter.' +
                    'Sub-directories or file paths are not allowed. For Example: folder2/, folder1/holdings-pdp-config.xml etc.,')
        }
    }
}