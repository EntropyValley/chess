package server.websocket;

import exceptions.GameNotFoundException;
import exceptions.UnauthorizedException;
import chess.InvalidMoveException;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.Session;
import com.google.gson.Gson;
import java.io.IOException;

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
                case LEAVE -> System.out.println("LEAVE");
                case RESIGN -> System.out.println("RESIGN");
                case null -> System.out.println("Empty Command");
            }
        } catch (IOException exception) {
            System.out.println("Error: " + exception.getMessage());
        }
    }

    private void sendError(Integer gameID, String username, String message) throws IOException {
        sessions.send(gameID, username, new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Error: " + message));
    }

    private void sendLoadGame(Integer gameID, GameData gameData) throws IOException {
        ServerMessage load = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData);
        sessions.broadcastAll(gameID, load);
    }

    private void sendNotification(Integer gameID, String username, String message) throws IOException {
        sessions.send(
                gameID, username,
                new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message)
        );
    }

    private void broadcastNotification(Integer gameID, String exclusionUsername, String message) throws IOException {
        sessions.broadcastExcept(
                gameID, exclusionUsername,
                new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message)
        );
    }

    private ChessGame.TeamColor teamColorFromGame(GameData gameData, String username) {
        return gameData.blackUsername().equals(username) ?
                    ChessGame.TeamColor.BLACK :
                gameData.whiteUsername().equals(username) ?
                    ChessGame.TeamColor.WHITE :
                    null;
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
                "User " + validation.username() + "joined as an observer"
            );
        } else {
            broadcastNotification(
                connectCommand.getGameID(), validation.username(),
                "Player " + validation.username() + " joined as " + color
            );
        }

        sendLoadGame(connectCommand.getGameID(), validation.gameData());
    }

    private void onMakeMove(Session session, MakeMoveCommand makeMoveCommand) throws IOException {
        Validation validation = validateCommand(session, makeMoveCommand);
        if (!validation.isValid()) {
            return;
        }

        ChessGame.TeamColor playerColor = teamColorFromGame(validation.gameData(), validation.username());
        ChessPosition startingPos = makeMoveCommand.getChessMove().getStartPosition();
        ChessPosition endingPos = makeMoveCommand.getChessMove().getEndPosition();
        ChessPiece movedPiece = validation.gameData().game().getBoard().getPiece(startingPos);
        ChessGame.TeamColor pieceColor = movedPiece.getTeamColor();

        if (playerColor != pieceColor) {
            sendError(
                makeMoveCommand.getGameID(),
                validation.username(),
                "Cannot move a piece that does not belong to you"
            );
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
            // TODO: go back and build a function in the game service to update the game
        } catch (Exception exception) {
            sendError(makeMoveCommand.getGameID(), validation.username(), "Unable to Update Game");
        }

        sendLoadGame(
            makeMoveCommand.getGameID(),
            newGameData
        );

        broadcastNotification(
            makeMoveCommand.getGameID(),
            validation.username(),
            validation.username() + " has moved " + movedPiece.getPieceType() + " from " +
                startingPos.getRow() + "-" + startingPos.getColumn() + " to " +
                endingPos.getRow()  + "-" + endingPos.getColumn() + "!"
        );

        // TODO: Check for game completion
    }
}

// TODO: Go back and update SQL calls for recently added Game Status
