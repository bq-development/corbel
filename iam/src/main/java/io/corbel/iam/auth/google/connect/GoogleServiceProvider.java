package io.corbel.iam.auth.google.connect;

import org.springframework.social.oauth2.AbstractOAuth2ServiceProvider;

import io.corbel.iam.auth.google.api.Google;
import io.corbel.iam.auth.google.api.impl.GoogleTemplate;

public class GoogleServiceProvider extends AbstractOAuth2ServiceProvider<Google> {

    public GoogleServiceProvider(String clientId, String clientSecret) {
        super(new GoogleOAuth2Template(clientId, clientSecret));
    }

    @Override
    public Google getApi(String accessToken) {
        return new GoogleTemplate(accessToken);
    }

}
