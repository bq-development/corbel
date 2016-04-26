package io.corbel.resources.rem.eventbus;

import io.corbel.event.ResourceEvent;
import io.corbel.eventbus.EventHandler;
import io.corbel.resources.rem.service.AclConfigurationService;
import io.corbel.resources.rem.service.DefaultAclConfigurationService;

import javax.ws.rs.core.Response;

import org.springframework.http.HttpStatus;

import com.google.gson.JsonObject;

public class AclConfigurationEventHandler implements EventHandler<ResourceEvent> {

    private AclConfigurationService aclConfigurationService;
    private final String aclAdminCollection;

    private static final String ALL = "@ALL";

    public AclConfigurationEventHandler(String aclAdminCollection) {
        this.aclAdminCollection = aclAdminCollection;
    }

    public void setAclConfigurationService(AclConfigurationService aclConfigurationService) {
        this.aclConfigurationService = aclConfigurationService;
    }

    @Override
    public void handle(ResourceEvent event) {

        if (!event.getType().equals(aclAdminCollection)) {
            return;
        }

        if (event.getResourceId().equals(ALL)) {
            aclConfigurationService.refreshRegistry();
            return;
        }
        String id = event.getResourceId();
        JsonObject collectionConfiguration = null;
        switch (event.getAction()) {
            case CREATE:
                // Why id contains entire url in event when Create Event? @see DefaultResourcesService line 98
                id = id.substring(id.lastIndexOf("/") + 1);
                collectionConfiguration = getCollectionConfiguration(id);
                aclConfigurationService.addAclConfiguration(collectionConfiguration.get(
                        DefaultAclConfigurationService.COLLECTION_NAME_FIELD).getAsString());
                String defaultPermission = "";
                if (collectionConfiguration.has(DefaultAclConfigurationService.DEFAULT_PERMISSION_FIELD)) {
                    defaultPermission = collectionConfiguration.get(DefaultAclConfigurationService.DEFAULT_PERMISSION_FIELD).getAsString();
                }
                aclConfigurationService.setResourcesWithDefaultPermission(
                        collectionConfiguration.get(DefaultAclConfigurationService.COLLECTION_NAME_FIELD).getAsString(),
                        collectionConfiguration.get(DefaultAclConfigurationService.DOMAIN_FIELD).getAsString(), defaultPermission);
                break;
            case DELETE:
                collectionConfiguration = getCollectionConfiguration(id);
                if (collectionConfiguration != null) {
                    aclConfigurationService.removeAclConfiguration(id,
                            collectionConfiguration.get(DefaultAclConfigurationService.COLLECTION_NAME_FIELD).getAsString());
                }
        }

    }

    private JsonObject getCollectionConfiguration(String onlyId) {
        Response response = aclConfigurationService.getConfiguration(onlyId);
        if (response.getStatus() == HttpStatus.OK.value()) {
            return (JsonObject) response.getEntity();
        }
        return null;
    }

    @Override
    public Class<ResourceEvent> getEventType() {
        return ResourceEvent.class;
    }

}
