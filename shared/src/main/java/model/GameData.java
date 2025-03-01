package model;

import chess.ChessGame;

public record GameData(
        int gameID,
        String whiteUserID,
        String blackUserID,
        String gameName,
        ChessGame game
) {}
