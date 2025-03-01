package dataaccess;

import model.GameData;

import java.util.HashSet;

public interface GameDAO {
    HashSet<GameData> requestGames() throws DataAccessException;
    void createGame(GameData game) throws DataAccessException;
    GameData findGame(int gameID) throws DataAccessException;
    boolean doesGameExist(int gameID) throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
    void clear() throws DataAccessException;
}
