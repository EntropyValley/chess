package service;

import dataaccess.GameDAO;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;

public class GameService {
    GameDAO gameDAO;
    AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public void clear() {
        try {
            gameDAO.clear();
            authDAO.clear();
        } catch (DataAccessException exception) {
            System.out.println("Unable to clear game-tangential data");
        }
    }
}
