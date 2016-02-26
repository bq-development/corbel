package io.corbel.resources.rem.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.corbel.lib.queries.request.Pagination;
import io.corbel.lib.queries.request.ResourceQuery;
import io.corbel.lib.queries.request.Search;
import io.corbel.lib.queries.request.Sort;
import io.corbel.resources.rem.dao.ResmiDao;
import io.corbel.resources.rem.model.ResourceUri;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.resmi.exception.InvalidApiParamException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class) public class TextSearchResmiServiceTest {
    static final String DOMAIN = "DOMAIN";

    String TYPE = "resource:TYPE";
    String RELATION_TYPE = "relation:TYPE";
    String ID = "test";
    String TEXT_SEARCH = "textSearchQuery";

    @Mock ResmiDao resmiDao;
    CollectionParameters collectionParametersMock;
    @Mock ResourceQuery resourceQueryMock;
    @Mock List<ResourceQuery> resourceQueriesMock;
    @Mock Search resourceSearchMock;
    @Mock Pagination paginationMock;
    @Mock Sort sortMock;
    @Mock RelationParameters relationParametersMock;
    private TextSearchResmiService textSearchResmiService;

    @Before
    public void setup() {
        textSearchResmiService = new TextSearchResmiService(resmiDao, Clock.systemUTC());
        when(relationParametersMock.getAggregation()).thenReturn(Optional.empty());
        when(relationParametersMock.getQueries()).thenReturn(Optional.ofNullable(resourceQueriesMock));
        when(relationParametersMock.getSearch()).thenReturn(Optional.ofNullable(resourceSearchMock));
        when(relationParametersMock.getPagination()).thenReturn(paginationMock);
        when(relationParametersMock.getSort()).thenReturn(Optional.ofNullable(sortMock));
        collectionParametersMock = relationParametersMock;
        reset(resmiDao);
    }

    @Test
    public void findRelationTestWithTextSearch() throws BadConfigurationException, InvalidApiParamException {
        JsonObject fakeResult = new JsonObject();
        fakeResult.addProperty("root", "result");

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TYPE, ID, RELATION_TYPE, "test");

        when(resmiDao.findRelation(eq(resourceUri), eq(Optional.of(resourceQueriesMock)), eq(Optional.of(TEXT_SEARCH)), eq(Optional.of(paginationMock)), eq(Optional.of(sortMock)))).thenReturn(fakeResult);
        final Search search = new Search(false, TEXT_SEARCH);
        when(collectionParametersMock.getSearch()).thenReturn(Optional.of(search));
        when(relationParametersMock.getPredicateResource()).thenReturn(Optional.of("test"));

        JsonElement result = textSearchResmiService.findRelation(resourceUri, Optional.of(relationParametersMock));
        assertThat(fakeResult).isEqualTo(result.getAsJsonObject());
    }

    @Test
    public void findRelationTestWithEmptyTextSearch() throws BadConfigurationException, InvalidApiParamException {
        JsonObject fakeResult = new JsonObject();
        fakeResult.addProperty("root", "result");

        ResourceUri resourceUri = new ResourceUri(DOMAIN, TYPE, ID, RELATION_TYPE, "test");

        when(resmiDao.findRelation(eq(resourceUri), eq(Optional.of(resourceQueriesMock)), eq(Optional.empty()), eq(Optional.of(paginationMock)), eq(Optional.of(sortMock)))).thenReturn(fakeResult);
        when(collectionParametersMock.getSearch()).thenReturn(Optional.empty());
        when(relationParametersMock.getPredicateResource()).thenReturn(Optional.of("test"));

        JsonElement result = textSearchResmiService.findRelation(resourceUri, Optional.of(relationParametersMock));
        assertThat(fakeResult).isEqualTo(result.getAsJsonObject());
    }

}
