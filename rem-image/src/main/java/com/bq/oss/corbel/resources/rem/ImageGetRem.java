package com.bq.oss.corbel.resources.rem;

import java.io.*;
import java.util.Arrays;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.output.TeeOutputStream;
import org.im4java.core.IM4JavaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.bq.oss.corbel.resources.rem.request.RequestParameters;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.request.ResourceParameters;
import com.bq.oss.corbel.resources.rem.service.ImageCacheService;
import com.bq.oss.corbel.resources.rem.service.RemService;
import com.bq.oss.corbel.resources.rem.service.ResizeImageService;
import com.bq.oss.lib.ws.api.error.ErrorResponseFactory;
import com.bq.oss.lib.ws.model.Error;


public class ImageGetRem extends BaseRem<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(ImageGetRem.class);
    private static final String RESIZE_IMAGE_NAME = "resizeImage";

    private final ResizeImageService resizeImageService;
    private final ImageCacheService imageCacheService;
    private RemService remService;

    public ImageGetRem(ResizeImageService resizeImageService, ImageCacheService imageCacheService) {
        this.resizeImageService = resizeImageService;
        this.imageCacheService = imageCacheService;
    }

    @Override
    public Response resource(String collection, ResourceId resourceId, RequestParameters<ResourceParameters> parameters,
            Optional<Void> entity) {
        Rem<?> restorGetRem = remService.getRem(collection, parameters.getAcceptedMediaTypes(), HttpMethod.GET, Arrays.<Rem>asList(this));

        if (restorGetRem != null) {

            Integer width = extractParam("image:width", parameters);
            Integer height = extractParam("image:height", parameters);
            MediaType mediaType = parameters.getAcceptedMediaTypes().get(0);

            InputStream inputStream = imageCacheService.getFromCache(restorGetRem, resourceId, width, height, collection, parameters);

            Response response = null;

            if (inputStream != null) {
                response = Response.ok(inputStream).type(javax.ws.rs.core.MediaType.valueOf(mediaType.toString())).build();
            } else {

                Response restorResponse = restorGetRem.resource(collection, resourceId, parameters, Optional.empty());
                response = restorResponse;
                Rem<InputStream> restorPutRem = (Rem<InputStream>) remService.getRem(collection, parameters.getAcceptedMediaTypes(),
                        HttpMethod.PUT);

                if ((response.getStatus() == 200) && (width != null || height != null)) {

                    StreamingOutput outputStream = output -> {

                        File file = File.createTempFile(RESIZE_IMAGE_NAME, "");

                        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
                                TeeOutputStream teeOutputStream = new TeeOutputStream(output, fileOutputStream);
                                InputStream input = (InputStream) restorResponse.getEntity()) {
                            resizeImageService.resizeImage(input, width, height, teeOutputStream);
                        } catch (IOException | InterruptedException | IM4JavaException e) {
                            LOG.error("Error while resizing a image", e);
                            throw new WebApplicationException(ErrorResponseFactory.getInstance().serverError(e));
                        }

                        imageCacheService.saveInCacheAsync(restorPutRem, resourceId, width, height, file.length(), collection,
                                parameters, file);
                    };

                    response = Response.ok(outputStream).type(javax.ws.rs.core.MediaType.valueOf(mediaType.toString())).build();
                }
            }

            return response;
        }

        LOG.warn("RESTOR not found. May be is needed to install it?");
        return ErrorResponseFactory.getInstance().notFound();

    }

    @Override
    public Class<Void> getType() {
        return Void.class;
    }

    private Integer extractParam(String paramName, RequestParameters<?> parameters) {
        Integer result = null;
        String paramString = parameters.getCustomParameterValue(paramName);
        if (paramString != null) {
            try {
                result = Integer.valueOf(paramString);
                if (result <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                throw new WebApplicationException(ErrorResponseFactory.getInstance().badRequest(
                        new Error("bad_request", paramName + " has an invalid value (integer in (0,MAX_INT]")));
            }

        }
        return result;
    }

    public void setRemService(RemService remService) {
        this.remService = remService;
    }

}
