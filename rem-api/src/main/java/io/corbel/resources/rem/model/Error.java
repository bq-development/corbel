package io.corbel.resources.rem.model;

/**
 * @author Francisco Sanchez
 */
public class Error {
    private final String error;
    private final String errorDescription;

    public Error(String error, String errorDescription) {
        this.error = error;
        this.errorDescription = errorDescription;
    }

    public String getError() {
        return error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

}
