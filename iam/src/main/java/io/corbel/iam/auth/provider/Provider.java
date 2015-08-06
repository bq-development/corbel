package io.corbel.iam.auth.provider;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import io.corbel.iam.auth.OauthParams;
import io.corbel.iam.exception.ExchangeOauthCodeException;
import io.corbel.iam.exception.MissingOAuthParamsException;
import io.corbel.iam.exception.OauthServerConnectionException;
import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.iam.model.Identity;

/**
 * @author Rubén Carrasco
 * 
 */
public interface Provider {

    String getAuthUrl(String assertion);

    Identity getIdentity(OauthParams params, String oAuthService, String domain) throws UnauthorizedException, MissingOAuthParamsException,
            ExchangeOauthCodeException, OauthServerConnectionException;

    void setConfiguration(Map<String, String> configuration);

    public static class UrlGenerator {
        public static String generateUrl(String url, String key, String value) {
            try {
                if (url.contains("?")) {
                    url = url + "&" + key + "=" + URLEncoder.encode(value, "UTF-8");
                } else {
                    url = url + "?" + key + "=" + URLEncoder.encode(value, "UTF-8");
                }
                return url;
            } catch (UnsupportedEncodingException e) {
                return url;
            }
        }
    }
}
