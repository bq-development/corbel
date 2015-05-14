package com.bq.oss.corbel.resources.rem.service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.bq.oss.corbel.resources.rem.Rem;
import com.bq.oss.corbel.resources.rem.model.RemDescription;
import com.bq.oss.corbel.resources.rem.request.*;

/**
 * @author Alberto J. Rubio
 */
public interface RemService {

    Rem<?> getRem(String type, List<MediaType> acceptedMediaTypes, HttpMethod method);

    Rem<?> getRem(String type, List<MediaType> acceptedMediaTypes, HttpMethod method, List<Rem> remsExcluded);

    Response collection(Rem rem, String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional entity);

    Response resource(Rem rem, String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional entity);

    Response relation(Rem rem, String type, ResourceId id, String rel, RequestParameters<RelationParameters> parameters, Optional entity);

    List<RemDescription> getRegisteredRemDescriptions();

    Rem getRem(String name);
}
