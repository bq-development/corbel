package com.bq.oss.corbel.iam.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;

import com.bq.oss.corbel.iam.exception.DuplicatedOauthServiceIdentityException;
import com.bq.oss.corbel.iam.exception.IdentityAlreadyExistsException;
import com.bq.oss.corbel.iam.model.Identity;
import com.bq.oss.corbel.iam.model.User;
import com.bq.oss.corbel.iam.repository.IdentityRepository;

/**
 * @author Rubén Carrasco
 *
 */
public class DefaultIdentityService implements IdentityService {

    private final IdentityRepository identityRepository;

    public DefaultIdentityService(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    @Override
    public Identity addIdentity(Identity identity) throws DuplicatedOauthServiceIdentityException, IdentityAlreadyExistsException {

        if (identityRepository.existsByDomainAndUserIdAndOauthService(identity.getDomain(), identity.getUserId(),
                identity.getOauthService())) {
            throw new DuplicatedOauthServiceIdentityException();
        }

        try {
            return identityRepository.save(identity);
        } catch (DataIntegrityViolationException e) {
            throw new IdentityAlreadyExistsException();
        }
    }

    @Override
    public void deleteUserIdentities(User user) {
        identityRepository.deleteByUserIdAndDomain(user.getId(), user.getDomain());

    }

    @Override
    public List<Identity> findUserIdentities(User user) {
        return identityRepository.findByUserIdAndDomain(user.getId(), user.getDomain());
    }
}
