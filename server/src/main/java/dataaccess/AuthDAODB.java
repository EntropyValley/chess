package dataaccess;

import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AuthDAODB implements AuthDAO {
    public AuthDAODB() throws DataAccessException {
        DatabaseManager.createDatabase();

        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                """
                CREATE TABlE IF NOT EXISTS authenticationPairs (
                    username varchar(256) NOT NULL,
                    authToken varchar(256) NOT NULL,
                    PRIMARY KEY (authToken),
                    INDEX (username),
                    INDEX (authToken)
                )
                """
            )) {
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to initiate authenticationPairs table");
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
    }

    @Override
    public void addAuth(AuthData authData) throws DataAccessException {
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
    }

    @Override
    public void clear() throws DataAccessException {

    }
}
