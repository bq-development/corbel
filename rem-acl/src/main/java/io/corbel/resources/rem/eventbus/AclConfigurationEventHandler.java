package io.corbel.resources.rem.eventbus;

import io.corbel.event.ResourceEvent;
import io.corbel.eventbus.EventHandler;
import io.corbel.resources.rem.service.AclConfigurationService;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

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

        switch (event.getAction()) {
            case CREATE:
                // Why id contains entire url in event? @see DefaultResourcesService line 98
                String onlyId = id.substring(id.lastIndexOf("/") + 1);
                try {
                    aclConfigurationService.addAclConfiguration(URLDecoder.decode(onlyId.substring(onlyId.indexOf(":") + 1), "UTF8"));
                } catch (UnsupportedEncodingException e) {
                    // Never happens
                }
                break;
            case DELETE:
                aclConfigurationService.removeAclConfiguration(id.substring(id.indexOf(":") + 1));
        }

    }

    @Override
    public Class<ResourceEvent> getEventType() {
        return ResourceEvent.class;
    }

}
