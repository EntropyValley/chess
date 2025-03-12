package server;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dataaccess.DataAccessException;
import exceptions.*;
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

    private static String checkAuthToken(Request request) throws UnauthorizedException {
        String authToken;
        try {
            authToken = request.headers("authorization");
        } catch (Exception exception) {
            throw new UnauthorizedException("Missing Auth");
        }

        if (authToken == null) {
            throw new UnauthorizedException("Missing Auth");
        }
        return authToken;
    }

    public Object hList(Request request, Response response) throws DataAccessException {
        String authToken = checkAuthToken(request);

        HashSet<GameData> games = gameService.listGames(authToken);
        response.status(200);
        return new Gson().toJson(Map.of("games", games));
    }

    public Object hCreate(Request request, Response response) throws BadRequestException, NoIDAvailableException {
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

    public Object hJoin(
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

        ChessGame.TeamColor color;

        try {
            color = ChessGame.TeamColor.valueOf(gameToJoin.get("playerColor").getAsString());
        } catch (Exception exception) {
            throw new BadRequestException("Invalid Color");
        }

        gameService.joinGame(
            authToken,
            gameToJoin.get("gameID").getAsInt(),
            color
        );
        response.status(200);
        return "{}";
    }
}
