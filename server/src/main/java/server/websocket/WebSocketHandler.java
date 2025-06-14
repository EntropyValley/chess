package server.websocket;

import dataaccess.DataAccessException;
import exceptions.GameNotFoundException;
import exceptions.UnauthorizedException;
import chess.InvalidMoveException;

import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.api.Session;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Arrays;

import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.messages.*;

import service.GameService;
import service.UserService;

import model.GameData;
import chess.*;

@WebSocket
public class WebSocketHandler {
    private final SessionManager sessions = new SessionManager();
    private final GameService gameService;
    private final UserService userService;

    public WebSocketHandler(GameService gameService, UserService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);

        try {
            switch (command.getCommandType()) {
                case CONNECT -> onConnect(session, command);
                case MAKE_MOVE -> onMakeMove(session, new Gson().fromJson(message, MakeMoveCommand.class));
                case LEAVE -> onLeave(session, command);
                case RESIGN -> onResign(session, command);
                case null -> System.out.println("Invalid Command");
            }
        } catch (IOException exception) {
            System.out.println("Error: " + exception.getMessage());
        }
    }

    @OnWebSocketError
    public void onError(Session session, Throwable exception) {
        System.out.println(exception.toString());
        System.out.println(Arrays.toString(exception.getStackTrace()));
    }

    private void sendError(Integer gameID, String username, String message) throws IOException {
        sessions.send(gameID, username, new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Error: " + message));
    }

    private void sendLoadGame(Integer gameID, String username, GameData gameData) throws IOException {
        ServerMessage load = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData);
        sessions.send(gameID, username, load);
    }

    private void broadcastLoadGame(Integer gameID, GameData gameData) throws IOException {
        ServerMessage load = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData);
        sessions.broadcastAll(gameID, load);
    }

    private void broadcastNotification(Integer gameID, String exclusionUsername, String message) throws IOException {
        sessions.broadcastExcept(
            gameID, exclusionUsername,
            new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message)
        );
    }

    private void notifyAll(Integer gameID, String message) throws IOException {
        sessions.broadcastAll(
            gameID,
            new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message)
        );
    }

    private ChessGame.TeamColor teamColorFromGame(GameData gameData, String username) {
        if (gameData.blackUsername() != null && gameData.blackUsername().equals(username)) {
            return ChessGame.TeamColor.BLACK;
        } else if (gameData.whiteUsername() != null && gameData.whiteUsername().equals(username)) {
            return ChessGame.TeamColor.WHITE;
        } else {
            return null;
        }
    }

    private record Validation(String username, GameData gameData, boolean isValid) {}

    private Validation validateCommand(Session session, UserGameCommand command) throws IOException {
        String username;

        try {
            username = userService.getUsernameFromToken(command.getAuthToken());
        } catch (UnauthorizedException exception) {
            session.getRemote().sendString(
                new Gson().toJson(
                    new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "ERROR: Unauthorized")
                )
            );
            return new Validation(null, null, false);
        }

        GameData gameData;

        try {
            gameData = gameService.getGame(command.getGameID());
        } catch (GameNotFoundException exception) {
            session.getRemote().sendString(
                new Gson().toJson(
                    new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "ERROR: Game Not Found")
                )
            );
            return new Validation(username, null, false);
        }

        return new Validation(username, gameData, true);
    }

    private void handleEndOfGame(UserGameCommand command, GameData gameToEnd, ChessGame.TeamColor resigning) {
        try {
            gameService.updateGame(
                command.getAuthToken(),
                new GameData(
                    gameToEnd.gameID(),
                    resigning == ChessGame.TeamColor.WHITE ? null : gameToEnd.whiteUsername(),
                    resigning == ChessGame.TeamColor.BLACK ? null : gameToEnd.blackUsername(),
                    gameToEnd.gameName(),
                    gameToEnd.game(),
                    GameData.GameStatus.ENDED
                )
            );
        } catch (DataAccessException exception) {
            System.out.println("Unable to update Game Status");
        }
    }

    private void detectEndGameConditions(MakeMoveCommand makeMoveCommand, ChessGame newGame, Validation validation, GameData newGameData) throws IOException {
        String looserUsername = null;
        String winnerUsername = null;
        if (newGame.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            looserUsername = validation.gameData().blackUsername();
            winnerUsername = validation.gameData().whiteUsername();
        } else if (newGame.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            looserUsername = validation.gameData().whiteUsername();
            winnerUsername = validation.gameData().blackUsername();
        }

        if (looserUsername != null || winnerUsername != null) {
            notifyAll(
                makeMoveCommand.getGameID(),
                looserUsername + " is in checkmate! " + winnerUsername + " has won!"
            );
            handleEndOfGame(makeMoveCommand, newGameData, null);
            return;
        }

        String stalemateUsername = null;
        if (newGame.isInStalemate(ChessGame.TeamColor.BLACK)) {
            stalemateUsername = validation.gameData().blackUsername();
        } else if (newGame.isInStalemate(ChessGame.TeamColor.WHITE)) {
            stalemateUsername = validation.gameData().whiteUsername();
        }

        if (stalemateUsername != null) {
            notifyAll(
                makeMoveCommand.getGameID(),
                stalemateUsername + " is in stalemate! Game Over!"
            );
            handleEndOfGame(makeMoveCommand, newGameData, null);
            return;
        }

        String checkedUsername = null;
        if (newGame.isInCheck(ChessGame.TeamColor.BLACK)) {
            checkedUsername = validation.gameData().blackUsername();
        } else if (newGame.isInCheck(ChessGame.TeamColor.WHITE)) {
            checkedUsername = validation.gameData().whiteUsername();
        }

        if (checkedUsername != null) {
            notifyAll(
                makeMoveCommand.getGameID(),
                checkedUsername + " is in check!"
            );
        }
    }

    private char charFromRow(int row) {
        return (char) ('a' + row);
    }

    private void onConnect(Session session, UserGameCommand connectCommand) throws IOException {
        Validation validation = validateCommand(session, connectCommand);
        if (!validation.isValid()) {
            return;
        }

        sessions.add(connectCommand.getGameID(), validation.username(), session);

        ChessGame.TeamColor color = teamColorFromGame(validation.gameData(), validation.username());

        if (color == null) {
            broadcastNotification(
                connectCommand.getGameID(), validation.username(),
                "User " + validation.username() + " joined as an observer"
            );
        } else {
            broadcastNotification(
                connectCommand.getGameID(), validation.username(),
                "Player " + validation.username() + " joined as " + color
            );
        }

        sendLoadGame(connectCommand.getGameID(), validation.username(), validation.gameData());
    }

    private void onMakeMove(Session session, MakeMoveCommand makeMoveCommand) throws IOException {
        System.out.println(makeMoveCommand.getChessMove());

        Validation validation = validateCommand(session, makeMoveCommand);
        if (!validation.isValid()) {
            return;
        }

        System.out.println(validation.gameData().game().getBoard().getPiece(new ChessPosition(2,4)));

        ChessGame.TeamColor playerColor = teamColorFromGame(validation.gameData(), validation.username());

        if (validation.gameData().game().getTeamTurn() != playerColor) {
            sendError(makeMoveCommand.getGameID(), validation.username(), "It is not currently your turn to move...");
            return;
        }

        ChessPosition startingPos = makeMoveCommand.getChessMove().getStartPosition();
        ChessPosition endingPos = makeMoveCommand.getChessMove().getEndPosition();
        ChessPiece movedPiece = validation.gameData().game().getBoard().getPiece(startingPos);
        ChessGame.TeamColor pieceColor = movedPiece != null ? movedPiece.getTeamColor() : null ;

        if (playerColor != pieceColor) {
            sendError(
                makeMoveCommand.getGameID(),
                validation.username(),
                "Cannot move a piece that does not belong to you"
            );
            return;
        }

        if (validation.gameData().status() == GameData.GameStatus.ENDED) {
            sendError(
                    makeMoveCommand.getGameID(), validation.username(),
                    "Game has already ended. You can no longer move pieces"
            );
            return;
        }

        ChessGame newGame = validation.gameData().game();
        try {
            newGame.makeMove(makeMoveCommand.getChessMove());
        } catch (InvalidMoveException exception) {
            sendError(makeMoveCommand.getGameID(), validation.username(), "Invalid Move");
            return;
        }

        GameData newGameData = new GameData(
            validation.gameData().gameID(),
            validation.gameData().whiteUsername(),
            validation.gameData().blackUsername(),
            validation.gameData().gameName(),
            newGame,
            validation.gameData().status()
        );

        try {
            gameService.updateGame(makeMoveCommand.getAuthToken(), newGameData);
        } catch (Exception exception) {
            sendError(makeMoveCommand.getGameID(), validation.username(), "Unable to Update Game...");
            return;
        }

        broadcastLoadGame(
            makeMoveCommand.getGameID(),
            newGameData
        );

        broadcastNotification(
            makeMoveCommand.getGameID(),
            validation.username(),
            validation.username() + " has moved " + movedPiece.getPieceType() + " from " +
                charFromRow(startingPos.getRow()) + startingPos.getColumn() + " to " +
                charFromRow(endingPos.getRow())  + endingPos.getColumn() + "!"
        );

        detectEndGameConditions(makeMoveCommand, newGame, validation, newGameData);
    }

    private void onLeave(Session session, UserGameCommand leaveCommand) throws IOException {
        Validation validation = validateCommand(session, leaveCommand);
        if (!validation.isValid()) {
            return;
        }

        ChessGame.TeamColor playerColor = teamColorFromGame(validation.gameData(), validation.username());
        if (playerColor != null) {
            try {
                gameService.updateGame(leaveCommand.getAuthToken(), new GameData(
                    validation.gameData().gameID(),
                    playerColor == ChessGame.TeamColor.WHITE ? null : validation.gameData().whiteUsername(),
                    playerColor == ChessGame.TeamColor.BLACK ? null : validation.gameData().blackUsername(),
                    validation.gameData().gameName(),
                    validation.gameData().game(),
                    validation.gameData().status()
                ));
            } catch (DataAccessException exception) {
                System.out.println("Unable to update game...");
            }
            broadcastNotification(
                leaveCommand.getGameID(), validation.username(),
                validation.username() + " has left the game..."
            );
        } else {
            broadcastNotification(
                leaveCommand.getGameID(), validation.username(),
                validation.username() + " is no longer observing the game..."
            );
        }

        sessions.remove(leaveCommand.getGameID(), validation.username());
    }

    private void onResign(Session session, UserGameCommand command) throws IOException {
        Validation validation = validateCommand(session, command);
        if (!validation.isValid()) {
            return;
        }

        ChessGame.TeamColor playerColor = teamColorFromGame(validation.gameData(), validation.username());
        if (playerColor == null) {
            sendError(
                command.getGameID(),
                validation.username(),
                validation.username() + " is not playing this game – cannot resign."
            );
            return;
        }

        if (validation.gameData().status() == GameData.GameStatus.ENDED) {
            sendError(
                command.getGameID(),
                validation.username(),
                "Game is over – cannot resign."
            );
            return;
        }

        notifyAll(command.getGameID(), validation.username() + " has resigned this game. Game Over!");
        handleEndOfGame(command, validation.gameData(), playerColor);
    }
}