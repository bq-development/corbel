package io.corbel.oauth.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.corbel.lib.ws.digest.DigesterFactory;

/**
 * @author Alberto J. Rubio
 */
public class User {

    @Id private String id;

    private String domain;

    @Pattern(regexp = "^(?!.*\\|).*$") @NotEmpty private String username;

    @NotEmpty private String password;


    @NotEmpty @Email private String email;

    private String salt;

    private Boolean emailValidated;

    private Role role;

    private Map<String, Object> properties = new HashMap<>();

    private String avatarUri;

    public Boolean getEmailValidated() {
        return emailValidated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password != null) {
            this.salt = DigesterFactory.generateSalt();
            password = DigesterFactory.md5(salt).digest(password);
        }
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean isEmailValidated() {
        return emailValidated;
    }

    public void setEmailValidated(Boolean emailValidated) {
        this.emailValidated = emailValidated;
    }

    public String getSalt() {
        return salt;
    }

    private void setSalt(String salt) {
        this.salt = salt;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean checkPassword(String password) {
        return Objects.equals(DigesterFactory.md5(getSalt()).digest(password), this.password);
    }

    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }

    public void removeProperty(String key) {
        properties.remove(key);
    }


    @JsonIgnore
    public User getUser() {
        this.setDomain(null);
        this.setPassword(null);
        this.setSalt(null);
        return this;
    }

    @JsonIgnore
    public User getUserProfile() {
        User profile = new User();
        profile.setUsername(this.username);
        profile.setEmail(this.email);
        profile.setProperties(this.properties);
        profile.setAvatarUri(this.avatarUri);
        return profile;
    }

    @JsonIgnore
    public User getUserWithOnlyId(){
        User returnedUser = new User();
        returnedUser.setId(this.getId());
        return returnedUser;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getAvatarUri() {
        return avatarUri;
    }

    public void setAvatarUri(String avatarUri) {
        this.avatarUri = avatarUri;
    }

    public void updateFields(User updateUser) {
        if (updateUser.getUsername() != null) {
            setUsername(updateUser.getUsername());
        }
        if (updateUser.getEmail() != null && !updateUser.getEmail().equals(this.getEmail())) {
            setEmail(updateUser.getEmail());
            setEmailValidated(false);
        }
        if (updateUser.getPassword() != null) {
            password = updateUser.getPassword();
            salt = updateUser.getSalt();
        }
        if (updateUser.getRole() != null) {
            setRole(updateUser.getRole());
        }
        if (updateUser.getProperties() != null && !updateUser.getProperties().isEmpty()) {
            for (Map.Entry<String, Object> entry : updateUser.getProperties().entrySet()) {
                if (entry.getValue() == null) {
                    removeProperty(entry.getKey());
                } else {
                    addProperty(entry.getKey(), entry.getValue());
                }
            }
        }

        Optional.ofNullable(updateUser.getAvatarUri()).ifPresent(newAvatar -> {
            if (newAvatar.isEmpty()){
                setAvatarUri(null);
            }
            else {
                setAvatarUri(newAvatar);
            }
        });
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof User)) {
            return false;
        }
        User that = (User) obj;
        return Objects.equals(this.email, that.email) && Objects.equals(this.password, that.password)
                && Objects.equals(this.username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, domain);
    }
}
