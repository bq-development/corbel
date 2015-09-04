package io.corbel.oauth;

import java.util.LinkedHashMap;

import io.corbel.oauth.model.Role;
import io.corbel.oauth.model.User;

/**
 * @author Cristian del Cerro
 */
public class TestUtils {

    static private String username = "userTest";
    static private String email = "user@testmail.com";
    static private String avatarUri = "avatarUri";

    public static User createUserTest(Role role) {
        User userTest = new User();
        userTest.setId("userIdTest");
        userTest.setEmail(email);
        userTest.setPassword("passwordTest");
        userTest.setUsername(username);
        userTest.setDomain("test");
        userTest.setAvatarUri(avatarUri);
        userTest.setRole(role);
        return userTest;
    }

    public static User getProfileUserTest() {
        User profile = new User();
        profile.setUsername(username);
        profile.setEmail(email);
        profile.setProperties(new LinkedHashMap<>());
        profile.setAvatarUri(avatarUri);
        return profile;
    }
}
