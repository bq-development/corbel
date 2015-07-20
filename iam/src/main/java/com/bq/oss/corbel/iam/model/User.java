package com.bq.oss.corbel.iam.model;

import com.bq.oss.lib.ws.digest.DigesterFactory;
import com.bq.oss.lib.ws.model.ModelValidator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.*;

/**
 * A {@link User} is end user of the systems supported by Corbel
 *
 * @author Alexander De Leon
 */
public class User extends TraceableEntity implements HasScopes {

    private String domain;
    @NotEmpty
    @Email
    protected String email;
    @NotEmpty
    protected String username;
    private String firstName;
    private String lastName;
    private String profileUrl;
    private String phoneNumber;
    private String country;
    private Set<String> scopes = new HashSet<>();
    private Map<String, Object> properties = new HashMap<>();
    private String salt;
    private String password;
    private List<String> groups;

    public User() {
        super();
    }

    public User(User other) {
        super(other);
        this.domain = other.domain;
        this.email = other.email;
        this.username = other.username;
        this.firstName = other.firstName;
        this.lastName = other.lastName;
        this.profileUrl = other.profileUrl;
        this.phoneNumber = other.phoneNumber;
        this.country = other.country;
        this.scopes = other.scopes;
        this.properties = other.properties;
        this.salt = other.salt;
        this.password = other.password;
        this.groups = other.groups;
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

    public boolean checkPassword(String password) {
        return Objects.equals(DigesterFactory.md5(getSalt()).digest(password), this.password);
    }

    public String getSalt() {
        return salt;
    }

    @JsonIgnore
    public User getUserProfile() {
        User u = new User(this);
        u.setPassword(null);
        u.salt = null;
        return u;
    }

    @JsonIgnore
    public User getUserWithOnlyId(){
        User returnedUser = new User();
        returnedUser.setProperties(null);
        returnedUser.setScopes(null);
        returnedUser.setId( this.getId() );
    return returnedUser;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }

    public boolean removeProperty(String key) {
        return properties.remove(key) != null;
    }

    public void updateUser(User updateUser) {
        if (updateUser.getUsername() != null) {
            setUsername(updateUser.getUsername());
        }
        if (updateUser.getEmail() != null) {
            setEmail(updateUser.getEmail());
        }
        if (updateUser.getFirstName() != null) {
            setFirstName(updateUser.getFirstName());
        }
        if (updateUser.getLastName() != null) {
            setLastName(updateUser.getLastName());
        }
        if (updateUser.getPhoneNumber() != null) {
            setPhoneNumber(updateUser.getPhoneNumber());
        }
        if (updateUser.getProfileUrl() != null) {
            setProfileUrl(updateUser.getProfileUrl());
        }
        if (updateUser.getCountry() != null) {
            setCountry(updateUser.getCountry());
        }
        if (updateUser.getScopes() != null && !updateUser.getScopes().isEmpty()) {
            setScopes(new HashSet<>(updateUser.getScopes()));
        }
        if (updateUser.getGroups() != null && !updateUser.getGroups().isEmpty()) {
            setGroups(updateUser.getGroups());
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
        if (updateUser.getPassword() != null) {
            this.password = updateUser.password;
            this.salt = updateUser.salt;
        }
        ModelValidator.validateObject(this);
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public void addGroups(List<String> groups) {
        if(this.groups == null) {
            this.groups = new ArrayList<>();
        }
        this.groups.addAll(groups);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        if (!super.equals(o)) return false;

        User user = (User) o;

        if (country != null ? !country.equals(user.country) : user.country != null) return false;
        if (domain != null ? !domain.equals(user.domain) : user.domain != null) return false;
        if (email != null ? !email.equals(user.email) : user.email != null) return false;
        if (firstName != null ? !firstName.equals(user.firstName) : user.firstName != null) return false;
        if (groups != null ? !groups.equals(user.groups) : user.groups != null) return false;
        if (lastName != null ? !lastName.equals(user.lastName) : user.lastName != null) return false;
        if (password != null ? !password.equals(user.password) : user.password != null) return false;
        if (phoneNumber != null ? !phoneNumber.equals(user.phoneNumber) : user.phoneNumber != null) return false;
        if (profileUrl != null ? !profileUrl.equals(user.profileUrl) : user.profileUrl != null) return false;
        if (properties != null ? !properties.equals(user.properties) : user.properties != null) return false;
        if (salt != null ? !salt.equals(user.salt) : user.salt != null) return false;
        if (scopes != null ? !scopes.equals(user.scopes) : user.scopes != null) return false;
        if (username != null ? !username.equals(user.username) : user.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (domain != null ? domain.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (profileUrl != null ? profileUrl.hashCode() : 0);
        result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (scopes != null ? scopes.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        result = 31 * result + (salt != null ? salt.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (groups != null ? groups.hashCode() : 0);
        return result;
    }
}
