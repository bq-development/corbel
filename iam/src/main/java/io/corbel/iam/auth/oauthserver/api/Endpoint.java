package io.corbel.iam.auth.oauthserver.api;

public final class Endpoint {

    private Endpoint () {}

    public static final String USERS = "/v1.0/user/me";
    public static final String AUTHORIZE = "/v1.0/oauth/authorize";
    public static final String TOKEN = "/v1.0/oauth/token";
}
