package server.websocket;

import exceptions.GameNotFoundException;
import exceptions.UnauthorizedException;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.Session;
import com.google.gson.Gson;
import java.io.IOException;

import websocket.commands.UserGameCommand;
import service.GameService;
import service.UserService;
import websocket.messages.*;
import model.GameData;
import chess.ChessGame;

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
                case MAKE_MOVE -> System.out.println("MAKE_MOVE");
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

    private void onConnect(Session session, UserGameCommand connectCommand) throws IOException {
        String username;

        try {
            username = userService.getUsernameFromToken(connectCommand.getAuthToken());
        } catch (UnauthorizedException exception) {
            session.getRemote().sendString(
                new Gson().toJson(
                    new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "ERROR: Unauthorized")
                )
            );
            return;
        }

        GameData game;

        try {
            game = gameService.getGame(connectCommand.getGameID());
        } catch (GameNotFoundException exception) {
            session.getRemote().sendString(
              new Gson().toJson(
                  new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "ERROR: Game Not Found")
              )
            );
            return;
        }

        sessions.add(connectCommand.getGameID(), username, session);

        ChessGame.TeamColor color = teamColorFromGame(game, username);

        if (color == null) {
            broadcastNotification(
                connectCommand.getGameID(), username,
                "User " + username + "joined as an observer"
            );
        } else {
            broadcastNotification(
                connectCommand.getGameID(), username,
                "Player " + username + " joined as " + color
            );
        }

        sendLoadGame(connectCommand.getGameID(), game);
    }
}
