package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand {
    ChessMove move;

    public MakeMoveCommand(CommandType commandType, String authToken, Integer gameID, ChessMove chessMove) {
        super(commandType, authToken, gameID);
        this.move = chessMove;
    }

    public ChessMove getChessMove() {
        return move;
    }
}
