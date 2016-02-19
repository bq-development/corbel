package io.corbel.resources.rem.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Search;
import io.corbel.lib.queries.request.Sort;
import io.corbel.resources.rem.dao.NotFoundException;
import io.corbel.resources.rem.dao.ResmiDao;
import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.resmi.exception.InvalidApiParamException;
import io.corbel.resources.rem.resmi.exception.StartsWithUnderscoreException;
import io.corbel.resources.rem.search.ResmiSearch;

import java.time.Clock;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author Francisco Sanchez
 */
@RunWith(MockitoJUnitRunner.class) public class WithSearchResmiServiceTest {

    String DOMAIN = "DOMAIN";
    String TYPE = "resource:TYPE";
    ResourceUri RESOURCE_URI = new ResourceUri(DOMAIN, TYPE);
    String RELATION = "resource:RELATION";
    String ID = "test";
    int PAGE = 2;
    int SIZE = 4;

    Gson gson = new Gson();

    @Mock ResmiDao resmiDao;
    @Mock ResmiSearch resmiSearch;
    @Mock SearchableFieldsRegistry searchableFieldRegistry;
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
        defaultResmiService = new WithSearchResmiService(resmiDao, resmiSearch, searchableFieldRegistry, gson, Clock.systemUTC());
        when(relationParametersMock.getAggregation()).thenReturn(Optional.empty());
        when(relationParametersMock.getQueries()).thenReturn(Optional.ofNullable(resourceQueriesMock));
        when(relationParametersMock.getSearch()).thenReturn(Optional.ofNullable(resourceSearchMock));
        when(relationParametersMock.getPagination()).thenReturn(paginationMock);
        when(relationParametersMock.getSort()).thenReturn(Optional.ofNullable(sortMock));
        collectionParametersMock = relationParametersMock;
        reset(resmiDao);
    }

    @Test
    public void findWithSearchTest() throws BadConfigurationException, InvalidApiParamException {
        ResourceUri resourceUri = new ResourceUri(DOMAIN, TYPE);

        JsonArray fakeResult = new JsonArray();
        Optional<String> search = Optional.of("my+search");
        when(paginationMock.getPage()).thenReturn(PAGE);
        when(paginationMock.getPageSize()).thenReturn(SIZE);
        when(resourceSearchMock.getText()).thenReturn(search);
        when(resourceSearchMock.getParams()).thenReturn(Optional.empty());
        when(resourceSearchMock.indexFieldsOnly()).thenReturn(true);
        when(searchableFieldRegistry.getFieldsFromResourceUri(eq(RESOURCE_URI))).thenReturn(new HashSet(Arrays.asList("t1", "t2")));
        when(resmiSearch.search(eq(RESOURCE_URI), eq(search.get()), eq(resourceQueriesMock), eq(paginationMock), eq(Optional.of(sortMock))))
                .thenReturn(fakeResult);

        JsonArray result = defaultResmiService.findCollection(resourceUri, Optional.of(collectionParametersMock));
        assertThat(fakeResult).isEqualTo(result);
    }

    @Test
    public void deleteResourceByIdTest() throws NotFoundException {
        ResourceUri uri = new ResourceUri(DOMAIN, TYPE, ID);
        defaultResmiService.deleteResource(uri);
        verify(resmiDao).deleteResource(uri);
        verify(resmiSearch, times(0)).deleteDocument(any());
    }

    @Test
    public void deleteIndexedResourceByIdTest() throws NotFoundException {
        ResourceUri uri = new ResourceUri(DOMAIN, TYPE, ID);
        when(searchableFieldRegistry.getFieldsFromResourceUri(eq(uri))).thenReturn(new HashSet(Arrays.asList("t1", "t2")));
        defaultResmiService.deleteResource(uri);
        verify(resmiDao).deleteResource(uri);
        verify(resmiSearch).deleteDocument(uri);
    }

    @Test
    public void createRelationTest() throws NotFoundException, StartsWithUnderscoreException {
        ResourceUri uri = new ResourceUri(DOMAIN, TYPE, ID, RELATION);
        when(searchableFieldRegistry.getFieldsFromResourceUri(eq(uri))).thenReturn(new HashSet(Arrays.asList("t1", "t2")));
        JsonObject relationData = new JsonObject();
        defaultResmiService.createRelation(uri, relationData);

        ArgumentCaptor<JsonObject> object = ArgumentCaptor.forClass(JsonObject.class);
        verify(resmiSearch).indexDocument(eq(uri), object.capture());

        assertThat(object.getValue().has("_src_id")).isTrue();
        assertThat(object.getValue().get("_src_id").getAsString()).isEqualTo(ID);

        verify(resmiDao).createRelation(uri, relationData);
    }

    @Test
    public void findInRelationWithSearchTest() throws BadConfigurationException, InvalidApiParamException {
        ResourceUri resourceUri = new ResourceUri(DOMAIN, TYPE, ID, RELATION);

        JsonArray fakeResult = new JsonArray();
        Optional<String> search = Optional.of("my+search");
        when(paginationMock.getPage()).thenReturn(PAGE);
        when(paginationMock.getPageSize()).thenReturn(SIZE);
        when(resourceSearchMock.getText()).thenReturn(search);
        when(resourceSearchMock.getParams()).thenReturn(Optional.empty());
        when(resourceSearchMock.indexFieldsOnly()).thenReturn(true);
        when(relationParametersMock.getQueries()).thenReturn(Optional.empty());
        when(searchableFieldRegistry.getFieldsFromResourceUri(eq(resourceUri))).thenReturn(new HashSet(Arrays.asList("t1", "t2")));
        when(resmiSearch.search(eq(resourceUri), eq(search.get()), any(), eq(paginationMock), eq(Optional.of(sortMock)))).thenReturn(
                fakeResult);

        JsonArray result = (JsonArray) defaultResmiService.findRelation(resourceUri, Optional.of(relationParametersMock));
        assertThat(result).isEqualTo(fakeResult);
        ArgumentCaptor<List> query = ArgumentCaptor.forClass(List.class);
        verify(resmiSearch).search(eq(resourceUri), eq(search.get()), query.capture(), eq(paginationMock), eq(Optional.of(sortMock)));
        assertThat(query.getValue().toString()).isEqualTo("[[{\"$eq\":{\"_src_id\":\"" + ID + "\"}}]]");
    }


}
