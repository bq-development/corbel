package io.corbel.iam.service;

import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.lib.token.reader.TokenReader;

import java.util.Set;

public interface UpgradeTokenService {

    Set<String> getScopesFromTokenToUpgrade(String assertion) throws UnauthorizedException;

    void upgradeToken(String assertion, TokenReader tokenReader, Set<String> scopes) throws UnauthorizedException;
}
