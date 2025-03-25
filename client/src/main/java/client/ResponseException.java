package client;

public class ResponseException extends RuntimeException {
    private final int code;

    public ResponseException(int code, String message) {
        super(String.format("Error [%d]: %s", code, message));
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
