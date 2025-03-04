package server;

import exceptions.BadRequestException;
import exceptions.UnauthorizedException;
import exceptions.UsernameTakenException;
import model.AuthData;
import model.UserData;
import service.UserService;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;

public class UserHandler {
    UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public Object _register(Request request, Response response) throws BadRequestException, UsernameTakenException {
        UserData userData;
        try {
            userData = new Gson().fromJson(request.body(), UserData.class);
        } catch (Exception exception) {
            throw new BadRequestException("Missing fields");
        }

        if (userData.email() == null) {
            throw new BadRequestException("Missing fields");
        }

        AuthData authData = userService.register(userData);
        response.status(200);
        return new Gson().toJson(authData);
    }

    public Object _login(Request request, Response response) throws BadRequestException, UnauthorizedException {
        UserData userData;
        try {
            userData = new Gson().fromJson(request.body(), UserData.class);
        } catch (Exception exception) {
            throw new BadRequestException("Missing Fields");
        }
        AuthData authData = userService.login(userData);
        response.status(200);
        return new Gson().toJson(authData);
    }

    public Object _logout(Request request, Response response) throws UnauthorizedException {
        String authToken;
        try {
            authToken = request.headers("authorization");
        } catch (Exception exception) {
            throw new UnauthorizedException("Missing Field");
        }

        if (authToken == null) {
            throw new UnauthorizedException("Missing Field");
        }

        userService.logout(authToken);
        response.status(200);
        return "{}";
    }
}
