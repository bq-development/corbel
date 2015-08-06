package io.corbel.iam.auth.provider;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import io.corbel.iam.auth.OauthParams;
import io.corbel.iam.exception.ExchangeOauthCodeException;
import io.corbel.iam.exception.MissingOAuthParamsException;
import io.corbel.iam.exception.OauthServerConnectionException;
import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.iam.model.Identity;
import io.corbel.iam.repository.IdentityRepository;

public class GoogleProviderTest {

    private static final String CLIENT_SECRET = "NnabCAv7xO5T6iiU56eZxund";
    private static final String CLIENT_ID = "673751863646-fl39i3ni7lkhq3uo74485glspd08pic2.apps.googleusercontent.com";
    private static final String URL_TEST = "http://example.com/connected";
    private static final String ASSERTION = "ñaksjdfhpiuwerfpijqbwepriuh";

    private Provider googleProvider;
    private IdentityRepository identityRepositoryMock;

    private static String idTestUser = "117031103264768184360";

    @Before
    public void before() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("redirectUri", URL_TEST);
        configuration.put("clientId", CLIENT_ID);
        configuration.put("clientSecret", CLIENT_SECRET);

        identityRepositoryMock = mock(IdentityRepository.class);
        Identity identity = new Identity();
        identity.setOauthId(idTestUser);
        when(identityRepositoryMock.findByOauthIdAndDomainAndOauthService(idTestUser, "qa-corbel", "google")).thenReturn(identity);
        googleProvider = new GoogleProvider(identityRepositoryMock);
        googleProvider.setConfiguration(configuration);
    }

    @Test
    public void getAuthUrlTest() throws URISyntaxException {
        String url = googleProvider.getAuthUrl(ASSERTION);
        System.out.println(url);
        URI uri = new URI(url);
        assertThat(uri.getHost()).isEqualTo("accounts.google.com");
    }

    @Test
    @Ignore
    public void isAuthenticatedWithTokenTest() throws UnauthorizedException, MissingOAuthParamsException, ExchangeOauthCodeException,
            OauthServerConnectionException {
        Identity identity = googleProvider.getIdentity(
                new OauthParams().setAccessToken("ya29.1.AADtN_Vdb6PAq4c8Wv4jtI5I2J2tdjt8nzD44lnUOEbpHnOdaLbL7dSMjwq01HGyjp7rHQ"),
                "google", "qa-corbel");
        assertThat(identity).isNotNull();
        assertThat(identity.getOauthId()).isEqualTo(idTestUser);
    }

    @Test
    @Ignore
    public void isAuthenticatedWithCodeTest() throws UnauthorizedException, MissingOAuthParamsException, ExchangeOauthCodeException,
            OauthServerConnectionException {
        String code = "4/6rNkyNMk-B4MEhdx9VxzouI-c78q.AuLzYhglK40QYKs_1NgQtmWDlyyKhwI";

        googleProvider.getIdentity(new OauthParams().setCode(code), "google", "qa-corbel");
    }
}
