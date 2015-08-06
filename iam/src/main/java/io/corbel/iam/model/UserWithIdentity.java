package io.corbel.iam.model;

import org.springframework.data.annotation.Transient;

/**
 * @author Cristian del Cerro
 */
public class UserWithIdentity extends User {

    @Transient private Identity identity;

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public Identity getIdentity() {
        if (identity == null) {
            return null;
        }
        Identity copy = new Identity(identity);
        synchronizeIdentityWithUser(copy);
        return copy;
    }

    private void synchronizeIdentityWithUser(Identity identity) {
        identity.setDomain(this.getDomain());
        identity.setUserId(this.getId());
    }
}
