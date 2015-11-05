package io.corbel.resources.rem;

import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import io.corbel.resources.rem.model.RemDescription;

/**
 * @author Alexander De Leon
 */
public interface RemRegistry {

    /**
     * Registry a REM with the resources
     * 
     * @param rem The REM implementation to register.
     * @param uriPattern The URI pattern for the resources that this REM is responsible. The pattern must follow the syntax from pattern
     * @param mediaType The Media Type that that this REM can resolve. If can have wild card (e.b. images/*)
     * @param methods The HTTP methods that this REM can resolve. If none are passed then it is assumed that ALL HTTP methods are supported.
     */
    void registerRem(Rem rem, String uriPattern, MediaType mediaType, HttpMethod... methods);

    /**
     * Registry a REM with the resources for ALL media types
     * 
     * @param rem The REM implementation to register.
     * @param uriPattern The URI pattern for the resources that this REM is responsible. The pattern must follow the syntax from pattern
     * @param methods The HTTP methods that this REM can resolve. If none are passed then it is assumed that ALL HTTP methods are supported.
     */
    void registerRem(Rem rem, String uriPattern, HttpMethod... methods);

    /**
     * Look up in the registry for a REM that can handle a resource with the specified characteristics
     * 
     * @param uri The URI of the resource
     * @param acceptableMediaTypes The media types of the representations preferred by the client.
     * @param method The HTTP method of the request
     * @return a REM if one is found or null
     */
    Rem getRem(String uri, List<MediaType> acceptableMediaTypes, HttpMethod method, List<Rem> remsExcluded);

    void unregisterRem(Class<?> remClass, String uriPattern, MediaType mediaType);

    Rem getRem(String name);

    List<RemDescription> getRegistryDescription();

}
