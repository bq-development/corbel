package io.corbel.iam.service;

import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.lib.token.reader.TokenReader;

import java.util.List;

public interface UpgradeTokenService {

    List<String> getScopesFromTokenToUpgrade(String assertion) throws UnauthorizedException;

    void upgradeToken(String assertion, TokenReader tokenReader, List<String> scopesToAdd) throws UnauthorizedException;
}
