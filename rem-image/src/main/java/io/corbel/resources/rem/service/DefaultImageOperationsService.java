package io.corbel.resources.rem.service;

import io.corbel.resources.rem.exception.ImageOperationsException;
import io.corbel.resources.rem.format.ImageFormat;
import io.corbel.resources.rem.model.ImageOperationDescription;
import io.corbel.resources.rem.operation.ImageOperation;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.Pipe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DefaultImageOperationsService implements ImageOperationsService {

    private final IMOperationFactory imOperationFactory;
    private final ConvertCmdFactory convertCmdFactory;
    private final Map<String, ImageOperation> operations;
    public static final String DEF_IMAGE_ARG = "-";

    public DefaultImageOperationsService(IMOperationFactory imOperationFactory, ConvertCmdFactory convertCmdFactory,
                                         Map<String, ImageOperation> operations) {
        this.imOperationFactory = imOperationFactory;
        this.convertCmdFactory = convertCmdFactory;
        this.operations = operations;
    }

    @Override
    public void applyConversion(List<ImageOperationDescription> parameters, InputStream image, OutputStream out, Optional<ImageFormat> format, String imMemoryLimit) throws ImageOperationsException, InterruptedException, IOException, IM4JavaException {
        IMOperation imOperation = imOperationFactory.create();
        addDefaultImageToIMOperation(imOperation, imMemoryLimit);
        addOperations(imOperation, parameters);
        addImageToIMOperation(imOperation, getOutputFormatParameter(format));

        ConvertCmd convertCmd = convertCmdFactory.create(image, out);
        convertCmd.run(imOperation);
    }

    private void addOperations(IMOperation imOperation, List<ImageOperationDescription> parameters) throws ImageOperationsException {
        for (ImageOperationDescription parameter : parameters) {

            String operationName = parameter.getName();
            ImageOperation currentOperation = operations.get(operationName);

            if (currentOperation == null) {
                throw new ImageOperationsException("Unknown operation: " + operationName);
            }

            imOperation.addSubOperation(currentOperation.apply(parameter.getParameters()));
        }
    }

    private void addDefaultImageToIMOperation(IMOperation imOperation, String imMemoryLimit) {
        imOperation.addRawArgs("-limit", "memory", imMemoryLimit);
        imOperation.addRawArgs("-limit", "map", imMemoryLimit);
        addImageToIMOperation(imOperation, DEF_IMAGE_ARG);
    }

    private void addImageToIMOperation(IMOperation imOperation, String extension) {
        imOperation.addImage(extension);
    }

    private String getOutputFormatParameter(Optional<ImageFormat> format) {
        return format.map(formatIn -> formatIn + ":-").orElse(DEF_IMAGE_ARG);
    }

    public static class IMOperationFactory {
        public IMOperation create() {
            return new IMOperation();
        }
    }

    public static class ConvertCmdFactory {
        public ConvertCmd create(InputStream in, OutputStream out) {
            ConvertCmd convertCmd = new ConvertCmd();
            convertCmd.setInputProvider(new Pipe(in, null));
            convertCmd.setOutputConsumer(new Pipe(null, out));
            return convertCmd;
        }
    }
}
