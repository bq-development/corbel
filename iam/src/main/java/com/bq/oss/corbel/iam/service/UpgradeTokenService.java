package com.bq.oss.corbel.iam.service;

import com.bq.oss.corbel.iam.exception.UnauthorizedException;
import io.corbel.lib.token.reader.TokenReader;

public interface UpgradeTokenService {

    public void upgradeToken(String assertion, TokenReader tokenReader) throws UnauthorizedException;
}
