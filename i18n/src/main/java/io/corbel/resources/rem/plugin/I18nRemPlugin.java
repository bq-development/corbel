package io.corbel.resources.rem.plugin;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import io.corbel.resources.rem.RemRegistry;
import io.corbel.resources.rem.i18n.I18nBaseRem;
import io.corbel.resources.rem.i18n.ioc.I18nIoc;
import io.corbel.resources.rem.i18n.ioc.I18nRemNames;
import io.corbel.lib.config.ConfigurationHelper;
import com.google.common.collect.ImmutableMap;

@Component public class I18nRemPlugin extends RemPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(I18nRemPlugin.class);
    private static final String ARTIFACT_ID = "i18n";

    @Override
    protected void init() {
        LOG.info("Initializing I18N plugin.");
        super.init();
        ConfigurationHelper.setConfigurationNamespace(ARTIFACT_ID);
        context = new AnnotationConfigApplicationContext(I18nIoc.class);
    }

    @Override
    protected void register(RemRegistry registry) {
        Map<HttpMethod, I18nBaseRem> rems = ImmutableMap.of(HttpMethod.GET, context.getBean(I18nRemNames.I18N_GET, I18nBaseRem.class),
                HttpMethod.PUT, context.getBean(I18nRemNames.I18N_PUT, I18nBaseRem.class), HttpMethod.DELETE,
                context.getBean(I18nRemNames.I18N_DELETE, I18nBaseRem.class));

        rems.forEach((method, rem) -> {
            rem.setRemService(remService);
            registry.registerRem(rem, "i18n:.*", MediaType.valueOf("application/corbel.i18n+json"), method);
        });
    }

    @Override
    protected String getArtifactName() {
        return ARTIFACT_ID;
    }

}
