package dataaccess;

import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM authenticationPairs WHERE authToken=?"
            )) {
                statement.setString(1, authToken);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return new AuthData(
                            result.getString("username"),
                            result.getString("authToken")
                        );
                    }
                } catch (SQLException exception) {
                    throw new DataAccessException("Unable to execute SQL Query");
                }
            } catch (SQLException exception) {
                throw new DataAccessException("Unable to prepare SQL Statement");
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to initiate database connection");
        }

        throw new DataAccessException("Unknown Data Access Error occurred");
    }

    @Override
    public void addAuth(AuthData authData) throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO authenticationPairs (username, authToken) VALUES (?,?)"
            )) {
                statement.setString(1, authData.username());
                statement.setString(2, authData.authToken());

                statement.executeUpdate();
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw new DataAccessException("Unable to add AuthData");
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to initiate database connection");
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM authenticationPairs WHERE authToken = ?"
            )) {
                statement.setString(1, authToken);
                statement.executeUpdate();
            } catch (SQLException exception) {
                throw new DataAccessException("Unable to remove AuthData");
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to initiate database connection");
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                "TRUNCATE TABLE authenticationPairs"
            )) {
                statement.executeUpdate();
            } catch (SQLException exception) {
                throw new DataAccessException("Unable to clear authenticationPairs table");
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to initiate database connection");
        }
    }
}
