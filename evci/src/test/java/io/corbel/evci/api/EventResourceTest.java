package io.corbel.evci.api;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import io.corbel.evci.service.EventsService;
import io.corbel.lib.token.TokenInfo;
import io.corbel.lib.token.reader.TokenReader;
import io.corbel.lib.ws.auth.AuthorizationInfo;
import io.corbel.lib.ws.auth.AuthorizationInfoProvider;
import io.corbel.lib.ws.auth.AuthorizationRequestFilter;
import io.corbel.lib.ws.auth.BearerTokenAuthenticator;
import io.corbel.lib.ws.gson.GsonMessageReaderWriterProvider;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.oauth.OAuthFactory;
import io.dropwizard.testing.junit.ResourceTestRule;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EventResourceTest {

    private static final String DOMAIN = "domain";
    private static final String TEST_USER_ID = "testUserId";
    private static final String AUTHORIZATION = "Authorization";
    private static final String TEST_TOKEN = "xxxx";

    private static final EventsService eventsService = mock(EventsService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static BearerTokenAuthenticator authenticatorMock = mock(BearerTokenAuthenticator.class);
    private static OAuthFactory oAuthFactory = new OAuthFactory<>(authenticatorMock, "realm", AuthorizationInfo.class);
    private static final AuthorizationRequestFilter filter = spy(new AuthorizationRequestFilter(oAuthFactory, null, "", false, "evci"));

    @ClassRule public static ResourceTestRule RULE;

    static {

        ResourceTestRule.Builder ruleBuilder = ResourceTestRule.builder();

        ruleBuilder.addResource(new EventResource(eventsService, objectMapper));
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        ruleBuilder.setMapper(objectMapper);

        ruleBuilder.addProvider(filter);
        ruleBuilder.addProvider(new AuthorizationInfoProvider().getBinder());
        ruleBuilder.addProvider(new GsonMessageReaderWriterProvider());

        RULE = ruleBuilder.build();
    }

    public EventResourceTest() throws AuthenticationException {
        TokenInfo tokenInfo = mock(TokenInfo.class);
        when(tokenInfo.getUserId()).thenReturn(TEST_USER_ID);
        when(tokenInfo.getDomainId()).thenReturn(DOMAIN);
        TokenReader readerMock = mock(TokenReader.class);
        when(readerMock.getInfo()).thenReturn(tokenInfo);
        AuthorizationInfo authorizationInfoMock = mock(AuthorizationInfo.class);
        when(authorizationInfoMock.getTokenReader()).thenReturn(readerMock);
        when(authenticatorMock.authenticate(TEST_TOKEN)).thenReturn(com.google.common.base.Optional.of(authorizationInfoMock));
        when(authorizationInfoMock.getUserId()).thenReturn(TEST_USER_ID);
        when(authorizationInfoMock.getDomainId()).thenReturn(DOMAIN);
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        when(requestMock.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TEST_TOKEN);
        doReturn(requestMock).when(filter).getRequest();
        doNothing().when(filter).checkTokenAccessRules(eq(authorizationInfoMock), any(), any());
        when(tokenInfo.getUserId()).thenReturn(TEST_USER_ID);
        when(tokenInfo.getDomainId()).thenReturn(DOMAIN);
    }


    @Before
    public void setUp() {
        reset(eventsService);
    }

    @Test
    public void testRegisterFormParamsEvent() {

        String type = "type:type";
        String dotType = "type.type";
        MultivaluedMap<String, String> event = new MultivaluedHashMap();
        event.add("key1", "value1");
        event.add("key2", "value2");
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/event/" + type).request()
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(Entity.form(event), Response.class);

        Assert.assertEquals(202, response.getStatus());

        ArgumentCaptor<JsonNode> captor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(eventsService).registerEvent(Mockito.eq(dotType), captor.capture());
        JsonNode json = captor.getValue();

        assertThat(json.get("header").get("domainId").textValue()).isEqualTo(DOMAIN);
        assertThat(json.get("header").get("userId").textValue()).isEqualTo(TEST_USER_ID);
        assertThat(json.get("content").get("key1").textValue()).isEqualTo("value1");
        assertThat(json.get("content").get("key2").textValue()).isEqualTo("value2");
    }

    @Test
    public void testRegisterFormParamsWithMultipleValueEvent() {

        String type = "type";
        MultivaluedMap<String, String> event = new MultivaluedHashMap();
        event.addAll("key1", Arrays.asList("value1", "value2"));
        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/event/" + type).request()
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(Entity.form(event), Response.class);

        Assert.assertEquals(202, response.getStatus());

        ArgumentCaptor<JsonNode> captor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(eventsService).registerEvent(Mockito.eq(type), captor.capture());

        JsonNode json = captor.getValue();
        assertThat(json.get("header").get("domainId").textValue()).isEqualTo(DOMAIN);
        assertThat(json.get("header").get("userId").textValue()).isEqualTo(TEST_USER_ID);
        assertThat(json.get("content").get("key1").toString()).isEqualTo("[\"value1\",\"value2\"]");
    }

    @Test(expected = ConstraintViolationException.class)
    public void testRegisterEmptyFormParamsEvent() throws Throwable {
        String type = "type";
        try {
            RULE.client().target("/" + ApiVersion.CURRENT + "/event/" + type).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                    .post(Entity.json(""), Response.class);
        } catch (ProcessingException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testRegisterEvent() {

        String type = "type";
        String eventJson = "{\"prop\":\"value\"}";

        Response response = RULE.client().target("/" + ApiVersion.CURRENT + "/event/" + type).request()
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).post(Entity.json(eventJson), Response.class);

        Assert.assertEquals(202, response.getStatus());

        ArgumentCaptor<JsonNode> captor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(eventsService).registerEvent(Mockito.eq(type), captor.capture());

        JsonNode json = captor.getValue();
        assertThat(json.get("header").get("domainId").textValue()).isEqualTo(DOMAIN);
        assertThat(json.get("header").get("userId").textValue()).isEqualTo(TEST_USER_ID);
        assertThat(json.get("content").get("prop").textValue()).isEqualTo("value");
    }

    @Test(expected = JsonParseException.class)
    public void testRegisterInvalidEvent() throws Throwable {

        String type = "type";
        String event = "sdfsdf";
        try {
            RULE.client().target("/" + ApiVersion.CURRENT + "/event/" + type).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                    .post(Entity.json(event), Response.class);
        } catch (ProcessingException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ConstraintViolationException.class)
    public void testRegisterEmptyEvent() throws Throwable {
        String type = "type";
        try {
            RULE.client().target("/" + ApiVersion.CURRENT + "/event/" + type).request().header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                    .post(Entity.json(""), Response.class);
        } catch (ProcessingException e) {
            throw e.getCause();
        }
    }

}
