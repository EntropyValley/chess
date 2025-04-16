package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import exceptions.*;
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

    private <T> T request(String httpMethod, String endpoint, Object request, Class<T> responseClass, AuthData authData) throws ResponseException, BadRequestException, GameNotFoundException, GenericTakenException, UnauthorizedException {
       String error;
       int errorCode;

        try {
            URL url = new URI(urlBase + "/" + endpoint).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(httpMethod);
            connection.setDoOutput(true);

            if (authData != null) {
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

                record ErrorRecord(String message) {}

                ErrorRecord errorRecord = new Gson().fromJson(errorBody.toString(), ErrorRecord.class);
                error = errorRecord.message();
                errorCode = statusCode;
            } else {
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
            }
        } catch (java.io.IOException exception) {
            throw new ConnectionException("Unable to Connect");
        } catch (Exception exception) {
            throw new ResponseException(exception.getMessage());
        }

        if (error != null) {
            switch (error) {
                case "Error: bad request":
                    throw new BadRequestException(String.format("Code: %d", errorCode));
                case "Error: game not found":
                    throw new GameNotFoundException(String.format("Code: %d", errorCode));
                case "Error: already taken":
                    throw new GenericTakenException(String.format("Code: %d", errorCode));
                case "Error: unauthorized":
                    throw new UnauthorizedException(String.format("Code: %d", errorCode));
                default:
                    throw new ResponseException(String.format("Code: %d", errorCode));
            }
        } else {
            throw new ResponseException("Unknown Error");
        }
    }

    public void clear() throws BadRequestException, GameNotFoundException, GenericTakenException, UnauthorizedException {
        this.request("DELETE", "/db", null, null, null);
    }

    public AuthData register(UserData userData) throws ResponseException, BadRequestException, GameNotFoundException, GenericTakenException, UnauthorizedException {
        return this.request("POST", "/user", userData, AuthData.class, null);
    }

    public AuthData login(UserData userData) throws ResponseException, BadRequestException, GameNotFoundException, GenericTakenException, UnauthorizedException {
        return this.request("POST", "/session", userData, AuthData.class, null);
    }

    public void logout(AuthData authData) throws ResponseException, BadRequestException, GameNotFoundException, GenericTakenException, UnauthorizedException {
        this.request("DELETE", "/session", null, null, authData);
    }

    public GameData[] listGames(AuthData authData) throws ResponseException, BadRequestException, GameNotFoundException, GenericTakenException, UnauthorizedException {
        record Games(GameData[] games) {}
        var response = this.request("GET", "/game", null, Games.class, authData);
        if (response != null) {
            return response.games();
        }
        return null;
    }

    public record createGameResponse(int gameID) {}

    public createGameResponse createGame(AuthData authData, String gameName) throws ResponseException, BadRequestException, GameNotFoundException, GenericTakenException, UnauthorizedException {
        record request(String gameName) {}

        return this.request("POST", "/game", new request(gameName), createGameResponse.class, authData);
    }

    public void joinGame(AuthData authData, int id, String color) throws ResponseException, BadRequestException, GameNotFoundException, GenericTakenException, UnauthorizedException {
        JsonObject request = new JsonObject();
        request.addProperty("gameID", id);
        request.addProperty("playerColor", color);

        this.request("PUT", "/game", request, GameData.class, authData);
    }
}
