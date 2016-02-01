package io.corbel.iam.jwt;

import java.security.InvalidKeyException;
import java.util.Collections;
import java.util.List;

import net.oauth.jsontoken.crypto.HmacSHA256Verifier;
import net.oauth.jsontoken.crypto.Verifier;
import net.oauth.jsontoken.discovery.VerifierProvider;

public class TokenUpgradeVerifierProvider implements VerifierProvider {

    private static final String ASSETS_ISSUER = "http://assets.bqws.io";
    private final String signerKey;

    public TokenUpgradeVerifierProvider(String signerKey) {
        super();
        this.signerKey = signerKey;
    }

    @Override
    public List<Verifier> findVerifier(String issuer, String keyId) {
        if (issuer.equals(ASSETS_ISSUER)) {
            try {
                return Collections.<Verifier>singletonList(new HmacSHA256Verifier(signerKey.getBytes()));
            } catch (InvalidKeyException e) {
                // This exception doesn't occur because the key is fixed
            }
        }
        return Collections.emptyList();
    }
}
