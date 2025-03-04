package model;

import chess.ChessGame;

import java.util.Objects;

public record GameData(
        int gameID,
        String whiteUsername,
        String blackUsername,
        String gameName,
        ChessGame game
) {
    public GameData {
        Objects.requireNonNull(gameName);
        Objects.requireNonNull(game);
    }
}
