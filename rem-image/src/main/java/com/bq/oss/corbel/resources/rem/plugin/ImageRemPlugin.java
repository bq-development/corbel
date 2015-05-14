package com.bq.oss.corbel.resources.rem.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.bq.oss.corbel.resources.rem.ImageGetRem;
import com.bq.oss.corbel.resources.rem.Rem;
import com.bq.oss.corbel.resources.rem.RemRegistry;
import com.bq.oss.corbel.resources.rem.ioc.RemImageIoc;
import com.bq.oss.corbel.resources.rem.ioc.RemImageIocNames;
import com.bq.oss.lib.config.ConfigurationHelper;

@Component public class ImageRemPlugin extends RemPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(ImageRemPlugin.class);
    private final String ARTIFACT_ID = "rem-image";

    @Override
    protected void init() {
        LOG.info("Initializing Image plugin.");
        super.init();
        ConfigurationHelper.setConfigurationNamespace(ARTIFACT_ID);
        context = new AnnotationConfigApplicationContext(RemImageIoc.class);
    }

    @Override
    protected void register(RemRegistry registry) {
        ImageGetRem bean = (ImageGetRem) context.getBean(RemImageIocNames.REM_GET, Rem.class);
        bean.setRemService(remService);
        registry.registerRem(bean, "^(?!" + context.getEnvironment().getProperty("image.cache.collection") + "$).*",
                MediaType.parseMediaType("image/*"), HttpMethod.GET);
    }

    @Override
    protected String getArtifactName() {
        return ARTIFACT_ID;
    }

}
