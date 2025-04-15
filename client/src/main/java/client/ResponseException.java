package client;

public class ResponseException extends RuntimeException {
    public ResponseException(String message) {
        super(String.format("Error: %s", message));
    }
}
