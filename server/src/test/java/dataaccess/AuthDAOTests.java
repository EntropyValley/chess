package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;
import service.GameService;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthDAOTests {
    AuthDAO authDAODB;

    @BeforeEach
    void initIndividual() {
        try {
            authDAODB = new AuthDAODB();
        } catch (DataAccessException exception) {
            fail();
        }
    }

    @AfterEach
    void destroy() {
        try {
            authDAODB.clear();
        } catch (DataAccessException exception) {
            System.out.println("Error clearing gameDAO or authDAO");
        }
    }

    @Test
    @Order(1)
    @DisplayName("Normal Add Auth")
    void addAuthNormal() {
        try {
            authDAODB.addAuth(new AuthData("hi", "goodbye"));
        } catch (DataAccessException exception) {
            fail();
        }

        try {
            authDAODB.getAuth("goodbye");
        } catch (DataAccessException exception) {
            fail();
        }
    }

    @Test
    @Order(2)
    @DisplayName("Normal Get Auth")
    void getAuthNormal() {
        try {
            authDAODB.addAuth(new AuthData("hi", "goodbye"));
        } catch (DataAccessException exception) {
            fail();
        }

        try {
            authDAODB.getAuth("goodbye");
        } catch (DataAccessException exception) {
            fail();
        }
    }

    @Test
    @Order(3)
    @DisplayName("Normal Delete Auth")
    void deleteAuthNormal() {
        try {
            authDAODB.addAuth(new AuthData("hi", "goodbye"));
        } catch (DataAccessException exception) {
            fail();
        }

        try {
            authDAODB.deleteAuth("goodbye");
        } catch (DataAccessException exception) {
            fail();
        }
    }

    @Test
    @Order(4)
    @DisplayName("Normal Clear Auth")
    void clearAuthNormal() {
        try {
            authDAODB.addAuth(new AuthData("hi", "goodbye"));
        } catch (DataAccessException exception) {
            fail();
        }

        try {
            authDAODB.clear();
        } catch (DataAccessException exception) {
            fail();
        }
    }

    @Test
    @Order(5)
    @DisplayName("Bad Add Auth")
    void addAuthBad() {
        try {
            authDAODB.addAuth(new AuthData("hi", "goodbye"));
        } catch (DataAccessException exception) {
            fail();
        }

        try {
            authDAODB.addAuth(new AuthData("hi", "goodbye"));
            fail();
        } catch (DataAccessException exception) {
            // success
        }
    }

    @Test
    @Order(6)
    @DisplayName("Bad Get Auth")
    void getAuthBad() {
        try {
            authDAODB.getAuth("goodbye");
            fail();
        } catch (DataAccessException exception) {
            // success
        }
    }

    @Test
    @Order(7)
    @DisplayName("Bad Delete Auth")
    void deleteAuthBad() {
        try {
            authDAODB.deleteAuth("goodbye");
            fail();
        } catch (DataAccessException exception) {
            // success
        }
    }
}
