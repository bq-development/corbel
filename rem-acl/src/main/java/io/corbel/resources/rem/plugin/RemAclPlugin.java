package io.corbel.resources.rem.plugin;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import io.corbel.event.ResourceEvent;
import io.corbel.eventbus.service.EventBus;
import io.corbel.lib.config.ConfigurationHelper;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.RemRegistry;
import io.corbel.resources.rem.acl.*;
import io.corbel.resources.rem.eventbus.AclConfigurationEventHandler;
import io.corbel.resources.rem.ioc.AclRemNames;
import io.corbel.resources.rem.ioc.RemAclIoc;
import io.corbel.resources.rem.service.AclResourcesService;

/**
 * @author Cristian del Cerro
 */

@Component public class RemAclPlugin extends RemPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(RemAclPlugin.class);

    private static final String ACL_MEDIA_TYPE = "application/corbel.acl+json";
    private static final String ARTIFACT_ID = "rem-acl";
    private static final String ACL_CONFIGURATION_COLLECTION = "acl:Configuration";

    private String aclConfigurationCollection;

    @Override
    protected void init() {
        LOG.info("Initializing ACL plugin.");
        super.init();
        ConfigurationHelper.setConfigurationNamespace(ARTIFACT_ID);
        context = new AnnotationConfigApplicationContext(RemAclIoc.class);

        AclPostRem aclPostRem = context.getBean(AclPostRem.class);
        AclGetRem aclGetRem = context.getBean(AclGetRem.class);
        AclPutRem aclPutRem = context.getBean(AclPutRem.class);
        AclDeleteRem aclDeleteRem = context.getBean(AclDeleteRem.class);

        aclPostRem.setRemService(remService);
        aclGetRem.setRemService(remService);
        aclPutRem.setRemService(remService);
        aclDeleteRem.setRemService(remService);
        context.getBean(SetUpAclPutRem.class).setRemService(remService);

        List<Pair<Rem, HttpMethod>> remsAndMethods = Arrays.asList(Pair.of(aclPostRem, HttpMethod.POST), Pair.of(aclPutRem, HttpMethod.PUT),
                Pair.of(aclGetRem, HttpMethod.GET), Pair.of(aclDeleteRem, HttpMethod.DELETE));

        AclResourcesService aclResourcesService = context.getBean(AclResourcesService.class);
        aclResourcesService.setRemService(remService);
        aclResourcesService.setRemsAndMethods(remsAndMethods);

        context.getBean(AclConfigurationEventHandler.class).setAclResourcesService(aclResourcesService);
        aclConfigurationCollection = context.getEnvironment().getProperty("rem.acl.configuration.collection", ACL_CONFIGURATION_COLLECTION);

        context.getBean(EventBus.class).dispatch(ResourceEvent.createResourceEvent(aclConfigurationCollection, "@ALL", "", ""));
    }

    @Override
    protected void register(RemRegistry registry) {
        registry.registerRem(context.getBean(AclRemNames.SETUP_PUT, Rem.class), ".*", MediaType.valueOf(ACL_MEDIA_TYPE), HttpMethod.PUT);
        registry.registerRem(context.getBean(AclRemNames.ADMIN_POST, Rem.class), aclConfigurationCollection, MediaType.ALL,
                HttpMethod.POST);
        registry.registerRem(context.getBean(AclRemNames.ADMIN_PUT, Rem.class), aclConfigurationCollection, MediaType.ALL, HttpMethod.PUT);
    }

    @Override
    protected String getArtifactName() {
        return ARTIFACT_ID;
    }

}
