package com.bq.oss.corbel.resources.rem.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.Pipe;

public class DefaultResizeImageService implements ResizeImageService {

    @Override
    public void resizeImage(InputStream image, Integer width, Integer height, OutputStream out) throws IOException, InterruptedException,
            IM4JavaException {
        ConvertCmd cmd = createConvertCmd();

        Pipe pipeIn = new Pipe(image, null);
        cmd.setInputProvider(pipeIn);

        Pipe pipeOut = new Pipe(null, out);
        cmd.setOutputConsumer(pipeOut);

        String flag = "";

        if (width != null && height != null) {
            flag = "!";
        }

        IMOperation operation = new IMOperation();
        operation.addImage("-");
        operation.resize(width, height, flag);
        operation.addImage("-");
        cmd.run(operation);
    }

    public ConvertCmd createConvertCmd() {
        return new ConvertCmd();
    }

}
