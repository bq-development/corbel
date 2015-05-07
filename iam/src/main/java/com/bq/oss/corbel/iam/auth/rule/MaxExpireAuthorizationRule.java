package com.bq.oss.corbel.iam.auth.rule;

import com.bq.oss.corbel.iam.auth.AuthorizationRequestContext;
import com.bq.oss.corbel.iam.auth.AuthorizationRule;
import com.bq.oss.corbel.iam.exception.UnauthorizedException;

/**
 * Ensures that the requested expiration time does not exceeds the maximum allowed expiration.
 * 
 * @author Alexander De Leon
 * 
 */
public class MaxExpireAuthorizationRule implements AuthorizationRule {

    private final int maxExpirationInMillis;

    public MaxExpireAuthorizationRule(int maxExpirationInMillis) {
        this.maxExpirationInMillis = maxExpirationInMillis;
    }

    @Override
    public void process(AuthorizationRequestContext context) throws UnauthorizedException {
        long currentTime = System.currentTimeMillis();
        if (context.getAuthorizationExpiration() > currentTime + maxExpirationInMillis) {
            throw new UnauthorizedException("Authorization request exceds maximum expiration. Maximum is " + maxExpirationInMillis
                    + " milliseconds since its issued time");
        }
    }
}
