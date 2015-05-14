package com.bq.oss.corbel.resources.href;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.jersey.api.uri.UriBuilderImpl;

/**
 * @author Alexander De Leon
 * 
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
        if (links.size() == 0) {
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
            this.resourceUri = UriBuilderImpl.fromUri(typeUri).path("/{id}").build(id);
        }

        public LinksBuilder buildSelfLink() {
            links.add(new Link(SELF, resourceUri.toASCIIString()));
            return LinksBuilder.this;
        }

        public LinksBuilder buildRelationLink(String relationName) {
            links.add(new Link(relationName, UriBuilderImpl.fromUri(resourceUri).path("/{relation}").build(relationName).toASCIIString()));
            return LinksBuilder.this;
        }

        public URI getResourceUri() {
            return resourceUri;
        }

    }

}
