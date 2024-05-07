package exceptions

import javaposse.jobdsl.dsl.DslException
import helper.ansi_color_helper

class AppfactoryAbortException extends DslException {

    public AppfactoryAbortException(String message){
        super(ansi_color_helper.decorateMessage(message, 'ERROR'));
    }
}
