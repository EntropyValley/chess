package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import com.google.gson.Gson;
import websocket.messages.*;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.io.IOException;
import java.util.Map;

public class SessionManager {
    public final ConcurrentHashMap<Integer, HashMap<String, Session>> sessions = new ConcurrentHashMap<>();

    public void add(Integer gameID, String username, Session session) {
        if (!sessions.containsKey(gameID)) {
            sessions.put(gameID, new HashMap<>());
        }
        sessions.get(gameID).put(username, session);
    }

    public void remove(Integer gameID, String username) {
        sessions.get(gameID).remove(username);
    }

    public void broadcastExcept(Integer gameID, String exclusionUser, ServerMessage message) throws IOException {
        HashMap<String, Session> gameConnections = sessions.get(gameID);
        ArrayList<String> removeList = new ArrayList<>();

        for (Map.Entry<String, Session> entry : gameConnections.entrySet()) {
            if (entry.getValue().isOpen()) {
                if (!entry.getKey().equals(exclusionUser)) {
                    entry.getValue().getRemote().sendString(new Gson().toJson(message));
                }
            } else {
                removeList.add(entry.getKey());
            }
        }

        for (String username : removeList) {
            gameConnections.remove(username);
        }
    }

    public void broadcastAll(Integer gameID, ServerMessage message) throws IOException {
        HashMap<String, Session> gameConnections = sessions.get(gameID);
        ArrayList<String> removeList = new ArrayList<>();

        for (Map.Entry<String, Session> entry : gameConnections.entrySet()) {
            if (entry.getValue().isOpen()) {
                entry.getValue().getRemote().sendString(new Gson().toJson(message));
            } else {
                removeList.add(entry.getKey());
            }
        }

        for (String username : removeList) {
            gameConnections.remove(username);
        }
    }

    public void send(Integer gameID, String user, ServerMessage message) throws IOException {
        Session userSession = sessions.get(gameID).get(user);
        if (userSession.isOpen()) {
            userSession.getRemote().sendString(new Gson().toJson(message));
        } else {
            sessions.get(gameID).remove(user);
        }
    }
}
