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
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to initiate authenticationPairs table");
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
            throw new DataAccessException("Unable to initiate authenticationPairs table");
        }

        return requestedGames;
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
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
                    }
                }
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Unable to initiate authenticationPairs table");
        }

        throw new DataAccessException("Unknown Data Access Error Occurred");
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
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
    }
}
