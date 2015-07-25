package com.bq.oss.corbel.iam.ioc;

import io.corbel.lib.config.ConfigurationIoC;
import io.corbel.lib.mongo.IdGenerator;
import io.corbel.lib.mongo.IdGeneratorMongoEventListener;
import io.corbel.lib.queries.parser.CustomJsonParser;
import io.corbel.lib.queries.parser.JacksonQueryParser;
import io.corbel.lib.queries.parser.QueryParser;
import io.corbel.lib.token.factory.TokenFactory;
import io.corbel.lib.token.ioc.OneTimeAccessTokenIoc;
import io.corbel.lib.token.parser.TokenParser;
import io.corbel.lib.token.repository.OneTimeAccessTokenRepository;
import io.corbel.lib.ws.auth.ioc.AuthorizationIoc;
import io.corbel.lib.ws.auth.repository.AuthorizationRulesRepository;
import io.corbel.lib.ws.cors.ioc.CorsIoc;
import io.corbel.lib.ws.digest.DigesterFactory;
import io.corbel.lib.ws.dw.ioc.CommonFiltersIoc;
import io.corbel.lib.ws.dw.ioc.DropwizardIoc;
import io.corbel.lib.ws.ioc.QueriesIoc;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;

import net.oauth.jsontoken.Checker;
import net.oauth.jsontoken.JsonTokenParser;
import net.oauth.jsontoken.crypto.SignatureAlgorithm;
import net.oauth.jsontoken.discovery.VerifierProvider;
import net.oauth.jsontoken.discovery.VerifierProviders;
import net.oauth.signatures.SignedJsonAssertionAudienceChecker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

import com.bq.oss.corbel.event.DomainDeletedEvent;
import com.bq.oss.corbel.event.ScopeUpdateEvent;
import com.bq.oss.corbel.eventbus.EventHandler;
import com.bq.oss.corbel.eventbus.ioc.EventBusListeningIoc;
import com.bq.oss.corbel.eventbus.service.EventBus;
import com.bq.oss.corbel.iam.api.DomainResource;
import com.bq.oss.corbel.iam.api.EmailResource;
import com.bq.oss.corbel.iam.api.GroupResource;
import com.bq.oss.corbel.iam.api.ScopeResource;
import com.bq.oss.corbel.iam.api.TokenResource;
import com.bq.oss.corbel.iam.api.UserResource;
import com.bq.oss.corbel.iam.api.UsernameResource;
import com.bq.oss.corbel.iam.auth.AuthorizationRequestContextFactory;
import com.bq.oss.corbel.iam.auth.AuthorizationRule;
import com.bq.oss.corbel.iam.auth.provider.AuthorizationProviderFactory;
import com.bq.oss.corbel.iam.auth.provider.FacebookProvider;
import com.bq.oss.corbel.iam.auth.provider.GoogleProvider;
import com.bq.oss.corbel.iam.auth.provider.OAuthServerProvider;
import com.bq.oss.corbel.iam.auth.provider.Provider;
import com.bq.oss.corbel.iam.auth.provider.SpringAuthorizationProviderFactory;
import com.bq.oss.corbel.iam.auth.provider.TwitterProvider;
import com.bq.oss.corbel.iam.auth.rule.ClientSideAuthenticationAllowedAuthorizationRule;
import com.bq.oss.corbel.iam.auth.rule.MaxExpireAuthorizationRule;
import com.bq.oss.corbel.iam.auth.rule.PrincipalExistsAuthorizationRule;
import com.bq.oss.corbel.iam.auth.rule.RequestDomainAuthorizationRule;
import com.bq.oss.corbel.iam.auth.rule.ScopesAuthorizationRule;
import com.bq.oss.corbel.iam.auth.rule.VersionAuthorizationRule;
import com.bq.oss.corbel.iam.cli.dsl.IamShell;
import com.bq.oss.corbel.iam.eventbus.DomainDeletedEventHandler;
import com.bq.oss.corbel.iam.eventbus.ScopeModifiedEventHandler;
import com.bq.oss.corbel.iam.jwt.ClientVerifierProvider;
import com.bq.oss.corbel.iam.jwt.TokenUpgradeVerifierProvider;
import com.bq.oss.corbel.iam.model.Client;
import com.bq.oss.corbel.iam.model.ClientIdGenerator;
import com.bq.oss.corbel.iam.model.Device;
import com.bq.oss.corbel.iam.model.DeviceIdGenerator;
import com.bq.oss.corbel.iam.model.Group;
import com.bq.oss.corbel.iam.model.GroupIdGenerator;
import com.bq.oss.corbel.iam.model.Identity;
import com.bq.oss.corbel.iam.model.IdentityIdGenerator;
import com.bq.oss.corbel.iam.repository.ClientRepository;
import com.bq.oss.corbel.iam.repository.DeviceRepository;
import com.bq.oss.corbel.iam.repository.DomainRepository;
import com.bq.oss.corbel.iam.repository.GroupRepository;
import com.bq.oss.corbel.iam.repository.IdentityRepository;
import com.bq.oss.corbel.iam.repository.ScopeRepository;
import com.bq.oss.corbel.iam.repository.UserRepository;
import com.bq.oss.corbel.iam.repository.UserTokenRepository;
import com.bq.oss.corbel.iam.repository.decorator.LowerCaseDecorator;
import com.bq.oss.corbel.iam.scope.MustacheScopeFillStrategy;
import com.bq.oss.corbel.iam.scope.ScopeFillStrategy;
import com.bq.oss.corbel.iam.service.AuthorizationService;
import com.bq.oss.corbel.iam.service.ClientService;
import com.bq.oss.corbel.iam.service.DefaultAuthorizationService;
import com.bq.oss.corbel.iam.service.DefaultClientService;
import com.bq.oss.corbel.iam.service.DefaultDeviceService;
import com.bq.oss.corbel.iam.service.DefaultDomainService;
import com.bq.oss.corbel.iam.service.DefaultEventsService;
import com.bq.oss.corbel.iam.service.DefaultGroupService;
import com.bq.oss.corbel.iam.service.DefaultIdentityService;
import com.bq.oss.corbel.iam.service.DefaultMailResetPasswordService;
import com.bq.oss.corbel.iam.service.DefaultRefreshTokenService;
import com.bq.oss.corbel.iam.service.DefaultScopeService;
import com.bq.oss.corbel.iam.service.DefaultUpgradeTokenService;
import com.bq.oss.corbel.iam.service.DefaultUserService;
import com.bq.oss.corbel.iam.service.DeviceService;
import com.bq.oss.corbel.iam.service.DomainService;
import com.bq.oss.corbel.iam.service.EventsService;
import com.bq.oss.corbel.iam.service.GroupService;
import com.bq.oss.corbel.iam.service.IdentityService;
import com.bq.oss.corbel.iam.service.MailResetPasswordService;
import com.bq.oss.corbel.iam.service.RefreshTokenService;
import com.bq.oss.corbel.iam.service.ScopeService;
import com.bq.oss.corbel.iam.service.UpgradeTokenService;
import com.bq.oss.corbel.iam.service.UserService;
import com.bq.oss.corbel.iam.utils.DefaultTokenCookieFactory;
import com.bq.oss.corbel.iam.utils.TokenCookieFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.google.gson.Gson;

/**
 * @author Alexander De Leon
 */
@SuppressWarnings("unused") @Configuration @Import({ConfigurationIoC.class, IamMongoIoc.class, IamProviderIoc.class,
        TokenVerifiersIoc.class, OneTimeAccessTokenIoc.class, DropwizardIoc.class, AuthorizationIoc.class, CorsIoc.class, QueriesIoc.class,
        EventBusListeningIoc.class, CommonFiltersIoc.class}) public class IamIoc {

    @Autowired(required = true) private Environment env;

    @Autowired private ClientRepository clientRepository;
    @Autowired private DomainRepository domainRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ScopeRepository scopeRepository;
    @Autowired private AuthorizationRulesRepository authorizationRulesRepository;
    @Autowired private IdentityRepository identityRepository;
    @Autowired private OneTimeAccessTokenRepository oneTimeAccessTokenRepository;
    @Autowired private UserTokenRepository userTokenRepository;
    @Autowired private DeviceRepository deviceRepository;
    @Autowired private GroupRepository groupRepository;

    private UserRepository getUserRepository() {
        return new LowerCaseDecorator(userRepository);
    }

    @Bean
    public EventHandler<DomainDeletedEvent> domainDeletedEventHandler() {
        return new DomainDeletedEventHandler(clientRepository, userRepository);
    }

    @Bean
    public EventHandler<ScopeUpdateEvent> scopeUpdateEventEventHandler(CacheManager cacheManager) {
        return new ScopeModifiedEventHandler(cacheManager);
    }


    @Bean
    public AuthorizationService getAuthorizationService(RefreshTokenService refreshTokenService, TokenFactory tokenFactory,
            ScopeService scopeService, ScopesAuthorizationRule scopesAuthorizationRule, UserService userService, EventsService eventsService) {
        return new DefaultAuthorizationService(getJsonTokenParser(), getAuthorizationRules(scopesAuthorizationRule), tokenFactory,
                getAuthorizationRequestContextFactory(scopeService), scopeService, getAuthorizationProviderFactory(), refreshTokenService,
                userTokenRepository, userService, eventsService);

    }

    @Bean
    public RefreshTokenService getRefreshTokenService(TokenParser tokenParser, TokenFactory tokenFactory,
            OneTimeAccessTokenRepository oneTimeAccessTokenRepository) {
        return new DefaultRefreshTokenService(tokenParser, getUserRepository(), tokenFactory, env.getProperty(
                "iam.auth.refreshToken.maxExpirationInSeconds", Long.class), oneTimeAccessTokenRepository);
    }

    @Bean
    public JsonTokenParser getJsonTokenParser() {
        return new JsonTokenParser(getVerifierProviders(), getAudienceChecker());
    }

    protected OneTimeAccessTokenRepository getOneTimeAccessTokenRepository() {
        return oneTimeAccessTokenRepository;
    }

    @Bean
    public TokenResource getTokenResource(AuthorizationService authorizationService, UpgradeTokenService upgradeTokenService) {
        return new TokenResource(authorizationService, upgradeTokenService, getSessionCookieFactory());
    }

    @Bean
    public UserResource getUserResource(UserService userService, DomainService domainService, IdentityService identityService,
            DeviceService deviceService) {
        return new UserResource(userService, domainService, identityService, deviceService, Clock.systemUTC());
    }

    @Bean
    public UsernameResource getUsernameResource(UserService userService) {
        return new UsernameResource(userService);
    }

    @Bean
    public EmailResource getEmailResource(UserService userService) {
        return new EmailResource(userService);
    }

    @Bean
    public ScopeResource getScopeResource(ScopeService scopeService) {
        return new ScopeResource(scopeService);
    }

    @Bean
    public DomainResource getDomainResource(ClientService clientService, DomainService domainService) {
        return new DomainResource(clientService, domainService);
    }

    @Bean
    public GroupResource getGroupResource(GroupService groupService) {
        return new GroupResource(groupService);
    }

    @Bean
    public ClientService getClientService(ClientRepository clientRepository) {
        return new DefaultClientService(clientRepository);
    }

    @Bean
    public IdentityService getIdentityService() {
        return new DefaultIdentityService(identityRepository);
    }

    @Bean
    DeviceService getDeviceService(DeviceRepository deviceRepository) {
        return new DefaultDeviceService(deviceRepository, getDeviceIdGenerator());
    }

    @Bean
    public UserService getUserService(EventsService eventsService, RefreshTokenService refreshTokenService,
            MailResetPasswordService mailResetPasswordService, Gson gson) {
        return new DefaultUserService(getUserRepository(), eventsService, userTokenRepository, authorizationRulesRepository,
                refreshTokenService, mailResetPasswordService, gson);
    }

    @Bean
    public MailResetPasswordService getMailResetPasswordService(EventsService eventsService, ScopeService scopeService,
            TokenFactory tokenFactory, ClientRepository clientRepository) {
        return new DefaultMailResetPasswordService(eventsService, scopeService, tokenFactory, clientRepository, env.getProperty(
                "iam.token.resetPasswordTokenScope", String.class), Clock.systemUTC(), env.getProperty(
                "iam.token.resetPasswordTokenDurationInSec", Long.class),
                env.getProperty("email.resetPassword.notification", String.class), env.getProperty("email.resetPassword.clientUrl",
                        String.class));
    }

    @Bean
    public EventsService getEventsService(EventBus eventBus) {
        return new DefaultEventsService(eventBus);

    }

    @Bean
    public GroupService getGroupService() {
        return new DefaultGroupService(groupRepository);
    }

    @Bean
    public AuthorizationProviderFactory getAuthorizationProviderFactory() {
        return new SpringAuthorizationProviderFactory();
    }

    @Bean(name = "facebook")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Provider getFacebookProvider() {
        return new FacebookProvider(identityRepository);

    }

    @Bean(name = "twitter")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Provider getTwitterProvider() {
        return new TwitterProvider(identityRepository);

    }

    @Bean(name = "google")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Provider getGoogleProvider() {
        return new GoogleProvider(identityRepository);
    }

    @Bean(name = "oauth-server")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Provider getCorbelProvider() {
        return new OAuthServerProvider(identityRepository);
    }

    private List<AuthorizationRule> getAuthorizationRules(ScopesAuthorizationRule scopesAuthorizationRule) {
        return Arrays.asList(new PrincipalExistsAuthorizationRule(), new VersionAuthorizationRule(), new RequestDomainAuthorizationRule(),
                scopesAuthorizationRule,
                new MaxExpireAuthorizationRule(env.getProperty("iam.auth.token.maxExpirationInMillis", Integer.class)),
                new ClientSideAuthenticationAllowedAuthorizationRule());
    }

    @Bean
    public ScopesAuthorizationRule getScopesAuthorizationRule(ScopeService scopeService) {
        return new ScopesAuthorizationRule(scopeService);
    }

    @Bean
    public JsonTokenParser getAuthorizationJsonTokenParser() {
        return new JsonTokenParser(getVerifierProviders(), getAudienceChecker());
    }

    @Bean
    public JsonTokenParser getTokenUpgradeJsonTokenParser() {
        return new JsonTokenParser(getUpgradeTokenVerifierProviders(), getAudienceChecker());
    }

    @Bean
    public IamShell getIamShell(GroupService groupService) {
        return new IamShell(clientRepository, scopeRepository, getUserRepository(), domainRepository, env.getProperty("iam.uri"),
                identityRepository, groupRepository);
    }

    @Bean
    public ScopeService getScopeService(EventsService eventsService, GroupService groupService) {
        return new DefaultScopeService(scopeRepository, groupService, authorizationRulesRepository, getScopeFillStrategy(),
                env.getProperty("iam.uri"), eventsService);
    }

    @Bean
    public IdGeneratorMongoEventListener<Client> getClientIdGeneratorMongoEventListener() {
        return new IdGeneratorMongoEventListener<>(getClientIdGenerator(), Client.class);
    }

    @Bean
    public IdGeneratorMongoEventListener<Identity> getIdentityIdGeneratorMongoEventListener() {
        return new IdGeneratorMongoEventListener<>(getIdentityIdGenerator(), Identity.class);
    }

    @Bean
    public IdGeneratorMongoEventListener<Device> getDeviceIdGeneratorMongoEventListener() {
        return new IdGeneratorMongoEventListener<>(getDeviceIdGenerator(), Device.class);
    }

    @Bean
    public DomainService getDomainService(ScopeService scopeService, EventsService eventsService) {
        return new DefaultDomainService(domainRepository, scopeService, eventsService);
    }

    @Bean
    public UpgradeTokenService getUpgradeTokenService(ScopeService scopeService) {
        return new DefaultUpgradeTokenService(getTokenUpgradeJsonTokenParser(), scopeService);
    }

    @Bean
    public TokenCookieFactory getSessionCookieFactory() {
        return new DefaultTokenCookieFactory(env.getProperty("token.cookie.path"), env.getProperty("token.cookie.domain"),
                env.getProperty("token.cookie.comment"), env.getProperty("token.cookie.secure", Boolean.class));
    }

    @Bean
    public QueryParser getQueryParser() {
        return new JacksonQueryParser(getCustomJsonParser());
    }

    @Bean
    public CustomJsonParser getCustomJsonParser() {
        return new CustomJsonParser(getObjectMapper().getFactory());
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JSR310Module());
        return mapper;
    }

    @Bean
    public Gson getGson() {
        return new Gson();
    }

    private IdGenerator<Client> getClientIdGenerator() {
        return new ClientIdGenerator(DigesterFactory.murmur3_32());
    }

    private IdGenerator<Identity> getIdentityIdGenerator() {
        return new IdentityIdGenerator();
    }

    private IdGenerator<Group> getGroupIdGenerator() {
        return new GroupIdGenerator(DigesterFactory.murmur3_32());
    }

    @Bean
    public IdGenerator<Device> getDeviceIdGenerator() {
        return new DeviceIdGenerator(DigesterFactory.sha1());
    }

    private ScopeFillStrategy getScopeFillStrategy() {
        return new MustacheScopeFillStrategy();
    }

    private AuthorizationRequestContextFactory getAuthorizationRequestContextFactory(ScopeService scopeService) {
        return new AuthorizationRequestContextFactory(clientRepository, domainRepository, getUserRepository());
    }

    private Checker getAudienceChecker() {
        return new SignedJsonAssertionAudienceChecker(env.getProperty("iam.uri"));
    }

    private VerifierProviders getVerifierProviders() {
        VerifierProviders providers = new VerifierProviders();
        providers.setVerifierProvider(SignatureAlgorithm.HS256, getClientVerifierProvider());
        providers.setVerifierProvider(SignatureAlgorithm.RS256, getClientVerifierProvider());
        return providers;
    }

    private VerifierProviders getUpgradeTokenVerifierProviders() {
        VerifierProviders providers = new VerifierProviders();
        providers.setVerifierProvider(SignatureAlgorithm.HS256, getTokenUpgradeVerifierProvider());
        return providers;
    }

    private VerifierProvider getClientVerifierProvider() {
        return new ClientVerifierProvider(clientRepository);
    }

    private VerifierProvider getTokenUpgradeVerifierProvider() {
        return new TokenUpgradeVerifierProvider(env.getProperty("assets.upgradeSignerKey"));
    }
}
