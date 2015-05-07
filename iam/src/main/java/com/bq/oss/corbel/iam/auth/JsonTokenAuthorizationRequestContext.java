package com.bq.oss.corbel.iam.auth;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import net.oauth.jsontoken.JsonToken;

import com.bq.oss.corbel.iam.model.Client;
import com.bq.oss.corbel.iam.model.Domain;
import com.bq.oss.corbel.iam.model.User;
import com.bq.oss.corbel.iam.repository.ClientRepository;
import com.bq.oss.corbel.iam.repository.DomainRepository;
import com.bq.oss.corbel.iam.repository.UserRepository;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Alexander De Leon
 * 
 */
public class JsonTokenAuthorizationRequestContext implements AuthorizationRequestContext {

    private static final String SCOPE = "scope";
    private static final String OAUTH_SERVICE = "oauth.service";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String VERSION = "version";
    private static final String PRINCIPAL = "prn";
    private static final String REQUEST_DOMAIN = "request_domain";
    private static final String DEVICE_ID = "device_id";

    private final JsonToken jsonToken;
    private final ClientRepository clientRepository;
    private final DomainRepository domainRepository;
    private final UserRepository userRepository;
    private Client client;
    private Domain domain;
    private Domain requestedDomain;
    private User user;
    private OauthParams oauthParams;
    private BasicParams basicParams;
    private Boolean hasPrincipal;
    private String principalId;

    public JsonTokenAuthorizationRequestContext(ClientRepository clientRepository, DomainRepository domainRepository,
            UserRepository userRepository, JsonToken jsonToken) {
        this.clientRepository = clientRepository;
        this.domainRepository = domainRepository;
        this.userRepository = userRepository;
        this.jsonToken = jsonToken;
    }

    @Override
    public String getIssuerClientId() {
        return jsonToken.getIssuer();
    }

    @Override
    public Client getIssuerClient() {
        if (client == null) {
            client = clientRepository.findOne(jsonToken.getIssuer());
        }
        return client;
    }

    @Override
    public Domain getIssuerClientDomain() {
        if (domain == null) {
            domain = domainRepository.findOne(getIssuerClient().getDomain());
        }
        return domain;
    }

    @Override
    public boolean isCrossDomain() {
        return !getRequestedDomain().equals(getIssuerClientDomain());
    }

    @Override
    public User getPrincipal() {
        return hasPrincipal() ? getPrincipal(getPrincipalId()) : null;
    }

    @Override
    public User getPrincipal(String principalId) {
        if (user == null) {
            Client issuerClient = getIssuerClient();
            user = userRepository.findByUsernameAndDomain(principalId, issuerClient.getDomain());
        }
        return user;
    }

    @Override
    public String getPrincipalId() {
        if (principalId == null) {
            principalId = hasPrincipal() ? jsonToken.getPayloadAsJsonObject().get(PRINCIPAL).getAsString() : null;
        }
        return principalId;
    }

    @Override
    public void setPrincipalId(String principalId) {
        hasPrincipal = null != principalId;
        this.principalId = principalId;
    }

    @Override
    public Set<String> getRequestedScopes() {
        JsonObject payload = jsonToken.getPayloadAsJsonObject();
        if (payload.has(SCOPE)) {
            String requestedScopes = payload.get(SCOPE).getAsString();
            if (requestedScopes.length() > 0) {
                return new HashSet<>(Arrays.asList(requestedScopes.split(" ")));
            }
        }
        return new HashSet<>();
    }

    @Override
    public boolean hasPrincipal() {
        if (hasPrincipal == null) {
            JsonObject payload = jsonToken.getPayloadAsJsonObject();
            hasPrincipal = payload.has(PRINCIPAL) && payload.get(PRINCIPAL).isJsonPrimitive();
        }
        return hasPrincipal;
    }

    @Override
    public Long getAuthorizationExpiration() {
        return jsonToken.getExpiration().getMillis();
    }

    @Override
    public boolean isOAuth() {
        return jsonToken.getPayloadAsJsonObject().has(OAUTH_SERVICE);
    }

    @Override
    public String getOAuthService() {
        return isOAuth() ? jsonToken.getPayloadAsJsonObject().get(OAUTH_SERVICE).getAsString() : null;
    }

    @Override
    public OauthParams getOauthParams() {
        return (oauthParams == null) ? (oauthParams = OauthParams.createFromJWT(jsonToken)) : oauthParams;
    }

    @Override
    public BasicParams getBasicParams() {
        return (basicParams == null) ? (basicParams = BasicParams.createFromJWT(jsonToken)) : basicParams;
    }

    @Override
    public String getDeviceId() {
        return Optional.ofNullable(jsonToken.getPayloadAsJsonObject().get(DEVICE_ID)).map(JsonElement::getAsString).orElseGet(() -> null);
    }

    @Override
    public boolean hasRefreshToken() {
        return jsonToken.getPayloadAsJsonObject().has(REFRESH_TOKEN);
    }

    @Override
    public String getRefreshToken() {
        return hasRefreshToken() ? jsonToken.getPayloadAsJsonObject().get(REFRESH_TOKEN).getAsString() : null;
    }

    @Override
    public boolean hasVersion() {
        return jsonToken.getPayloadAsJsonObject().has(VERSION);
    }

    @Override
    public String getVersion() {
        return hasVersion() ? jsonToken.getPayloadAsJsonObject().get(VERSION).getAsString() : null;
    }

    @Override
    public boolean isBasic() {
        return jsonToken.getPayloadAsJsonObject().has(BasicParams.BASIC_AUTH_USERNAME)
                && jsonToken.getPayloadAsJsonObject().has(BasicParams.BASIC_AUTH_PASSWORD);
    }

    @Override
    public Domain getRequestedDomain() {
        if (requestedDomain == null) {
            requestedDomain = jsonToken.getPayloadAsJsonObject().has(REQUEST_DOMAIN) ? domainRepository.findOne(jsonToken
                    .getPayloadAsJsonObject().get(REQUEST_DOMAIN).getAsString()) : getIssuerClientDomain();
        }
        return requestedDomain;
    }
}
