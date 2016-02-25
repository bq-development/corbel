package io.corbel.resources.rem.acl;

import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.resources.rem.BaseRem;
import io.corbel.resources.rem.model.ManagedCollection;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.service.AclConfigurationService;

import java.util.Optional;

import javax.ws.rs.core.Response;

public class AclPutManagedCollectionRem extends BaseRem<ManagedCollection> {

    private final AclConfigurationService aclConfigurationService;

    public AclPutManagedCollectionRem(AclConfigurationService aclConfigurationService) {
        this.aclConfigurationService = aclConfigurationService;
    }

    @Override
    public Response resource(String type, ResourceId id, RequestParameters<ResourceParameters> parameters,
            Optional<ManagedCollection> entity) {

        return entity.map(managedCollection -> aclConfigurationService.updateConfiguration(id, managedCollection)).orElseGet(
                () -> ErrorResponseFactory.getInstance().badRequest());

    }

    @Override
    public Class<ManagedCollection> getType() {
        return ManagedCollection.class;
    }

}
