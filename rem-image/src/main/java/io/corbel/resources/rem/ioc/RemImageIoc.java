package io.corbel.resources.rem.ioc;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

import io.corbel.lib.config.ConfigurationIoC;
import io.corbel.resources.rem.ImageDeleteRem;
import io.corbel.resources.rem.ImageGetRem;
import io.corbel.resources.rem.ImagePutRem;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.operation.*;
import io.corbel.resources.rem.service.DefaultImageCacheService;
import io.corbel.resources.rem.service.DefaultImageOperationsService;
import io.corbel.resources.rem.service.ImageCacheService;
import io.corbel.resources.rem.service.ImageOperationsService;

@SuppressWarnings("unused") @Configuration @EnableAsync @Import({ConfigurationIoC.class}) public class RemImageIoc {

    private static final String CACHE_COLLECTION_DEFAULT = "image:ImageCache";
    @Autowired private Environment env;

    @Bean
    public static Map<String, ImageOperation> getOperations(List<ImageOperation> imageOperationList) {
        return imageOperationList.stream().collect(Collectors.toMap(ImageOperation::getOperationName, imageOperation -> imageOperation));
    }

    @Bean
    public ImageOperationsService getImageOperationsService(List<ImageOperation> imageOperationList) {
        return new DefaultImageOperationsService(new DefaultImageOperationsService.IMOperationFactory(),
                new DefaultImageOperationsService.ConvertCmdFactory(), getOperations(imageOperationList));
    }

    @Bean
    public Crop getCropOperation() {
        return new Crop();
    }

    @Bean
    public Blur getBlurOperation() {
        return new Blur();
    }

    @Bean
    public CropFromCenter getCropFromCenterOperation() {
        return new CropFromCenter();
    }

    @Bean
    public Resize getResizeOperation() {
        return new Resize();
    }

    @Bean
    public ResizeAndFill getResizeAndFillOperation() {
        return new ResizeAndFill();
    }

    @Bean
    public ResizeHeight getResizeheight() {
        return new ResizeHeight();
    }

    @Bean
    public ResizeWidth getResizeWidth() {
        return new ResizeWidth();
    }

    @Bean
    public ImageCacheService getImageCacheService() {
        return new DefaultImageCacheService(env.getProperty("image.cache.collection", CACHE_COLLECTION_DEFAULT));
    }

    @Bean(name = RemImageIocNames.REM_GET)
    public Rem getImageGetRem(ImageOperationsService imageOperationsService, ImageCacheService imageCacheService) {
        return new ImageGetRem(imageOperationsService, imageCacheService, env.getProperty("imagemagick.conver.memoryLimit", "200MiB"));
    }

    @Bean(name = RemImageIocNames.REM_PUT)
    public Rem getImagePutRem(ImageCacheService imageCacheService) {
        return new ImagePutRem(imageCacheService);
    }

    @Bean(name = RemImageIocNames.REM_DELETE)
    public Rem getImageDeleteRem(ImageCacheService imageCacheService) {
        return new ImageDeleteRem(imageCacheService);
    }

}
