package io.corbel.iam.exception;

public class DomainAlreadyExists extends Exception {

    private final String domain;

    public DomainAlreadyExists(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }
}
