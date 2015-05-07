package com.bq.oss.corbel.iam.service;

import java.util.List;

import com.bq.oss.corbel.iam.exception.DuplicatedOauthServiceIdentityException;
import com.bq.oss.corbel.iam.exception.IdentityAlreadyExistsException;
import com.bq.oss.corbel.iam.model.Identity;
import com.bq.oss.corbel.iam.model.User;

/**
 * @author Rubén Carrasco
 *
 */
public interface IdentityService {

    Identity addIdentity(Identity identity) throws DuplicatedOauthServiceIdentityException, IdentityAlreadyExistsException;

    void deleteUserIdentities(User user);

    List<Identity> findUserIdentities(User user);
}
