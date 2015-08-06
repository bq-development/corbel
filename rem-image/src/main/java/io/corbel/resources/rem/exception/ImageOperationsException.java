package io.corbel.resources.rem.exception;

public class ImageOperationsException extends Exception {
    public ImageOperationsException(String message) {
        super(message);
    }

    public ImageOperationsException(String message, Throwable cause) {
        super(message, cause);
    }
}
