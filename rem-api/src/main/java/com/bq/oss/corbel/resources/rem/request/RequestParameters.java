package com.bq.oss.corbel.resources.rem.request;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MultivaluedHashMap;
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

    List<MediaType> getAcceptedMediaTypes();

    String getCustomParameterValue(String parameterName);

    List<String> getCustomParameterValues(String parameterName);

    MultivaluedMap<String, String> getParams();

    MultivaluedMap<String, String> getHeaders();

    Long getContentLength();

    static <E> RequestParameters<E> emptyParameters() {
        return new RequestParameters<E>() {
            @Override
            public List<MediaType> getAcceptedMediaTypes() {
                return Collections.emptyList();
            }

            @Override
            public E getApiParameters() {
                return null;
            }

            @Override
            public TokenInfo getTokenInfo() {
                return null;
            }

            @Override
            public String getCustomParameterValue(String parameterName) {
                return null;
            }

            @Override
            public List<String> getCustomParameterValues(String parameterName) {
                return Collections.emptyList();
            }

            @Override
            public MultivaluedMap<String, String> getParams() {
                return new MultivaluedHashMap();
            }

            @Override
            public MultivaluedMap<String, String> getHeaders() {
                return new MultivaluedHashMap();
            }

            @Override
            public Long getContentLength() {
                return null;
            }
        };
    }



}
