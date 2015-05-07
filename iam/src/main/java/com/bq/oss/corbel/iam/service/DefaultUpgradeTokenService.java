package com.bq.oss.corbel.iam.service;

import java.security.SignatureException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.oauth.jsontoken.JsonToken;
import net.oauth.jsontoken.JsonTokenParser;

import com.bq.oss.corbel.iam.exception.UnauthorizedException;
import com.bq.oss.lib.token.reader.TokenReader;
import com.google.gson.JsonObject;

public class DefaultUpgradeTokenService implements UpgradeTokenService {

    private final JsonTokenParser jsonTokenParser;
    private final ScopeService scopeService;

    public DefaultUpgradeTokenService(JsonTokenParser jsonTokenParser, ScopeService scopeService) {
        this.jsonTokenParser = jsonTokenParser;
        this.scopeService = scopeService;
    }

    @Override
    public void upgradeToken(String assertion, TokenReader tokenReader) throws UnauthorizedException {
        try {
            JsonToken jwt = jsonTokenParser.verifyAndDeserialize(assertion);
            JsonObject payload = jwt.getPayloadAsJsonObject();
            String[] scopesToAdd = new String[0];
            if (payload.has("scope") && payload.get("scope").isJsonPrimitive()) {
                String scopesToAddFromToken = payload.get("scope").getAsString();
                if (!scopesToAddFromToken.isEmpty()) {
                    scopesToAdd = scopesToAddFromToken.split(" ");
                }
            }

            publishScopes(new HashSet<>(Arrays.asList(scopesToAdd)), tokenReader);
        } catch (IllegalStateException | SignatureException e) {
            throw new UnauthorizedException(e.getMessage());
        }
    }

    private void publishScopes(Set<String> scopes, TokenReader tokenReader) {
        scopeService.addAuthorizationRules(tokenReader.getToken(), scopes, tokenReader.getInfo().getUserId(), tokenReader.getInfo()
                .getClientId());
    }
}
