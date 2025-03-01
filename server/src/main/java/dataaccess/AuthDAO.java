package dataaccess;

import model.AuthData;
import exceptions.BadRequestException;

public interface AuthDAO {
    AuthData getAuth(String authToken) throws DataAccessException;
    void addAuth(AuthData authData) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
    void clear() throws DataAccessException;
}
