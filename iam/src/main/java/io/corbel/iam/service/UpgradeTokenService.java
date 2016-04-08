package io.corbel.iam.service;

import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.lib.token.reader.TokenReader;

import java.util.List;

public interface UpgradeTokenService {

    public List<String> getScopesFromTokenToUpgrade(String assertion) throws UnauthorizedException;

    public void upgradeToken(String assertion, TokenReader tokenReader, List<String> scopesToAdd) throws UnauthorizedException;
}
