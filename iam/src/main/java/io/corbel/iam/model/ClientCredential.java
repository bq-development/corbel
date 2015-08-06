package io.corbel.iam.model;

import java.util.Objects;

/**
 * @author Alexander De Leon
 * 
 */
public class ClientCredential {

    private String key;
    private SignatureAlgorithm signatureAlgorithm;

    public ClientCredential() {
        // empty
    }

    public ClientCredential(SignatureAlgorithm signatureAlgorithm, String key) {
        this.key = key;
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public SignatureAlgorithm getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(SignatureAlgorithm signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    @Override
    public int hashCode() {
        return Objects.hash(signatureAlgorithm, key);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ClientCredential)) {
            return false;
        }
        ClientCredential that = (ClientCredential) obj;
        return Objects.equals(this.signatureAlgorithm, that.signatureAlgorithm) && Objects.equals(this.key, that.key);
    }

}
