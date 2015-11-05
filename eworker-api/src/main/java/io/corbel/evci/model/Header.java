package io.corbel.evci.model;

/**
 * @author Alberto J. Rubio
 */
public class Header {

    private final String domainId;
    private final String clientId;
    private final String userId;

    public Header(String domainId, String clientId, String userId) {
        this.domainId = domainId;
        this.clientId = clientId;
        this.userId = userId;
    }

    public String getDomainId() {
        return domainId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Header header = (Header) o;

        if (domainId != null ? !domainId.equals(header.domainId) : header.domainId != null) return false;
        if (clientId != null ? !clientId.equals(header.clientId) : header.clientId != null) return false;
        return !(userId != null ? !userId.equals(header.userId) : header.userId != null);

    }

    @Override
    public int hashCode() {
        int result = domainId != null ? domainId.hashCode() : 0;
        result = 31 * result + (clientId != null ? clientId.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        return result;
    }
}
