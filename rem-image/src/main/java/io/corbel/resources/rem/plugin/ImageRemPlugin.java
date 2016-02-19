package io.corbel.resources.rem.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import io.corbel.lib.config.ConfigurationHelper;
import io.corbel.resources.rem.ImageBaseRem;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.RemRegistry;
import io.corbel.resources.rem.ioc.RemImageIoc;
import io.corbel.resources.rem.ioc.RemImageIocNames;

@Component public class ImageRemPlugin extends RemPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(ImageRemPlugin.class);
    private static final String IMAGE_CACHE_COLLECTION = "image.cache.collection";
    private static final String IMAGE_PATH = "image/*";
    private static final String ARTIFACT_ID = "rem-image";

    @Override
    protected void init() {
        LOG.info("Initializing Image plugin.");
        super.init();
        ConfigurationHelper.setConfigurationNamespace(ARTIFACT_ID);
        context = new AnnotationConfigApplicationContext(RemImageIoc.class);
    }

    @Override
    protected void register(RemRegistry registry) {
        String cacheCollection = context.getEnvironment().getProperty(IMAGE_CACHE_COLLECTION);
        registerRem(RemImageIocNames.REM_GET, HttpMethod.GET, registry, cacheCollection);
        registerRem(RemImageIocNames.REM_PUT, HttpMethod.PUT, registry, cacheCollection);
        registerRem(RemImageIocNames.REM_DELETE, HttpMethod.DELETE, registry, cacheCollection);
    }

    private void registerRem(String remImageIocNames, HttpMethod httpMethod, RemRegistry registry, String cacheCollection) {
        ImageBaseRem beanImageRem = (ImageBaseRem) context.getBean(remImageIocNames, Rem.class);
        beanImageRem.setRemService(remService);
        registry.registerRem(beanImageRem, "^(?!" + cacheCollection + "$).*", MediaType.parseMediaType(IMAGE_PATH), httpMethod);
    }

    @Override
    protected String getArtifactName() {
        return ARTIFACT_ID;
    }

}
