package io.corbel.resources.rem.request;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.springframework.http.MediaType;

import io.corbel.lib.token.TokenInfo;

/**
 * @author Alexander De Leon
 * 
 */
public class RequestParametersImpl<E> implements RequestParameters<E> {

    private final E apiParameters;
    private final TokenInfo tokenInfo;
    private final String requestedDomain;
    private final List<MediaType> acceptedMediaTypes;
    private final MultivaluedMap<String, String> params;
    private final Long contentLength;
    private final MultivaluedMap<String, String> headers;

    public RequestParametersImpl(E apiParameters, TokenInfo tokenInfo, String requestedDomain, List<MediaType> acceptedMediaTypes,
            Long contentLength,
            MultivaluedMap<String, String> params, MultivaluedMap<String, String> headers) {
        this.apiParameters = apiParameters;
        this.tokenInfo = tokenInfo;
        this.requestedDomain = requestedDomain;
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
    public String getRequestedDomain() {
        return requestedDomain;
    }

    @Override
    public List<MediaType> getAcceptedMediaTypes() {
        return acceptedMediaTypes;
    }

    @Override
    public String getCustomParameterValue(String parameterName) {
        return params != null ? params.getFirst(parameterName) : null;
    }

    @Override
    public List<String> getCustomParameterValues(String parameterName) {
        return params != null ? params.get(parameterName) : null;
    }

    @Override
    public MultivaluedMap<String, String> getParams() {
        return params;
    }

    @Override
    public MultivaluedMap<String, String> getHeaders() {
        return headers;
    }

    @Override
    public Long getContentLength() {
        return contentLength;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((acceptedMediaTypes == null) ? 0 : acceptedMediaTypes.hashCode());
        result = prime * result + ((apiParameters == null) ? 0 : apiParameters.hashCode());
        result = prime * result + ((contentLength == null) ? 0 : contentLength.hashCode());
        result = prime * result + ((headers == null) ? 0 : headers.hashCode());
        result = prime * result + ((params == null) ? 0 : params.hashCode());
        result = prime * result + ((tokenInfo == null) ? 0 : tokenInfo.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RequestParametersImpl other = (RequestParametersImpl) obj;
        if (acceptedMediaTypes == null) {
            if (other.acceptedMediaTypes != null) {
                return false;
            }
        } else if (!acceptedMediaTypes.equals(other.acceptedMediaTypes)) {
            return false;
        }
        if (apiParameters == null) {
            if (other.apiParameters != null) {
                return false;
            }
        } else if (!apiParameters.equals(other.apiParameters)) {
            return false;
        }
        if (contentLength == null) {
            if (other.contentLength != null) {
                return false;
            }
        } else if (!contentLength.equals(other.contentLength)) {
            return false;
        }
        if (headers == null) {
            if (other.headers != null) {
                return false;
            }
        } else if (!headers.equals(other.headers)) {
            return false;
        }
        if (params == null) {
            if (other.params != null) {
                return false;
            }
        } else if (!params.equals(other.params)) {
            return false;
        }
        if (tokenInfo == null) {
            if (other.tokenInfo != null) {
                return false;
            }
        } else if (!tokenInfo.equals(other.tokenInfo)) {
            return false;
        }
        return true;
    }



}
