package com.bq.oss.corbel.iam.auth.provider;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;

import com.bq.oss.corbel.iam.repository.IdentityRepository;

public class TwitterProviderTest {

    private static final String ASSERTION = "aksjdfhpiuwerfpijqbwepriuh";
    private static final String CONSUMER_SECRET = "xbETkxaKt5mbkmKaex2RhB6dVYWqJ7ZuL9Y4RQAG8c";
    private static final String CONSUMER_KEY = "YzY0NjIoVRcTNldwIXLSQ";
    private static final String URL_TEST = "http://testqacorbel.com/test";

    private static Provider twitterProvider;
    private static IdentityRepository identityRepositoryMock;

    @BeforeClass
    public static void before() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("redirectUri", URL_TEST);
        configuration.put("consumerKey", CONSUMER_KEY);
        configuration.put("consumerSecret", CONSUMER_SECRET);
        identityRepositoryMock = mock(IdentityRepository.class);
        twitterProvider = new TwitterProvider(identityRepositoryMock);
        twitterProvider.setConfiguration(configuration);
    }

    @Test
    public void getAuthUrlTest() throws URISyntaxException, UnsupportedEncodingException {
        String url = twitterProvider.getAuthUrl(ASSERTION);
        System.out.println(url);
        URI uri = new URI(url);
        Pattern pattern = Pattern.compile("oauth_token=([^&]+)");
        Matcher matcher = pattern.matcher(uri.getQuery());
        matcher.find();
        String token = matcher.group(1);
        String query = "oauth_token=" + token + "&oauth_callback=http://testqacorbel.com/test?assertion=" + ASSERTION;
        assertThat(uri.getQuery()).isEqualTo(query);
    }

}
