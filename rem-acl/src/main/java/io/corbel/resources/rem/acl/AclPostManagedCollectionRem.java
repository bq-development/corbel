package io.corbel.resources.rem.acl;

import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.BaseRem;
import io.corbel.resources.rem.model.ManagedCollection;
import io.corbel.resources.rem.request.CollectionParameters;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.service.AclConfigurationService;

import java.net.URI;
import java.util.Optional;

import javax.ws.rs.core.Response;

public class AclPostManagedCollectionRem extends BaseRem<ManagedCollection> {

    private final AclConfigurationService aclConfigurationService;

    public AclPostManagedCollectionRem(AclConfigurationService aclResourcesService) {
        this.aclConfigurationService = aclResourcesService;
    }

    @Override
    public Response collection(String type, RequestParameters<CollectionParameters> parameters, URI uri, Optional<ManagedCollection> entity) {

        return entity.filter(ManagedCollection::validate).map(managedCollection -> {
            managedCollection.setDomain(parameters.getRequestedDomain());
            return aclConfigurationService.createConfiguration(uri, managedCollection);
        }).orElseGet(() -> ErrorResponseFactory.getInstance().badRequest());

    }

    @Override
    public Class<ManagedCollection> getType() {
        return ManagedCollection.class;
    }

}
