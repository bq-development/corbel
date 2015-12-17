package io.corbel.resources.rem.resmi;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.corbel.resources.rem.request.RelationParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.service.ResmiService;

import java.util.Optional;

import org.junit.Before;
import org.mockito.Mockito;

import com.google.gson.JsonObject;

/**
 * @author Rubén Carrasco
 * 
 */
public abstract class ResmiRemTest {

    protected static final String DOMAIN = "DOMAIN";
    protected static final String TEST_TYPE = "testType";
    protected static final String TEST_TYPE_RELATION = "testTypeRelation";
    protected static final String ID = "testId";
    protected static final String DST_ID = "resourceDstId";
    protected static final ResourceId TEST_ID = new ResourceId(ID);
    protected static final ResourceId TEST_WILDCARD_ID = new ResourceId("_");
    protected ResmiService resmiServiceMock;

    @Before
    public void setup() {
        resmiServiceMock = mock(ResmiService.class);
    }

    protected JsonObject getTestResource() {
        JsonObject testResource = new JsonObject();
        testResource.addProperty("_id", "1324");
        testResource.addProperty("name", "asdf");
        return testResource;
    }

    protected JsonObject getTestRelationData() {
        JsonObject testResource = new JsonObject();
        testResource.addProperty("data1", true);
        testResource.addProperty("data2", "data2");
        return testResource;
    }

    protected RequestParameters getParameters(String uri) {
        return getParameters(Optional.of(uri));
    }

    protected RequestParameters getParametersWithEmptyUri() {
        return getParameters(Optional.empty());
    }

    private RequestParameters getParameters(Optional<String> optional) {
        RequestParameters requestParameters = Mockito.mock(RequestParameters.class);
        RelationParameters parameters = Mockito.mock(RelationParameters.class);
        when(parameters.getPredicateResource()).thenReturn(optional);
        when(parameters.getQueries()).thenReturn(Optional.empty());
        when(requestParameters.getApiParameters()).thenReturn(parameters);
        when(requestParameters.getOptionalApiParameters()).thenReturn(Optional.of(parameters));
        when(requestParameters.getRequestedDomain()).thenReturn(DOMAIN);
        return requestParameters;
    }

}
