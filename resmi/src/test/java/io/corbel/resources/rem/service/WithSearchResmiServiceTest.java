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
import io.corbel.resources.rem.search.ResmiSearch;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.JsonArray;

/**
 * @author Francisco Sanchez
 */
@RunWith(MockitoJUnitRunner.class) public class WithSearchResmiServiceTest {

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
    @Mock Search resourceSearchMock;
    @Mock Pagination paginationMock;
    @Mock Sort sortMock;
    @Mock RelationParameters relationParametersMock;
    private DefaultResmiService defaultResmiService;

    @Before
    public void setup() {
        defaultResmiService = new WithSearchResmiService(resmiDao, resmiSearch, searchableFieldRegistry, Clock.systemUTC());
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
    public void findWithSearchTest() throws BadConfigurationException {
        ResourceUri resourceUri = new ResourceUri(TYPE);

        JsonArray fakeResult = new JsonArray();
        Optional<String> search = Optional.of("my+search");
        when(paginationMock.getPage()).thenReturn(PAGE);
        when(paginationMock.getPageSize()).thenReturn(SIZE);
        when(resourceSearchMock.getText()).thenReturn(search);
        when(resourceSearchMock.getParams()).thenReturn(Optional.empty());
        when(searchableFieldRegistry.getFieldsFromResourceUri(eq(RESOURCE_URI))).thenReturn(new HashSet(Arrays.asList("t1", "t2")));
        when(
                resmiSearch.search(eq(RESOURCE_URI), eq(search.get()), eq(Optional.of(resourceQueriesMock)), eq(paginationMock),
                        eq(Optional.of(sortMock)))).thenReturn(fakeResult);

        JsonArray result = defaultResmiService.findCollection(resourceUri, Optional.of(collectionParametersMock));
        assertThat(fakeResult).isEqualTo(result);
    }

    @Test
    public void deleteResourceByIdTest() throws NotFoundException {
        ResourceUri uri = new ResourceUri(TYPE, ID);
        defaultResmiService.deleteResource(uri);
        verify(resmiDao).deleteResource(uri);
        verify(resmiSearch, times(0)).deleteDocument(any());
    }

    @Test
    public void deleteIndexedResourceByIdTest() throws NotFoundException {
        ResourceUri uri = new ResourceUri(TYPE, ID);
        when(searchableFieldRegistry.getFieldsFromType(eq(TYPE))).thenReturn(new HashSet(Arrays.asList("t1", "t2")));
        defaultResmiService.deleteResource(uri);
        verify(resmiDao).deleteResource(uri);
        verify(resmiSearch).deleteDocument(uri);
    }


}
