package model;

import chess.ChessGame;

import java.util.Objects;

public record GameData(
        int gameID,
        String whiteUsername,
        String blackUsername,
        String gameName,
        ChessGame game,
        GameStatus status
) {
    public enum GameStatus {
        STARTING, ENDED
    }

    public GameData {
        Objects.requireNonNull(gameName);
        Objects.requireNonNull(game);
    }
}
