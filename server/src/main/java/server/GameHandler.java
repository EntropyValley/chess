package server;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dataaccess.DataAccessException;
import exceptions.BadRequestException;
import exceptions.ColorTakenException;
import exceptions.GameNotFoundException;
import exceptions.NoIDAvailableException;
import model.GameData;
import service.GameService;

import spark.Request;
import spark.Response;
import java.util.HashSet;
import java.util.Map;

public class GameHandler {
    GameService gameService;

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    private static String checkAuthToken(Request request) throws BadRequestException {
        String authToken;
        try {
            authToken = request.headers("authorization");
        } catch (Exception exception) {
            throw new BadRequestException("Missing Field");
        }

        if (authToken == null) {
            throw new BadRequestException("Missing Field");
        }
        return authToken;
    }

    public Object _list(Request request, Response response) throws BadRequestException, DataAccessException {
        String authToken = checkAuthToken(request);

        HashSet<GameData> games = gameService.listGames(authToken);
        response.status(200);
        return new Gson().toJson(Map.of("games", games));
    }

    public Object _create(Request request, Response response) throws BadRequestException, NoIDAvailableException {
        String authToken = checkAuthToken(request);

        JsonObject newGame;
        try {
            newGame = new Gson().fromJson(request.body(), JsonObject.class);
        } catch (JsonSyntaxException exception) {
            throw new BadRequestException("Invalid JSON format");
        }

        if (!newGame.has("gameName")) {
            throw new BadRequestException("Missing Field");
        }

        int gameID = gameService.createGame(authToken, newGame.get("gameName").getAsString());
        response.status(200);
        return "{\"gameID\": " + gameID + "}";
    }

    public Object _join(
            Request request,
            Response response
    ) throws BadRequestException, ColorTakenException, DataAccessException, GameNotFoundException {
        String authToken = checkAuthToken(request);

        JsonObject gameToJoin;
        try {
            gameToJoin = new Gson().fromJson(request.body(), JsonObject.class);
        } catch (JsonSyntaxException exception) {
            throw new BadRequestException("Invalid JSON format");
        }

        if (!gameToJoin.has("gameID") || !gameToJoin.has("playerColor")) {
            throw new BadRequestException("Missing Field");
        }

        gameService.joinGame(
            authToken,
            gameToJoin.get("gameID").getAsInt(),
            ChessGame.TeamColor.valueOf(gameToJoin.get("playerColor").getAsString())
        );
        response.status(200);
        return "{}";
    }
}
