package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import exceptions.UnauthorizedException;
import exceptions.UsernameTakenException;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class UserService {
    UserDAO userDAO;
    AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    private AuthData getNewAuthData(UserData user) {
        AuthData authData;

        while (true) {
            String authToken = UUID.randomUUID().toString();
            authData = new AuthData(user.username(), authToken);

            try {
                authDAO.addAuth(authData);
                break;
            } catch (DataAccessException exception) {
                // Ignore exception, try again for new authKey
            }
        }

        return authData;
    }

    public void clear() throws DataAccessException {
        userDAO.clear();
        authDAO.clear();
    }

    public AuthData register(UserData userData) throws UsernameTakenException {
        try {
            userDAO.createUser(userData);
        } catch (DataAccessException exception) {
            throw new UsernameTakenException("Username Taken");
        }

        return getNewAuthData(userData);
    }

    public AuthData login(UserData user) throws UnauthorizedException {
        boolean isAuthed;

        try {
            isAuthed = userDAO.validateUser(user.username(), user.password());
        } catch (DataAccessException exception) {
            throw new UnauthorizedException("Invalid Username / Password");
        }

        if (isAuthed) {
            return getNewAuthData(user);
        } else {
            throw new UnauthorizedException("Invalid Username / Password");
        }
    }

    public void logout(String authToken)  throws UnauthorizedException{
        try {
            authDAO.deleteAuth(authToken);
        } catch (DataAccessException exception) {
            throw new UnauthorizedException("AuthToken does not exist");
        }
    }
}
