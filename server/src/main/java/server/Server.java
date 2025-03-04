package server;

import exceptions.BadRequestException;
import exceptions.UnauthorizedException;
import spark.*;

import dataaccess.*;
import service.*;
import exceptions.*;

public class Server {

    private final UserService userService;
    private final GameService gameService;

    private final UserHandler userHandler;
    private final GameHandler gameHandler;

    public Server() {
        UserDAO userDAO = new UserDAOMem();
        AuthDAO authDAO = new AuthDAOMem();
        GameDAO gameDAO = new GameDAOMem();

        this.userService = new UserService(userDAO, authDAO);
        this.gameService = new GameService(gameDAO, authDAO);

        this.userHandler = new UserHandler(userService);
        this.gameHandler = new GameHandler(gameService);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.delete("/db", this::clear);

        Spark.post("/user", userHandler::_register);

        Spark.post("/session", userHandler::_login);
        Spark.delete("/session", userHandler::_logout);

        Spark.get("/game", gameHandler::_list);
        Spark.post("/game", gameHandler::_create);
        Spark.put("/game", gameHandler::_join);

        Spark.exception(BadRequestException.class, this::badRequestExceptionHandler);
        Spark.exception(UnauthorizedException.class, this::unauthorizedExceptionHandler);
        Spark.exception(UsernameTakenException.class, this::usernameTakenExceptionHandler);
        Spark.exception(GameNotFoundException.class, this::gameNotFoundExceptionHandler);
        Spark.exception(ColorTakenException.class, this::colorTakenExceptionHandler);
        Spark.exception(Exception.class, this::otherExceptionHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object clear(Request request, Response response) {
        try {
            userService.clear();
            gameService.clear();
        } catch (DataAccessException exception) {
            response.status(500);
            return "{\"message\": \"Error: " + exception.getMessage() + "\"}";
        }
        response.status(200);
        return "{}";
    }

    private void badRequestExceptionHandler(BadRequestException exception, Request request, Response response) {
        response.status(400);
        response.body("{\"message\": \"Error: bad request\"}");
    }

    private void unauthorizedExceptionHandler(UnauthorizedException exception, Request request, Response response) {
        response.status(401);
        response.body("{\"message\": \"Error: unauthorized\"}");
    }

    private void usernameTakenExceptionHandler(UsernameTakenException exception, Request request, Response response) {
        response.status(400);
        response.body("{\"message\": \"Error: already taken\"");
    }

    private void gameNotFoundExceptionHandler(GameNotFoundException exception, Request request, Response response) {
        response.status(400);
        response.body("{\"message\": \"Error: game not found\"");
    }

    private void colorTakenExceptionHandler(ColorTakenException exception, Request request, Response response) {
        response.status(400);
        response.body("{\"message\": \"Error: already taken\"}");
    }

    private void otherExceptionHandler(Exception exception, Request request, Response response) {
        response.status(500);
        response.body("{\"message\": \"Error: %s\"}".formatted(exception.getMessage()));
    }
}
