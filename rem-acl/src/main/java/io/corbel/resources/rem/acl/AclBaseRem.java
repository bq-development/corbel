package io.corbel.resources.rem.acl;

import com.google.common.collect.Lists;
import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.BaseRem;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.request.*;
import io.corbel.resources.rem.service.AclResourcesService;
import io.corbel.resources.rem.service.RemService;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.http.MediaType;

import javax.ws.rs.core.Response;

/**
 * @author Rubén Carrasco
 */
abstract public class AclBaseRem extends BaseRem<InputStream> {

    public static final List<MediaType> JSON_MEDIATYPE = Collections.singletonList(MediaType.APPLICATION_JSON);

    protected final AclResourcesService aclResourcesService;
    protected List<Rem> remsToExclude;
    protected RemService remService;

    public AclBaseRem(AclResourcesService aclResourcesService, List<Rem> remsToExclude) {
        this.aclResourcesService = aclResourcesService;
        this.remsToExclude = remsToExclude;
    }

    public void setRemService(RemService remService) {
        this.remService = remService;
    }

    @Override
    final public Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<InputStream> entity) {
        return collection(type, parameters, uri, entity, Optional.empty());
    }

    @Override
    final public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<InputStream> entity) {
        return resource(type, id, parameters, entity, Optional.empty());
    }

    @Override
    final public Response relation(String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters,
                             Optional<InputStream> entity) {
        return relation(type, id, relation, parameters, entity, Optional.empty());
    }

    @Override
    final public Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<InputStream> entity, Optional<List<Rem>> excludedRems) {
        return collectionWithAcl(type, parameters, uri, entity, excludedRems);
    }

    @Override
    final public Response relation(String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters, Optional<InputStream> entity, Optional<List<Rem>> excludedRems) {
        return relationWithAcl(type, id, relation, parameters, entity, excludedRems);
    }

    @Override
    final public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<InputStream> entity, Optional<List<Rem>> excludedRems) {
        return resourceWithAcl(type, id, parameters, entity, excludedRems);
    }

    //Default implementations assume that if subclass does not override it is because that HTTP Method is not supported for this type of request
    protected Response collectionWithAcl(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<InputStream> entity, Optional<List<Rem>> excludedRems){
        return ErrorResponseFactory.getInstance().methodNotAllowed();
    }

    protected Response resourceWithAcl(String type, ResourceId id, RequestParameters<ResourceParameters> parameters, Optional<InputStream> entity, Optional<List<Rem>> excludedRems){
        return ErrorResponseFactory.getInstance().methodNotAllowed();
    }

    protected Response relationWithAcl(String type, ResourceId id, String relation, RequestParameters<RelationParameters> parameters, Optional<InputStream> entity, Optional<List<Rem>> excludedRems){
        return ErrorResponseFactory.getInstance().methodNotAllowed();
    }


    @Override
    public Class<InputStream> getType() {
        return InputStream.class;
    }

    protected List<Rem> getExcludedRems(Optional<List<Rem>> excludedRems) {
        List<Rem> excluded = Lists.newArrayList(this);
        if (remsToExclude != null) {
            excluded.addAll(remsToExclude);
        }
        excludedRems.ifPresent(excluded::addAll);
        return excluded;
    }

}
