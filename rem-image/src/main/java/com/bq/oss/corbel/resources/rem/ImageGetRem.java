package com.bq.oss.corbel.resources.rem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.output.TeeOutputStream;
import org.im4java.core.IM4JavaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;
import com.bq.oss.corbel.resources.rem.model.ImageOperationDescription;
import com.bq.oss.corbel.resources.rem.request.RequestParameters;
import com.bq.oss.corbel.resources.rem.request.ResourceId;
import com.bq.oss.corbel.resources.rem.request.ResourceParameters;
import com.bq.oss.corbel.resources.rem.service.ImageCacheService;
import com.bq.oss.corbel.resources.rem.service.ImageOperationsService;
import com.bq.oss.corbel.resources.rem.service.RemService;
import com.bq.oss.lib.ws.api.error.ErrorResponseFactory;
import com.bq.oss.lib.ws.model.Error;


public class ImageGetRem extends BaseRem<Void> {

    public static final String OPERATIONS_PARAMETER = "image:operations";
    public static final String IMAGE_WIDTH_PARAMETER = "image:width";
    public static final String IMAGE_HEIGHT_PARAMETER = "image:height";
    private static final Logger LOG = LoggerFactory.getLogger(ImageGetRem.class);
    private static final String TEMP_IMAGE_NAME = "temp_";
    private static long MAX_TEMP_IMAGE_ID = 100000000000l;

    private final ImageOperationsService imageOperationsService;
    private final ImageCacheService imageCacheService;
    private final AtomicLong tempIdCounter;
    private RemService remService;

    public ImageGetRem(ImageOperationsService imageOperationsService, ImageCacheService imageCacheService) {
        this.imageOperationsService = imageOperationsService;
        this.imageCacheService = imageCacheService;
        this.tempIdCounter = new AtomicLong(0l);
    }

    @Override
    public Response resource(String collection, ResourceId resourceId, RequestParameters<ResourceParameters> requestParameters,
            Optional<Void> entity) {

        Rem<?> restorGetRem = remService.getRem("RestorGetRem");

        if (restorGetRem == null) {
            LOG.warn("RESTOR not found. May be is needed to install it?");
            return ErrorResponseFactory.getInstance().notFound();
        }

        String operationsChain = getOperationsChain(requestParameters);

        if (operationsChain.isEmpty()) {
            return restorGetRem.resource(collection, resourceId, requestParameters, Optional.empty());
        }

        MediaType mediaType = requestParameters.getAcceptedMediaTypes().get(0);

        InputStream inputStream = imageCacheService.getFromCache(restorGetRem, resourceId, operationsChain, collection, requestParameters);

        if (inputStream != null) {
            return Response.ok(inputStream).type(javax.ws.rs.core.MediaType.valueOf(mediaType.toString())).build();
        }

        Response response = restorGetRem.resource(collection, resourceId, requestParameters, Optional.empty());

        @SuppressWarnings("unchecked")
        Rem<InputStream> restorPutRem = (Rem<InputStream>) remService.getRem(collection, requestParameters.getAcceptedMediaTypes(),
                HttpMethod.PUT);

        if (response.getStatus() != 200) {
            return response;
        }

        List<ImageOperationDescription> operations;

        try {
            operations = getParameters(operationsChain);
        } catch (ImageOperationsException e) {
            return ErrorResponseFactory.getInstance().badRequest(new Error("bad_request", e.getMessage()));
        }

        final long uniqueCounter = tempIdCounter.getAndIncrement();

        if (uniqueCounter >= MAX_TEMP_IMAGE_ID) {
            tempIdCounter.set(0);
        }

        StreamingOutput outputStream = output -> {

            File file = File.createTempFile(TEMP_IMAGE_NAME + uniqueCounter, "");

            try (FileOutputStream fileOutputStream = new FileOutputStream(file);
                    TeeOutputStream teeOutputStream = new TeeOutputStream(output, fileOutputStream);
                    InputStream input = (InputStream) response.getEntity()) {
                imageOperationsService.applyConversion(operations, input, teeOutputStream);
            } catch (IOException | InterruptedException | IM4JavaException | ImageOperationsException e) {
                LOG.error("Error working with image", e);
                throw new WebApplicationException(ErrorResponseFactory.getInstance().invalidEntity(e.getMessage()));
            }

            imageCacheService.saveInCacheAsync(restorPutRem, resourceId, operationsChain, file.length(), collection, requestParameters,
                    file);
        };

        return Response.ok(outputStream).type(javax.ws.rs.core.MediaType.valueOf(mediaType.toString())).build();
    }

    @Override
    public Class<Void> getType() {
        return Void.class;
    }

    public void setRemService(RemService remService) {
        this.remService = remService;
    }

    private String getOperationsChain(RequestParameters<ResourceParameters> parameters) {
        String operationsChain = "";

        String imageWidth = parameters.getCustomParameterValue(IMAGE_WIDTH_PARAMETER);
        String imageHeight = parameters.getCustomParameterValue(IMAGE_HEIGHT_PARAMETER);

        if (imageWidth != null) {
            if (imageHeight != null) {
                operationsChain += "resize=(" + imageWidth + ", " + imageHeight + ")";
            } else {
                operationsChain += "resizeWidth=" + imageWidth;
            }
        } else if (imageHeight != null) {
            operationsChain += "resizeHeight=" + imageHeight;
        }

        String operationsParameters = parameters.getCustomParameterValue(OPERATIONS_PARAMETER);

        if (operationsParameters != null) {
            operationsChain += (operationsChain.isEmpty()) ? operationsParameters : ";" + operationsParameters;
        }

        return operationsChain;
    }

    public List<ImageOperationDescription> getParameters(String parametersString) throws ImageOperationsException {
        List<ImageOperationDescription> parameters = new LinkedList<>();

        for (String rawParameter : parametersString.split(";")) {
            if (rawParameter.trim().isEmpty()) {
                continue;
            }

            String splittedParameter[] = rawParameter.split("=");

            if (splittedParameter.length != 2) {
                throw new ImageOperationsException("Invalid image operation: " + rawParameter);
            }

            parameters.add(new ImageOperationDescription(splittedParameter[0].trim(), splittedParameter[1].trim()));
        }

        return parameters;
    }
}
