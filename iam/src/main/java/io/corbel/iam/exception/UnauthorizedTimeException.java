package io.corbel.iam.exception;

public class UnauthorizedTimeException extends UnauthorizedException {

    private static final long serialVersionUID = -822589169427171311L;

    public UnauthorizedTimeException(String originalExceptionMessage) {
        super("Invalid provided time, please check your system\'s clock. \nOriginal message raised: " + originalExceptionMessage);
    }
}
