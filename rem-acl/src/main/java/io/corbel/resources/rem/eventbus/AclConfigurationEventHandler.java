package io.corbel.resources.rem.eventbus;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.corbel.event.ResourceEvent;
import io.corbel.eventbus.EventHandler;
import io.corbel.resources.rem.service.AclResourcesService;

public class AclConfigurationEventHandler implements EventHandler<ResourceEvent> {

    private AclResourcesService aclResourcesService;
    private final Pattern collectionPattern = Pattern.compile("^(?:.*/)?[\\w-_]+?(?::(?<collection>[\\w-_:]+))?$");
    private final String aclAdminCollection;

    private static final String ALL = "@ALL";

    public AclConfigurationEventHandler(String aclAdminCollection) {
        this.aclAdminCollection = aclAdminCollection;
    }

    public void setAclResourcesService(AclResourcesService aclResourcesService) {
        this.aclResourcesService = aclResourcesService;
    }

    @Override
    public void handle(ResourceEvent event) {

        if (!event.getType().equals(aclAdminCollection)) {
            return;
        }

        if (event.getResourceId().equals(ALL)) {
            aclResourcesService.refreshRegistry();
            return;
        }

        switch (event.getAction()) {
            case CREATE:
                extractUriPattern(event.getResourceId()).ifPresent(aclResourcesService::addAclConfiguration);
                break;
            case DELETE:
                extractUriPattern(event.getResourceId()).ifPresent(aclResourcesService::removeAclConfiguration);
        }

    }

    @Override
    public Class<ResourceEvent> getEventType() {
        return ResourceEvent.class;
    }

    private Optional<String> extractUriPattern(String resourceId) {
        Matcher matcher = collectionPattern.matcher(resourceId);

        if (!matcher.matches()) {
            return Optional.empty();
        }

        return Optional.ofNullable(matcher.group("collection")).filter(c -> !c.isEmpty());
    }

}
