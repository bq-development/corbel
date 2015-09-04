package io.corbel.oauth.model;

import org.springframework.data.annotation.Id;

/**
 * @author Rubén Carrasco
 * 
 */
public class Client {

    @Id private String name;
    private String key;
    private String domain;
    private String redirectRegexp;

    private String resetUrl;
    private String resetNotificationId;

    private Boolean validationEnabled;
    private String validationUrl;
    private String validationNotificationId;

    private String changePasswordNotificationId;

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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getRedirectRegexp() {
        return redirectRegexp;
    }

    public void setRedirectRegexp(String redirectRegexp) {
        this.redirectRegexp = redirectRegexp;
    }

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

    public Boolean isValidationEnabled() {
        return validationEnabled;
    }

    public void setValidationEnabled(boolean validationEnabled) {
        this.validationEnabled = validationEnabled;
    }

    public String getValidationUrl() {
        return validationUrl;
    }

    public void setValidationUrl(String validationUrl) {
        this.validationUrl = validationUrl;
    }

    public String getValidationNotificationId() {
        return validationNotificationId;
    }

    public void setValidationNotificationId(String validationNotificationId) {
        this.validationNotificationId = validationNotificationId;
    }

    public String getChangePasswordNotificationId() {
        return changePasswordNotificationId;
    }

    public void setChangePasswordNotificationId(String changePasswordNotificationId) {
        this.changePasswordNotificationId = changePasswordNotificationId;
    }
}
