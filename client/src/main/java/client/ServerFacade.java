package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ServerFacade {

    private final String urlBase;

    public ServerFacade(String url) {
        this.urlBase = url;
    }

    private <T> T request(String httpMethod, String endpoint, Object request, Class<T> responseClass, AuthData authData) throws ResponseException {
        try {
            URL url = new URI(urlBase + "/" + endpoint).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(httpMethod);
            connection.setDoOutput(true);

            if (authData != null && authData.authToken() != null) {
                connection.setRequestProperty("authorization", authData.authToken());
            }

            if (request != null) {
                connection.addRequestProperty("Content-Type", "application/json");
                try (OutputStream requestBody = connection.getOutputStream()) {
                    requestBody.write((new Gson().toJson(request)).getBytes());
                }
            }

            connection.connect();
            int statusCode = connection.getResponseCode();
            if (statusCode / 100 != 2) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String line;
                StringBuilder errorBody = new StringBuilder();

                while ((line = errorReader.readLine()) != null) {
                    errorBody.append(line);
                }
                errorReader.close();

                throw new ResponseException(statusCode, errorBody.toString());
            }

            BufferedReader successReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder successBody = new StringBuilder();

            while ((line = successReader.readLine()) != null) {
                successBody.append(line);
            }
            successReader.close();

            if (responseClass != null) {
                return new Gson().fromJson(successBody.toString(), responseClass);
            } else {
                return null;
            }
        } catch (Exception exception) {
            throw new ResponseException(-1, exception.getMessage());
        }
    }

    public void clear() throws ResponseException {
        this.request("DELETE", "/db", null, null, null);
    }

    public boolean register(UserData userData) throws ResponseException {
        try {
            this.request("POST", "/user", userData, AuthData.class, null);
            return true;
        } catch (Exception exception) {
            System.out.println("User " + userData.username() + " already exists");
            return false;
        }
    }

    public AuthData login(UserData userData) throws ResponseException {
        try {
            return this.request("POST", "/session", userData, AuthData.class, null);
        } catch (Exception exception) {
            System.out.println("Could not login user " + userData.username());
            return null;
        }
    }

    public void logout(AuthData authData) throws ResponseException {
        try {
            this.request("DELETE", "/session", null, null, authData);
        } catch (Exception exception) {
            System.out.println("Unable to logout user");
        }
    }

    public GameData[] listGames(AuthData authData) throws ResponseException {
        record Games(GameData[] games) {}
        try {
            var response = this.request("GET", "/game", null, Games.class, authData);

            if (response != null) {
                return response.games();
            }
            return null;
        } catch (Exception exception) {
            System.out.println("Unable to fetch games list");
            return null;
        }
    }

    public boolean createGame(AuthData authData, String gameName) throws ResponseException {
        JsonObject request = new JsonObject();
        request.addProperty("gameName", gameName);

        try {
            this.request("POST", "/game", request, GameData.class, authData);
            return true;
        } catch (Exception exception) {
            System.out.println("Unable to create game");
            return false;
        }
    }

    public boolean joinGame(AuthData authData, int id, String color) throws ResponseException {
        JsonObject request = new JsonObject();
        request.addProperty("gameID", id);
        request.addProperty("playerColor", color);

        try {
            this.request("PUT", "/game", request, GameData.class, authData);
            return true;
        } catch (Exception exception) {
            System.out.println("Unable to join game");
            return false;
        }
    }
}
