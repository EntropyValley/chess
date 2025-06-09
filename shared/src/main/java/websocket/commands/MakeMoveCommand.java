package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand {
    ChessMove chessMove;

    public MakeMoveCommand(CommandType commandType, String authToken, Integer gameID, ChessMove chessMove) {
        super(commandType, authToken, gameID);
        this.chessMove = chessMove;
    }

    public ChessMove getChessMove() {
        return chessMove;
    }
}
