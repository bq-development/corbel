package io.corbel.iam.api;

import io.corbel.iam.model.Domain;
import io.corbel.iam.model.Identity;
import io.corbel.iam.model.User;
import io.corbel.iam.model.UserWithIdentity;
import com.google.common.collect.Sets;
import io.dropwizard.testing.junit.ResourceTestRule;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import java.util.Set;

/**
 * @author Alexander De Leon
 */
public abstract class UserResourceTestBase {

    protected static final String TEST_CLIENT_ID = "client_id";
    protected static final String TEST_USER_ID = "id";
    protected static final String TEST_DOMAIN_ID = "domain";
    protected static final Domain TEST_DOMAIN = new Domain();
    protected static final String TEST_OTHER_DOMAIN = "other_domain";
    protected static final String TEST_USER_EMAIL = "some@email.com";
    protected static final String TEST_USER_FIRST_NAME = "firstname";
    protected static final String TEST_USER_LAST_NAME = "lastname";
    protected static final String TEST_USER_PHONE = "phone";
    protected static final String TEST_USER_URL = "url";
    protected static final Set<String> TEST_SCOPES = Sets.newHashSet("scope1", "scope2");
    protected static final String TEST_USERNAME = "username";
    protected static final String TEST_TOKEN = "xxxx";
    protected static final String AUTHORIZATION = "Authorization";
    protected static final String TEST_PROPERTY = "prop";
    protected static final Object TEST_PROPERTY_VAL = "prop_val";

    protected User createTestUser() {
        return createTestUser(new User());
    }

    protected User createTestUser(User user) {
        user.setId(TEST_USER_ID);
        user.setDomain(TEST_DOMAIN_ID);
        user.setEmail(TEST_USER_EMAIL);
        user.setFirstName(TEST_USER_FIRST_NAME);
        user.setLastName(TEST_USER_LAST_NAME);
        user.setPhoneNumber(TEST_USER_PHONE);
        user.setProfileUrl(TEST_USER_URL);
        user.setScopes(null);
        user.setUsername(TEST_USERNAME);
        user.addProperty(TEST_PROPERTY, TEST_PROPERTY_VAL);
        return user;
    }

    protected User getTestUser() {
        User user = createTestUser(new User());
        user.setScopes(TEST_SCOPES);
        return user;
    }

    protected UserWithIdentity getTestUserWithIdentity() {
        UserWithIdentity userWithIdentity = (UserWithIdentity) createTestUser(new UserWithIdentity());
        userWithIdentity.setScopes(TEST_SCOPES);
        Identity identity = new Identity();
        identity.setOauthId("0000");
        identity.setOauthService("corbel");
        userWithIdentity.setIdentity(identity);
        return userWithIdentity;
    }

    protected User removeId(User createTestUser) {
        createTestUser.setId(null);
        return createTestUser;
    }

    protected Builder addUserClient() {
        return getTestRule().client().target("/v1.0/user").request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN);
    }

    protected Builder getUserClient(String id) {
        return getTestRule().client().target("/v1.0/user/" + id).request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN);
    }

    protected Builder getUserProfile(String id) {
        return getTestRule().client().target("/v1.0/user/" + id + "/profile").request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN);
    }

    protected Builder getUserClientMe() {
        return getTestRule().client().target("/v1.0/user/me").request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN);
    }

    protected ResourceTestRule getTestRule() {
        return null;
    }

    protected Builder apiCall(String authorization, String url) {
        return apiCall(authorization, url, MediaType.APPLICATION_JSON_TYPE);
    }

    protected Builder apiCall(String authorization, String url, MediaType accept) {
        return getTestRule().client().target(url).request(accept).header(AUTHORIZATION, authorization);
    }

}