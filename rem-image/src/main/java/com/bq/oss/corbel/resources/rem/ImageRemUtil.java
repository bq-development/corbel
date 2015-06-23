package com.bq.oss.corbel.resources.rem;

import java.util.Optional;

import javax.ws.rs.core.MultivaluedMap;

import com.bq.oss.corbel.resources.rem.request.*;
import com.bq.oss.lib.queries.request.Pagination;

public class ImageRemUtil {

    public RequestParameters<CollectionParameters> getCollectionParametersWithPrefix(String prefix,
            RequestParameters<ResourceParameters> requestParameters) {

        MultivaluedMap<String, String> newParameters = requestParameters.getParams();
        newParameters.putSingle("prefix", "cachedImage." + prefix);

        return new RequestParametersImpl<>(new CollectionParametersImpl(new Pagination(1, 10), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty()), requestParameters.getTokenInfo(),
                requestParameters.getAcceptedMediaTypes(), requestParameters.getContentLength(), newParameters,
                requestParameters.getHeaders());

    }

}
