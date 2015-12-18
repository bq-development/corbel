package io.corbel.resources.rem.ioc;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import io.corbel.event.ResourceEvent;
import io.corbel.eventbus.EventHandler;
import io.corbel.eventbus.ioc.EventBusListeningIoc;
import io.corbel.lib.config.ConfigurationIoC;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.acl.*;
import io.corbel.resources.rem.eventbus.AclConfigurationEventHandler;
import io.corbel.resources.rem.service.AclResourcesService;
import io.corbel.resources.rem.service.DefaultAclResourcesService;

/**
 * @author Cristian del Cerro
 */
@Configuration @Import({ConfigurationIoC.class, EventBusListeningIoc.class}) public class RemAclIoc {

    @Autowired private Environment env;

    @Bean
    public EventHandler<ResourceEvent> getAclConfigurationEventHandlerAsEventHandler() {
        return new AclConfigurationEventHandler(env.getProperty("rem.acl.configuration.collection"));
    }

    @Bean(name = AclRemNames.POST)
    public Rem getAclPostRem() {
        return new AclPostRem(getAclResourceService(), Collections.singletonList(getAclPutRem()));
    }

    @Bean(name = AclRemNames.GET)
    public Rem getAclGetRem() {
        return new AclGetRem(getAclResourceService());
    }

    @Bean(name = AclRemNames.PUT)
    public Rem getAclPutRem() {
        return new AclPutRem(getAclResourceService(), Collections.singletonList(getAclGetRem()));
    }

    @Bean(name = AclRemNames.DELETE)
    public Rem getAclDeleteRem() {
        return new AclDeleteRem(getAclResourceService(), Collections.singletonList(getAclGetRem()));
    }

    @Bean(name = AclRemNames.SETUP_PUT)
    public Rem getAclSetUpPutRem() {
        return new SetUpAclPutRem(getAclResourceService(), Arrays.asList(getAclGetRem(), getAclPutRem()));
    }

    @Bean(name = AclRemNames.ADMIN_POST)
    public Rem getAclPostManagedCollectionRem() {
        return new AclPostManagedCollectionRem(getAclResourceService());
    }

    @Bean(name = AclRemNames.ADMIN_PUT)
    public Rem getAclPutManagedCollectionRem() {
        return new AclPutManagedCollectionRem(getAclResourceService());
    }

    @Bean
    public Gson getGson() {
        return new Gson();
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public AclResourcesService getAclResourceService() {
        return new DefaultAclResourcesService(getGson(), env.getProperty("rem.acl.configuration.collection"));
    }

}
