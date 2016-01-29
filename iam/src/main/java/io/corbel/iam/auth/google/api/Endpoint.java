package io.corbel.iam.auth.google.api;

public final class Endpoint {

    private Endpoint () {}

    public static final String USER_INFO = "https://www.googleapis.com/oauth2/v2/userinfo";
    public static final String AUTHORIZE = "https://accounts.google.com/o/oauth2/auth";
    public static final String TOKEN = "https://accounts.google.com/o/oauth2/token";

}
