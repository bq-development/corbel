package io.corbel.iam.ioc;

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

import io.corbel.event.DomainDeletedEvent;
import io.corbel.event.ScopeUpdateEvent;
import io.corbel.eventbus.EventHandler;
import io.corbel.eventbus.ioc.EventBusListeningIoc;
import io.corbel.eventbus.service.EventBus;
import io.corbel.iam.api.DomainResource;
import io.corbel.iam.api.EmailResource;
import io.corbel.iam.api.GroupResource;
import io.corbel.iam.api.ScopeResource;
import io.corbel.iam.api.TokenResource;
import io.corbel.iam.api.UserResource;
import io.corbel.iam.api.UsernameResource;
import io.corbel.iam.auth.AuthorizationRequestContextFactory;
import io.corbel.iam.auth.AuthorizationRule;
import io.corbel.iam.auth.provider.AuthorizationProviderFactory;
import io.corbel.iam.auth.provider.FacebookProvider;
import io.corbel.iam.auth.provider.GoogleProvider;
import io.corbel.iam.auth.provider.OAuthServerProvider;
import io.corbel.iam.auth.provider.Provider;
import io.corbel.iam.auth.provider.SpringAuthorizationProviderFactory;
import io.corbel.iam.auth.provider.TwitterProvider;
import io.corbel.iam.auth.rule.ClientSideAuthenticationAllowedAuthorizationRule;
import io.corbel.iam.auth.rule.MaxExpireAuthorizationRule;
import io.corbel.iam.auth.rule.PrincipalExistsAuthorizationRule;
import io.corbel.iam.auth.rule.RequestDomainAuthorizationRule;
import io.corbel.iam.auth.rule.ScopesAuthorizationRule;
import io.corbel.iam.auth.rule.VersionAuthorizationRule;
import io.corbel.iam.cli.dsl.IamShell;
import io.corbel.iam.eventbus.DomainDeletedEventHandler;
import io.corbel.iam.eventbus.ScopeModifiedEventHandler;
import io.corbel.iam.jwt.ClientVerifierProvider;
import io.corbel.iam.jwt.TokenUpgradeVerifierProvider;
import io.corbel.iam.model.Client;
import io.corbel.iam.model.ClientIdGenerator;
import io.corbel.iam.model.Device;
import io.corbel.iam.model.DeviceIdGenerator;
import io.corbel.iam.model.Group;
import io.corbel.iam.model.GroupIdGenerator;
import io.corbel.iam.model.Identity;
import io.corbel.iam.model.IdentityIdGenerator;
import io.corbel.iam.repository.ClientRepository;
import io.corbel.iam.repository.DeviceRepository;
import io.corbel.iam.repository.DomainRepository;
import io.corbel.iam.repository.GroupRepository;
import io.corbel.iam.repository.IdentityRepository;
import io.corbel.iam.repository.ScopeRepository;
import io.corbel.iam.repository.UserRepository;
import io.corbel.iam.repository.UserTokenRepository;
import io.corbel.iam.repository.decorator.LowerCaseDecorator;
import io.corbel.iam.scope.MustacheScopeFillStrategy;
import io.corbel.iam.scope.ScopeFillStrategy;
import io.corbel.iam.service.AuthorizationService;
import io.corbel.iam.service.ClientService;
import io.corbel.iam.service.DefaultAuthorizationService;
import io.corbel.iam.service.DefaultClientService;
import io.corbel.iam.service.DefaultDeviceService;
import io.corbel.iam.service.DefaultDomainService;
import io.corbel.iam.service.DefaultEventsService;
import io.corbel.iam.service.DefaultGroupService;
import io.corbel.iam.service.DefaultIdentityService;
import io.corbel.iam.service.DefaultMailResetPasswordService;
import io.corbel.iam.service.DefaultRefreshTokenService;
import io.corbel.iam.service.DefaultScopeService;
import io.corbel.iam.service.DefaultUpgradeTokenService;
import io.corbel.iam.service.DefaultUserService;
import io.corbel.iam.service.DeviceService;
import io.corbel.iam.service.DomainService;
import io.corbel.iam.service.EventsService;
import io.corbel.iam.service.GroupService;
import io.corbel.iam.service.IdentityService;
import io.corbel.iam.service.MailResetPasswordService;
import io.corbel.iam.service.RefreshTokenService;
import io.corbel.iam.service.ScopeService;
import io.corbel.iam.service.UpgradeTokenService;
import io.corbel.iam.service.UserService;
import io.corbel.iam.utils.DefaultTokenCookieFactory;
import io.corbel.iam.utils.TokenCookieFactory;
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
