package com.bq.oss.corbel.iam.auth.provider;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.bq.oss.corbel.iam.auth.OauthParams;
import com.bq.oss.corbel.iam.exception.ExchangeOauthCodeException;
import com.bq.oss.corbel.iam.exception.MissingOAuthParamsException;
import com.bq.oss.corbel.iam.exception.OauthServerConnectionException;
import com.bq.oss.corbel.iam.exception.UnauthorizedException;
import com.bq.oss.corbel.iam.model.Identity;
import com.bq.oss.corbel.iam.repository.IdentityRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class FacebookProviderTest {

    private static final String CLIENT_SECRET = "d74e4ddea4ed89b51959d414d6fc13c0";
    private static final String CLIENT_ID = "657485767623244";
    private static final String URL_TEST = "http://testqacorbel.com/test";
    private static final String URL_FACEBOOK_ENDPOINT = "https://graph.facebook.com";

    private static final String URL_GET_APP_ACCES_TOKEN = URL_FACEBOOK_ENDPOINT + "/oauth/access_token?client_id=" + CLIENT_ID
            + "&client_secret=" + CLIENT_SECRET + "&grant_type=client_credentials";
    private static final String URL_GET_TEST_USERS = URL_FACEBOOK_ENDPOINT + "/" + CLIENT_ID + "/accounts/test-users?";

    private static final String idTestUser = "100007548313760";
    private static String tokenTestUser;
    private static final String ASSERTION = "aksjdfhpiuwerfpijqbwepriuh";

    private Provider facebookProvider;
    private IdentityRepository identityRepositoryMock;

    @BeforeClass
    public static void loginWithTestUser() throws UnsupportedEncodingException, URISyntaxException {
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getClasses().add(JacksonJsonProvider.class);

        Client client = Client.create(clientConfig);

        WebResource webResource = client.resource(URL_GET_APP_ACCES_TOKEN);
        String appAccessToken = webResource.get(String.class);

        WebResource webResource2 = client.resource(new URI(URL_GET_TEST_USERS
                + URLEncoder.encode(appAccessToken, "UTF-8").replace("%3D", "=")));
        ObjectNode jsonNode = webResource2.get(ObjectNode.class);

        for (JsonNode testUser : jsonNode.path("data")) {
            if (testUser.path("id").asText().equals(idTestUser)) {
                tokenTestUser = testUser.path("access_token").asText();
            }

        }
    }

    @Before
    public void before() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("redirectUri", URL_TEST);
        configuration.put("clientId", CLIENT_ID);
        configuration.put("clientSecret", CLIENT_SECRET);

        identityRepositoryMock = mock(IdentityRepository.class);
        Identity identity = new Identity();
        identity.setOauthId(idTestUser);
        when(identityRepositoryMock.findByOauthIdAndDomainAndOauthService(idTestUser, "qa-corbel", "facebook")).thenReturn(identity);
        facebookProvider = new FacebookProvider(identityRepositoryMock);
        facebookProvider.setConfiguration(configuration);

    }

    @Test
    public void getAuthUrlTest() throws URISyntaxException {
        String url = facebookProvider.getAuthUrl(ASSERTION);
        URI uri = new URI(url);
        assertThat(uri.getQuery()).isEqualTo(
                "client_id=657485767623244&response_type=token&redirect_uri=http://testqacorbel.com/test?assertion=" + ASSERTION);
        assertThat(uri.getHost()).isEqualTo("www.facebook.com");
    }

    @Test
    public void isAuthenticatedWithTokenTest() throws UnauthorizedException, MissingOAuthParamsException, ExchangeOauthCodeException,
            OauthServerConnectionException {
        Identity identity = facebookProvider.getIdentity(new OauthParams().setAccessToken(tokenTestUser), "facebook", "qa-corbel");
        assertThat(identity).isNotNull();
        assertThat(identity.getOauthId()).isEqualTo(idTestUser);
    }

    @Test
    @Ignore
    public void isAuthenticatedWithCodeTest() throws UnauthorizedException, MissingOAuthParamsException, ExchangeOauthCodeException,
            OauthServerConnectionException {
        String code = "AQCT2l8k1p_64NwXWgs9_Ld6UcB-365SNLvj1FiTS-8lG2KLrtIZPvAf-2rqD7Hrw1SiGpoSTwfOCnO9RPkvjNUc2816_e1YOjoysmMvjf62HyGcIhPcJ6VTFct84YNlC8rQcErIYRv44twDjR2il8SXm4XzEX_lhf1dITC5B9Stb2axCLbCr3kf-aqIVlNpmaNFwtGSPdoAbY80gEnFSNyM357ucnLzCE607LfkiQ83NtbFByxHmoME1zR-4o5zvqRihDMUAkM2E-FrjCpc0L7lE3RKlgRj8gAwvAJIYzZqOCZqMpLsFz0i-tyYfOK3KpU#_=_";

        facebookProvider.getIdentity(new OauthParams().setCode(code), "facebook", "qa-corbel");
    }
}
