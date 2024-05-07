def getParameters(args) {
    return {
        parameterSeparatorDefinition {
            name('FLYWAY_HEADER')
            separatorStyle('')
            sectionHeader(args.flywaySpecificSeparatorDesc)
            sectionHeaderStyle('')
        }
        stringParam('LOCATION', '', 'Comma-separated list of locations(relative to root of your repository) to scan recursively for migrations.\n' +
                'Locations point to a directory in the repository, may only contain SQL migrations and are only scanned recursively down non-hidden directories.\n' +
                'Unprefixed locations will be assumed as a relative filesystem path from \n' +
                'Wildcards can be used to reduce duplication of location paths. (e.g. migrations/*/oracle) Supported wildcards:\n' +
                ' ** : Matches any 0 or more directories\n' +
                ' * : Matches any 0 or more non-separator characters\n' +
                ' ? : Matches any 1 non-separator character\n')
        validatingStringParameterDefinition {
            name('FLYWAY_COMMAND')
            defaultValue('')
            regex('(?i)^\\s*(info|migrate|validate|clean|baseline|repair)\\s*,?(\\s*(info|migrate|validate|clean|baseline|repair)\\s*,?)*$') // strings like "info", "info, migrate" are valid and "info, migrato" would be invalid.
            failedValidationMessage('Please ensure you\'ve entered a valid command.')
            description("Command to run, i.e. 'migrate', 'info', 'validate', etc. Multiple commands can be provided as comma-separated values which will be executed in the order as provided. For more information on flyway commands visit, <a href='https://flywaydb.org/documentation/usage/commandline' target=\"_blank\"> Flyway Documentation.</a>")
        }
        credentialsParam('DB_CREDENTIALS') {
            type('com.kony.AppFactory.Jenkins.credentials.impl.DatabaseCredentialsImpl')
            required()
            defaultValue('')
            description('Database configuration credential containing details like database URL, username and password for the database on which migrations are needed to be run.')
        }
        stringParam('OPTIONS', '', "A string containing command line options to be used with flyway command. For example: -schemas=sample, for more information on options supported by flyway visit, For more information on flyway commands visit, <a href='https://flywaydb.org/documentation/configuration/parameters' target=\"_blank\"> Flyway Documentation.</a>")
    }
}