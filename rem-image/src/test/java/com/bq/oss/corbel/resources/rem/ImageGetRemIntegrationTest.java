package com.bq.oss.corbel.resources.rem;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.bq.oss.corbel.resources.rem.request.RequestParameters;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.request.ResourceParameters;
import com.bq.oss.corbel.resources.rem.service.DefaultResizeImageService;
import com.bq.oss.corbel.resources.rem.service.ImageCacheService;
import com.bq.oss.corbel.resources.rem.service.RemService;

@Ignore // Needs imagemagick package installed
@RunWith(MockitoJUnitRunner.class)
public class ImageGetRemIntegrationTest {

    private static final String COLLECTION_TEST = "test:Test";
    private static final ResourceId RESOURCE_ID = new ResourceId("resourceId");
    @Mock private RemService remService;
    @Mock private RequestParameters<ResourceParameters> parameters;
    @Mock private Rem restorRem;
    @Mock private ImageCacheService imageCacheService;

    private ImageGetRem imageGetRem;

    @Before
    public void before() throws IOException {
        imageGetRem = new ImageGetRem(new DefaultResizeImageService(), imageCacheService);
        imageGetRem.setRemService(remService);

        List<MediaType> mediaTypes = Arrays.asList(MediaType.IMAGE_JPEG);
        when(parameters.getAcceptedMediaTypes()).thenReturn(mediaTypes);
        when(remService.getRem(COLLECTION_TEST, mediaTypes, HttpMethod.GET, Arrays.asList(imageGetRem))).thenReturn(restorRem);

        InputStream entity = Thread.currentThread().getContextClassLoader().getResourceAsStream("codereview.jpg");

        when(restorRem.resource(COLLECTION_TEST, RESOURCE_ID, parameters, Optional.empty())).thenReturn(Response.ok(entity).build());
    }

    @Test
    public void resourceTest() throws IOException {
        when(parameters.getCustomParameterValue("image:width")).thenReturn("250");
        Response response = imageGetRem.resource(COLLECTION_TEST, RESOURCE_ID, parameters, Optional.empty());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ((StreamingOutput) response.getEntity()).write(byteArrayOutputStream);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

        assertThat(image.getWidth()).isEqualTo(250);
    }
}
