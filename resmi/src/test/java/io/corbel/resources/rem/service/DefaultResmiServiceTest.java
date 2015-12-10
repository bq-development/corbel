package io.corbel.resources.rem.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.mongodb.core.index.Index;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import io.corbel.lib.queries.request.*;
import io.corbel.resources.rem.dao.NotFoundException;
import io.corbel.resources.rem.dao.RelationMoveOperation;
import io.corbel.resources.rem.dao.ResmiDao;
import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.resmi.exception.MongoAggregationException;
import io.corbel.resources.rem.resmi.exception.StartsWithUnderscoreException;

/**
 * @author Francisco Sanchez
 */
@RunWith(MockitoJUnitRunner.class) public class DefaultResmiServiceTest {

    private static final long DATE = 1234;
    private static final String _UPDATED_AT = "_updatedAt";
    private static final String _CREATED_AT = "_createdAt";

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.forLanguageTag("ES"));

    String TYPE = "resource:TYPE";
    String RELATION_TYPE = "relation:TYPE";
    String ID = "test";
    String USER_ID = "123";

    String RELATION_URI = "RELATION_URI";

    @Mock ResmiDao resmiDao;
    CollectionParameters collectionParametersMock;
    @Mock ResourceQuery resourceQueryMock;
    @Mock List<ResourceQuery> resourceQueriesMock;
    @Mock Search resourceSearchMock;
    @Mock Pagination paginationMock;
    @Mock Sort sortMock;
    @Mock RelationParameters relationParametersMock;
    private DefaultResmiService defaultResmiService;

    @Before
    public void setup() {
        defaultResmiService = new DefaultResmiService(resmiDao, Clock.systemUTC());
        when(relationParametersMock.getAggregation()).thenReturn(Optional.empty());
        when(relationParametersMock.getQueries()).thenReturn(Optional.ofNullable(resourceQueriesMock));
        when(relationParametersMock.getQueries()).thenReturn(Optional.ofNullable(resourceQueriesMock));
        when(relationParametersMock.getSearch()).thenReturn(Optional.ofNullable(resourceSearchMock));
        when(relationParametersMock.getPagination()).thenReturn(paginationMock);
        when(relationParametersMock.getSort()).thenReturn(Optional.ofNullable(sortMock));
        collectionParametersMock = relationParametersMock;
        reset(resmiDao);
    }

    @Test
    public void findTest() throws BadConfigurationException {
        ResourceUri resourceUri = new ResourceUri(TYPE);
        JsonArray fakeResult = new JsonArray();
        when(
                resmiDao.findCollection(eq(resourceUri), eq(Optional.of(resourceQueriesMock)), eq(Optional.of(paginationMock)),
                        eq(Optional.of(sortMock)))).thenReturn(fakeResult);
        when(collectionParametersMock.getSearch()).thenReturn(Optional.empty());
        JsonArray result = defaultResmiService.findCollection(resourceUri, Optional.of(collectionParametersMock));
        assertThat(fakeResult).isEqualTo(result);
    }

    @Test
    public void findResourceByIdTest() throws BadConfigurationException {
        ResourceUri resourceUri = new ResourceUri(TYPE, ID);

        JsonObject fakeResult = new JsonObject();
        when(resmiDao.findResource(eq(resourceUri))).thenReturn(fakeResult);
        when(collectionParametersMock.getSearch()).thenReturn(Optional.empty());

        JsonObject result = defaultResmiService.findResource(resourceUri);
        assertThat(fakeResult).isEqualTo(result);
    }

    @Test
    public void findRelationTest() throws BadConfigurationException {
        JsonElement fakeResult = new JsonObject();
        ResourceUri resourceUri = new ResourceUri(TYPE, ID, RELATION_TYPE, "test");

        when(
                resmiDao.findRelation(eq(resourceUri), eq(Optional.of(resourceQueriesMock)), eq(Optional.of(paginationMock)),
                        eq(Optional.of(sortMock)))).thenReturn(fakeResult);
        when(collectionParametersMock.getSearch()).thenReturn(Optional.empty());
        when(relationParametersMock.getPredicateResource()).thenReturn(Optional.of("test"));

        JsonElement result = defaultResmiService.findRelation(resourceUri, Optional.of(relationParametersMock));
        assertThat(fakeResult).isEqualTo(result);
    }

    @Test
    public void countCollectionTest() throws BadConfigurationException, MongoAggregationException {
        JsonElement fakeResult = new JsonObject();
        when(resmiDao.count(eq(new ResourceUri(TYPE)), eq(resourceQueriesMock))).thenReturn(fakeResult);
        when(collectionParametersMock.getAggregation()).thenReturn(Optional.of(new Count("*")));
        when(collectionParametersMock.getSearch()).thenReturn(Optional.empty());
        JsonElement result = defaultResmiService.aggregate(new ResourceUri(TYPE), collectionParametersMock);
        assertThat(fakeResult).isEqualTo(result);
    }

    @Test
    public void countRelationTest() throws BadConfigurationException, MongoAggregationException {
        JsonElement fakeResult = new JsonObject();
        ResourceUri resourceUri = new ResourceUri(TYPE, ID, RELATION_TYPE);
        when(resmiDao.count(eq(resourceUri), eq(resourceQueriesMock))).thenReturn(fakeResult);
        when(collectionParametersMock.getAggregation()).thenReturn(Optional.of(new Count("*")));
        when(collectionParametersMock.getSearch()).thenReturn(Optional.empty());
        JsonElement result = defaultResmiService.aggregate(resourceUri, relationParametersMock);
        assertThat(fakeResult).isEqualTo(result);
    }

    @Test
    public void saveResourceTest() throws StartsWithUnderscoreException {
        ResourceUri uri = new ResourceUri(TYPE);
        JsonObject fakeResult = new JsonObject();
        defaultResmiService.saveResource(uri, fakeResult, Optional.of(USER_ID));
        assertThat(fakeResult.get(DefaultResmiService.ID).getAsString()).startsWith(USER_ID);
        assertThat(fakeResult.get(_CREATED_AT)).isNotNull();
        assertThat(fakeResult.get(_UPDATED_AT)).isNotNull();
        verify(resmiDao).saveResource(uri, fakeResult);
    }

    @Test(expected = StartsWithUnderscoreException.class)
    public void saveResourceWithUnderscoreTest() throws StartsWithUnderscoreException {
        ResourceUri uri = new ResourceUri(TYPE);
        JsonObject fakeResult = new JsonObject();
        fakeResult.add("_test", new JsonPrimitive("123"));
        defaultResmiService.saveResource(uri, fakeResult, Optional.of(USER_ID));
    }

    @Test
    public void upsertTest() throws StartsWithUnderscoreException {
        String id = "123";
        ResourceUri resourceUri = new ResourceUri(TYPE, id);
        JsonObject fakeResult = new JsonObject();
        defaultResmiService.updateResource(resourceUri, fakeResult);
        assertThat(fakeResult.get(_CREATED_AT)).isNotNull();
        assertThat(fakeResult.get(_UPDATED_AT)).isNotNull();
        verify(resmiDao).updateResource(resourceUri, fakeResult);
    }

    @Test
    public void upsertWithDatesTest() throws StartsWithUnderscoreException, ParseException {
        String id = "123";
        ResourceUri resourceUri = new ResourceUri(TYPE, id);
        JsonObject fakeResult = new JsonObject();
        fakeResult.addProperty(_CREATED_AT, DATE);
        fakeResult.addProperty(_UPDATED_AT, DATE);
        defaultResmiService.updateResource(resourceUri, fakeResult);
        assertThat(fakeResult.get(_CREATED_AT)).isNotNull();
        assertThat(extractMillis(fakeResult.get(_UPDATED_AT).getAsString())).isNotEqualTo(DATE);
        verify(resmiDao).updateResource(resourceUri, fakeResult);
    }

    @Test(expected = StartsWithUnderscoreException.class)
    public void upsertWithUnderscoreTest() throws StartsWithUnderscoreException {
        String id = "123";
        ResourceUri resourceUri = new ResourceUri(TYPE, id);

        JsonObject fakeResult = new JsonObject();
        fakeResult.add("_test", new JsonPrimitive("123"));
        defaultResmiService.updateResource(resourceUri, fakeResult);
    }

    @Test
    public void createRelationTest() throws NotFoundException, StartsWithUnderscoreException {
        String resourceId = "test";
        JsonObject jsonObject = new JsonObject();
        ResourceUri resourceUri = new ResourceUri(TYPE, resourceId, RELATION_TYPE, RELATION_URI);
        defaultResmiService.createRelation(resourceUri, jsonObject);
        assertThat(jsonObject.get(_CREATED_AT)).isNotNull();
        assertThat(jsonObject.get(_UPDATED_AT)).isNotNull();
        verify(resmiDao).createRelation(resourceUri, jsonObject);
    }

    private long extractMillis(String date) throws ParseException {
        date = date.replace("ISODate(", "").replace(")", "").replace("T", " ").replace("Z", "");
        return formatter.parse(date).getTime();
    }

    @Test(expected = StartsWithUnderscoreException.class)
    public void createRelationWithUnderscoreTest() throws NotFoundException, StartsWithUnderscoreException {
        String resourceId = "test";
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("_test", new JsonPrimitive("123"));
        ResourceUri resourceUri = new ResourceUri(TYPE, resourceId, RELATION_TYPE, RELATION_URI);
        defaultResmiService.createRelation(resourceUri, jsonObject);
    }

    @Test
    public void moveElementTest() throws NotFoundException {
        ResourceUri resourceUri = new ResourceUri(TYPE, ID, RELATION_TYPE, RELATION_URI);

        RelationMoveOperation relationMoveOperation = new RelationMoveOperation(1);

        defaultResmiService.moveRelation(resourceUri, relationMoveOperation);
        verify(resmiDao).moveRelation(resourceUri, relationMoveOperation);
    }

    @Test
    public void deleteResourceByIdTest() throws NotFoundException {
        ResourceUri uri = new ResourceUri(TYPE, ID);
        defaultResmiService.deleteResource(uri);
        verify(resmiDao).deleteResource(uri);
    }

    @Test
    public void deleteRelationTest() throws NotFoundException {
        ResourceUri uri = new ResourceUri(TYPE, ID, RELATION_TYPE, "dst");
        defaultResmiService.deleteRelation(uri, Optional.empty());
        verify(resmiDao).deleteRelation(uri, Optional.empty());
    }

    @Test
    public void ensureCollectionIndexTest() throws NotFoundException {
        Index index = mock(Index.class);
        ResourceUri resourceUri = new ResourceUri(TYPE);
        defaultResmiService.ensureIndex(resourceUri, index);
        verify(resmiDao).ensureIndex(resourceUri, index);
    }

    @Test
    public void ensureRelationIndexTest() throws NotFoundException {
        Index index = mock(Index.class);
        ResourceUri resourceUri = new ResourceUri(TYPE).setRelation(RELATION_TYPE);
        defaultResmiService.ensureIndex(resourceUri, index);
        verify(resmiDao).ensureIndex(resourceUri, index);
    }

    @Test
    public void averageTest() throws BadConfigurationException, MongoAggregationException {
        JsonElement fakeResult = new JsonObject();
        ResourceUri resourceUri = new ResourceUri(TYPE);
        when(resmiDao.average(eq(resourceUri), eq(resourceQueriesMock), eq("testField"))).thenReturn(fakeResult);
        when(collectionParametersMock.getAggregation()).thenReturn(Optional.of(new Average("testField")));
        when(collectionParametersMock.getSearch()).thenReturn(Optional.empty());
        JsonElement result = defaultResmiService.aggregate(resourceUri, collectionParametersMock);
        assertThat(result).isEqualTo(fakeResult);
    }

    @Test
    public void maxTest() throws BadConfigurationException, MongoAggregationException {
        JsonElement fakeResult = new JsonObject();
        ResourceUri resourceUri = new ResourceUri(TYPE);
        when(resmiDao.max(eq(resourceUri), eq(resourceQueriesMock), eq("testField"))).thenReturn(fakeResult);
        when(collectionParametersMock.getAggregation()).thenReturn(Optional.of(new Max("testField")));
        when(collectionParametersMock.getSearch()).thenReturn(Optional.empty());
        JsonElement result = defaultResmiService.aggregate(resourceUri, collectionParametersMock);
        assertThat(result).isEqualTo(fakeResult);
    }

    @Test
    public void minTest() throws BadConfigurationException, MongoAggregationException {
        JsonElement fakeResult = new JsonObject();
        ResourceUri resourceUri = new ResourceUri(TYPE);
        when(resmiDao.min(eq(resourceUri), eq(resourceQueriesMock), eq("testField"))).thenReturn(fakeResult);
        when(collectionParametersMock.getAggregation()).thenReturn(Optional.of(new Min("testField")));
        when(collectionParametersMock.getSearch()).thenReturn(Optional.empty());
        JsonElement result = defaultResmiService.aggregate(resourceUri, collectionParametersMock);
        assertThat(result).isEqualTo(fakeResult);
    }
}
