package io.corbel.notifications.model;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import java.util.Map;

public class Domain {

    @Id
    private String id;

    @NotEmpty
    private Map<String, String> properties;

    @NotNull
    private Map<String, String> templates;

    private Boolean productionEnvironment;

    private String appleNotificationsCertificate;

    private String appleNotificationsPassword;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Map<String, String> getTemplates() {
        return templates;
    }

    public void setTemplates(Map<String, String> templates) {
        this.templates = templates;
    }

    public Boolean isProductionEnvironment() {
        return productionEnvironment;
    }

    public void setProductionEnvironment(Boolean productionEnvironment) {
        this.productionEnvironment = productionEnvironment;
    }

    public String getAppleNotificationsCertificate() {
        return appleNotificationsCertificate;
    }

    public void setAppleNotificationsCertificate(String appleNotificationsCertificate) {
        this.appleNotificationsCertificate = appleNotificationsCertificate;
    }

    public String getAppleNotificationsPassword() {
        return appleNotificationsPassword;
    }

    public void setAppleNotificationsPassword(String appleNotificationsPassword) {
        this.appleNotificationsPassword = appleNotificationsPassword;
    }

    public void updateDomain(Domain domain) {
        if(domain.getProperties() != null && !domain.getProperties().isEmpty()) {
            setProperties(domain.getProperties());
        }
        if(domain.getTemplates()!= null && !domain.getTemplates().isEmpty()) {
            setTemplates(domain.getTemplates());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Domain domain = (Domain) o;

        if (id != null ? !id.equals(domain.id) : domain.id != null) return false;
        if (properties != null ? !properties.equals(domain.properties) : domain.properties != null) return false;
        if (templates != null ? !templates.equals(domain.templates) : domain.templates != null) return false;
        if (appleNotificationsCertificate != null ? !appleNotificationsCertificate.equals(domain.appleNotificationsCertificate) : domain.appleNotificationsCertificate != null)
            return false;
        return appleNotificationsPassword != null ? appleNotificationsPassword.equals(domain.appleNotificationsPassword) : domain.appleNotificationsPassword == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        result = 31 * result + (templates != null ? templates.hashCode() : 0);
        result = 31 * result + (appleNotificationsCertificate != null ? appleNotificationsCertificate.hashCode() : 0);
        result = 31 * result + (appleNotificationsPassword != null ? appleNotificationsPassword.hashCode() : 0);
        return result;
    }
}