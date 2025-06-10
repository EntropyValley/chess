package service;

import chess.ChessGame;
import dataaccess.GameDAO;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import exceptions.*;
import model.AuthData;
import model.GameData;

import java.util.HashSet;

public class GameService {
    GameDAO gameDAO;
    AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public void clear() throws DataAccessException {
        gameDAO.clear();
        authDAO.clear();
    }

    public GameData getGame(Integer gameID) throws GameNotFoundException {
        try {
           return gameDAO.findGame(gameID);
        } catch (DataAccessException exception) {
            throw new GameNotFoundException("Game " + gameID + "cannot be found");
        }
    }

    public HashSet<GameData> listGames(String authToken) throws UnauthorizedException, DataAccessException {
        try {
            authDAO.getAuth(authToken);
        } catch (DataAccessException exception) {
            throw new UnauthorizedException("Not Authorized");
        }

        try {
            return gameDAO.requestGames();
        } catch (DataAccessException exception) {
            throw new DataAccessException("Game Storage not initialized");
        }
    }

    public int createGame(
            String authToken,
            String name
    ) throws UnauthorizedException, NoIDAvailableException, BadRequestException {
        try {
            authDAO.getAuth(authToken);
        } catch (DataAccessException exception) {
            throw new UnauthorizedException("Not Authorized");
        }

        if (name == null || name.isBlank()) {
            throw new BadRequestException("Name is Missing");
        }

        int gameID;

        try {
            gameID = gameDAO.getNextID();
        } catch (DataAccessException exception) {
            throw new NoIDAvailableException("Failed to get next ID");
        }

        try {
            gameDAO.createGame(
                new GameData(
                    gameID, null, null, name, new ChessGame(), GameData.GameStatus.STARTING)
            );
        } catch (DataAccessException exception) {
            throw new BadRequestException("Unable to create game");
        }

        return gameID;
    }

    public void joinGame(String authToken,
                         int gameID,
                         ChessGame.TeamColor desiredColor
    ) throws GameNotFoundException, ColorTakenException, DataAccessException {
        AuthData authData;
        try {
            authData = authDAO.getAuth(authToken);
        } catch (DataAccessException exception) {
            throw new UnauthorizedException("Not Authorized");
        }

        GameData gameData;
        try {
            gameData = gameDAO.findGame(gameID);
        } catch (DataAccessException exception) {
            throw new GameNotFoundException("Game not found");
        }

        String white = gameData.whiteUsername();
        String black = gameData.blackUsername();

        if (desiredColor == ChessGame.TeamColor.WHITE && white == null) {
            white = authData.username();
        } else if (desiredColor == ChessGame.TeamColor.BLACK && black == null) {
            black = authData.username();
        } else {
            throw new ColorTakenException("Color " + desiredColor + " not available");
        }

        try {
            gameDAO.updateGame(
                new GameData(gameID, white, black, gameData.gameName(), gameData.game(), gameData.status())
            );
        } catch (DataAccessException exception) {
            throw new DataAccessException("Unable to update game");
        }
    }

    public void updateGame(
        String authToken, GameData gameData
    ) throws DataAccessException {
        try {
            authDAO.getAuth(authToken);
        } catch (DataAccessException exception) {
            throw new UnauthorizedException("Not Authorized");
        }

        try {
            gameDAO.updateGame(gameData);
        } catch (DataAccessException exception) {
            throw new DataAccessException("Unable to update game data");
        }

    }
}
