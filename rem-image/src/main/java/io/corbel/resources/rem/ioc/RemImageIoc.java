package io.corbel.resources.rem.ioc;

import io.corbel.resources.rem.ImageDeleteRem;
import io.corbel.resources.rem.ImageGetRem;
import io.corbel.resources.rem.ImagePutRem;
import io.corbel.resources.rem.Rem;
import io.corbel.resources.rem.operation.*;
import io.corbel.resources.rem.service.DefaultImageCacheService;
import io.corbel.resources.rem.service.DefaultImageOperationsService;
import io.corbel.resources.rem.service.ImageCacheService;
import io.corbel.resources.rem.service.ImageOperationsService;
import io.corbel.resources.rem.util.ImageRemUtil;
import io.corbel.lib.config.ConfigurationIoC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Configuration
@EnableAsync
@Import({ConfigurationIoC.class})
public class RemImageIoc {

    @Autowired
    private Environment env;

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
        return new DefaultImageCacheService(env.getProperty("image.cache.collection"));
    }

    @Bean
    public ImageRemUtil getImageRemUtil() {
        return new ImageRemUtil();
    }

    @Bean(name = RemImageIocNames.REM_GET)
    public Rem getImageGetRem(ImageOperationsService imageOperationsService, ImageCacheService imageCacheService) {
        return new ImageGetRem(imageOperationsService, imageCacheService, env.getProperty("imagemagick.conver.memoryLimit", "200MiB"));
    }

    @Bean(name = RemImageIocNames.REM_PUT)
    public Rem getImagePutRem(ImageRemUtil imageRemUtil) {
        return new ImagePutRem(env.getProperty("image.cache.collection", "image:ImageCache"), imageRemUtil);
    }

    @Bean(name = RemImageIocNames.REM_DELETE)
    public Rem getImageDeleteRem(ImageRemUtil imageRemUtil) {
        return new ImageDeleteRem(env.getProperty("image.cache.collection", "image:ImageCache"), imageRemUtil);
    }

}
