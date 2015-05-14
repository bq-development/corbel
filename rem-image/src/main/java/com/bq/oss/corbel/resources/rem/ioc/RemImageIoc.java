package com.bq.oss.corbel.resources.rem.ioc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

import com.bq.oss.corbel.resources.rem.ImageGetRem;
import com.bq.oss.corbel.resources.rem.Rem;
import com.bq.oss.corbel.resources.rem.service.DefaultImageCacheService;
import com.bq.oss.corbel.resources.rem.service.DefaultResizeImageService;
import com.bq.oss.corbel.resources.rem.service.ImageCacheService;
import com.bq.oss.corbel.resources.rem.service.ResizeImageService;
import com.bq.oss.lib.config.ConfigurationIoC;

@Configuration // Import configuration mechanism
@EnableAsync @Import({ConfigurationIoC.class}) public class RemImageIoc {

    @Autowired private Environment env;

    @Bean
    public ResizeImageService getResizeImageService() {
        return new DefaultResizeImageService();
    }

    @Bean
    public ImageCacheService getImageCacheService() {
        return new DefaultImageCacheService(env.getProperty("image.cache.collection"));
    }

    @Bean(name = RemImageIocNames.REM_GET)
    public Rem getImageGetRem() {
        return new ImageGetRem(getResizeImageService(), getImageCacheService());
    }
}
