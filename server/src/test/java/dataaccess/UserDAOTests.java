package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDAOTests {
    UserDAO userDAODB;

    @BeforeEach
    void initIndividual() {
        try {
            userDAODB = new UserDAODB();
        } catch (DataAccessException exception) {
            fail();
        }
    }

    @AfterEach
    void destroy() {
        clearDB();
    }

    @Test
    @Order(1)
    @DisplayName("Normal Create Game")
    void createUserNormal() {
        createSingleUser("TestyMcTester");
        successfulGetSingleUser("TestyMcTester");

    }

    @Test
    @Order(2)
    @DisplayName("Normal Get User")
    void getUserNormal() {
        createSingleUser("TestyMcTester");
        createSingleUser("NotYourGoats");
        successfulGetSingleUser("NotYourGoats");
        successfulGetSingleUser("TestyMcTester");
    }

    @Test
    @Order(3)
    @DisplayName("Normal ValidateUser")
    void validateUserNormal() {
        createSingleUser("TestyMcTestFace");

        try {
            boolean valid = userDAODB.validateUser("TestyMcTestFace", "glunk");
            assertTrue(valid);
        } catch (DataAccessException exception) {
            fail();
        }
    }

    @Test
    @Order(4)
    @DisplayName("Normal Clear")
    void clearNormal() {
        createSingleUser("TestyMcTestFace");
        clearDB();
        failureGetSingleUser();
    }

    @Test
    @Order(5)
    @DisplayName("Bad Get User")
    void badGetUser() {
        failureGetSingleUser();
    }

    @Test
    @Order(6)
    @DisplayName("Bad Create User")
    void badCreateUser() {
        createSingleUser("MouthAche49");
        failureCreateSingleUser("MouthAche49");
    }

    @Test
    @Order(6)
    @DisplayName("Bad Validate User")
    void badValidateUser() {
        createSingleUser("MouthAche49");

        try {
            boolean valid = userDAODB.validateUser("MouthAche49", "sploosh");
            assertFalse(valid);
        } catch (DataAccessException exception) {
            fail();
        }
    }

    private void failureGetSingleUser() {
        try {
            userDAODB.getUser("TestyMcTestFace");
            fail();
        } catch (DataAccessException exception) {
            // success
        }
    }

    private void clearDB() {
        try {
            userDAODB.clear();
        } catch (DataAccessException e) {
            fail();
        }
    }


    private void successfulGetSingleUser(String username) {
        try {
            userDAODB.getUser(
                username
            );
        } catch (DataAccessException exception) {
            fail();
        }
    }

    private void createSingleUser(String username) {
        try {
            userDAODB.createUser(
                    new UserData(username, "glunk", "seventeen@geese.inyourhouse")
            );
        } catch (DataAccessException exception) {
            fail();
        }
    }

    private void failureCreateSingleUser(String username) {
        try {
            userDAODB.createUser(
                    new UserData(username, "glunk", "seventeen@geese.inyourhouse")
            );
            fail();
        } catch (DataAccessException exception) {
            //success
        }
    }
}
