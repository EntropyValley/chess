package websocket;

import chess.ChessMove;
import client.ResponseException;
import model.AuthData;
import websocket.commands.*;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import com.google.gson.Gson;

public class WebSocketFacade extends Endpoint {
    Session session;

    public WebSocketFacade(String url) throws ResponseException {
        try {
            String wsURL = url.replace("http", "ws") + "/ws";
            URI wsURI = new URI(wsURL);

            WebSocketContainer wsContainer = ContainerProvider.getWebSocketContainer();
            session = wsContainer.connectToServer(this, wsURI);

            session.addMessageHandler((MessageHandler.Whole<String>) msg -> {
                ServerMessage serverMessage = new Gson().fromJson(msg, ServerMessage.class);

                switch(serverMessage.getServerMessageType()) {
                    case NOTIFICATION -> System.out.println("NOTIFICATION");
                    case ERROR -> System.out.println("ERROR");
                    case LOAD_GAME -> System.out.println("LOAD");
                    case null -> System.out.println("Invalid Server Message received...");
                }
            });
        } catch (URISyntaxException exception) {
            System.out.println("Invalid Server URI, please reconfigure...");
        } catch (DeploymentException exception) {
            System.out.println("Error connecting to websocket...");
        } catch (IOException exception) {
            System.out.println("Error communicating with websocket server...");
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig configuration) {
    }

    public void sendCommand(UserGameCommand command)  {
        try {
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException exception) {
            System.out.println("Unable to send command to server...");
        }
    }

    public void connect(AuthData authData, Integer gameID) {
        sendCommand(
            new UserGameCommand(UserGameCommand.CommandType.CONNECT, authData.authToken(), gameID)
        );
    }

    public void makeMove(AuthData authData, Integer gameID, ChessMove chessMove) {
        sendCommand(
            new MakeMoveCommand(UserGameCommand.CommandType.MAKE_MOVE, authData.authToken(), gameID, chessMove)
        );
    }

    public void resign(AuthData authData, Integer gameID) {
        sendCommand(
            new UserGameCommand(UserGameCommand.CommandType.RESIGN, authData.authToken(), gameID)
        );
    }

    public void leave(AuthData authData, Integer gameID) {
        sendCommand(
            new UserGameCommand(UserGameCommand.CommandType.LEAVE, authData.authToken(), gameID)
        );
    }
}
