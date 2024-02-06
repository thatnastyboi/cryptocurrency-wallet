package bg.sofia.uni.fmi.mjt.wallet.crypto.exception;

public class FailedRequestException extends Exception {
    public FailedRequestException(String message) {
        super(message);
    }

    public FailedRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}