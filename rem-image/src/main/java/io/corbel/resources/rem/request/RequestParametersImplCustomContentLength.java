package io.corbel.resources.rem.request;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.springframework.http.MediaType;

import io.corbel.lib.token.TokenInfo;

public class RequestParametersImplCustomContentLength<E> implements RequestParameters<E> {

    private final RequestParameters<E> requestParameters;
    private final Long contentLength;

    public RequestParametersImplCustomContentLength(RequestParameters<E> requestParameters, Long contentLength) {
        super();
        this.requestParameters = requestParameters;
        this.contentLength = contentLength;
    }

    @Override
    public E getApiParameters() {
        return requestParameters.getApiParameters();
    }

    @Override
    public TokenInfo getTokenInfo() {
        return requestParameters.getTokenInfo();
    }

    @Override
    public String getRequestedDomain() {
        return requestParameters.getRequestedDomain();
    }

    @Override
    public List<MediaType> getAcceptedMediaTypes() {
        return requestParameters.getAcceptedMediaTypes();
    }

    @Override
    public String getCustomParameterValue(String parameterName) {
        return requestParameters.getCustomParameterValue(parameterName);
    }

    @Override
    public List<String> getCustomParameterValues(String parameterName) {
        return requestParameters.getCustomParameterValues(parameterName);
    }

    @Override
    public MultivaluedMap<String, String> getParams() {
        return requestParameters.getParams();
    }

    @Override
    public MultivaluedMap<String, String> getHeaders() {
        return requestParameters.getHeaders();
    }

    @Override
    public Long getContentLength() {
        return this.contentLength;
    }
}
