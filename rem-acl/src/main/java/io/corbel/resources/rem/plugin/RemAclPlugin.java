package io.corbel.resources.rem.plugin;

import io.corbel.lib.config.ConfigurationHelper;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.RemRegistry;
import io.corbel.resources.rem.acl.AclDeleteRem;
import io.corbel.resources.rem.acl.AclGetRem;
import io.corbel.resources.rem.acl.AclPostRem;
import io.corbel.resources.rem.acl.AclPutRem;
import io.corbel.resources.rem.acl.SetUpAclPutRem;
import io.corbel.resources.rem.ioc.AclRemNames;
import io.corbel.resources.rem.ioc.RemAclIoc;
import io.corbel.resources.rem.service.AclResourcesService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * @author Cristian del Cerro
 */

@Component public class RemAclPlugin extends RemPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(RemAclPlugin.class);

    private static final String ACL_MEDIA_TYPE = "application/corbel.acl+json";
    private static final String ARTIFACT_ID = "rem-acl";

    private String uriPattern;

    @Override
    protected void init() {
        LOG.info("Initializing ACL plugin.");
        super.init();
        ConfigurationHelper.setConfigurationNamespace(ARTIFACT_ID);
        context = new AnnotationConfigApplicationContext(RemAclIoc.class);
        context.getBean(AclResourcesService.class).setRemService(remService);
        context.getBean(AclPostRem.class).setRemService(remService);
        context.getBean(AclGetRem.class).setRemService(remService);
        context.getBean(AclPutRem.class).setRemService(remService);
        context.getBean(AclDeleteRem.class).setRemService(remService);
        context.getBean(SetUpAclPutRem.class).setRemService(remService);
        uriPattern = context.getEnvironment().getProperty("acl.uriPattern", String.class);
    }

    @Override
    protected void register(RemRegistry registry) {
        registry.registerRem(context.getBean(AclRemNames.POST, Rem.class), uriPattern, MediaType.ALL, HttpMethod.POST);
        registry.registerRem(context.getBean(AclRemNames.GET, Rem.class), uriPattern, MediaType.ALL, HttpMethod.GET);
        registry.registerRem(context.getBean(AclRemNames.PUT, Rem.class), uriPattern, MediaType.ALL, HttpMethod.PUT);
        registry.registerRem(context.getBean(AclRemNames.DELETE, Rem.class), uriPattern, MediaType.ALL, HttpMethod.DELETE);
        registry.registerRem(context.getBean(AclRemNames.SETUP_PUT, Rem.class), uriPattern, MediaType.valueOf(ACL_MEDIA_TYPE),
                HttpMethod.PUT);
    }

    @Override
    protected String getArtifactName() {
        return ARTIFACT_ID;
    }
}
