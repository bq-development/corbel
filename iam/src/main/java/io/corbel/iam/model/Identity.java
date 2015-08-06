package io.corbel.iam.model;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Rubén Carrasco
 * 
 */
public class Identity extends Entity {

    private String domain;
    private String userId;
    @NotEmpty private String oauthService;
    @NotEmpty private String oauthId;

    public Identity() {}

    /**
     * Copy constructor
     */
    Identity(Identity other) {
        this.domain = other.domain;
        this.oauthId = other.oauthId;
        this.oauthService = other.oauthService;
        this.userId = other.userId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOauthService() {
        return oauthService;
    }

    public void setOauthService(String oauthService) {
        this.oauthService = oauthService;
    }

    public String getOauthId() {
        return oauthId;
    }

    public void setOauthId(String oauthId) {
        this.oauthId = oauthId;
    }

    @Override
    public String toString() {
        return "Identity [domain=" + domain + ", userId=" + userId + ", oauthService=" + oauthService + ", oauthId=" + oauthId + "]";
    }

}
