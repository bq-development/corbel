package com.bq.oss.corbel.resources.rem.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.Pipe;

import com.bq.oss.corbel.resources.rem.exception.ImageOperationsException;
import com.bq.oss.corbel.resources.rem.model.ImageOperationDescription;
import com.bq.oss.corbel.resources.rem.operation.ImageOperation;

public class DefaultImageOperationsService implements ImageOperationsService {

    private final IMOperationFactory imOperationFactory;
    private final ConvertCmdFactory convertCmdFactory;
    private final Map<String, ImageOperation> operations;

    public DefaultImageOperationsService(IMOperationFactory imOperationFactory, ConvertCmdFactory convertCmdFactory,
            Map<String, ImageOperation> operations) {
        this.imOperationFactory = imOperationFactory;
        this.convertCmdFactory = convertCmdFactory;
        this.operations = operations;
    }

    @Override
    public void applyConversion(List<ImageOperationDescription> parameters, InputStream image, OutputStream out)
            throws ImageOperationsException,
            InterruptedException, IOException, IM4JavaException {

        IMOperation imOperation = imOperationFactory.create();
        imOperation.addImage("-");

        for (ImageOperationDescription parameter : parameters) {

            String operationName = parameter.getName();
            ImageOperation currentOperation = operations.get(operationName);

            if (currentOperation == null) {
                throw new ImageOperationsException("Unknown operation: " + operationName);
            }

            imOperation.addSubOperation(currentOperation.apply(parameter.getParameters()));

        }

        imOperation.addImage("-");

        ConvertCmd convertCmd = convertCmdFactory.create(image, out);
        convertCmd.run(imOperation);
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
