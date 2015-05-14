package com.bq.oss.corbel.resources.rem.request;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.springframework.http.MediaType;

import com.bq.oss.lib.token.TokenInfo;

/**
 * @author Alexander De Leon
 * 
 */
public class RequestParametersImpl<E> implements RequestParameters<E> {

    private final E apiParameters;
    private final TokenInfo tokenInfo;
    private final List<MediaType> acceptedMediaTypes;
    private final MultivaluedMap<String, String> params;
    private final Long contentLength;
    private final MultivaluedMap<String, String> headers;

    public RequestParametersImpl(E apiParameters, TokenInfo tokenInfo, List<MediaType> acceptedMediaTypes, Long contentLength,
            MultivaluedMap<String, String> params, MultivaluedMap<String, String> headers) {
        this.apiParameters = apiParameters;
        this.tokenInfo = tokenInfo;
        this.acceptedMediaTypes = acceptedMediaTypes;
        this.contentLength = contentLength;
        this.params = params;
        this.headers = headers;
    }

    @Override
    public E getApiParameters() {
        return apiParameters;
    }

    @Override
    public TokenInfo getTokenInfo() {
        return tokenInfo;
    }

    @Override
    public List<MediaType> getAcceptedMediaTypes() {
        return acceptedMediaTypes;
    }

    @Override
    public String getCustomParameterValue(String parameterName) {
        return params.getFirst(parameterName);
    }

    @Override
    public List<String> getCustomParameterValues(String parameterName) {
        return params.get(parameterName);
    }

    @Override
    public MultivaluedMap<String, String> getHeaders() {
        return headers;
    }

    @Override
    public Long getContentLength() {
        return contentLength;
    }

}
