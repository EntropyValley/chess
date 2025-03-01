package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import dataaccess.DataAccessException;

public class UserService {
    UserDAO userDAO;
    AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public void clear() {
        try {
            userDAO.clear();
            authDAO.clear();
        } catch (DataAccessException exception) {
            System.out.println("Unable to clear user-tangential data");
        }
    }
}
