package dataaccess;

import model.GameData;

import java.util.HashSet;

public class GameDAOMem implements GameDAO {
    HashSet<GameData> gameDataStorage;
    int nextID;

    public GameDAOMem() {
        gameDataStorage = new HashSet<>();
        nextID = 1;
    }

    @Override
    public HashSet<GameData> requestGames() throws DataAccessException {
        if (gameDataStorage != null) {
            return gameDataStorage;
        }
        throw new DataAccessException("Game storage not initialized");
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        try {
            findGame(game.gameID());
        } catch (DataAccessException exception) {
            gameDataStorage.add(game);
            return;
        }

        throw new DataAccessException("Game with ID=" + game.gameID() + " already exists");
    }

    @Override
    public GameData findGame(int gameID) throws DataAccessException {
        for (GameData gameData : gameDataStorage) {
            if (gameData.gameID() == gameID) {
                return gameData;
            }
        }
        throw new DataAccessException("Game with ID=" + gameID + " does not exist");
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        try {
            gameDataStorage.remove(findGame(game.gameID()));
            createGame(game);
        } catch(DataAccessException exception) {
            throw new DataAccessException("Multiple copies of game with ID=" + game.gameID() + " exist within memory");
        }
    }

    @Override
    public int getNextID() throws DataAccessException {
        if (nextID >= 0) {
            return nextID++;
        }
        throw new DataAccessException("Invalid ID");
    }

    @Override
    public void clear() throws DataAccessException {
        try {
            gameDataStorage.clear();
        } catch (Exception exception) {
            throw new DataAccessException("Could not clear game storage (" + exception.getMessage() + ")");
        }
    }
}
