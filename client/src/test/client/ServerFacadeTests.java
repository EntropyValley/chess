package client;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void initTest() {
        try {
            serverFacade.clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void registerSuccess() {
        register1();
    }

    @Test
    public void registerFailure() {
        try {
            AuthData authData = serverFacade.register(new UserData("1", "1", "1"));
            assertNotNull(authData);
        }  catch (Exception e) {
            fail();
        }

        try {
            serverFacade.register(new UserData("1", "1", "1"));
            fail();
        }  catch (Exception e) {
            //success
        }
    }

    @Test
    public void loginSuccess() {
        try {
            serverFacade.register(new UserData("1", "1", "1"));
        } catch (Exception e) {
            fail();
        }

        try {
            AuthData authData = serverFacade.login(new UserData("1", "1", null));
            assertNotNull(authData);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void loginFailure() {
        try {
            serverFacade.login(new UserData("1", "1", null));
            fail();
        } catch (Exception e) {
            // success
        }
    }

    @Test
    public void logoutSuccess() {
        AuthData authData = register1();

        if (authData == null) {
            fail();
        }

        try {
            serverFacade.logout(authData);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void logoutFailure() {
        AuthData authData = register1();

        if (authData == null) {
            fail();
        }

        try {
            serverFacade.logout(authData);
        } catch (Exception e) {
            fail();
        }

        try {
            serverFacade.logout(authData);
            fail();
        } catch (Exception e) {
            // success
        }
    }

    @Test
    public void createGameSuccess() {
        AuthData authData = register1();

        if (authData == null) {
            fail();
        }

        ServerFacade.CreateGameResponse response = createGame(authData);

        if (response == null) {
            fail();
        }

        assertEquals(1, response.gameID());
    }

    @Test
    public void createGameFailure() {
        try {
            serverFacade.createGame(null, "Game");
            fail();
        } catch (Exception e) {
            // success
        }
    }

    @Test
    public void listGamesSuccess() {
        AuthData authData = register1();

        if (authData == null) {
            fail();
        }

        ServerFacade.CreateGameResponse response = createGame(authData);

        if (response == null) {
            fail();
        }

        assertEquals(1, response.gameID());

        ServerFacade.CreateGameResponse response2 = createGame(authData);

        if (response2 == null) {
            fail();
        }

        GameData[] games = null;

        try {
            games = serverFacade.listGames(authData);
        } catch (Exception e) {
            fail();
        }

        assertEquals(2, games.length);
    }

    @Test
    public void listGamesFailure() {
        try {
            serverFacade.listGames(null);
            fail();
        } catch (Exception e) {
            // success
        }
    }

    @Test
    void joinGameSuccess() {
        AuthData authData = register1();

        if (authData == null) {
            fail();
        }

        ServerFacade.CreateGameResponse response = createGame(authData);

        if (response == null) {
            fail();
        }

        assertEquals(1, response.gameID());

        try {
            serverFacade.joinGame(authData, response.gameID(), "WHITE");
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void joinGameFailure() {
        AuthData authData = register1();

        if (authData == null) {
            fail();
        }

        ServerFacade.CreateGameResponse response = createGame(authData);

        if (response == null) {
            fail();
        }

        assertEquals(1, response.gameID());

        try {
            serverFacade.joinGame(authData, response.gameID(), "WHITE");
        } catch (Exception e) {
            fail();
        }

        AuthData authData2 = register2();

        try {
            serverFacade.joinGame(authData2, response.gameID(), "WHITE");
            fail();
        } catch (Exception e) {
            // success
        }
    }

    private static ServerFacade.CreateGameResponse createGame(AuthData authData) {
        ServerFacade.CreateGameResponse response = null;

        try {
            response = serverFacade.createGame(authData, "Game");
        } catch (Exception e) {
            fail();
        }
        return response;
    }

    private static AuthData register1() {
        AuthData authData = null;
        try {
            authData = serverFacade.register(new UserData("1", "1", "1"));
        } catch (Exception e) {
            fail();
        }
        return authData;
    }

    private static AuthData register2() {
        AuthData authData = null;
        try {
            authData = serverFacade.register(new UserData("2", "2", "2"));
        } catch (Exception e) {
            fail();
        }
        return authData;
    }
}
