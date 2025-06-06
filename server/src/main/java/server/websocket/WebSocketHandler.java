package server.websocket;

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

        switch(command.getCommandType()) {
            case CONNECT -> System.out.println("CONNECT");
            case MAKE_MOVE -> System.out.println("MAKE_MOVE");
            case LEAVE -> System.out.println("LEAVE");
            case RESIGN -> System.out.println("RESIGN");
        }
    }

    private void sendError(Integer gameID, String username, String message) throws IOException {
        sessions.send(gameID, username, new ErrorMessage(ServerMessage.ServerMessageType.ERROR, message));
    }

    private void sendLoadGame(Integer gameID, String username, GameData gameData) throws IOException {
        sessions.send(gameID, username, new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData));
    }

    private void sendNotification(Integer gameID, String username, String message) throws IOException {
        sessions.send(
                gameID, username,
                new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message)
        );
    }

    private void broadcastNotification(Integer gameID, String exclusionUsername, String message) throws IOException {
        sessions.broadcast(
                gameID, exclusionUsername,
                new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message)
        );
    }

}
