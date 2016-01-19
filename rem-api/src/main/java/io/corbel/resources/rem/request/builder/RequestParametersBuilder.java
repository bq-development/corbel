package io.corbel.resources.rem.request.builder;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.springframework.http.MediaType;

import io.corbel.lib.token.TokenInfo;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.RequestParametersImpl;

/**
 * @author Rubén Carrasco
 *
 */
public class RequestParametersBuilder<E> {

    private E apiParameters;
    private TokenInfo tokenInfo;
    private String requestedDomain;
    private List<MediaType> acceptedMediaTypes;
    private MultivaluedMap<String, String> params;
    private Long contentLength;
    private MultivaluedMap<String, String> headers;

    public RequestParametersBuilder(String requestedDomain) {
        this.requestedDomain = requestedDomain;
    }

    public RequestParametersBuilder(RequestParameters<E> parameters) {
        this.apiParameters = parameters.getOptionalApiParameters().map(apiParameters -> apiParameters).orElse(null);
        this.tokenInfo = parameters.getTokenInfo();
        this.requestedDomain = parameters.getRequestedDomain();
        this.acceptedMediaTypes = parameters.getAcceptedMediaTypes();
        params(parameters.getParams());
        this.contentLength = parameters.getContentLength();
        headers(parameters.getHeaders());
    }

    public RequestParameters<E> build() {
        return new RequestParametersImpl<E>(apiParameters, tokenInfo, requestedDomain, acceptedMediaTypes, contentLength, params, headers);
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
        this.params = new MultivaluedHashMap<>();
        params.forEach((k, v) -> this.params.addAll(k, v));
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
        this.headers = new MultivaluedHashMap<>();
        headers.forEach((k, v) -> this.headers.addAll(k, v));
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
