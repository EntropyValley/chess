package server;

import exceptions.*;
import spark.*;

import dataaccess.*;
import service.*;

public class Server {

    private final UserService userService;
    private final GameService gameService;

    private final UserHandler userHandler;
    private final GameHandler gameHandler;

    public Server() {
        UserDAO userDAO;

        try {
            userDAO = new UserDAODB();
        } catch (DataAccessException exception) {
            System.out.println("Could not load User DB DAO, reverting to Mem:\n" + exception.getMessage());
            userDAO = new UserDAOMem();
        }

        AuthDAO authDAO;

        try {
            authDAO = new AuthDAODB();
        } catch (DataAccessException exception) {
            System.out.println("Could not load Auth DB DAO, reverting to Mem:\n" + exception.getMessage());
            authDAO = new AuthDAOMem();
        }

        GameDAO gameDAO;

        try {
            gameDAO = new GameDAODB();
        } catch (DataAccessException exception) {
            System.out.println("Could not load Game DB DAO, reverting to Mem:\n" + exception.getMessage());
            gameDAO = new GameDAOMem();
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

        Spark.post("/user", userHandler::hRegister);

        Spark.post("/session", userHandler::hLogin);
        Spark.delete("/session", userHandler::hLogout);

        Spark.get("/game", gameHandler::hList);
        Spark.post("/game", gameHandler::hCreate);
        Spark.put("/game", gameHandler::hJoin);

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
        response.status(403);
        response.body("{\"message\": \"Error: already taken\"}");
    }

    private void gameNotFoundExceptionHandler(GameNotFoundException exception, Request request, Response response) {
        response.status(400);
        response.body("{\"message\": \"Error: game not found\"}");
    }

    private void colorTakenExceptionHandler(ColorTakenException exception, Request request, Response response) {
        response.status(403);
        response.body("{\"message\": \"Error: already taken\"}");
    }

    private void otherExceptionHandler(Exception exception, Request request, Response response) {
        response.status(500);
        response.body("{\"message\": \"Error: %s\"}".formatted(exception.getMessage()));
    }
}
