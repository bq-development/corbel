package io.corbel.resources.rem.util;

import io.corbel.lib.queries.request.Pagination;
import io.corbel.resources.rem.request.*;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Optional;

public class ImageRemUtil {

    public RequestParameters<CollectionParameters> getCollectionParametersWithPrefix(
            String originalFilename, RequestParameters<ResourceParameters> requestParameters, String cacheCollection) {

        MultivaluedMap<String, String> newParameters = new MultivaluedHashMap<String, String>(requestParameters.getParams());
        newParameters.putSingle("prefix", cacheCollection + "/" + originalFilename);

        return new RequestParametersImpl<>(new CollectionParametersImpl(new Pagination(1, 10), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty()), requestParameters.getTokenInfo(),
                requestParameters.getAcceptedMediaTypes(), requestParameters.getContentLength(), newParameters,
                requestParameters.getHeaders());

    }
}
