package model;

import java.util.Objects;

public record AuthData(
        String username,
        String authToken
) {
    public AuthData {
        Objects.requireNonNull(username);
        Objects.requireNonNull(authToken);
    }
}
