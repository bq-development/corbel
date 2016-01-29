package io.corbel.resources.rem.request;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MultivaluedMap;

import org.springframework.http.MediaType;

import io.corbel.lib.token.TokenInfo;

/**
 * @author Alexander De Leon
 * 
 */
public interface RequestParameters<E> {

    @Deprecated
    E getApiParameters();

    default Optional<E> getOptionalApiParameters() {
        return Optional.ofNullable(getApiParameters());
    }

    TokenInfo getTokenInfo();

    String getRequestedDomain();

    List<MediaType> getAcceptedMediaTypes();

    String getCustomParameterValue(String parameterName);

    List<String> getCustomParameterValues(String parameterName);

    MultivaluedMap<String, String> getParams();

    MultivaluedMap<String, String> getHeaders();

    Long getContentLength();

}
