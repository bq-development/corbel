package io.corbel.resources.service;

import java.net.URI;
import java.util.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.RemRegistry;
import io.corbel.resources.rem.model.RemDescription;
import io.corbel.resources.rem.request.*;
import io.corbel.resources.rem.service.RemService;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;

/**
 * @author Alberto J. Rubio
 *
 */
public class DefaultRemService implements RemService {

    private final Map<String, List<Rem>> remsExcludedForUri = new HashMap<>();
    private final RemRegistry registry;

    public DefaultRemService(RemRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Rem getRem(String name) {
        return registry.getRem(name);
    }

    @Override
    public Rem getRem(String type, List<MediaType> acceptedMediaTypes, HttpMethod method) {
        return getRemFromRegistry(type, acceptedMediaTypes, method, null);
    }

    @Override
    public Rem getRem(String type, List<MediaType> acceptedMediaTypes, HttpMethod method, List<Rem> remsExcluded) {
        List<Rem> remsToExclude = new ArrayList<>();
        if (remsExcluded != null) {
            remsToExclude.addAll(remsExcluded);
        }
        if (remsExcludedForUri.containsKey(type)) {
            remsToExclude.addAll(remsExcludedForUri.get(type));
        }
        return getRemFromRegistry(type, acceptedMediaTypes, method, remsToExclude);
    }

    private Rem getRemFromRegistry(String type, List<MediaType> acceptedMediaTypes, HttpMethod method, List<Rem> remsExcluded) {
        Rem rem = registry.getRem(type, acceptedMediaTypes, method, remsExcluded);
        if (rem == null) {
            throw new WebApplicationException(ErrorResponseFactory.getInstance().notFound());
        }
        return rem;
    }

    @Override
    public void registerExcludedRems(String uri, List<Rem> excludedRems) {
        if (excludedRems != null) {
            if (remsExcludedForUri.containsKey(uri)) {
                remsExcludedForUri.get(uri).addAll(excludedRems);
            } else {
                remsExcludedForUri.put(uri, excludedRems);
            }
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Response collection(Rem rem, String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional entity) {
        return rem.collection(type, parameters, uri, entity);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Response resource(Rem rem, String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional entity) {
        return rem.resource(type, id, parameters, entity);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Response relation(Rem rem, String type, ResourceId id, String rel, RequestParameters<RelationParameters> parameters,
            Optional entity) {
        return rem.relation(type, id, rel, parameters, entity);
    }

    @Override
    public List<RemDescription> getRegisteredRemDescriptions() {
        return registry.getRegistryDescription();
    }

}
