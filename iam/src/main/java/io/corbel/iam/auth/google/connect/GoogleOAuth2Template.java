package io.corbel.iam.auth.google.connect;

import java.util.Map;

import org.springframework.http.*;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Template;
import org.springframework.util.MultiValueMap;

import io.corbel.iam.auth.google.api.Endpoint;

public class GoogleOAuth2Template extends OAuth2Template {

    public GoogleOAuth2Template(String clientId, String clientSecret) {
        super(clientId, clientSecret, Endpoint.AUTHORIZE, Endpoint.TOKEN);
        setUseParametersForClientAuthentication(true);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected AccessGrant postForAccessGrant(String accessTokenUrl, MultiValueMap<String, String> parameters) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(parameters, headers);
        ResponseEntity<Map> responseEntity = getRestTemplate().exchange(accessTokenUrl, HttpMethod.POST, requestEntity, Map.class);
        Map<String, Object> responseMap = responseEntity.getBody();
        return extractAccessGrant(responseMap);
    }

    private AccessGrant extractAccessGrant(Map<String, Object> result) {
        String accessToken = (String) result.get("access_token");
        String scope = (String) result.get("scope");
        String refreshToken = (String) result.get("refresh_token");
        Number expiresIn = (Number) result.get("expires_in");
        return createAccessGrant(accessToken, scope, refreshToken, expiresIn.longValue(), result);
    }

}
