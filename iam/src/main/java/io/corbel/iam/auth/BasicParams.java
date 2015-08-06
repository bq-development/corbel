package io.corbel.iam.auth;

import net.oauth.jsontoken.JsonToken;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonPrimitive;

public class BasicParams {
    private final String username;
    private final String password;
    public static final String BASIC_AUTH_USERNAME = "basic_auth.username";
    public static final String BASIC_AUTH_PASSWORD = "basic_auth.password";

    public BasicParams(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static BasicParams createFromJWT(JsonToken jsonToken) {
        BasicParams params = new BasicParams(getJWTAttribute(jsonToken, BASIC_AUTH_USERNAME), getJWTAttribute(jsonToken,
                BASIC_AUTH_PASSWORD));
        return params.isEmpty() ? null : params;
    }

    private static String getJWTAttribute(JsonToken jsonToken, String name) {
        JsonPrimitive param = jsonToken.getParamAsPrimitive(name);
        return param != null ? param.getAsString() : null;
    }

    private boolean isEmpty() {
        return username == null && password == null;
    }

    public boolean isMissing() {
        return StringUtils.isBlank(username) || StringUtils.isBlank(password);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
