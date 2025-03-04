package dataaccess;

import model.UserData;

import java.util.HashSet;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class UserDAOMem implements UserDAO {
    private final HashSet<UserData> userDataStorage;

    public UserDAOMem() {
        this.userDataStorage = new HashSet<>();
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        for (UserData userData : userDataStorage) {
            if (userData.username().equals(username)) {
                return userData;
            }
        }
        throw new DataAccessException("UserData not found for username " + username);
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException {
        try {
            getUser(userData.username());
        } catch (DataAccessException exception) {
            UserData hashedUserData = new UserData(
                    userData.username(),
                    BCrypt.withDefaults().hashToString(10, userData.password().toCharArray()),
                    userData.email()
            );
            userDataStorage.add(hashedUserData);
            return;
        }

        throw new DataAccessException("Userdata already exists for username " + userData.username());
    }

    @Override
    public boolean validateUser(String username, String password) throws DataAccessException {
        UserData userData = getUser(username);
        BCrypt.Result hashResult = BCrypt.verifyer().verify(password.toCharArray(), userData.password().toCharArray());
        return hashResult.verified;
    }

    @Override
    public void clear() throws DataAccessException {
        try {
            this.userDataStorage.clear();
        } catch (Exception exception) {
            throw new DataAccessException("Could not clear user storage: (" + exception.getMessage() + ")");
        }
    }
}
