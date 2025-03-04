package server;

import service.GameService;
import spark.Request;
import spark.Response;

public class GameHandler {
    GameService gameService;

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public Object _list(Request request, Response response) {
        return "{}";
    }

    public Object _create(Request request, Response response) {
        return "{}";
    }

    public Object _join(Request request, Response response) {
        return "{}";
    }
}
