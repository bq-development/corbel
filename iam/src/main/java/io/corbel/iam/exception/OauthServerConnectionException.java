package io.corbel.iam.exception;

public class OauthServerConnectionException extends Exception {

    private final String oAuthService;
    private final String message;

    public OauthServerConnectionException(String oAuthService, String message) {
        this.message = message;
        this.oAuthService = oAuthService;
    }

    public String getOAuthService() {
        return oAuthService;
    }

    public String getMessage() {
        return message;
    }

}
