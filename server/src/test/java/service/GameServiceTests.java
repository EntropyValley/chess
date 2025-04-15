package service;

import chess.ChessGame;
import dataaccess.*;

import model.AuthData;
import model.GameData;

import java.util.HashSet;
import java.util.UUID;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameServiceTests {
    private static GameDAO gameDAO;
    private static AuthDAO authDAO;

    private GameService gameService;

    @BeforeAll
    static void init() {
        try {
            gameDAO = new GameDAODB();
            authDAO = new AuthDAODB();
        } catch (DataAccessException exception) {
            System.out.println("FAILURE");
        }
    }

    @BeforeEach
    void initIndividual() {
        gameService = new GameService(gameDAO, authDAO);
    }

    @AfterEach
    void destroy() {
        try {
            gameDAO.clear();
            authDAO.clear();
        } catch (DataAccessException exception) {
            System.out.println("Error clearing gameDAO or authDAO");
        }
    }

    @Test
    @Order(1)
    @DisplayName("Normal List Games")
    void listNormal() {
        String authToken = UUID.randomUUID().toString();
        try {
            authDAO.addAuth(new AuthData("testUser", authToken));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        GameData game1 = new GameData(1, null, null, "Game 1", new ChessGame());
        GameData game2 = new GameData(2, null, null, "Game 2", new ChessGame());
        GameData game3 = new GameData(3, null, null, "Game 3", new ChessGame());

        try {
            gameDAO.createGame(game1);
            gameDAO.createGame(game2);
            gameDAO.createGame(game3);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        HashSet<GameData> retrievedGames;
        try {
            retrievedGames = gameService.listGames(authToken);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        assertEquals(3, retrievedGames.size());
    }

    @Test
    @Order(2)
    @DisplayName("Bad Auth List Games")
    void listNoAuth() throws DataAccessException {
        String authToken = UUID.randomUUID().toString();

        GameData game1 = new GameData(1, null, null, "Game 1", new ChessGame());
        GameData game2 = new GameData(2, null, null, "Game 2", new ChessGame());
        GameData game3 = new GameData(3, null, null, "Game 3", new ChessGame());

        try {
            gameDAO.createGame(game1);
            gameDAO.createGame(game2);
            gameDAO.createGame(game3);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            gameService.listGames(authToken);
        } catch (UnauthorizedException e) {
            return;
        }

        fail("UnauthorizedException not thrown");
    }

    @Test
    @Order(3)
    @DisplayName("Normal Create Game")
    void createNormal() {
        String authToken = UUID.randomUUID().toString();
        try {
            authDAO.addAuth(new AuthData("testUser", authToken));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            gameService.createGame(authToken, "New Game");
        } catch (NoIDAvailableException | BadRequestException e) {
            throw new RuntimeException(e);
        }

        HashSet<GameData> retrievedGames;

        try {
            retrievedGames = gameService.listGames(authToken);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        assertEquals(1, retrievedGames.size());
    }

    @Test
    @Order(4)
    @DisplayName("Null Name Create Game")
    void createNull() {
        String authToken = UUID.randomUUID().toString();
        try {
            authDAO.addAuth(new AuthData("testUser", authToken));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            gameService.createGame(authToken, null);
        } catch (NoIDAvailableException e) {
            throw new RuntimeException(e);
        } catch (BadRequestException e) {
            return;
        }

        fail("BadRequestException not thrown");
    }

    @Test
    @Order(5)
    @DisplayName("Normal Join Game")
    void joinNormal()  {
        String authToken = UUID.randomUUID().toString();
        try {
            authDAO.addAuth(new AuthData("testUser", authToken));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        GameData game = new GameData(1, null, null, "Game 1", new ChessGame());
        try {
            gameDAO.createGame(game);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            gameService.joinGame(authToken, 1, ChessGame.TeamColor.WHITE);
        } catch (ColorTakenException | DataAccessException | GameNotFoundException e) {
            throw new RuntimeException(e);
        }

        GameData retrievedGame;
        try {
            retrievedGame = gameDAO.findGame(1);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        assertEquals(new GameData(1, "testUser", null, "Game 1", new ChessGame()), retrievedGame);
    }

    @Test
    @Order(6)
    @DisplayName("Already Taken Join Game")
    void joinAlreadyTaken() {
        String authToken = UUID.randomUUID().toString();
        try {
            authDAO.addAuth(new AuthData("testUser1", authToken));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        GameData game = new GameData(1, null, null, "Game 1", new ChessGame());
        try {
            gameDAO.createGame(game);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            gameService.joinGame(authToken, 1, ChessGame.TeamColor.WHITE);
        } catch (ColorTakenException | DataAccessException | GameNotFoundException e) {
            throw new RuntimeException(e);
        }

        String authToken2 = UUID.randomUUID().toString();
        try {
            authDAO.addAuth(new AuthData("testUser2", authToken2));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            gameService.joinGame(authToken2, 1, ChessGame.TeamColor.WHITE);
        } catch (DataAccessException | GameNotFoundException e) {
            throw new RuntimeException(e);
        } catch (ColorTakenException e) {
            return;
        }

        fail("ColorTakenException not thrown");
    }

    @Test
    @Order(7)
    @DisplayName("Clear")
    void clear() {
        String authToken = UUID.randomUUID().toString();
        try {
            authDAO.addAuth(new AuthData("testUser", authToken));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        GameData game = new GameData(1, null, null, "Game 1", new ChessGame());

        try {
            gameDAO.createGame(game);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            gameService.clear();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        HashSet<GameData> storedGames;
        try {
            storedGames = gameDAO.requestGames();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        assertTrue(storedGames.isEmpty());

        try {
            authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            return;
        }

        fail("UserData not cleared");
    }
}