package helper

class ansi_color_helper {
    /* ANSI color coding in console logs */
    def static INFO_ANSI_PREFIX        = '\033[0;32m'  // ansi Green color code
    def static WARN_ANSI_PREFIX        = '\033[0;33m'  // ansi Yellow color code
    def static ERROR_ANSI_PREFIX       = '\033[0;31m'  // ansi Red color code
    def static ANSI_PREFIX_NO_COLOR    ='\033[0m'      // ansi no color code

    /**
     * Decorate Message with colors.
     *
     * @param message
     * @param logType
     * @param isExit
     *
     * @return colored message
     * */
    static String decorateMessage(String message, String logType = 'INFO') {
        def ANSI_PREFIX
        switch (logType) {
            case 'INFO':
                ANSI_PREFIX = INFO_ANSI_PREFIX
                break
            case 'WARN':
                ANSI_PREFIX = WARN_ANSI_PREFIX
                break
            case 'ERROR':
                ANSI_PREFIX = ERROR_ANSI_PREFIX
                break
            default:
                ANSI_PREFIX = ANSI_PREFIX_NO_COLOR
                break
        }
        return "${ANSI_PREFIX} ${message} ${ANSI_PREFIX_NO_COLOR}"
    }
}
