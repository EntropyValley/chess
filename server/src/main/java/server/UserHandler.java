package server;

import service.UserService;
import spark.Request;
import spark.Response;

public class UserHandler {
    UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public Object _register(Request request, Response response) {
        return "{}";
    }

    public Object _login(Request request, Response response) {
        return "{}";
    }

    public Object _logout(Request request, Response response) {
        return "{}";
    }
}
