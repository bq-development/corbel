package io.corbel.resources.rem.model;

public class RemDescription {
    private final String remName;
    private final String httpMethod;
    private final String mediaType;
    private final String uriPattern;

    public RemDescription(String remName, String httpMethod, String mediaType, String uriPattern) {
        this.remName = remName;
        this.httpMethod = httpMethod;
        this.mediaType = mediaType;
        this.uriPattern = uriPattern;
    }

    public String getRemName() {
        return remName;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getUriPattern() {
        return uriPattern;
    }

    @Override
    public String toString() {
        return "{" + "remName = '" + remName + '\'' + ", httpMethod = '" + httpMethod + '\'' + ", mediaType = '" + mediaType + '\''
                + ", uriPattern = '" + uriPattern + '\'' + '}';
    }
}
