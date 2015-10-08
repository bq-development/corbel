package io.corbel.resources.rem.request.builder;

import io.corbel.lib.token.TokenInfo;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.RequestParametersImpl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.springframework.http.MediaType;

/**
 * @author Rubén Carrasco
 *
 */
public class RequestParametersBuilder<E> {

    private E apiParameters;
    private TokenInfo tokenInfo;
    private List<MediaType> acceptedMediaTypes;
    private MultivaluedMap<String, String> params;
    private Long contentLength;
    private MultivaluedMap<String, String> headers;

    public RequestParameters<E> build() {
        return new RequestParametersImpl<E>(apiParameters, tokenInfo, acceptedMediaTypes, contentLength, params, headers);
    }

    public RequestParametersBuilder<E> requestParameters(RequestParameters<E> parameters) {
        this.apiParameters = parameters.getOptionalApiParameters().get();
        this.apiParameters = parameters.getOptionalApiParameters().get();
        this.tokenInfo = parameters.getTokenInfo();
        this.acceptedMediaTypes = parameters.getAcceptedMediaTypes();
        this.params = parameters.getParams();
        this.contentLength = parameters.getContentLength();
        this.headers = parameters.getHeaders();
        return this;
    }

    public RequestParametersBuilder<E> apiParameters(E apiParameters) {
        this.apiParameters = apiParameters;
        return this;
    }

    public RequestParametersBuilder<E> tokenInfo(TokenInfo tokenInfo) {
        this.tokenInfo = tokenInfo;
        return this;
    }

    public RequestParametersBuilder<E> acceptedMediaTypes(List<MediaType> acceptedMediaTypes) {
        this.acceptedMediaTypes = acceptedMediaTypes;
        return this;
    }

    public RequestParametersBuilder<E> acceptedMediaType(MediaType mediaType) {
        if (acceptedMediaTypes == null) {
            acceptedMediaTypes = new ArrayList<>();
        }
        acceptedMediaTypes.add(mediaType);
        return this;
    }

    public RequestParametersBuilder<E> params(MultivaluedMap<String, String> params) {
        this.params = params;
        return this;
    }

    public RequestParametersBuilder<E> param(String key, String value) {
        if (params == null) {
            params = new MultivaluedHashMap<>();
        }
        params.add(key, value);
        return this;
    }

    public RequestParametersBuilder<E> contentLength(Long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    public RequestParametersBuilder<E> headers(MultivaluedMap<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public RequestParametersBuilder<E> header(String key, String value) {
        if (headers == null) {
            headers = new MultivaluedHashMap<>();
        }
        headers.add(key, value);
        return this;
    }
}
