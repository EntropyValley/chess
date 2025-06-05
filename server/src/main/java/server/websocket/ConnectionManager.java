package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import com.google.gson.Gson;
import websocket.messages.ServerMessage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.io.IOException;
import java.util.Map;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, HashMap<String, Session>> connections = new ConcurrentHashMap<>();

    public void add(Integer gameID, String username, Session session) {
        if (!connections.containsKey(gameID)) {
            connections.put(gameID, new HashMap<>());
        }
        connections.get(gameID).put(username, session);
    }

    public void remove(String username, Integer gameID) {
        connections.get(gameID).remove(username);
    }

    public void broadcast(String exclusionUser, ServerMessage message, Integer gameID) throws IOException {
        HashMap<String, Session> gameConnections = connections.get(gameID);

        for (Map.Entry<String, Session> entry : gameConnections.entrySet()) {
            if (entry.getValue().isOpen()) {
                if (!entry.getKey().equals(exclusionUser)) {
                    entry.getValue().getRemote().sendString(new Gson().toJson(message));
                }
            } else {
                gameConnections.remove(entry.getKey());
            }
        }
    }

    public void send(String user, ServerMessage message, Integer gameID) throws IOException {
        Session userSession = connections.get(gameID).get(user);
        if (userSession.isOpen()) {
            userSession.getRemote().sendString(new Gson().toJson(message));
        } else {
            connections.get(gameID).remove(user);
        }
    }
}
