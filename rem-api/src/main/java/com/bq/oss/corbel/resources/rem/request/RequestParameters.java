package com.bq.oss.corbel.resources.rem.request;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.springframework.http.MediaType;

import com.bq.oss.lib.token.TokenInfo;

/**
 * @author Alexander De Leon
 * 
 */
public interface RequestParameters<E> {

    E getApiParameters();

    TokenInfo getTokenInfo();

    List<MediaType> getAcceptedMediaTypes();

    String getCustomParameterValue(String parameterName);

    List<String> getCustomParameterValues(String parameterName);

    MultivaluedMap<String, String> getHeaders();

    Long getContentLength();

}
