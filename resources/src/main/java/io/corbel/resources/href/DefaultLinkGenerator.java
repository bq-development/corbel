package io.corbel.resources.href;

import java.net.URI;
import java.util.Optional;
import java.util.Set;

import io.corbel.resources.href.LinksBuilder.GroundLinksBuilder;
import io.corbel.resources.href.LinksBuilder.ResourceLinksBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Alexander De Leon
 * 
 */
public class DefaultLinkGenerator implements LinkGenerator {

    private static final String ID = "id";

    @Override
    public JsonObject addResourceLinks(JsonObject resource, URI typeUri, Optional<Set<String>> relations) {
        LinksBuilder builder = new LinksBuilder();
        Optional<ResourceLinksBuilder> resourceLinksBuilder = addSelfLink(resource, builder.typeUri(typeUri));
        if (resourceLinksBuilder.isPresent() && relations.isPresent()) {
            addRelationsLink(relations.get(), resourceLinksBuilder.get());
        }
        builder.appendLinks(resource);
        return resource;
    }

    private Optional<ResourceLinksBuilder> addSelfLink(JsonObject resource, GroundLinksBuilder builder) {
        ResourceLinksBuilder resourceLinksBuilder = null;
        if (resource.has(ID)) {
            JsonElement idElement = resource.get(ID);
            if (idElement.isJsonPrimitive()) {
                resourceLinksBuilder = builder.id(idElement.getAsString());
                resourceLinksBuilder.buildSelfLink();
            }
        }
        return Optional.ofNullable(resourceLinksBuilder);
    }

    private void addRelationsLink(Set<String> resourceRelations, ResourceLinksBuilder builder) {
        for (String relation : resourceRelations) {
            builder.buildRelationLink(relation);
        }
    }

}
