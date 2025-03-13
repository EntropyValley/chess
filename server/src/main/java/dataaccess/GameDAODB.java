package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

public class GameDAODB implements GameDAO {

    public GameDAODB() throws DataAccessException{
        DatabaseManager.createDatabase();

        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    """
                    CREATE TABlE IF NOT EXISTS games (
                        id int NOT NULL AUTO_INCREMENT,
                        name varchar(256) NOT NULL,
                        white varchar(256) DEFAULT NULL,
                        black varchar(256) DEFAULT NULL,
                        game text DEFAULT NULL,
                        PRIMARY KEY (id),
                        INDEX(id)
                    )
                    """
            )) {
                statement.executeUpdate();
            } catch (SQLException exception) {
                throw new DataAccessException("Unable to initiate games table");
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to initiate connection to DB");
        }
    }

    private GameData readDBGame(ResultSet results) throws SQLException {
        int id = results.getInt("id");
        String name = results.getString("name");
        String white = results.getString("white");
        String black = results.getString("black");
        ChessGame game = new Gson().fromJson(results.getString("game"), ChessGame.class);

        return new GameData(id, white, black, name, game);
    }

    @Override
    public HashSet<GameData> requestGames() throws DataAccessException {
        HashSet<GameData> requestedGames = new HashSet<>();

        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                "SELECT (id, name, white, black, game) FROM games"
            )) {
                try (ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        requestedGames.add(readDBGame(results));
                    }
                }
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to initiate connection to DB");
        }

        return requestedGames;
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(
                """
                    INSERT INTO games
                    (id, name, white, black, game)
                    VALUES
                    (?, ?, ?, ?, ?)
                """
            )) {
                statement.setInt(1, game.gameID());
                statement.setString(2, game.gameName());
                statement.setString(3, game.whiteUsername());
                statement.setString(4, game.blackUsername());
                statement.setString(5, new Gson().toJson(game.game()));

                statement.executeUpdate();
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw new DataAccessException("Unable to insert game");
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to initiate connection to DB");
        }
    }

    @Override
    public GameData findGame(int gameID) throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                "SELECT (id, name, white, black, game) FROM games WHERE id = ?"
            )) {
                statement.setInt(1, gameID);

                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                       return readDBGame(results);
                    } else {
                        throw new DataAccessException("Unable to find game");
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
    public void updateGame(GameData game) throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE games SET name=?, white=?, black=?, game=? WHERE id=?"
            )) {
                statement.setString(1, game.gameName());
                statement.setString(2, game.whiteUsername());
                statement.setString(3, game.blackUsername());
                statement.setString(4, new Gson().toJson(game.game()));
                statement.setInt(5, game.gameID());

                int updateCount = statement.executeUpdate();
                if (updateCount < 1) {
                    connection.rollback();
                    throw new DataAccessException("Could not find Game");
                }
                connection.commit();
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to initiate connection to DB");
        }
    }

    @Override
    public int getNextID() throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT MAX(id) as current_max_id FROM games"
            )) {
                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        return results.getInt("current_max_id");
                    }
                }
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to initiate authenticationPairs table");
        }
        return 0;
    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "TRUNCATE TABLE games"
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
