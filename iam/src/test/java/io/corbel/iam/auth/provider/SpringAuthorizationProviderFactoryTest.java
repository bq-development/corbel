package io.corbel.iam.auth.provider;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import io.corbel.iam.model.Domain;
import io.corbel.iam.repository.IdentityRepository;

public class SpringAuthorizationProviderFactoryTest {

    private static SpringAuthorizationProviderFactory springAuthorizationProviderFactory;

    private static Domain domainMock;

    @BeforeClass
    public static void createMocks() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        IdentityRepository identityRepositoryMock = mock(IdentityRepository.class);
        when(applicationContext.getBean("facebook", Provider.class)).thenReturn(new FacebookProvider(identityRepositoryMock));
        when(applicationContext.getBean("corbel", Provider.class)).thenReturn(new OAuthServerProvider(identityRepositoryMock));

        springAuthorizationProviderFactory = new SpringAuthorizationProviderFactory();
        springAuthorizationProviderFactory.setApplicationContext(applicationContext);

        Map<String, Map<String, String>> map = new HashMap<>();

        Map<String, String> mapFacebook = new HashMap<>();
        mapFacebook.put("type", "facebook");
        mapFacebook.put("clientId", "lalalala");
        mapFacebook.put("clientSecret", "lalalala");
        Map<String, String> corbel = new HashMap<>();
        corbel.put("type", "corbel");
        corbel.put("clientId", "lalalala");
        corbel.put("clientSecret", "lalalala");
        corbel.put("oAuthServerUrl", "lalalala");

        map.put("facebook", mapFacebook);
        map.put("corbel", corbel);

        domainMock = mock(Domain.class);
        when(domainMock.getAuthConfigurations()).thenReturn(map);
    }

    @Test
    public void getProviderTest() {

        assertThat(springAuthorizationProviderFactory.getProvider(domainMock, "facebook")).isInstanceOf(FacebookProvider.class);
        assertThat(springAuthorizationProviderFactory.getProvider(domainMock, "corbel")).isInstanceOf(OAuthServerProvider.class);

    }

    @Test(expected = IllegalArgumentException.class)
    public void getNotExistingProviderTest() {
        springAuthorizationProviderFactory.getProvider(domainMock, "null");
    }
}
