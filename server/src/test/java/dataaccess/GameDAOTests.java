package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameDAOTests {
    GameDAO gameDAODB;

    @BeforeEach
    void initIndividual() {
        try {
            gameDAODB = new GameDAODB();
        } catch (DataAccessException exception) {
            fail();
        }
    }

    @AfterEach
    void destroy() {
        try {
            gameDAODB.clear();
        } catch (DataAccessException exception) {
            System.out.println("Error clearing gameDAO");
        }
    }

    @Test
    @Order(1)
    @DisplayName("Normal Create Game")
    void createGameNormal() {
        try {
            gameDAODB.createGame(
                    new GameData(1, null, null, "New Game", new ChessGame())
            );
        } catch (DataAccessException exception) {
            fail();
        }

        try {
            HashSet<GameData> games = gameDAODB.requestGames();
            assertEquals(1, games.size());
        } catch (DataAccessException exception) {
            fail();
        }
    }

    @Test
    @Order(2)
    @DisplayName("Normal Request Games")
    void requestGamesNormal() {
        createThreeGames();

        try {
            HashSet<GameData> games = gameDAODB.requestGames();
            assertEquals(3, games.size());
        } catch (DataAccessException exception) {
            fail();
        }
    }

    @Test
    @Order(3)
    @DisplayName("Normal Find Game")
    void findGameNormal() {
        createThreeGames();

        try {
            GameData game = gameDAODB.findGame(2);
            assertNotNull(game);
        } catch (DataAccessException exception) {
            fail();
        }
    }

    @Test
    @Order(4)
    @DisplayName("Normal Clear")
    void clearNormal() {
        createThreeGames();
        clear();

        try {
            gameDAODB.findGame(1);
            fail();
        } catch (DataAccessException exception) {
            // success
        }
    }

    private void clear() {
        try {
            gameDAODB.clear();
        } catch (DataAccessException exception) {
            fail();
        }
    }

    @Test
    @Order(5)
    @DisplayName("Bad Create Game")
    void createGameBad() {
        try {
            gameDAODB.createGame(
                    new GameData(1, null, null, "New Game", new ChessGame())
            );
            gameDAODB.createGame(
                    new GameData(1, null, null, "New Game", new ChessGame())
            );
            fail();
        } catch (DataAccessException exception) {
            // Success
        }

        try {
            HashSet<GameData> games = gameDAODB.requestGames();
            assertEquals(1, games.size());
        } catch (DataAccessException exception) {
            fail();
        }
    }

    @Test
    @Order(6)
    @DisplayName("Bad Request Games")
    void requestGamesBad() {
        createThreeGames();

        try {
            HashSet<GameData> games = gameDAODB.requestGames();
            assertEquals(3, games.size());
        } catch (DataAccessException exception) {
            fail();
        }

        try {
            gameDAODB.clear();
            HashSet<GameData> games = gameDAODB.requestGames();
            if (!games.isEmpty()) {
                fail();
            }
        } catch (DataAccessException exception) {
            fail();
        }
    }

    private void createThreeGames() {
        try {
            gameDAODB.createGame(
                    new GameData(1, null, null, "New Game", new ChessGame())
            );
            gameDAODB.createGame(
                    new GameData(2, null, null, "New Game 2", new ChessGame())
            );
            gameDAODB.createGame(
                    new GameData(3, null, null, "New Game 3", new ChessGame())
            );
        } catch (DataAccessException exception) {
            fail();
        }
    }

    @Test
    @Order(7)
    @DisplayName("Bad Find Game")
    void findGameBad() {
        try {
            gameDAODB.findGame(2);
            fail();
        } catch (DataAccessException exception) {
            // success
        }
    }
}
