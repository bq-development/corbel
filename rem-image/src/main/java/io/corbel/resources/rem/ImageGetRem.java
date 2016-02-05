package io.corbel.resources.rem;

import io.corbel.lib.ws.api.error.ErrorResponseFactory;
import io.corbel.lib.ws.model.Error;
import io.corbel.resources.rem.exception.ImageOperationsException;
import io.corbel.resources.rem.format.ImageFormat;
import io.corbel.resources.rem.model.ImageOperationDescription;
import io.corbel.resources.rem.plugin.RestorRemNames;
import io.corbel.resources.rem.request.RequestParameters;
import io.corbel.resources.rem.request.ResourceId;
import io.corbel.resources.rem.request.ResourceParameters;
import io.corbel.resources.rem.service.ImageCacheService;
import io.corbel.resources.rem.service.ImageOperationsService;
import io.corbel.resources.rem.service.RemService;
import org.apache.commons.io.output.TeeOutputStream;
import org.im4java.core.IM4JavaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class ImageGetRem extends ImageBaseRem {

    public static final String FORMAT_PARAMETER = "image:format";
    public static final String OPERATIONS_PARAMETER = "image:operations";
    public static final String IMAGE_WIDTH_PARAMETER = "image:width";
    public static final String IMAGE_HEIGHT_PARAMETER = "image:height";
    private static final Logger LOG = LoggerFactory.getLogger(ImageGetRem.class);
    private static final String TEMP_IMAGE_PREFIX = "temp_";

    private final ImageOperationsService imageOperationsService;
    private final ImageCacheService imageCacheService;
    private final String imMemoryLimit;


    public ImageGetRem(ImageOperationsService imageOperationsService, ImageCacheService imageCacheService, String memoryLimit) {
        this.imageOperationsService = imageOperationsService;
        this.imageCacheService = imageCacheService;
        this.imMemoryLimit = memoryLimit;
    }

    @Override
    public Response resource(String collection, ResourceId resourceId, RequestParameters<ResourceParameters> requestParameters,
                             Optional<InputStream> entity) {

        Rem<?> restorGetRem = remService.getRem(RestorRemNames.RESTOR_GET);

        if (restorGetRem == null) {
            LOG.warn("RESTOR not found. May be is needed to install it?");
            return ErrorResponseFactory.getInstance().notFound();
        }

        String operationsChain = getOperationsChain(requestParameters);
        List<ImageOperationDescription> operations;
        Optional<ImageFormat> imageFormat;
        try {
            operations = getParameters(operationsChain);
            imageFormat = getImageFormat(requestParameters);
        } catch (ImageOperationsException e) {
            return ErrorResponseFactory.getInstance().badRequest(new Error("bad_request", e.getMessage()));
        }

        if (operationsChain.isEmpty() && !imageFormat.isPresent()) {
            return restorGetRem.resource(collection, resourceId, requestParameters, Optional.empty());
        }

        MediaType mediaType = requestParameters.getAcceptedMediaTypes().get(0);

        InputStream inputStream = imageCacheService.getFromCache(restorGetRem, resourceId, operationsChain, imageFormat, collection,
                requestParameters);

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


        StreamingOutput outputStream = output -> {

            File file = File.createTempFile(TEMP_IMAGE_PREFIX + UUID.randomUUID().toString(), "");

            try (FileOutputStream fileOutputStream = new FileOutputStream(file);
                 TeeOutputStream teeOutputStream = new TeeOutputStream(output, fileOutputStream);
                 InputStream input = (InputStream) response.getEntity()) {
                imageOperationsService.applyConversion(operations, input, teeOutputStream, imageFormat, imMemoryLimit);
            } catch (IOException | InterruptedException | IM4JavaException | ImageOperationsException e) {
                LOG.error("Error working with image", e);
                throw new WebApplicationException(ErrorResponseFactory.getInstance().invalidEntity(e.getMessage()));
            }

            imageCacheService.saveInCacheAsync(restorPutRem, resourceId, operationsChain, imageFormat, file.length(), collection,
                    requestParameters, file);
        };

        return Response.ok(outputStream).type(javax.ws.rs.core.MediaType.valueOf(mediaType.toString())).build();
    }

    @Override
    public Class<InputStream> getType() {
        return InputStream.class;
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

    private Optional<ImageFormat> getImageFormat(RequestParameters<ResourceParameters> parameters) throws ImageOperationsException {
        String outputFormatString = parameters.getCustomParameterValue(FORMAT_PARAMETER);
        try {
            return Optional.ofNullable(ImageFormat.safeValueOf(outputFormatString));
        } catch (IllegalArgumentException i) {
            throw new ImageOperationsException("Invalid image output format: " + outputFormatString);
        }
    }

    protected List<ImageOperationDescription> getParameters(String parametersString) throws ImageOperationsException {
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
