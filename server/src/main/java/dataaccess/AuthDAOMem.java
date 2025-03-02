package dataaccess;

import model.AuthData;

import java.util.HashSet;
import java.util.Iterator;

public class AuthDAOMem implements AuthDAO {
    private HashSet<AuthData> authDataStorage;

    public AuthDAOMem() {
        this.authDataStorage = new HashSet<>();
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        for (AuthData authData : authDataStorage) {
            if (authData.authToken().equals(authToken)) {
                return authData;
            }
        }
        throw new DataAccessException("AuthData not found for token " + authToken);
    }

    @Override
    public void addAuth(AuthData authData) throws DataAccessException {
        try {
            getAuth(authData.authToken());
        } catch (DataAccessException exception) {
            authDataStorage.add(authData);
            return;
        }
        throw new DataAccessException("Token " + authData.authToken() + " already exists");
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        try {
            getAuth(authToken);

            Iterator<AuthData> iterator = authDataStorage.iterator();

            while (iterator.hasNext()) {
                AuthData authData = iterator.next();

                if (authData.authToken().equals(authToken)) {
                    iterator.remove();
                    break;
                }
            }
        } catch (DataAccessException exception) {
            throw new DataAccessException("Token " + authToken + " does not exist");
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try {
            this.authDataStorage.clear();
        } catch (Exception exception) {
            throw new DataAccessException("Could not clear data storage: (" + exception.getMessage() + ")");
        }
    }
}
