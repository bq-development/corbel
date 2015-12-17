package io.corbel.resources.rem.util;

import java.util.Optional;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import io.corbel.lib.queries.request.Pagination;
import io.corbel.resources.rem.request.*;

public class ImageRemUtil {

    public RequestParameters<CollectionParameters> getCollectionParametersWithPrefix(
            String originalFilename, RequestParameters<ResourceParameters> requestParameters, String cacheCollection) {

        MultivaluedMap<String, String> newParameters = new MultivaluedHashMap<String, String>(requestParameters.getParams());
        newParameters.putSingle("prefix", cacheCollection + "/" + originalFilename);

        return new RequestParametersImpl<>(new CollectionParametersImpl(new Pagination(1, 10), Optional.empty(), Optional.empty(),
 Optional.empty(), Optional.empty(),
                        Optional.empty()),
                requestParameters.getTokenInfo(), requestParameters.getRequestedDomain(),
                requestParameters.getAcceptedMediaTypes(), requestParameters.getContentLength(), newParameters,
                requestParameters.getHeaders());
    }
}
