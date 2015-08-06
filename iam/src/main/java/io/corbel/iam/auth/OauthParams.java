package io.corbel.iam.auth;

import net.oauth.jsontoken.JsonToken;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonPrimitive;

/**
 * @author Rubén Carrasco
 */
public class OauthParams {

    private String accessToken;
    private String code;
    private String token;
    private String verifier;
    private String redirectUri;

    public static OauthParams createFromJWT(JsonToken jsonToken) {
        OauthParams params = new OauthParams().setAccessToken(getJWTAttribute(jsonToken, "oauth.access_token"))
                .setCode(getJWTAttribute(jsonToken, "oauth.code")).setToken(getJWTAttribute(jsonToken, "oauth.token"))
                .setVerifier(getJWTAttribute(jsonToken, "oauth.verifier")).setRedirectUri(getJWTAttribute(jsonToken, "oauth.redirect_uri"));

        return params.isEmpty() ? null : params;
    }

    private boolean isEmpty() {
        return accessToken == null && code == null && token == null && verifier == null && redirectUri == null;
    }

    private static String getJWTAttribute(JsonToken jsonToken, String name) {
        JsonPrimitive param = jsonToken.getParamAsPrimitive(name);
        return param != null ? param.getAsString() : null;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getCode() {
        return code;
    }

    public String getToken() {
        return token;
    }

    public String getVerifier() {
        return verifier;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public OauthParams setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public OauthParams setCode(String code) {
        this.code = code;
        return this;
    }

    public OauthParams setToken(String token) {
        this.token = token;
        return this;
    }

    public OauthParams setVerifier(String verifier) {
        this.verifier = verifier;
        return this;
    }

    public OauthParams setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

    public boolean isMissing() {
        return StringUtils.isBlank(code) && StringUtils.isBlank(accessToken)
                && (StringUtils.isBlank(token) || StringUtils.isBlank(verifier));
    }

}