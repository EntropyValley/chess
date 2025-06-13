package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import client.ResponseException;
import model.*;
import ui.ClientUtils;
import websocket.commands.*;
import websocket.messages.*;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import com.google.gson.Gson;

public class WebSocketFacade extends Endpoint {
    Session session;
    GameData currentGame;
    ChessGame.TeamColor currentColor;

    public WebSocketFacade(String url) throws ResponseException {
        try {
            String wsURL = url.replace("http", "ws") + "/ws";
            URI wsURI = new URI(wsURL);

            WebSocketContainer wsContainer = ContainerProvider.getWebSocketContainer();
            session = wsContainer.connectToServer(this, wsURI);

            session.addMessageHandler((MessageHandler.Whole<String>) msg -> {
                ServerMessage serverMessage = new Gson().fromJson(msg, ServerMessage.class);

                switch(serverMessage.getServerMessageType()) {
                    case NOTIFICATION -> onNotification(new Gson().fromJson(msg, NotificationMessage.class));
                    case ERROR -> onError(new Gson().fromJson(msg, ErrorMessage.class));
                    case LOAD_GAME -> onLoadGame(new Gson().fromJson(msg, LoadGameMessage.class));
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

    public void connect(AuthData authData, Integer gameID, ChessGame.TeamColor color) {
        currentColor = color;
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

    public void onNotification(NotificationMessage message) {
        ClientUtils.genericOutput(message.message());
    }

    public void onError(ErrorMessage message) {
        ClientUtils.failureOutput(message.error());
    }

    public void onLoadGame(LoadGameMessage message) {
        currentGame = message.game();
        ClientUtils.outputGame(
            currentGame, currentColor!=null ? currentColor : ChessGame.TeamColor.WHITE,
            null, null
        );
    }

    public void reloadCurrentGame() {
        ClientUtils.outputGame(
            currentGame, currentColor!=null ? currentColor : ChessGame.TeamColor.WHITE,
            null, null
        );
    }

    public void showAvailableMoves(ChessPosition startingPosition) {
        HashSet<ChessMove> availableMoves = currentGame.game().getValidMovesForPositionOnBoard(startingPosition);
        HashSet<ChessPosition> endingPositions = new HashSet<>();

        for (ChessMove move : availableMoves) {
            endingPositions.add(move.getEndPosition());
        }

        ClientUtils.outputGame(
            currentGame, currentColor!=null ? currentColor : ChessGame.TeamColor.WHITE,
            startingPosition, endingPositions
        );
    }
}
