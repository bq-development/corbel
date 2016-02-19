package io.corbel.resources.href;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander De Leon
 */
public class LinksBuilder {

    public static final String SELF = "self";

    private final List<Link> links;

    public LinksBuilder() {
        super();
        this.links = new ArrayList<>();
    }

    public GroundLinksBuilder typeUri(URI typeUri) {
        return new GroundLinksBuilder(typeUri);
    }

    public class GroundLinksBuilder {
        private final URI typeUri;

        private GroundLinksBuilder(URI typeUri) {
            this.typeUri = typeUri;
        }

        public ResourceLinksBuilder id(String id) {
            return new ResourceLinksBuilder(typeUri, id);
        }

    }

    public void appendLinks(JsonObject object) {
        if (links.isEmpty()) {
            return;
        }
        JsonArray objectLinks = new JsonArray();

        for (Link link : links) {
            JsonElement jsonObjectLink = new Gson().toJsonTree(link);
            objectLinks.add(jsonObjectLink);
        }
        object.add("links", objectLinks);
    }

    public class ResourceLinksBuilder {
        private final URI resourceUri;

        public ResourceLinksBuilder(URI typeUri, String id) {
            this.resourceUri = UriBuilder.fromUri(typeUri).path("/{id}").buildFromEncoded(new String[]{id});
        }

        public LinksBuilder buildSelfLink() {
            links.add(new Link(SELF, resourceUri.toASCIIString()));
            return LinksBuilder.this;
        }

        public LinksBuilder buildRelationLink(String relationName) {
            links.add(new Link(relationName, UriBuilder.fromUri(resourceUri).path("/{relation}").buildFromEncoded(relationName).toASCIIString()));
            return LinksBuilder.this;
        }

        public URI getResourceUri() {
            return resourceUri;
        }

    }

}
