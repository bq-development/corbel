/*
 * Copyright (C) 2013 StarTIC
 */
package com.bq.oss.corbel.resources.api;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Optional;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.bq.oss.lib.queries.builder.QueryParametersBuilder;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.validation.beanvalidation.CustomValidatorBean;

import com.bq.oss.corbel.event.ResourceEvent;
import com.bq.oss.corbel.eventbus.service.EventBus;
import com.bq.oss.corbel.rem.internal.RemEntityTypeResolver;
import com.bq.oss.corbel.resources.rem.Rem;
import com.bq.oss.corbel.resources.rem.RemRegistry;
import com.bq.oss.corbel.resources.rem.request.CollectionParameters;
import com.bq.oss.corbel.resources.rem.request.RelationParameters;
import com.bq.oss.corbel.resources.rem.request.RequestParameters;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.service.RemService;
import com.bq.oss.corbel.resources.service.DefaultRemService;
import com.bq.oss.corbel.resources.service.DefaultResourcesService;
import com.bq.oss.lib.queries.exception.MalformedJsonQueryException;
import com.bq.oss.lib.queries.parser.*;
import com.bq.oss.lib.queries.request.*;
import com.bq.oss.lib.token.TokenInfo;
import com.bq.oss.lib.token.reader.TokenReader;
import com.bq.oss.lib.ws.auth.AuthorizationInfo;
import com.bq.oss.lib.ws.auth.AuthorizationInfoProvider;
import com.bq.oss.lib.ws.auth.BearerTokenAuthenticator;
import com.bq.oss.lib.ws.encoding.MatrixEncodingRequestFilter;
import com.bq.oss.lib.ws.json.serialization.EmptyEntitiesAllowedJacksonMessageBodyProvider;
import com.bq.oss.lib.ws.queries.QueryParametersProvider;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.ClientResponse;

import io.dropwizard.auth.oauth.OAuthProvider;
import io.dropwizard.testing.junit.ResourceTestRule;

/**
 * @author Alexander De Leon
 */
public class RemResourceTest {

    private static final String DOMAIN = "domain";
    private static final String TEST_USER_ID = "testUserId";
    private static final String AUTHORIZATION = "Authorization";
    private static final String TEST_TOKEN = "xxxx";

    private static final String NO_JSON_DATA = "No JSON DATA";
    private static final String TEST_TYPE = "test:TestObject";
    private static final ResourceId RESOURCE_ID = new ResourceId("123");
    private static final ResourceId RESOURCE_WILDCARD_ID = new ResourceId("_");
    private static final String TEST_REL = "test:ToRelationObject";
    private static final String COLLECTION_URI = "/" + ApiVersion.CURRENT + "/resource/" + TEST_TYPE;
    private static final String RESOURCE_URI = COLLECTION_URI + "/" + RESOURCE_ID.getId();
    private static final String RESOURCE_WILDCARD_URI = COLLECTION_URI + "/" + RESOURCE_WILDCARD_ID.getId();
    private static final String RELATION_URI = RESOURCE_URI + "/" + TEST_REL;
    private static final String RELATION_WILDCARD_URI = RESOURCE_WILDCARD_URI + "/" + TEST_REL;
    private static final String TEST_OK = "testOk";
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_DEFAULT_LIMIT = 50;
    private static final String jsonTest = "{\"field1\":\"field1content\"}";
    private static final AuthorizationInfoProvider authorizationInfoProviderSpy = spy(new AuthorizationInfoProvider());
    @ClassRule public static ResourceTestRule RULE;
    private static Rem<JsonObject> remMock = mock(Rem.class);
    private static RemRegistry registryMock = mock(RemRegistry.class);
    private static RemService remService = new DefaultRemService(registryMock);
    private static RemEntityTypeResolver remEntityTypeResolverMock = mock(RemEntityTypeResolver.class);
    private static QueryParser queryParserMock = mock(QueryParser.class);

    private static BearerTokenAuthenticator authenticatorMock = mock(BearerTokenAuthenticator.class);
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static javax.validation.Validator validator = new CustomValidatorBean();
    private static EventBus eventBusMock = mock(EventBus.class);

    private static final RemResource remResource;

    static {
        ResourceTestRule.Builder ruleBuilder = ResourceTestRule.builder();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        ruleBuilder.setMapper(objectMapper);

        ruleBuilder.addProperty("com.sun.jersey.spi.container.ContainerRequestFilters",
                Lists.newArrayList(new MatrixEncodingRequestFilter("^(.*/v1.0/resource/.+/.+/.+;r=)(.+)$")));

        QueryParametersBuilder queryParametersBuilder = new QueryParametersBuilder(createQueryParser(), createAggregationParser(),
                createSortParser(), createPaginationParser());

        DefaultResourcesService defaultResourcesService = new DefaultResourcesService(remService, remEntityTypeResolverMock, DEFAULT_LIMIT,
                MAX_DEFAULT_LIMIT, queryParametersBuilder, eventBusMock);

        remResource = new RemResource(defaultResourcesService);
        ruleBuilder.addResource(remResource);

        ruleBuilder.addProvider(new QueryParametersProvider(DEFAULT_LIMIT, MAX_DEFAULT_LIMIT, queryParametersBuilder));

        ruleBuilder.addProvider(new OAuthProvider<>(authenticatorMock, null));
        ruleBuilder.addProvider(authorizationInfoProviderSpy);
        ruleBuilder.addProvider(new EmptyEntitiesAllowedJacksonMessageBodyProvider(objectMapper, validator));


        RULE = ruleBuilder.build();
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    public RemResourceTest() throws Exception {
        when(
                registryMock.getRem(Mockito.matches("test:TestObject/?.*"), Mockito.anyList(), Mockito.any(HttpMethod.class),
                        Mockito.eq(null))).thenReturn(remMock);
        when(remEntityTypeResolverMock.getEntityType(remMock)).thenReturn((Class) JsonObject.class);
        TokenInfo token = mock(TokenInfo.class);
        when(token.getUserId()).thenReturn(TEST_USER_ID);
        when(token.getDomainId()).thenReturn(DOMAIN);
        TokenReader readerMock = mock(TokenReader.class);
        when(readerMock.getInfo()).thenReturn(token);
        AuthorizationInfo authorizationInfoMock = mock(AuthorizationInfo.class);
        when(authorizationInfoMock.getTokenReader()).thenReturn(readerMock);
        when(authenticatorMock.authenticate(TEST_TOKEN)).thenReturn(com.google.common.base.Optional.of(authorizationInfoMock));
        when(authorizationInfoMock.getUserId()).thenReturn(TEST_USER_ID);
        doReturn(authorizationInfoMock).when(authorizationInfoProviderSpy).getValue(any());

    }

    private static JacksonQueryParser createQueryParser() {
        return new JacksonQueryParser(getJsonParser());
    }

    private static JacksonAggregationParser createAggregationParser() {
        return new JacksonAggregationParser(getJsonParser());
    }

    private static PaginationParser createPaginationParser() {
        return new DefaultPaginationParser();
    }

    private static SortParser createSortParser() {
        return new JacksonSortParser(getJsonParser());
    }

    private static CustomJsonParser getJsonParser() {
        return new CustomJsonParser(new ObjectMapper().getFactory());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetCollection() {
        Response testResponse = Response.ok().entity(TEST_OK).build();
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);
        when(
                remMock.collection(Mockito.eq(TEST_TYPE), parametersCaptor.capture(), Mockito.any(URI.class),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);

        assertThat(RULE.client().resource(COLLECTION_URI).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(String.class)).isEqualTo(
                TEST_OK);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetCollectionWithPaginationParameters() {
        Pagination pagination = new Pagination(3, 20);
        Response testResponse = Response.ok().entity(TEST_OK).build();
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);

        when(
                remMock.collection(Mockito.eq(TEST_TYPE), parametersCaptor.capture(), Mockito.any(URI.class),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(
                RULE.client().resource(COLLECTION_URI).queryParam("api:pageSize", "20").queryParam("api:page", "3")
                        .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(String.class)).isEqualTo(TEST_OK);
        assertThat(((CollectionParameters) parametersCaptor.getValue().getApiParameters()).getPagination()).isEqualTo(pagination);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetCollectionWithPaginationDefaultsParameters() {
        Pagination pagination = new Pagination(0, DEFAULT_LIMIT);
        Response testResponse = Response.ok().entity(TEST_OK).build();
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);

        when(
                remMock.collection(Mockito.eq(TEST_TYPE), parametersCaptor.capture(), Mockito.any(URI.class),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(RULE.client().resource(COLLECTION_URI).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(String.class)).isEqualTo(
                TEST_OK);
        assertThat(((CollectionParameters) parametersCaptor.getValue().getApiParameters()).getPagination()).isEqualTo(pagination);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetCollectionWithIllegalPaginationParameters() {
        Response testResponse = Response.status(Response.Status.BAD_REQUEST).build();
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);

        when(
                remMock.collection(Mockito.eq(TEST_TYPE), parametersCaptor.capture(), Mockito.any(URI.class),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(

        RULE.client().resource(COLLECTION_URI).queryParam("api:limit", "-20").queryParam("api:page", "3")
                .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).head().getStatus() == testResponse.getStatus());
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetCollectionWithSort() throws UnsupportedEncodingException {
        String sortRequest = "{\"price\":\"asc\"}";
        Response testResponse = Response.ok().entity(TEST_OK).build();
        Sort sort = new Sort("asc", "price");
        Optional<Sort> optionalSort = Optional.of(sort);
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);

        when(remMock.collection(Mockito.eq(TEST_TYPE), parametersCaptor.capture(), Mockito.any(URI.class), Mockito.eq(Optional.empty())))
                .thenReturn(testResponse);
        assertThat(
                RULE.client().resource(COLLECTION_URI).queryParam("api:sort", URLEncoder.encode(sortRequest, "UTF-8"))
                        .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(String.class)).isEqualTo(
                TEST_OK);
        assertThat(((CollectionParameters) parametersCaptor.getValue().getApiParameters()).getSort()).isEqualTo(optionalSort);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @Test
    public void testGetCollectionWithBadJSONSort() {
        assertThat(
                RULE.client().resource(COLLECTION_URI).queryParam("api:sort", NO_JSON_DATA).type(MediaType.APPLICATION_JSON_TYPE)
                        .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).head().getStatus()).isEqualTo(
                Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetCollectionWithErrorSort() throws UnsupportedEncodingException {
        assertThat(
                RULE.client().resource(COLLECTION_URI)
                        .queryParam("api:sort", URLEncoder.encode("{\"price\":\"Bad Order Method\"}", "UTF-8"))
                        .type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).head().getStatus()).isEqualTo(
                Response.Status.BAD_REQUEST.getStatusCode());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetCollectionWithQuery() throws MalformedJsonQueryException, UnsupportedEncodingException {
        Response testResponse = Response.ok().entity(TEST_OK).build();
        String query = "[{\"$eq\":{\"artist\":\"Metallica\"}}]";
        JacksonQueryParser parser = createQueryParser();
        ResourceQuery resourceQuery = parser.parse(query);
        Optional<ResourceQuery> optionalResourceQuery = Optional.of(resourceQuery);
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);
        when(queryParserMock.parse(query)).thenReturn(resourceQuery);
        when(
                remMock.collection(Mockito.eq(TEST_TYPE), parametersCaptor.capture(), Mockito.any(URI.class),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(
                RULE.client().resource(COLLECTION_URI).queryParam("api:query", URLEncoder.encode(query, "UTF-8"))
                        .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(String.class)).isEqualTo(TEST_OK);
        assertThat(((CollectionParameters) parametersCaptor.getValue().getApiParameters()).getQuery()).isEqualTo(optionalResourceQuery);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @Test
    public void testGetCollectionWithMalformedQuery() throws MalformedJsonQueryException, UnsupportedEncodingException {
        String query = "[{\"$in\":{\"artist\":\"Metallica\"}}]";
        when(queryParserMock.parse(query)).thenThrow(new MalformedJsonQueryException(""));
        assertThat(
                RULE.client().resource(COLLECTION_URI).queryParam("api:query", URLEncoder.encode(query, "UTF-8"))
                        .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).head().getStatus()).isEqualTo(
                Response.Status.BAD_REQUEST.getStatusCode());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetCollectionWithRepeatField() throws MalformedJsonQueryException, UnsupportedEncodingException {
        Response testResponse = Response.ok().entity(TEST_OK).build();
        String query = "[{\"$lte\":{\"duration\":238.0}},{\"$gte\":{\"duration\":238.0}}]";
        JacksonQueryParser parser = createQueryParser();
        ResourceQuery resourceQuery = parser.parse(query);
        Optional<ResourceQuery> optionalResourceQuery = Optional.of(resourceQuery);
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);
        when(queryParserMock.parse(query)).thenReturn(resourceQuery);
        when(
                remMock.collection(Mockito.eq(TEST_TYPE), parametersCaptor.capture(), Mockito.any(URI.class),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(
                RULE.client().resource(COLLECTION_URI).queryParam("api:query", URLEncoder.encode(query, "UTF-8"))
                        .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(String.class)).isEqualTo(TEST_OK);
        assertThat(((CollectionParameters) parametersCaptor.getValue().getApiParameters()).getQuery()).isEqualTo(optionalResourceQuery);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetCollectionWithRegex() throws MalformedJsonQueryException, UnsupportedEncodingException {
        Response testResponse = Response.ok().entity(TEST_OK).build();
        String query = "[{\"$like\":{\"name\":\"[A-Za-z]+[A-Za-z]\"}}]";
        JacksonQueryParser parser = createQueryParser();
        ResourceQuery resourceQuery = parser.parse(query);
        Optional<ResourceQuery> optionalResourceQuery = Optional.of(resourceQuery);
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);
        when(queryParserMock.parse(query)).thenReturn(resourceQuery);
        when(
                remMock.collection(Mockito.eq(TEST_TYPE), parametersCaptor.capture(), Mockito.any(URI.class),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(
                RULE.client().resource(COLLECTION_URI).queryParam("api:query", URLEncoder.encode(query, "UTF-8"))
                        .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(String.class)).isEqualTo(TEST_OK);
        assertThat(((CollectionParameters) parametersCaptor.getValue().getApiParameters()).getQuery()).isEqualTo(optionalResourceQuery);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testPostCollection() {
        Response testResponse = Response.ok().header("Location", RESOURCE_ID.getId()).entity(TEST_OK).build();
        // TODO Complete Pagination, Query and Sort
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);
        when(
                remMock.collection(Mockito.eq(TEST_TYPE), parametersCaptor.capture(), Mockito.any(URI.class),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(
                RULE.client().resource(COLLECTION_URI).type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                        .post(String.class, jsonTest)).isEqualTo(TEST_OK);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
        verify(eventBusMock).dispatch(ResourceEvent.createResourceEvent(TEST_TYPE, RESOURCE_ID.getId(), DOMAIN));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testDeleteCollection() {
        Response testResponse = Response.ok().entity(TEST_OK).build();
        // TODO Complete Pagination and Sort
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);

        when(
                remMock.collection(Mockito.eq(TEST_TYPE), parametersCaptor.capture(), Mockito.any(URI.class),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(RULE.client().resource(COLLECTION_URI).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).delete(String.class)).isEqualTo(
                TEST_OK);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetCollectionWithAggregation() throws UnsupportedEncodingException {
        String aggRequest = "{\"$count\":\"field\"}";
        Response testResponse = Response.ok().entity(TEST_OK).build();

        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);

        when(remMock.collection(Mockito.eq(TEST_TYPE), parametersCaptor.capture(), Mockito.any(URI.class), Mockito.eq(Optional.empty())))
                .thenReturn(testResponse);
        assertThat(
                RULE.client().resource(COLLECTION_URI).queryParam("api:aggregation", URLEncoder.encode(aggRequest, "UTF-8"))
                        .accept(MediaType.APPLICATION_JSON).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(String.class)).isEqualTo(
                TEST_OK);
        Aggregation operation = new Count("field");
        assertThat(((CollectionParameters) parametersCaptor.getValue().getApiParameters()).getAggregation()).isEqualTo(
                Optional.of(operation));
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @Test
    public void testGetCollectionWithBadJSONAggregation() {
        assertThat(
                RULE.client().resource(COLLECTION_URI).queryParam("api:aggregation", NO_JSON_DATA)
                        .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).head().getStatus()).isEqualTo(
                Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetCollectionWithErrorAggregation() throws UnsupportedEncodingException {
        String query = "{\"$operation\":\"field\"}";
        assertThat(
                RULE.client().resource(COLLECTION_URI).queryParam("api:aggregation", URLEncoder.encode(query, "UTF-8"))
                        .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).head().getStatus()).isEqualTo(
                Response.Status.BAD_REQUEST.getStatusCode());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetResource() {
        Response testResponse = Response.ok().entity(TEST_OK).build();
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);

        when(
                remMock.resource(Mockito.eq(TEST_TYPE), Mockito.eq(RESOURCE_ID), parametersCaptor.capture(),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(RULE.client().resource(RESOURCE_URI).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(String.class)).isEqualTo(TEST_OK);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetResourceWithWildCardId() {
        Response testResponse = Response.ok().entity(TEST_OK).build();
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);

        when(
                remMock.collection(Mockito.eq(TEST_TYPE), parametersCaptor.capture(), Mockito.any(URI.class),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(RULE.client().resource(RESOURCE_WILDCARD_URI).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(String.class))
                .isEqualTo(TEST_OK);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testCustomParam() {
        String uri = RESOURCE_URI + "?ns:param=1";
        Response testResponse = Response.ok().entity(TEST_OK).build();
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);

        when(
                remMock.resource(Mockito.eq(TEST_TYPE), Mockito.eq(RESOURCE_ID), parametersCaptor.capture(),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(RULE.client().resource(uri).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(String.class)).isEqualTo(TEST_OK);
        assertThat(parametersCaptor.getValue().getCustomParameterValue("ns:param")).isEqualTo("1");
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testDeleteResource() {
        Response testResponse = Response.ok().entity(TEST_OK).build();
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);

        when(
                remMock.resource(Mockito.eq(TEST_TYPE), Mockito.eq(RESOURCE_ID), parametersCaptor.capture(),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(RULE.client().resource(RESOURCE_URI).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).delete(String.class)).isEqualTo(
                TEST_OK);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
        verify(eventBusMock).dispatch(ResourceEvent.deleteResourceEvent(TEST_TYPE, RESOURCE_ID.getId(), DOMAIN));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testDeleteResourceWithWildCardId() {
        Response testResponse = Response.ok().entity(TEST_OK).build();
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);

        when(
                remMock.collection(Mockito.eq(TEST_TYPE), parametersCaptor.capture(), Mockito.any(URI.class),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(RULE.client().resource(RESOURCE_WILDCARD_URI).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).delete(String.class))
                .isEqualTo(TEST_OK);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testPutResource() {
        Response testResponse = Response.ok().entity(TEST_OK).build();
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);
        when(
                remMock.resource(Mockito.eq(TEST_TYPE), Mockito.eq(RESOURCE_ID), parametersCaptor.capture(),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(
                RULE.client().resource(RESOURCE_URI).type(MediaType.APPLICATION_JSON_TYPE).entity(jsonTest)
                        .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).put(String.class)).isEqualTo(TEST_OK);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
        verify(eventBusMock).dispatch(ResourceEvent.updateResourceEvent(TEST_TYPE, RESOURCE_ID.getId(), DOMAIN));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testPutResourceWithWildCardId() {
        Response testResponse = Response.ok().entity(TEST_OK).build();
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);
        when(
                remMock.collection(Mockito.eq(TEST_TYPE), parametersCaptor.capture(), Mockito.any(URI.class),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(
                RULE.client().resource(RESOURCE_WILDCARD_URI).type(MediaType.APPLICATION_JSON_TYPE).entity(jsonTest)
                        .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).put(String.class)).isEqualTo(TEST_OK);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetRelation() {
        Response testResponse = Response.ok().entity(TEST_OK).build();
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);

        when(
                remMock.relation(Mockito.eq(TEST_TYPE), Mockito.eq(RESOURCE_ID), Mockito.eq(TEST_REL), parametersCaptor.capture(),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(RULE.client().resource(RELATION_URI).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(String.class)).isEqualTo(TEST_OK);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetRelationWithWildCardId() {
        Response testResponse = Response.ok().entity(TEST_OK).build();
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);

        when(
                remMock.relation(Mockito.eq(TEST_TYPE), Mockito.eq(RESOURCE_WILDCARD_ID), Mockito.eq(TEST_REL), parametersCaptor.capture(),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(RULE.client().resource(RELATION_WILDCARD_URI).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(String.class))
                .isEqualTo(TEST_OK);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetRelationWithPaginationParameters() {
        Response testResponse = Response.ok().entity(TEST_OK).build();
        Pagination pagination = new Pagination(3, 20);
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);

        when(
                remMock.relation(Mockito.eq(TEST_TYPE), Mockito.eq(RESOURCE_ID), Mockito.eq(TEST_REL), parametersCaptor.capture(),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(
                RULE.client().resource(RELATION_URI).queryParam("api:pageSize", "20").queryParam("api:page", "3")
                        .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(String.class)).isEqualTo(TEST_OK);
        assertThat(((RelationParameters) parametersCaptor.getValue().getApiParameters()).getPagination()).isEqualTo(pagination);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetRelationWithPaginationDefaultsParameters() {
        Response testResponse = Response.ok().entity(TEST_OK).build();
        Pagination pagination = new Pagination(0, DEFAULT_LIMIT);
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);

        when(
                remMock.relation(Mockito.eq(TEST_TYPE), Mockito.eq(RESOURCE_ID), Mockito.eq(TEST_REL), parametersCaptor.capture(),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(RULE.client().resource(RELATION_URI).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(String.class)).isEqualTo(TEST_OK);
        assertThat(((RelationParameters) parametersCaptor.getValue().getApiParameters()).getPagination()).isEqualTo(pagination);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetRelationWithIllegalPaginationParameters() {
        Response testResponse = Response.status(Response.Status.BAD_REQUEST).build();
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);

        when(
                remMock.relation(Mockito.eq(TEST_TYPE), Mockito.eq(RESOURCE_ID), Mockito.eq(TEST_REL), parametersCaptor.capture(),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(RULE.client().resource(RELATION_URI).queryParam("api:limit", "-20")

        .queryParam("api:page", "3").header(AUTHORIZATION, "Bearer " + TEST_TOKEN).head().getStatus()).isEqualTo(testResponse.getStatus());
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetRelationWithSort() throws IOException {
        String sortRequest = "{\"price\":\"asc\"}";
        Sort sort = new Sort("asc", "price");
        Optional<Sort> optionalSort = Optional.of(sort);
        Response testResponse = Response.ok().entity(TEST_OK).build();
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);

        when(
                remMock.relation(Mockito.eq(TEST_TYPE), Mockito.eq(RESOURCE_ID), Mockito.eq(TEST_REL), parametersCaptor.capture(),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(
                RULE.client().resource(RELATION_URI).queryParam("api:sort", URLEncoder.encode(sortRequest, "UTF-8"))
                        .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(String.class)).isEqualTo(TEST_OK);
        assertThat(((RelationParameters) parametersCaptor.getValue().getApiParameters()).getSort()).isEqualTo(optionalSort);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @Test
    public void testGetRelationWithBadJsonSort() {
        assertThat(
                RULE.client().resource(RELATION_URI).queryParam("api:sort", NO_JSON_DATA).header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                        .head().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetRelationWithErrorSort() throws UnsupportedEncodingException {
        assertThat(
                RULE.client().resource(RELATION_URI).queryParam("api:sort", URLEncoder.encode("{\"price\":\"Bad Order Method\"}", "UTF-8"))
                        .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).head().getStatus()).isEqualTo(
                Response.Status.BAD_REQUEST.getStatusCode());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetRelationWithQuery() throws MalformedJsonQueryException, UnsupportedEncodingException {
        Response testResponse = Response.ok().entity(TEST_OK).build();
        String query = "[{\"$eq\":{\"artist\":\"Metallica\"}}]";
        JacksonQueryParser parser = createQueryParser();
        ResourceQuery resourceQuery = parser.parse(query);
        Optional<ResourceQuery> optionalResourceQuery = Optional.of(resourceQuery);
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);

        when(queryParserMock.parse(query)).thenReturn(resourceQuery);
        when(
                remMock.relation(Mockito.eq(TEST_TYPE), Mockito.eq(RESOURCE_ID), Mockito.eq(TEST_REL), parametersCaptor.capture(),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(
                RULE.client().resource(RELATION_URI).queryParam("api:query", URLEncoder.encode(query, "UTF-8"))
                        .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(String.class)).isEqualTo(TEST_OK);
        assertThat(((RelationParameters) parametersCaptor.getValue().getApiParameters()).getQuery()).isEqualTo(optionalResourceQuery);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @Test
    public void testGetRelationWithMalformedQuery() throws MalformedJsonQueryException, UnsupportedEncodingException {
        String query = "[{\"$in\":{\"artist\":\"Helloween\"}}]";
        when(queryParserMock.parse(query)).thenThrow(new MalformedJsonQueryException(""));
        assertThat(
                RULE.client().resource(RELATION_URI).queryParam("api:query", URLEncoder.encode(query, "UTF-8"))
                        .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).head().getStatus()).isEqualTo(
                Response.Status.BAD_REQUEST.getStatusCode());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    // Test TODO JACKSON
    public void testPutRelation() {
        String uri = RELATION_URI + ";r=test:Entity%2F654"; // %2F => /
        Response testResponse = Response.noContent().build();
        // TODO Complete Pagination, Query and Sort
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);
        when(
                remMock.relation(Mockito.eq(TEST_TYPE), Mockito.eq(RESOURCE_ID), Mockito.eq(TEST_REL), parametersCaptor.capture(),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(
                RULE.client().resource(uri).type(MediaType.APPLICATION_JSON_TYPE).header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                        .put(ClientResponse.class).getStatus()).isEqualTo(204);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    // Test TODO JACKSON
    public void testPutRelationWithWildCardId() {
        String uri = RELATION_WILDCARD_URI + ";r=test:Entity%2F654"; // %2F => /
        Response testResponse = Response.noContent().build();
        // TODO Complete Pagination, Query and Sort
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);
        when(
                remMock.relation(Mockito.eq(TEST_TYPE), Mockito.eq(RESOURCE_WILDCARD_ID), Mockito.eq(TEST_REL), parametersCaptor.capture(),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(
                RULE.client().resource(uri).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).type(MediaType.APPLICATION_JSON_TYPE)
                        .put(ClientResponse.class).getStatus()).isEqualTo(204);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    // Test TODO JACKSON
    public void testPutRelationWithNginxEncoding() {
        String uri = RELATION_URI + ";r=test:Entity/654"; // %2F => /
        Response testResponse = Response.noContent().build();
        // TODO Complete Pagination, Query and Sort
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);
        when(
                remMock.relation(Mockito.eq(TEST_TYPE), Mockito.eq(RESOURCE_ID), Mockito.eq(TEST_REL), parametersCaptor.capture(),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(
                RULE.client().resource(uri).header(AUTHORIZATION, "Bearer " + TEST_TOKEN).type(MediaType.APPLICATION_JSON_TYPE)
                        .put(ClientResponse.class).getStatus()).isEqualTo(204);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testPutRelationWithNginxEncoding2() {
        String relUri = "/resource/test:TestObject/testId/test:ToRelationObject;r=test:ToRelationObject/testToRelationId";
        Response testResponse = Response.noContent().build();
        // TODO Complete Pagination, Query and Sort
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);
        when(
                remMock.relation(Mockito.eq("test:TestObject"), Mockito.eq(new ResourceId("testId")), Mockito.eq("test:ToRelationObject"),
                        parametersCaptor.capture(), optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(
                RULE.client().resource("/" + ApiVersion.CURRENT + relUri).header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                        .delete(ClientResponse.class).getStatus()).isEqualTo(204);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testDeleteRelation() {
        Response testResponse = Response.ok().entity(TEST_OK).build();
        // TODO Complete Pagination and Sort
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);

        when(
                remMock.relation(Mockito.eq(TEST_TYPE), Mockito.eq(RESOURCE_ID), Mockito.eq(TEST_REL), parametersCaptor.capture(),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(
                RULE.client().resource(RELATION_URI).type(MediaType.APPLICATION_JSON_TYPE).entity(jsonTest)
                        .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).delete(String.class)).isEqualTo(TEST_OK);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testDeleteRelationWithWildCardId() {
        Response testResponse = Response.ok().entity(TEST_OK).build();
        // TODO Complete Pagination and Sort
        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);

        when(
                remMock.relation(Mockito.eq(TEST_TYPE), Mockito.eq(RESOURCE_WILDCARD_ID), Mockito.eq(TEST_REL), parametersCaptor.capture(),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(
                RULE.client().resource(RELATION_WILDCARD_URI).type(MediaType.APPLICATION_JSON_TYPE).entity(jsonTest)
                        .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).delete(String.class)).isEqualTo(TEST_OK);
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @Test
    public void testNotFoundRem() {
        String collectionUri = "/resource/test:SomeEntity";
        assertThat(
                RULE.client().resource("/" + ApiVersion.CURRENT + collectionUri).header(AUTHORIZATION, "Bearer " + TEST_TOKEN)
                        .get(ClientResponse.class).getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetRelationWithAggregation() throws IOException {
        String aggRequest = "{\"$count\":\"field\"}";
        Response testResponse = Response.ok().entity(TEST_OK).build();

        ArgumentCaptor<RequestParameters> parametersCaptor = ArgumentCaptor.forClass(RequestParameters.class);
        ArgumentCaptor<Optional> optionalJsonObjectCaptor = ArgumentCaptor.forClass(Optional.class);

        when(
                remMock.relation(Mockito.eq(TEST_TYPE), Mockito.eq(RESOURCE_ID), Mockito.eq(TEST_REL), parametersCaptor.capture(),
                        optionalJsonObjectCaptor.capture())).thenReturn(testResponse);
        assertThat(
                RULE.client().resource(RELATION_URI).queryParam("api:aggregation", URLEncoder.encode(aggRequest, "UTF-8"))
                        .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).get(String.class)).isEqualTo(TEST_OK);
        Aggregation operation = new Count("field");
        assertThat(((RelationParameters) parametersCaptor.getValue().getApiParameters()).getAggregation())
                .isEqualTo(Optional.of(operation));
        assertThat(parametersCaptor.getValue().getTokenInfo().getUserId()).isSameAs(TEST_USER_ID);
    }

    @Test
    public void testGetRelationWithBadJsonAggregation() {
        assertThat(
                RULE.client().resource(RELATION_URI).queryParam("api:aggregation", NO_JSON_DATA)
                        .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).head().getStatus()).isEqualTo(
                Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetRelationWithErrorAggregation() throws UnsupportedEncodingException {
        assertThat(
                RULE.client().resource(RELATION_URI)
                        .queryParam("api:aggregation", URLEncoder.encode("{\"$operation\":\"field\"}", "UTF-8"))
                        .header(AUTHORIZATION, "Bearer " + TEST_TOKEN).head().getStatus()).isEqualTo(
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}
