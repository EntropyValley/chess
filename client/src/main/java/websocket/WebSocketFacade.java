package websocket;

import client.ResponseException;
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

            session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String msg) {
                    ServerMessage serverMessage = new Gson().fromJson(msg, ServerMessage.class);

                    switch(serverMessage.getServerMessageType()) {
                        case NOTIFICATION -> System.out.println("NOTIFICATION");
                        case ERROR -> System.out.println("ERROR");
                        case LOAD_GAME -> System.out.println("LOAD");
                        case null -> System.out.println("Invalid Server Message received...");
                    }
                }
            });
        } catch (URISyntaxException exception) {

        } catch (DeploymentException exception) {

        } catch (IOException exception) {
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig configuration) {
    }
}
