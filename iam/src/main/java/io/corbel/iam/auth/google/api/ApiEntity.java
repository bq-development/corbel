package io.corbel.iam.auth.google.api;

import static org.springframework.util.StringUtils.hasText;

public abstract class ApiEntity {

    private String id;

    private String etag;

    protected ApiEntity() {}

    protected ApiEntity(String id) {
        this.id = hasText(id) ? id : null;
    }

    public String getId() {
        return id;
    }

    public String getEtag() {
        return etag;
    }

}