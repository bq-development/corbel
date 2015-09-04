/*
 * Copyright (C) 2013 StarTIC
 */
package io.corbel.oauth.api;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import io.corbel.lib.token.TokenGrant;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.exception.TokenVerificationException;
import io.corbel.lib.token.factory.TokenFactory;
import io.corbel.lib.token.model.TokenType;
import io.corbel.lib.token.parser.TokenParser;
import io.corbel.lib.token.reader.TokenReader;
import io.corbel.oauth.model.Client;
import io.corbel.oauth.model.User;
import io.corbel.oauth.repository.UserRepository;
import io.corbel.oauth.service.ClientService;
import io.corbel.oauth.token.TokenExpireTime;
import io.dropwizard.testing.junit.ResourceTestRule;

/**
 * @author Alexander De Leon
 * 
 */
public class TokenResourceTest {

    private static final String OAUTH_TOKEN_ENDPOINT = "/oauth/token";

    private static final String TEST_CLIENT_ID = "temp_client";
    private static Client TEST_CLIENT;
    private static final String BAD_TEST_CLIENT = "bad_client";
    private static final String TEST_SECRET = "temp_secret";
    private static final String BAD_TEST_SECRET = "bad_temp_secret";
    private static final String TEST_CODE = "test_code";
    private static final String BAD_TEST_CODE = "bad_test_code";

    private static final String GRANT_TYPE = "grant_type";
    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String BAD_GRANT_TYPE = "bad_grant_type";

    private static final String CODE = "code";
    private static final String TEST_TOKEN = "testtoken";
    private static final String CLIENT_ID = "client_id";
    private static final String SECRET_ID = "client_secret";
    private static final String CLIENT_DOMAIN = "test";
    private static final long TEST_EXPIRE_AT = 0L;

    private static final String USER_ID = "user_id";
    private static final TokenInfo TEST_INFO = TokenInfo.newBuilder().setType(TokenType.CODE).setUserId(USER_ID).setClientId(TEST_CLIENT_ID)
            .build();
    private static final TokenInfo TEST_INFO_TOKEN = TokenInfo.newBuilder().setType(TokenType.TOKEN).setUserId(USER_ID)
            .setClientId(TEST_CLIENT_ID).setDomainId(CLIENT_DOMAIN).build();

    private static final String VALIDATED_MAIL_REQUIRED = "validated_mail_required";

    private static final TokenParser tokenParserMock = mock(TokenParser.class);
    private static final TokenFactory tokenFactoryMock = mock(TokenFactory.class);
    private static final TokenReader readerMock = mock(TokenReader.class);
    private static final ClientService clientServiceMock = mock(ClientService.class);
    private static final UserRepository userRepositoryMock = mock(UserRepository.class);
    private static final TokenExpireTime tokenExpireTimeMock = mock(TokenExpireTime.class);

    private TokenGrant tokenGrant;



    @ClassRule public static ResourceTestRule RULE = ResourceTestRule.builder()
            .addResource(new TokenResource(tokenParserMock, tokenFactoryMock, clientServiceMock, userRepositoryMock, tokenExpireTimeMock))
            .build();

    @Before
    public void setUp() throws TokenVerificationException {
        TEST_CLIENT = new Client();
        TEST_CLIENT.setDomain(CLIENT_DOMAIN);
        TEST_CLIENT.setKey(SECRET_ID);
        TEST_CLIENT.setName(TEST_CLIENT_ID);
        when(clientServiceMock.verifyClientSecret(TEST_SECRET, TEST_CLIENT)).thenReturn(true);
        when(clientServiceMock.verifyClientSecret(BAD_TEST_SECRET, TEST_CLIENT)).thenReturn(false);
        when(clientServiceMock.findByName(TEST_CLIENT_ID)).thenReturn(Optional.of(TEST_CLIENT));
        when(clientServiceMock.findByName(BAD_TEST_CLIENT)).thenReturn(Optional.empty());
        when(tokenExpireTimeMock.getTokenExpireTimeFromResponseType(TokenType.TOKEN)).thenReturn(TEST_EXPIRE_AT);
        when(readerMock.getInfo()).thenReturn(TEST_INFO);
        when(tokenParserMock.parseAndVerify(TEST_CODE)).thenReturn(readerMock);
        tokenGrant = new TokenGrant(TEST_TOKEN, TEST_EXPIRE_AT);
    }

    @Test
    public void testOK() throws TokenVerificationException {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap();
        formData.add(GRANT_TYPE, AUTHORIZATION_CODE);
        formData.add(CODE, TEST_CODE);
        formData.add(CLIENT_ID, TEST_CLIENT_ID);
        formData.add(SECRET_ID, TEST_SECRET);

        when(tokenFactoryMock.createToken(TEST_INFO_TOKEN, tokenExpireTimeMock.getTokenExpireTimeFromResponseType(TokenType.TOKEN)))
                .thenReturn(tokenGrant);

        when(readerMock.getInfo()).thenReturn(TEST_INFO);

        Response response = invokeResource(formData);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(TokenGrant.class)).isEqualTo(tokenGrant);
    }

    @Test
    public void testMissingGrantType() {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap();
        formData.add(CODE, TEST_CODE);
        formData.add(CLIENT_ID, TEST_CLIENT_ID);
        formData.add(SECRET_ID, TEST_SECRET);
        Response response = invokeResource(formData);
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testMissingCode() {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap();
        formData.add(GRANT_TYPE, AUTHORIZATION_CODE);
        formData.add(CLIENT_ID, TEST_CLIENT_ID);
        formData.add(SECRET_ID, TEST_SECRET);

        Response response = invokeResource(formData);
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testInvalidGrantType() {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap();
        formData.add(GRANT_TYPE, BAD_GRANT_TYPE);
        formData.add(CODE, TEST_CODE);
        formData.add(CLIENT_ID, TEST_CLIENT_ID);
        formData.add(SECRET_ID, TEST_SECRET);

        Response response = invokeResource(formData);
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void testInvalidCode() throws TokenVerificationException {

        when(tokenParserMock.parseAndVerify(BAD_TEST_CODE)).thenThrow(new TokenVerificationException("Error"));

        MultivaluedMap<String, String> formData = new MultivaluedHashMap();
        formData.add(GRANT_TYPE, AUTHORIZATION_CODE);
        formData.add(CODE, BAD_TEST_CODE);
        formData.add(CLIENT_ID, TEST_CLIENT_ID);
        formData.add(SECRET_ID, TEST_SECRET);

        Response response = invokeResource(formData);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void testBadCredentials() throws TokenVerificationException {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap();
        formData.add(GRANT_TYPE, AUTHORIZATION_CODE);
        formData.add(CODE, TEST_CODE);
        formData.add(CLIENT_ID, BAD_TEST_CLIENT);
        formData.add(SECRET_ID, TEST_SECRET);

        Response response = invokeResource(formData);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void testBadClientSecret() throws TokenVerificationException {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap();
        formData.add(GRANT_TYPE, AUTHORIZATION_CODE);
        formData.add(CODE, TEST_CODE);
        formData.add(CLIENT_ID, TEST_CLIENT_ID);
        formData.add(SECRET_ID, BAD_TEST_SECRET);

        when(tokenFactoryMock.createToken(TEST_INFO, tokenExpireTimeMock.getTokenExpireTimeFromResponseType(TokenType.TOKEN)))
                .thenReturn(tokenGrant);

        Response response = invokeResource(formData);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void testValidatedEmailToken() {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap();
        formData.add(GRANT_TYPE, AUTHORIZATION_CODE);
        formData.add(CODE, TEST_CODE);
        formData.add(CLIENT_ID, TEST_CLIENT_ID);
        formData.add(SECRET_ID, TEST_SECRET);
        formData.add(VALIDATED_MAIL_REQUIRED, "true");

        User user = new User();
        user.setEmailValidated(true);
        when(userRepositoryMock.findOne(USER_ID)).thenReturn(user);

        Response response = invokeResource(formData);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testNotValidatedEmailToken() {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap();
        formData.add(GRANT_TYPE, AUTHORIZATION_CODE);
        formData.add(CODE, TEST_CODE);
        formData.add(CLIENT_ID, TEST_CLIENT_ID);
        formData.add(SECRET_ID, TEST_SECRET);
        formData.add(VALIDATED_MAIL_REQUIRED, "true");

        User user = new User();
        user.setEmailValidated(false);
        when(userRepositoryMock.findOne(USER_ID)).thenReturn(user);

        Response response = invokeResource(formData);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void testNotUserAndValidatedEmailToken() {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap();
        formData.add(GRANT_TYPE, AUTHORIZATION_CODE);
        formData.add(CODE, TEST_CODE);
        formData.add(CLIENT_ID, TEST_CLIENT_ID);
        formData.add(SECRET_ID, TEST_SECRET);
        formData.add(VALIDATED_MAIL_REQUIRED, "true");

        when(userRepositoryMock.findOne(USER_ID)).thenReturn(null);

        Response response = invokeResource(formData);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    private Response invokeResource(MultivaluedMap<String, String> formData) {
        return RULE.client().target("/" + ApiVersion.CURRENT + OAUTH_TOKEN_ENDPOINT).request().post(Entity.form(formData), Response.class);
    }

}
