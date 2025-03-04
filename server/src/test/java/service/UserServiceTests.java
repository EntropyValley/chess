package service;

import dataaccess.*;
import exceptions.UnauthorizedException;
import exceptions.UsernameTakenException;
import model.AuthData;
import model.UserData;

import org.junit.jupiter.api.*;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTests {
    private static UserDAO userDAO;
    private static AuthDAO authDAO;

    private UserService userService;

    @BeforeAll
    static void init() {
        userDAO = new UserDAOMem();
        authDAO = new AuthDAOMem();
    }

    @BeforeEach
    void initIndividual() {
        userService = new UserService(userDAO, authDAO);
    }

    @AfterEach
    void destroy() {
        try {
            userDAO.clear();
            authDAO.clear();
        } catch (DataAccessException exception) {
            System.out.println("Error clearing userDAO or authDAO");
        }
    }

    @Test
    @Order(1)
    @DisplayName("Normal Register")
    void registerNormal() {
        UserData userData = new UserData("test1", "test2", "test3");
        AuthData authData;
        try {
            authData = userService.register(userData);
        } catch (UsernameTakenException e) {
            throw new RuntimeException(e);
        }

        assertEquals("test1", authData.username());
        assertNotNull(authData.authToken());
    }

    @Test
    @Order(2)
    @DisplayName("Duplicate Username Register")
    void registerDuplicate() {
        UserData userData = new UserData("test1", "test2", "test3");

        try {
            userService.register(userData);
        } catch (UsernameTakenException e) {
            throw new RuntimeException(e);
        }

        try {
            userService.register(userData);
        } catch (UsernameTakenException e) {
            return;
        }

        fail("UsernameTakenException not thrown");
    }

    @Test
    @Order(3)
    @DisplayName("Normal Login")
    void loginNormal() {
        UserData userData = new UserData("test1", "test2", "test3");
        try {
             userService.register(userData);
        } catch (UsernameTakenException e) {
            throw new RuntimeException(e);
        }

        AuthData authData2;
        try {
            authData2 = userService.login(userData);
        } catch (UnauthorizedException e) {
            throw new RuntimeException(e);
        }

        assertEquals("test1", authData2.username());
        assertNotNull(authData2.authToken());
    }

    @Test
    @Order(4)
    @DisplayName("Invalid Login")
    void loginInvalid() {
        UserData userData = new UserData("test1", "test2", "test3");

        try {
            userService.login(userData);
        } catch (UnauthorizedException e) {
            return;
        }

        fail("UnauthorizedException not thrown");
    }

    @Test
    @Order(5)
    @DisplayName("Normal Logout")
    void logoutNormal() {
        UserData userData = new UserData("test1", "test2", "test3");
        AuthData authData;
        try {
            authData = userService.register(userData);
        } catch (UsernameTakenException e) {
            throw new RuntimeException(e);
        }

        try {
            userService.logout(authData.authToken());
        } catch (UnauthorizedException e) {
            throw new RuntimeException(e);
        }

        try {
            authDAO.getAuth(authData.authToken());
        } catch (DataAccessException e) {
            return;
        }

        fail("AuthData still exists");
    }

    @Test
    @Order(6)
    @DisplayName("Invalid Token Logout")
    void logoutInvalid() {
        try {
            userService.logout(UUID.randomUUID().toString());
        } catch (UnauthorizedException e) {
            return;
        }

        fail("Logged Out Nonexistent Session");
    }


    @Test
    @Order(7)
    @DisplayName("Clear")
    void clear() {
        UserData userData = new UserData("test1", "test2", "test3");

        AuthData initialAuth;
        try {
            initialAuth = userService.register(userData);
        } catch (UsernameTakenException e) {
            throw new RuntimeException(e);
        }

        try {
            userService.clear();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        boolean caught = false;
        try {
            userDAO.getUser("test1");
        } catch (DataAccessException e) {
            caught = true;
        }

        assertTrue(caught);

        caught = false;
        try {
            authDAO.getAuth(initialAuth.authToken());
        } catch (DataAccessException e) {
            caught = true;
        }

        assertTrue(caught);
    }
}
