package io.corbel.iam.service;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import io.corbel.lib.queries.request.AggregationResultsFactory;
import io.corbel.lib.queries.request.JsonAggregationResultsFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;

import io.corbel.iam.exception.DomainAlreadyExists;
import io.corbel.iam.model.Domain;
import io.corbel.iam.model.Scope;
import io.corbel.iam.repository.DomainRepository;
import com.google.common.collect.Sets;

/**
 * @author Alexander De Leon
 * 
 */
@RunWith(MockitoJUnitRunner.class) public class DefaultDomainServiceTest {

    public static final String SCOPE_A = "scopeA";
    public static final String SCOPE_B = "scopeB";
    public static final String SCOPE_C = "scopeC";
    private static final String TEST_DOMAIN_ID = "some_domain";
    private static final Domain TEST_DOMAIN = new Domain();

    Scope scopeA;
    Scope scopeB;
    Scope scopeC;

    @Mock private DomainRepository domainRepositoryMock;
    @Mock private DefaultScopeService defaultScopeServiceMock;
    @Mock private EventsService eventsServiceMock;

    private DefaultDomainService domainService;

    @Before
    public void setup() {
        scopeA = mock(Scope.class);
        scopeB = mock(Scope.class);
        scopeC = mock(Scope.class);

        when(scopeA.getId()).thenReturn(SCOPE_A);
        when(scopeB.getId()).thenReturn(SCOPE_B);
        when(scopeC.getId()).thenReturn(SCOPE_C);


        domainService = new DefaultDomainService(domainRepositoryMock, defaultScopeServiceMock, eventsServiceMock, new JsonAggregationResultsFactory());
    }

    @Test
    public void testNotAllowedScopes() {
        Domain domain = mock(Domain.class);
        when(domain.getScopes()).thenReturn(Sets.newHashSet(SCOPE_A, "scopeB"));
        when(domainRepositoryMock.findOne(TEST_DOMAIN_ID)).thenReturn(domain);

        when(defaultScopeServiceMock.expandScopes(Sets.newHashSet(SCOPE_A, "scopeB"))).thenReturn(
                new HashSet<>(Arrays.asList(scopeA, scopeB)));
        when(defaultScopeServiceMock.expandScopes(Sets.newHashSet(SCOPE_C))).thenReturn(new HashSet<>(Arrays.asList(scopeC)));

        assertThat(domainService.scopesAllowedInDomain(Sets.newHashSet(SCOPE_C), TEST_DOMAIN)).isFalse();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNotExistScopes() {
        Domain domain = mock(Domain.class);
        when(domain.getScopes()).thenReturn(Sets.newHashSet(SCOPE_A, "scopeB"));
        when(domainRepositoryMock.findOne(TEST_DOMAIN_ID)).thenReturn(domain);

        when(defaultScopeServiceMock.expandScopes(anySet())).thenThrow(IllegalStateException.class);

        assertThat(domainService.scopesAllowedInDomain(Sets.newHashSet(SCOPE_C), TEST_DOMAIN)).isFalse();
    }

    @Test
    public void testNotAllowedSomeScopes() {
        Domain domain = mock(Domain.class);
        when(domain.getScopes()).thenReturn(Sets.newHashSet(SCOPE_A, "scopeB"));
        when(domainRepositoryMock.findOne(TEST_DOMAIN_ID)).thenReturn(domain);

        when(defaultScopeServiceMock.expandScopes(Sets.newHashSet(SCOPE_A, "scopeB"))).thenReturn(
                new HashSet<>(Arrays.asList(scopeA, scopeB)));
        when(defaultScopeServiceMock.expandScopes(Sets.newHashSet(SCOPE_A, SCOPE_C))).thenReturn(
                new HashSet<>(Arrays.asList(scopeA, scopeC)));

        assertThat(domainService.scopesAllowedInDomain(Sets.newHashSet(SCOPE_A, SCOPE_C), TEST_DOMAIN)).isFalse();
    }

    @Test
    public void testAllowedScopes() {
        Domain domain = mock(Domain.class);
        when(domain.getScopes()).thenReturn(Sets.newHashSet(SCOPE_A, "scopeB"));

        when(defaultScopeServiceMock.expandScopes(Sets.newHashSet(SCOPE_A))).thenReturn(new HashSet<>(Arrays.asList(scopeA)));
        when(defaultScopeServiceMock.expandScopes(Sets.newHashSet(SCOPE_A, "scopeB"))).thenReturn(
                new HashSet<>(Arrays.asList(scopeA, scopeB)));

        assertThat(domainService.scopesAllowedInDomain(Sets.newHashSet(SCOPE_A), domain)).isTrue();
    }

    @Test
    public void testDomainWithNoScopes() {
        Domain domain = mock(Domain.class);
        when(domain.getScopes()).thenReturn(Sets.<String>newHashSet());
        when(domainRepositoryMock.findOne(TEST_DOMAIN_ID)).thenReturn(domain);

        when(defaultScopeServiceMock.expandScopes(Sets.newHashSet(SCOPE_A))).thenReturn(new HashSet<>(Arrays.asList(scopeA)));

        assertThat(domainService.scopesAllowedInDomain(Sets.newHashSet(SCOPE_A), TEST_DOMAIN)).isFalse();
    }

    @Test
    public void testDomainNullScopes() {
        Domain domain = mock(Domain.class);
        when(domain.getScopes()).thenReturn(null);
        when(domainRepositoryMock.findOne(TEST_DOMAIN_ID)).thenReturn(domain);

        when(defaultScopeServiceMock.expandScopes(Sets.newHashSet(SCOPE_A))).thenReturn(new HashSet<>(Arrays.asList(scopeA)));

        assertThat(domainService.scopesAllowedInDomain(Sets.newHashSet(SCOPE_A), TEST_DOMAIN)).isFalse();
    }

    @Test
    public void testNullScopes() {
        Domain domain = mock(Domain.class);
        when(domain.getScopes()).thenReturn(Sets.newHashSet(SCOPE_A, "scopeB"));
        when(domainRepositoryMock.findOne(TEST_DOMAIN_ID)).thenReturn(domain);

        when(defaultScopeServiceMock.expandScopes(Sets.newHashSet(SCOPE_A, "scopeB"))).thenReturn(
                new HashSet<>(Arrays.asList(scopeA, scopeB)));

        assertThat(domainService.scopesAllowedInDomain(null, TEST_DOMAIN)).isTrue();
    }

    @Test
    public void testEmptyScopes() {
        Domain domain = mock(Domain.class);
        when(domain.getScopes()).thenReturn(Sets.newHashSet(SCOPE_A, "scopeB"));
        when(domainRepositoryMock.findOne(TEST_DOMAIN_ID)).thenReturn(domain);

        when(defaultScopeServiceMock.expandScopes(Sets.newHashSet(SCOPE_A, "scopeB"))).thenReturn(
                new HashSet<>(Arrays.asList(scopeA, scopeB)));

        assertThat(domainService.scopesAllowedInDomain(Sets.<String>newHashSet(), TEST_DOMAIN)).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOAuthServiceAllowed() {
        String oAuthService = "testService";

        Domain domain = mock(Domain.class);
        HashMap map = mock(HashMap.class);
        when(map.get(oAuthService)).thenReturn(new HashMap<String, String>());
        when(domain.getAuthConfigurations()).thenReturn(map);

        assertThat(domainService.oAuthServiceAllowedInDomain(oAuthService, domain)).isEqualTo(true);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOAuthServiceNotAllowed() {
        String oAuthService = "testService";

        Domain domain = mock(Domain.class);
        when(domain.getAuthConfigurations()).thenReturn(null);
        HashMap map = mock(HashMap.class);
        when(map.get(oAuthService)).thenReturn(null);
        when(domain.getAuthConfigurations()).thenReturn(map);
        when(domainRepositoryMock.findOne(TEST_DOMAIN_ID)).thenReturn(domain);

        assertThat(domainService.oAuthServiceAllowedInDomain(oAuthService, TEST_DOMAIN)).isEqualTo(false);
    }

    @Test
    public void testCreate() throws DomainAlreadyExists {
        Domain domain = new Domain();
        domain.setId(TEST_DOMAIN_ID);

        ArgumentCaptor<Domain> domainCaptor = ArgumentCaptor.forClass(Domain.class);

        domainService.insert(domain);

        verify(domainRepositoryMock).insert(domainCaptor.capture());
        assertEquals(TEST_DOMAIN_ID, domainCaptor.getValue().getId());
    }

    @SuppressWarnings("unchecked")
    @Test(expected = DomainAlreadyExists.class)
    public void testCreateAlreadyExisting() throws DomainAlreadyExists {
        Mockito.doThrow(DataIntegrityViolationException.class).when(domainRepositoryMock).insert(any());
        domainService.insert(TEST_DOMAIN);
    }

    @Test
    public void testUpdate() {
        domainService.update(TEST_DOMAIN);
        verify(domainRepositoryMock).patch(TEST_DOMAIN);
    }

    @Test
    public void testDelete() {
        domainService.delete(TEST_DOMAIN.getId());
        verify(domainRepositoryMock).delete(TEST_DOMAIN.getId());
        verify(eventsServiceMock).sendDomainDeletedEvent(TEST_DOMAIN.getId());

        verifyNoMoreInteractions(domainRepositoryMock, eventsServiceMock);
    }
}
