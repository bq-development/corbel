package com.bq.oss.corbel.resources.rem.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.mongodb.core.index.Index;

import com.bq.oss.corbel.resources.rem.dao.NotFoundException;
import com.bq.oss.corbel.resources.rem.dao.RelationMoveOperation;
import com.bq.oss.corbel.resources.rem.dao.ResmiDao;
import com.bq.oss.corbel.resources.rem.model.ResourceUri;
import com.bq.oss.corbel.resources.rem.model.SearchableFields;
import com.bq.oss.corbel.resources.rem.request.CollectionParameters;
import com.bq.oss.corbel.resources.rem.request.RelationParameters;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.resmi.exception.StartsWithUnderscoreException;
import com.bq.oss.corbel.resources.rem.search.ResmiSearch;
import com.bq.oss.lib.queries.request.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
    ResourceUri RESOURCE_URI = new ResourceUri(TYPE);
    String ID = "test";
    int PAGE = 2;
    int SIZE = 4;
    String USER_ID = "123";

    String RELATION_URI = "RELATION_URI";

    @Mock ResmiDao resmiDao;
    @Mock ResmiSearch resmiSearch;
    @Mock SearchableFieldsRegistry searchableFieldRegistry;
    CollectionParameters collectionParametersMock;
    @Mock ResourceQuery resourceQueryMock;
    @Mock List<ResourceQuery> resourceQueriesMock;
    @Mock ResourceSearch resourceSearchMock;
    @Mock Pagination paginationMock;
    @Mock Sort sortMock;
    @Mock RelationParameters relationParametersMock;
    private DefaultResmiService defaultResmiService;

    @Before
    public void setup() {
        defaultResmiService = new DefaultResmiService(resmiDao, resmiSearch, searchableFieldRegistry, Clock.systemUTC());
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
        JsonArray fakeResult = new JsonArray();
        when(resmiDao.find(eq("resource:TYPE"), eq(Optional.of(resourceQueriesMock)), eq(paginationMock), eq(Optional.of(sortMock))))
                .thenReturn(fakeResult);
        when(collectionParametersMock.getSearch()).thenReturn(Optional.empty());
        JsonArray result = defaultResmiService.find("resource:TYPE", collectionParametersMock);
        assertThat(fakeResult).isEqualTo(result);
    }

    @Test
    public void findWithSearchTest() throws BadConfigurationException {
        JsonArray fakeResult = new JsonArray();
        String search = "my+search";
        when(paginationMock.getPage()).thenReturn(PAGE);
        when(paginationMock.getPageSize()).thenReturn(SIZE);
        when(resourceSearchMock.getSearch()).thenReturn(search);
        when(searchableFieldRegistry.getFieldsFromResourceUri(eq(RESOURCE_URI))).thenReturn(new HashSet(Arrays.asList("t1", "t2")));
        when(resmiSearch.search(eq(RESOURCE_URI), eq(search), eq(PAGE), eq(SIZE))).thenReturn(fakeResult);
        JsonArray result = defaultResmiService.find(TYPE, collectionParametersMock);
        assertThat(fakeResult).isEqualTo(result);
    }

    @Test
    public void findResourceByIdTest() throws BadConfigurationException {
        ResourceId resourceId = new ResourceId(ID);
        JsonObject fakeResult = new JsonObject();
        when(resmiDao.findById(eq(TYPE), eq(ID))).thenReturn(fakeResult);
        when(collectionParametersMock.getSearch()).thenReturn(Optional.empty());
        JsonObject result = defaultResmiService.findResourceById(TYPE, resourceId);
        assertThat(fakeResult).isEqualTo(result);
    }

    @Test
    public void findRelationTest() throws BadConfigurationException {
        ResourceId resourceId = new ResourceId(ID);
        JsonElement fakeResult = new JsonObject();
        when(
                resmiDao.findRelation(eq(TYPE), eq(resourceId), eq(RELATION_TYPE), eq(Optional.of(resourceQueriesMock)),
                        eq(paginationMock), eq(Optional.of(sortMock)), eq(Optional.of("test")))).thenReturn(fakeResult);
        when(collectionParametersMock.getSearch()).thenReturn(Optional.empty());
        when(relationParametersMock.getPredicateResource()).thenReturn(Optional.of("test"));
        JsonElement result = defaultResmiService.findRelation(TYPE, resourceId, RELATION_TYPE, relationParametersMock);
        assertThat(fakeResult).isEqualTo(result);
    }

    @Test
    public void countCollectionTest() throws BadConfigurationException {
        AggregationResult fakeResult = new CountResult();
        when(resmiDao.count(eq(new ResourceUri(TYPE)), eq(resourceQueriesMock))).thenReturn((CountResult) fakeResult);
        when(collectionParametersMock.getAggregation()).thenReturn(Optional.of(new Count("*")));
        when(collectionParametersMock.getSearch()).thenReturn(Optional.empty());
        AggregationResult result = defaultResmiService.aggregate(new ResourceUri(TYPE), collectionParametersMock);
        assertThat(fakeResult).isEqualTo(result);
    }

    @Test
    public void countRelationTest() {
        AggregationResult fakeResult = new CountResult();
        ResourceUri resourceUri = new ResourceUri(TYPE, ID, RELATION_TYPE);
        when(resmiDao.count(eq(resourceUri), eq(resourceQueriesMock))).thenReturn((CountResult) fakeResult);
        when(collectionParametersMock.getAggregation()).thenReturn(Optional.of(new Count("*")));
        when(collectionParametersMock.getSearch()).thenReturn(Optional.empty());
        AggregationResult result = defaultResmiService.aggregate(resourceUri, relationParametersMock);
        assertThat(fakeResult).isEqualTo(result);
    }

    @Test
    public void saveResourceTest() throws StartsWithUnderscoreException {
        JsonObject fakeResult = new JsonObject();
        defaultResmiService.save(TYPE, fakeResult, Optional.of(USER_ID));
        assertThat(fakeResult.get(DefaultResmiService.ID).getAsString()).startsWith(USER_ID);
        assertThat(fakeResult.get(_CREATED_AT)).isNotNull();
        assertThat(fakeResult.get(_UPDATED_AT)).isNotNull();
        verify(resmiDao).save(TYPE, fakeResult);
    }

    @Test
    public void saveResourceWithCreatedAtTest() throws StartsWithUnderscoreException, ParseException {
        JsonObject fakeResult = new JsonObject();
        fakeResult.addProperty(_CREATED_AT, DATE);
        fakeResult.addProperty(_UPDATED_AT, DATE);
        defaultResmiService.save(TYPE, fakeResult, Optional.of(USER_ID));
        assertThat(fakeResult.get(DefaultResmiService.ID).getAsString()).startsWith(USER_ID);
        assertThat(extractMillis(fakeResult.get(_CREATED_AT).getAsString())).isEqualTo(DATE);
        assertThat(extractMillis(fakeResult.get(_UPDATED_AT).getAsString())).isNotEqualTo(DATE);
        verify(resmiDao).save(TYPE, fakeResult);
    }

    @Test(expected = StartsWithUnderscoreException.class)
    public void saveResourceWithUnderscoreTest() throws StartsWithUnderscoreException {
        JsonObject fakeResult = new JsonObject();
        fakeResult.add("_test", new JsonPrimitive("123"));
        defaultResmiService.save(TYPE, fakeResult, Optional.of(USER_ID));
    }

    @Test
    public void upsertTest() throws StartsWithUnderscoreException {
        String id = "123";
        JsonObject fakeResult = new JsonObject();
        defaultResmiService.upsert(TYPE, id, fakeResult);
        assertThat(fakeResult.get(_CREATED_AT)).isNotNull();
        assertThat(fakeResult.get(_UPDATED_AT)).isNotNull();
        verify(resmiDao).upsert(TYPE, id, fakeResult);
    }

    @Test
    public void upsertWithDatesTest() throws StartsWithUnderscoreException, ParseException {
        String id = "123";
        JsonObject fakeResult = new JsonObject();
        fakeResult.addProperty(_CREATED_AT, DATE);
        fakeResult.addProperty(_UPDATED_AT, DATE);
        defaultResmiService.upsert(TYPE, id, fakeResult);
        assertThat(extractMillis(fakeResult.get(_CREATED_AT).getAsString())).isEqualTo(DATE);
        assertThat(extractMillis(fakeResult.get(_UPDATED_AT).getAsString())).isNotEqualTo(DATE);
        verify(resmiDao).upsert(TYPE, id, fakeResult);
    }

    @Test(expected = StartsWithUnderscoreException.class)
    public void upsertWithUnderscoreTest() throws StartsWithUnderscoreException {
        String id = "123";
        JsonObject fakeResult = new JsonObject();
        fakeResult.add("_test", new JsonPrimitive("123"));
        defaultResmiService.upsert(TYPE, id, fakeResult);
    }

    @Test
    public void createRelationTest() throws NotFoundException, StartsWithUnderscoreException {
        String resourceId = "test";
        JsonObject jsonObject = new JsonObject();
        defaultResmiService.createRelation(TYPE, resourceId, RELATION_TYPE, RELATION_URI, jsonObject);
        assertThat(jsonObject.get(_CREATED_AT)).isNotNull();
        assertThat(jsonObject.get(_UPDATED_AT)).isNotNull();
        verify(resmiDao).createRelation(TYPE, resourceId, RELATION_TYPE, RELATION_URI, jsonObject);
    }

    @Test
    public void createRelationWithCreatedAtTest() throws NotFoundException, StartsWithUnderscoreException, ParseException {
        String resourceId = "test";
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(_CREATED_AT, DATE);
        jsonObject.addProperty(_UPDATED_AT, DATE);
        defaultResmiService.createRelation(TYPE, resourceId, RELATION_TYPE, RELATION_URI, jsonObject);
        assertThat(extractMillis(jsonObject.get(_CREATED_AT).getAsString())).isEqualTo(DATE);
        assertThat(extractMillis(jsonObject.get(_UPDATED_AT).getAsString())).isNotEqualTo(DATE);
        verify(resmiDao).createRelation(TYPE, resourceId, RELATION_TYPE, RELATION_URI, jsonObject);
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
        defaultResmiService.createRelation(TYPE, resourceId, RELATION_TYPE, RELATION_URI, jsonObject);
    }

    @Test
    public void moveElementTest() throws NotFoundException {
        ResourceId resourceId = new ResourceId(ID);
        RelationMoveOperation relationMoveOperation = new RelationMoveOperation(1);

        defaultResmiService.moveElement(TYPE, resourceId, RELATION_TYPE, RELATION_URI, relationMoveOperation);
        verify(resmiDao).moveElement(TYPE, ID, RELATION_TYPE, RELATION_URI, relationMoveOperation);
    }

    @Test
    public void deleteResourceByIdTest() throws NotFoundException {
        defaultResmiService.deleteResourceById(TYPE, ID);
        verify(resmiDao).deleteById(TYPE, ID);
        verify(resmiSearch, times(0)).deleteDocument(any());
    }

    @Test
    public void deleteIndexedResourceByIdTest() throws NotFoundException {
        when(searchableFieldRegistry.getFieldsFromType(eq(TYPE))).thenReturn(new HashSet(Arrays.asList("t1", "t2")));
        defaultResmiService.deleteResourceById(TYPE, ID);
        verify(resmiDao).deleteById(TYPE, ID);
        verify(resmiSearch).deleteDocument(eq(new ResourceUri(TYPE, ID)));
    }

    @Test
    public void deleteRelationTest() throws NotFoundException {
        ResourceId resourceId = new ResourceId(ID);
        Optional<String> dstId = Optional.of("dst");
        defaultResmiService.deleteRelation(TYPE, resourceId, RELATION_TYPE, dstId);
        verify(resmiDao).deleteRelation(TYPE, resourceId, RELATION_TYPE, dstId);
    }

    @Test
    public void getSearchableFieldsTest() {
        defaultResmiService.getSearchableFields();
        verify(resmiDao).findAll(DefaultResmiService.SEARCHABLE_FIELDS, SearchableFields.class);
    }

    @Test
    public void addSearchableFieldsTest() {
        SearchableFields searchableFields = new SearchableFields(TYPE, new HashSet(Arrays.asList("t1", "t2")));
        defaultResmiService.addSearchableFields(searchableFields);
        verify(resmiDao).save(DefaultResmiService.SEARCHABLE_FIELDS, searchableFields);
    }

    @Test
    public void ensureCollectionIndexTest() throws NotFoundException {
        Index index = mock(Index.class);
        defaultResmiService.ensureCollectionIndex(TYPE, index);
        verify(resmiDao).ensureCollectionIndex(TYPE, index);
    }

    @Test
    public void ensureRelationIndexTest() throws NotFoundException {
        Index index = mock(Index.class);
        defaultResmiService.ensureRelationIndex(TYPE, RELATION_TYPE, index);
        verify(resmiDao).ensureRelationIndex(TYPE, RELATION_TYPE, index);
    }

    @Test
    public void averageTest() {
        AggregationResult fakeResult = new AverageResult();
        ResourceUri resourceUri = new ResourceUri(TYPE);
        when(resmiDao.average(eq(resourceUri), eq(resourceQueriesMock), eq("testField"))).thenReturn((AverageResult) fakeResult);
        when(collectionParametersMock.getAggregation()).thenReturn(Optional.of(new Average("testField")));
        when(collectionParametersMock.getSearch()).thenReturn(Optional.empty());
        AggregationResult result = defaultResmiService.aggregate(resourceUri, collectionParametersMock);
        assertThat(result).isEqualTo(fakeResult);
    }
}
