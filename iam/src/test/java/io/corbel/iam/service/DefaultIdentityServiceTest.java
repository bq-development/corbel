package io.corbel.iam.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;

import io.corbel.iam.exception.DuplicatedOauthServiceIdentityException;
import io.corbel.iam.exception.IdentityAlreadyExistsException;
import io.corbel.iam.model.Identity;
import io.corbel.iam.model.User;
import io.corbel.iam.repository.IdentityRepository;

/**
 * @author Rubén Carrasco
 *
 */
@RunWith(MockitoJUnitRunner.class) public class DefaultIdentityServiceTest {

    @Mock private IdentityRepository identityRepository;

    private IdentityService identityService;

    @Before
    public void setUp() throws Exception {
        identityService = new DefaultIdentityService(identityRepository);
    }

    @Test
    public void testAddIdentity() throws DuplicatedOauthServiceIdentityException, IdentityAlreadyExistsException {
        Identity identity = createIdentity();
        when(
                identityRepository.existsByDomainAndUserIdAndOauthService(identity.getDomain(), identity.getUserId(),
                        identity.getOauthService())).thenReturn(false);

        identityService.addIdentity(identity);
        verify(identityRepository).save(identity);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = IdentityAlreadyExistsException.class)
    public void testAddDuplicatedIdentity() throws DuplicatedOauthServiceIdentityException, IdentityAlreadyExistsException {
        when(identityRepository.existsByDomainAndUserIdAndOauthService(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
        when(identityRepository.save(Mockito.<Identity>any())).thenThrow(DataIntegrityViolationException.class);
        identityService.addIdentity(createIdentity());
    }

    @Test(expected = DuplicatedOauthServiceIdentityException.class)
    public void testAddDuplicatedOauthServiceIdentity() throws DuplicatedOauthServiceIdentityException, IdentityAlreadyExistsException {
        when(identityRepository.existsByDomainAndUserIdAndOauthService(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        identityService.addIdentity(createIdentity());
    }

    @Test
    public void testDeleteUserIdentitites() {
        User user = createUser();
        identityService.deleteUserIdentities(user);
        verify(identityRepository).deleteByUserIdAndDomain(user.getId(), user.getDomain());
    }

    @Test
    public void testfindUserIdentitites() {
        User user = createUser();
        identityService.findUserIdentities(user);
        verify(identityRepository).findByUserIdAndDomain(user.getId(), user.getDomain());
    }

    private Identity createIdentity() {
        Identity identity = new Identity();
        identity.setDomain("domain");
        identity.setOauthService("oauthService");
        identity.setUserId("userId");
        return identity;
    }

    private User createUser() {
        User user = new User();
        user.setUsername("username");
        user.setDomain("domain");
        return user;
    }
}
