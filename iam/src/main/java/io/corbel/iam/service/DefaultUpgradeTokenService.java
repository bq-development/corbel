package io.corbel.iam.service;

import com.google.gson.JsonObject;
import io.corbel.iam.exception.UnauthorizedException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import io.corbel.iam.model.Entity;
import io.corbel.iam.model.Scope;
import io.corbel.iam.model.UserToken;
import io.corbel.iam.repository.UserTokenRepository;
import io.corbel.lib.token.reader.TokenReader;
import net.oauth.jsontoken.JsonToken;
import net.oauth.jsontoken.JsonTokenParser;

import java.security.SignatureException;
import java.util.*;

public class DefaultUpgradeTokenService implements UpgradeTokenService {

    private static final String SCOPE = "scope";
    private final JsonTokenParser jsonTokenParser;
    private final ScopeService scopeService;
    private final UserTokenRepository userTokenRepository;

    public DefaultUpgradeTokenService(JsonTokenParser jsonTokenParser, ScopeService scopeService, UserTokenRepository userTokenRepository) {
        this.jsonTokenParser = jsonTokenParser;
        this.scopeService = scopeService;
        this.userTokenRepository = userTokenRepository;
    }

    @Override
    public void upgradeToken(String assertion, TokenReader tokenReader, List<String> scopesToAdd) throws UnauthorizedException {
        try {
            Set<Scope> scopes = getUpgradedScopes(new HashSet<>(scopesToAdd), tokenReader);
            publishScopes(scopes, tokenReader);
            saveUserToken(tokenReader.getToken(), scopes);
        } catch (IllegalStateException e) {
            throw new UnauthorizedException(e.getMessage());
        }
    }

    @Override
    public List<String> getScopesFromTokenToUpgrade(String assertion) throws UnauthorizedException {
        try {
            JsonToken jwt = jsonTokenParser.verifyAndDeserialize(assertion);
            JsonObject payload = jwt.getPayloadAsJsonObject();
            List<String> scopes = new ArrayList<>();

            if (payload.has(SCOPE) && payload.get(SCOPE).isJsonPrimitive()) {
                String scopesToAddFromToken = payload.get(SCOPE).getAsString();
                if (!scopesToAddFromToken.isEmpty()) {
                    scopes = Arrays.asList(scopesToAddFromToken.split(" "));
                }
            }
            return scopes;
        } catch (SignatureException e) {
            throw new UnauthorizedException(e.getMessage());
        }
    }

    private void saveUserToken(String token, Set<Scope> scopes) {
        UserToken userToken = userTokenRepository.findByToken(token);
        Set<String> scopeIds = scopes.stream().map(Entity::getId).collect(Collectors.toSet());
        userToken.getScopes().addAll(scopeIds);
        userTokenRepository.save(userToken);
    }

    private void publishScopes(Set<Scope> scopes, TokenReader tokenReader) {
        scopeService.addAuthorizationRules(tokenReader.getToken(), scopes);
    }

    private Set<Scope> getUpgradedScopes(Set<String> scopesIds, TokenReader tokenReader) {
        Set<Scope> scopes = scopeService.expandScopes(scopesIds);
        return scopeService.fillScopes(scopes, tokenReader.getInfo().getUserId(), tokenReader.getInfo().getClientId(),
                tokenReader.getInfo().getDomainId());
    }
}
