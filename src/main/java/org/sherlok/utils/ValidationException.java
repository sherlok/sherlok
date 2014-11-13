package org.sherlok.utils;


/**
 * Whenever something is not valid. The error message should provide meaningful
 * about 1) what object type was not valid 2) what object id and 3) what exactly
 * was not valid.
 * 
 * @author renaud@apache.org
 */
@SuppressWarnings("serial")
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(Exception e) {
        super(e);
    }

    public ValidationException(String msg, Exception e) {
        super(msg, e);
    }

    public static class ValidationErrorMessage {
        String errorMessage;

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /** To display errors nicely as JSON */
    public Object toJson() {
        ValidationErrorMessage m = new ValidationErrorMessage();
        m.errorMessage = getMessage();
        return m;
    }
}
