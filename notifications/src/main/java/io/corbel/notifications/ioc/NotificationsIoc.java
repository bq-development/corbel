package io.corbel.notifications.ioc;

import java.io.InputStream;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.corbel.lib.queries.request.AggregationResultsFactory;
import io.corbel.lib.queries.request.JsonAggregationResultsFactory;
import io.corbel.notifications.api.DomainResource;
import io.corbel.notifications.repository.DomainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;

import io.corbel.lib.config.ConfigurationIoC;
import io.corbel.lib.mongo.config.DefaultMongoConfiguration;
import io.corbel.lib.queries.mongo.repository.QueriesRepositoryFactoryBean;
import io.corbel.lib.token.ioc.TokenIoc;
import io.corbel.lib.ws.auth.ioc.AuthorizationIoc;
import io.corbel.lib.ws.cors.ioc.CorsIoc;
import io.corbel.lib.ws.dw.ioc.CommonFiltersIoc;
import io.corbel.lib.ws.dw.ioc.DropwizardIoc;
import io.corbel.lib.ws.dw.ioc.MongoHealthCheckIoc;
import io.corbel.lib.ws.ioc.QueriesIoc;
import io.corbel.notifications.api.NotificationsResource;
import io.corbel.notifications.cli.dsl.NotificationsShell;
import io.corbel.notifications.repository.NotificationRepository;
import io.corbel.notifications.service.AndroidPushNotificationsService;
import io.corbel.notifications.service.ApplePushNotificationsService;
import io.corbel.notifications.service.DefaultSenderNotificationsService;
import io.corbel.notifications.service.EmailNotificationsService;
import io.corbel.notifications.service.NotificationsDispatcher;
import io.corbel.notifications.service.NotificationsService;
import io.corbel.notifications.service.NotificationsServiceFactory;
import io.corbel.notifications.service.SenderNotificationsService;
import io.corbel.notifications.service.SpringNotificationsServiceFactory;
import io.corbel.notifications.template.DefaultNotificationFiller;
import io.corbel.notifications.template.NotificationFiller;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ApnsServiceBuilder;

/**
 * Created by Alberto J. Rubio
 */
@Configuration @Import({ConfigurationIoC.class, DropwizardIoc.class, TokenIoc.class, AuthorizationIoc.class, CorsIoc.class,
        QueriesIoc.class, MongoHealthCheckIoc.class, CommonFiltersIoc.class}) @EnableMongoRepositories(
        value = "io.corbel.notifications.repository", repositoryFactoryBeanClass = QueriesRepositoryFactoryBean.class) public class NotificationsIoc
        extends DefaultMongoConfiguration {

    @Autowired private Environment env;

    @Bean
    public NotificationsShell getNotificationsShell(NotificationRepository notificationRepository,
                                                    DomainRepository domainRepository) {
        return new NotificationsShell(notificationRepository, domainRepository);
    }

    @Bean
    public NotificationsResource getTemplateResource(NotificationRepository notificationRepository,
            SenderNotificationsService senderNotificationsService) {
        return new NotificationsResource(notificationRepository, senderNotificationsService);
    }

    @Bean
    public DomainResource getDomainResource(DomainRepository domainRepository,
                                                                    AggregationResultsFactory aggregationResultsFactory) {
        return new DomainResource(domainRepository, aggregationResultsFactory);
    }

    @Bean
    public MongoRepositoryFactory getMongoRepositoryFactory(MongoOperations mongoOperations) {
        return new MongoRepositoryFactory(mongoOperations);
    }

    @Bean
    public SenderNotificationsService getNotificationsEventService(NotificationRepository notificationRepository,
            NotificationsDispatcher notificationsDispatcher, DomainRepository domainRepository) {
        return new DefaultSenderNotificationsService(getTemplateFiller(), notificationsDispatcher, notificationRepository,
                domainRepository);
    }

    @Bean
    public NotificationFiller getTemplateFiller() {
        return new DefaultNotificationFiller();
    }

    @Bean
    public NotificationsDispatcher getNotificationsDispatcher(NotificationsServiceFactory notificationsServiceFactory) {
        return new NotificationsDispatcher(notificationsServiceFactory);
    }

    @Bean
    public NotificationsServiceFactory getNotificationsServiceFactory() {
        return new SpringNotificationsServiceFactory();
    }

    @Bean(name = "email")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public NotificationsService getNotificationsService() {
        return new EmailNotificationsService();
    }

    @Bean(name = "android")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public NotificationsService getAndroidPushNotificationService() {
        return new AndroidPushNotificationsService();
    }

    @Bean(name = "apple")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public NotificationsService getApplePushNotificationService() {
        return new ApplePushNotificationsService(getApnsService());
    }

    public ApnsService getApnsService() {
        InputStream certificate = this.getClass().getClassLoader().getResourceAsStream("certs/" + env.getProperty("apple.cert.name"));
        ApnsServiceBuilder apnsServiceBuilder = APNS.newService().withCert(certificate, env.getProperty("apple.cert.password"));
        if (env.getProperty("apple.cert.production", Boolean.class)) {
            apnsServiceBuilder.withProductionDestination();
        } else {
            apnsServiceBuilder.withSandboxDestination();
        }
        return apnsServiceBuilder.build();
    }

    @Bean
    public Gson getGson() {
        return new Gson();
    }

    @Bean
    public AggregationResultsFactory<JsonElement> aggregationResultsFactory(Gson gson){
        return new JsonAggregationResultsFactory(gson);
    }

    @Override
    protected Environment getEnvironment() {
        return env;
    }

    @Override
    protected String getDatabaseName() {
        return "notifications";
    }
}
