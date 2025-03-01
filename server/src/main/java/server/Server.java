package server;

import exceptions.BadRequestException;
import exceptions.UnauthorizedException;
import spark.*;

import dataaccess.*;
import service.*;
import exceptions.*;

public class Server {
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;

    private UserService userService;
    private GameService gameService;

    private UserHandler userHandler;
    private GameHandler gameHandler;

    public Server() {
        try {
            this.userDAO = new UserDAOMem();
            this.authDAO = new AuthDAOMem();
            this.gameDAO = new GameDAOMem();
        } catch (DataAccessException exception) {
            System.out.println("Failed to initialize DAO Memory");
        }

        this.userService = new UserService(userDAO, authDAO);
        this.gameService = new GameService(gameDAO, authDAO);

        this.userHandler = new UserHandler(userService);
        this.gameHandler = new GameHandler(gameService);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.delete("/db", this::clear);

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
        userService.clear();
        gameService.clear();
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
