package io.corbel.iam.model;


import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * A {@link Client} is an agent that uses Corbel services;
 * 
 * @author Alexander De Leon
 * 
 */
public class Client extends TraceableEntity implements HasScopes {

    private String domain;
    @NotEmpty private String name;
    private String key;
    private String version;
    private SignatureAlgorithm signatureAlgorithm;
    private Set<String> scopes = new HashSet<>();
    private Boolean clientSideAuthentication;
    private String resetUrl;
    private String resetNotificationId;

    public String getResetUrl() {
        return resetUrl;
    }

    public void setResetUrl(String resetUrl) {
        this.resetUrl = resetUrl;
    }

    public String getResetNotificationId() {
        return resetNotificationId;
    }

    public void setResetNotificationId(String resetNotificationId) {
        this.resetNotificationId = resetNotificationId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public SignatureAlgorithm getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(SignatureAlgorithm signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public Boolean getClientSideAuthentication() {
        return clientSideAuthentication;
    }

    public void setClientSideAuthentication(Boolean clientSideAuthentication) {
        this.clientSideAuthentication = clientSideAuthentication;
    }

    @Override
    public Set<String> getScopes() {
        return scopes;
    }

    @Override
    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    @Override
    public boolean addScope(String scope) {
        return scopes.add(scope);
    }

    @Override
    public boolean removeScope(String scope) {
        return scopes.remove(scope);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Client))
            return false;
        if (!super.equals(o))
            return false;

        Client client = (Client) o;

        if (clientSideAuthentication != null ? !clientSideAuthentication.equals(client.clientSideAuthentication)
                : client.clientSideAuthentication != null)
            return false;
        if (domain != null ? !domain.equals(client.domain) : client.domain != null)
            return false;
        if (key != null ? !key.equals(client.key) : client.key != null)
            return false;
        if (name != null ? !name.equals(client.name) : client.name != null)
            return false;
        if (resetNotificationId != null ? !resetNotificationId.equals(client.resetNotificationId) : client.resetNotificationId != null)
            return false;
        if (resetUrl != null ? !resetUrl.equals(client.resetUrl) : client.resetUrl != null)
            return false;
        if (scopes != null ? !scopes.equals(client.scopes) : client.scopes != null)
            return false;
        if (signatureAlgorithm != client.signatureAlgorithm)
            return false;
        if (version != null ? !version.equals(client.version) : client.version != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (domain != null ? domain.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (signatureAlgorithm != null ? signatureAlgorithm.hashCode() : 0);
        result = 31 * result + (scopes != null ? scopes.hashCode() : 0);
        result = 31 * result + (clientSideAuthentication != null ? clientSideAuthentication.hashCode() : 0);
        result = 31 * result + (resetUrl != null ? resetUrl.hashCode() : 0);
        result = 31 * result + (resetNotificationId != null ? resetNotificationId.hashCode() : 0);
        return result;
    }
}
