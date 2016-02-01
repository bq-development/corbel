package io.corbel.resources.rem.util;

import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceParameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImageRemUtilTest {

    @Mock
    private RequestParameters<ResourceParameters> params;
    private static final String ORIGINAL_FILENAME = "ORIGINAL_FILENAME";
    private static final String CACHE_COLLECTION = "CACHE:COLLECTION";
    private static final String PREFIX_FIELD_VALUE = "prefix";
    private ImageRemUtil imageRemUtil;

    @Before
    public void setUp() {
        imageRemUtil = new ImageRemUtil();
        MultivaluedMap<String, String> map = new MultivaluedHashMap<String, String>();
        when(params.getParams()).thenReturn(map);
    }

    @Test
    public void test() {
        String expectedPrefixOutput = CACHE_COLLECTION + "/" + ORIGINAL_FILENAME;
        RequestParameters<CollectionParameters> newParams = imageRemUtil.getCollectionParametersWithPrefix(ORIGINAL_FILENAME, params, CACHE_COLLECTION);
        assertThat(newParams.getCustomParameterValue(PREFIX_FIELD_VALUE)).isEqualTo(expectedPrefixOutput);
    }
}
