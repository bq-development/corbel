package io.corbel.iam.service;

import java.util.List;

import io.corbel.iam.exception.DuplicatedOauthServiceIdentityException;
import io.corbel.iam.exception.IdentityAlreadyExistsException;
import io.corbel.iam.model.Identity;
import io.corbel.iam.model.User;

/**
 * @author Rubén Carrasco
 *
 */
public interface IdentityService {

    Identity addIdentity(Identity identity) throws DuplicatedOauthServiceIdentityException, IdentityAlreadyExistsException;

    void deleteUserIdentities(User user);

    List<Identity> findUserIdentities(User user);
}
