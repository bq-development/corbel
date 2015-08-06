package io.corbel.iam.jwt;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.List;

import net.oauth.jsontoken.crypto.HmacSHA256Verifier;
import net.oauth.jsontoken.crypto.RsaSHA256Verifier;
import net.oauth.jsontoken.crypto.Verifier;
import net.oauth.jsontoken.discovery.VerifierProvider;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.corbel.iam.model.ClientCredential;
import io.corbel.iam.model.SignatureAlgorithm;
import io.corbel.iam.repository.ClientRepository;

/**
 * @author Alexander De Leon
 * 
 */
public class ClientVerifierProvider implements VerifierProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ClientVerifierProvider.class);

    private final ClientRepository clientRepository;

    public ClientVerifierProvider(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public List<Verifier> findVerifier(String issuer, String keyId) {
        ClientCredential credential = clientRepository.findCredentialById(issuer);
        try {
            return credential == null ? null : Arrays.<Verifier>asList(getVerifier(credential));
        } catch (InvalidKeySpecException | InvalidKeyException | DecoderException e) {
            LOG.error("Client {} contains invalid public key", new Object[] {issuer}, e);
            return null;
        }
    }

    private Verifier getVerifier(ClientCredential credential) throws InvalidKeyException, InvalidKeySpecException {
        SignatureAlgorithm signatureAlgorithm = credential.getSignatureAlgorithm();
        switch (signatureAlgorithm) {
            case HS256:
                return getHmacVerifier(credential);

            default:
                return getRsaVerifier(credential);
        }
    }

    private Verifier getRsaVerifier(ClientCredential client) throws InvalidKeySpecException {
        return new RsaSHA256Verifier(getPublicKey(client));
    }

    private Verifier getHmacVerifier(ClientCredential credential) throws InvalidKeyException {
        return new HmacSHA256Verifier(credential.getKey().getBytes());
    }

    private PublicKey getPublicKey(ClientCredential credential) throws InvalidKeySpecException {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedPublicKey(credential));
            return keyFactory.generatePublic(spec);
        } catch (NoSuchAlgorithmException e) {
            // RSA must be a valid algorithm
            return null;
        }

    }

    private byte[] decodedPublicKey(ClientCredential credential) {
        return Base64.decode(credential.getKey());
    }
}
