package io.corbel.resources.rem.model;


/**
 * @author Francisco Sanchez
 */
public class RestorResourceUri {

    private String domain;
    private String mediaType;
    private String type;
    private String typeId;
    public RestorResourceUri() {}

    public RestorResourceUri(String domain, String mediaType, String type) {
        this(domain, mediaType, type, null);
    }

    public RestorResourceUri(String domain, String mediaType, String type, String typeId) {
        this.domain = domain;
        this.mediaType = mediaType;
        this.type = type;
        this.typeId = typeId;
    }

    public String getDomain() {
        return domain;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getType() {
        return type;
    }

    public RestorResourceUri setType(String type) {
        this.type = type;
        return this;
    }

    public String getTypeId() {
        return typeId;
    }

    public RestorResourceUri setTypeId(String typeId) {
        this.typeId = typeId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RestorResourceUri that = (RestorResourceUri) o;

        if (domain != null ? !domain.equals(that.domain) : that.domain != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (typeId != null ? !typeId.equals(that.typeId) : that.typeId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = domain != null ? domain.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (typeId != null ? typeId.hashCode() : 0);
        return result;
    }
}
