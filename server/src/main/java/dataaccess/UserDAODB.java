package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAODB implements UserDAO {
    public UserDAODB() throws DataAccessException {
        DatabaseManager.createDatabase();

        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    """
                    CREATE TABlE IF NOT EXISTS users (
                        name varchar(256) NOT NULL,
                        email varchar(256) DEFAULT NULL,
                        passHash varchar(256) DEFAULT NULL,
                        PRIMARY KEY (name),
                        INDEX(name)
                    )
                    """
            )) {
                statement.executeUpdate();
            } catch (SQLException exception) {
                throw new DataAccessException("Unable to initiate users table");
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to initiate connection to DB");
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT (name, email, passHash) FROM users WHERE name = ?"
            )) {
                statement.setString(1, username);

                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        return new UserData(
                                results.getString("name"),
                                results.getString("email"),
                                results.getString("passHash")
                        );
                    } else {
                        throw new DataAccessException("Unable to find user");
                    }
                } catch (SQLException exception) {
                    throw new DataAccessException("Unable to execute select query");
                }
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to initiate connection to DB");
        }
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException {
        String hashedPass = BCrypt.hashpw(userData.password(), BCrypt.gensalt());

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(
                    """
                        INSERT INTO users
                        (name, email, passhash)
                        VALUES
                        (?, ?, ?)
                    """
            )) {
                statement.setString(1, userData.username());
                statement.setString(2, userData.email());
                statement.setString(2, hashedPass);

                statement.executeUpdate();
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw new DataAccessException("Unable to insert user");
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to initiate connection to DB");
        }
    }

    @Override
    public boolean validateUser(String username, String password) throws DataAccessException {
        UserData user = getUser(username);
        return BCrypt.checkpw(password, user.password());
    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "TRUNCATE TABLE users"
            )) {
                statement.executeUpdate();
            } catch (SQLException exception) {
                throw new DataAccessException("Unable to clear users table");
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to initiate database connection");
        }
    }
}
