package io.corbel.oauth.ioc;

import io.corbel.eventbus.ioc.EventBusIoc;
import io.corbel.eventbus.service.EventBus;
import io.corbel.lib.config.ConfigurationIoC;
import io.corbel.lib.token.factory.TokenFactory;
import io.corbel.lib.token.ioc.OneTimeAccessTokenIoc;
import io.corbel.lib.token.ioc.TokenIoc;
import io.corbel.lib.token.parser.TokenParser;
import io.corbel.lib.token.provider.SessionProvider;
import io.corbel.lib.token.reader.TokenReader;
import io.corbel.lib.token.repository.OneTimeAccessTokenRepository;
import io.corbel.lib.ws.auth.BasicAuthProvider;
import io.corbel.lib.ws.auth.JsonUnauthorizedHandler;
import io.corbel.lib.ws.auth.OAuthProvider;
import io.corbel.lib.ws.cors.ioc.CorsIoc;
import io.corbel.lib.ws.dw.ioc.CommonFiltersIoc;
import io.corbel.lib.ws.dw.ioc.DropwizardIoc;
import io.corbel.oauth.api.*;
import io.corbel.oauth.api.auth.ClientCredentialsAuthenticator;
import io.corbel.oauth.api.auth.TokenAuthenticator;
import io.corbel.oauth.cli.dsl.OauthShell;
import io.corbel.oauth.filter.AuthFilterRegistrar;
import io.corbel.oauth.filter.FilterRegistry;
import io.corbel.oauth.filter.InMemoryFilterRegistry;
import io.corbel.oauth.mail.EmailValidationConfiguration;
import io.corbel.oauth.mail.NotificationConfiguration;
import io.corbel.oauth.model.Client;
import io.corbel.oauth.repository.ClientRepository;
import io.corbel.oauth.repository.UserRepository;
import io.corbel.oauth.repository.decorator.LowerCaseDecorator;
import io.corbel.oauth.service.ClientService;
import io.corbel.oauth.service.DefaultClientService;
import io.corbel.oauth.service.DefaultMailChangePasswordService;
import io.corbel.oauth.service.DefaultMailResetPasswordService;
import io.corbel.oauth.service.DefaultMailValidationService;
import io.corbel.oauth.service.DefaultSendNotificationService;
import io.corbel.oauth.service.DefaultUserService;
import io.corbel.oauth.service.MailChangePasswordService;
import io.corbel.oauth.service.MailResetPasswordService;
import io.corbel.oauth.service.MailValidationService;
import io.corbel.oauth.service.SendNotificationService;
import io.corbel.oauth.service.UserService;
import io.corbel.oauth.session.DefaultSessionBuilder;
import io.corbel.oauth.session.DefaultSessionCookieFactory;
import io.corbel.oauth.session.SessionBuilder;
import io.corbel.oauth.session.SessionCookieFactory;
import io.corbel.oauth.token.TokenExpireTime;
import io.dropwizard.auth.UnauthorizedHandler;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.auth.oauth.OAuthFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import com.google.gson.Gson;

/**
 * @author by Alberto J. Rubio
 */
@Configuration @Import({ConfigurationIoC.class, OauthMongoIoc.class, TokenVerifiersIoc.class, OneTimeAccessTokenIoc.class, TokenIoc.class,
        CommonFiltersIoc.class, DropwizardIoc.class, CorsIoc.class, EventBusIoc.class}) @ComponentScan({"io.corbel.oauth.filter.plugin",
        "com.bqreaders.silkroad.oauth.filter.plugin"}) public class OauthIoc {

    @Autowired private Environment env;

    @Autowired private EventBus eventBus;

    @Autowired private UserRepository userRepository;

    @Autowired private ClientRepository clientRepository;

    @Autowired private OneTimeAccessTokenRepository oneTimeAccessTokenRepository;

    private UserRepository getUserRepository() {
        return new LowerCaseDecorator(userRepository);
    }

    @Bean
    public UserService getUserService(MailValidationService mailValidationService, MailResetPasswordService mailResetPasswordService,
            MailChangePasswordService mailChangePasswordService) {
        return new DefaultUserService(getUserRepository(), mailValidationService, mailResetPasswordService, mailChangePasswordService);
    }

    @Bean
    public ClientService getClientService() {
        return new DefaultClientService(clientRepository);
    }

    @Bean
    public Gson getGson() {
        return new Gson();
    }

    @Bean
    public TokenResource getAccessTokenResource(TokenParser tokenParser, TokenFactory tokenFactory) {
        return new TokenResource(tokenParser, tokenFactory, getClientService(), getUserRepository(), getExpireTime());
    }

    @Bean
    public SessionCookieFactory getSessionCookieFactory() {
        return new DefaultSessionCookieFactory(env.getProperty("session.cookie.path"), env.getProperty("session.cookie.domain"),
                env.getProperty("session.cookie.comment"), env.getProperty("session.cookie.maxAge", Integer.class), env.getProperty(
                "session.cookie.secure", Boolean.class));
    }

    @Bean
    public AuthorizeResource getAuthorizeResource(TokenFactory tokenFactory, UserService userService, ClientService clientService,
            SessionBuilder sessionBuilder) {
        return new AuthorizeResource(userService, tokenFactory, clientService, getSessionCookieFactory(), getExpireTime(), sessionBuilder,
                getFilterRegistry());
    }

    @Bean
    public SessionBuilder getSessionBuilder(TokenFactory tokenFactory) {
        return new DefaultSessionBuilder(tokenFactory, Integer.valueOf(env.getProperty("session.cookie.maxAge")));
    }

    public TokenExpireTime getExpireTime() {
        return new TokenExpireTime(env.getProperty("oauth.token.codeDurationInSec", Long.class), env.getProperty(
                "oauth.token.accessTokenDurationInSec", Long.class));
    }

    @Bean
    public UserResource getUserResource(UserService userService, ClientService clientService) {
        return new UserResource(userService, clientService);
    }

    @Bean
    public UsernameResource getUsernameResource(UserService userService) {
        return new UsernameResource(userService);
    }

    @Bean
    public SignoutResource getSignoutResource(SessionCookieFactory sessionCookieFactory) {
        return new SignoutResource(sessionCookieFactory);
    }

    @Bean
    public MailValidationService getMailValidationService(TokenFactory tokenFactory, ClientService clientService) {
        return new DefaultMailValidationService(mailValidationConfiguration(), getSendMailService(), tokenFactory, clientService);
    }

    @Bean
    public EmailValidationConfiguration mailValidationConfiguration() {
        return new EmailValidationConfiguration(env.getProperty("email.validation.notification"),
                env.getProperty("email.validation.clientUrl"),
                env.getProperty("oauth.token.emailValidationTokenDurationInSec", Long.class), env.getProperty("email.validation.enabled",
                        Boolean.class));
    }

    @Bean
    public MailResetPasswordService getMailResetPasswordService(TokenFactory tokenFactory, SendNotificationService sendNotificationService,
            ClientService clientService) {
        return new DefaultMailResetPasswordService(getMailResetPasswordConfiguration(), sendNotificationService, tokenFactory);
    }

    @Bean
    public MailChangePasswordService getMailChangePasswordService() {
        return new DefaultMailChangePasswordService(env.getProperty("user.changePassword.notification"), getSendMailService());
    }

    private NotificationConfiguration getMailResetPasswordConfiguration() {
        return new NotificationConfiguration(env.getProperty("email.resetPassword.notification"),
                env.getProperty("email.resetPassword.clientUrl"),
                env.getProperty("oauth.token.resetPasswordTokenDurationInSec", Long.class));
    }

    @Bean
    public SendNotificationService getSendMailService() {
        return new DefaultSendNotificationService(eventBus);
    }

    @Bean(name = "sessionProvider")
    public SessionProvider getSessionProvider(TokenParser tokenParser) {
        return new SessionProvider(tokenParser);
    }

    @Bean
    public UnauthorizedHandler getUnauthorizedHandler() {
        return new JsonUnauthorizedHandler();
    }

    @Bean
    public BasicAuthProvider getBasicAuthProvider() {
        BasicAuthFactory<Client> factory = new BasicAuthFactory<>(new ClientCredentialsAuthenticator(clientRepository), "access token",
                Client.class);
        factory.responseBuilder(getUnauthorizedHandler());
        return new BasicAuthProvider(factory);
    }

    @Bean
    public OAuthProvider getOAuthProvider(TokenParser tokenParser) {
        OAuthFactory<TokenReader> factory = new OAuthFactory<>(new TokenAuthenticator(tokenParser), "access token", TokenReader.class);
        factory.responseBuilder(getUnauthorizedHandler());
        return new OAuthProvider(factory);
    }

    @Bean
    public OauthShell getOauthShell() {
        return new OauthShell(clientRepository, getUserRepository());
    }

    @Bean
    public FilterRegistry getFilterRegistry() {
        return new InMemoryFilterRegistry();
    }

    @Bean
    AuthFilterRegistrar getAuthFilterRegistrar() {
        return new AuthFilterRegistrar(getFilterRegistry());
    }

    protected OneTimeAccessTokenRepository getOneTimeAccessTokenRepository() {
        return oneTimeAccessTokenRepository;
    }

}
