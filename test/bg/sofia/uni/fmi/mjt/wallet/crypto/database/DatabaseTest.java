package bg.sofia.uni.fmi.mjt.wallet.crypto.database;

import bg.sofia.uni.fmi.mjt.wallet.crypto.account.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabaseTest {
    private static final String TEST_USERS_INFO =
        """
            0;test1;9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08;0.0;PLC;1406.3994687999493;50.0                                                                                          
            0;test2;9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08;0.0;PLC;1406.3994687999493;50.0                                                                        
                        """;
    private static final String TEST_OUTPUT_DIRECTORY = "test-database";
    private static final String TEST_DATABASE_FILE_NAME = "test-accounts.txt";
    private static final Path TEST_FILE_PATH = Paths.get(TEST_OUTPUT_DIRECTORY, TEST_DATABASE_FILE_NAME);

    private Database testDatabase;
    private Set<Account> testData;

    @BeforeEach
    void setUp() {
        try {
            Files.createDirectories(TEST_FILE_PATH.getParent());
            Files.write(TEST_FILE_PATH, TEST_USERS_INFO.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        testDatabase = new Database(TEST_FILE_PATH);
        testData = testDatabase.getDatabase();
    }

    @AfterEach
    void tearDown() {
        TEST_FILE_PATH.toFile().delete();
        TEST_FILE_PATH.getParent().toFile().delete();
    }

    @Test
    void testLoadData() {
        int testDataSize = testData.size();

        assertEquals(2, testDataSize,
            "Expected size of test database is 2, but was " + testDataSize);

        assertTrue(testData.contains(
                Account.fromCSV("0;test1;9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08;0.0;PLC;1406.3994687999493;50.0")),
            "Expected loaded data to contain account with name test1 but it did not");
        assertTrue(testData.contains(
                Account.fromCSV("0;test2;9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08;0.0;PLC;1406.3994687999493;50.0")),
            "Expected loaded data to contain account with name test2 but it did not");
    }

    @Test
    void testSaveData() {
        Set<Account> updatedTestData = new HashSet<>(testData);
        updatedTestData.add(
            Account.fromCSV("0;test3;9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08;0.0;PLC;1406.3994687999493;50.0"));

        testDatabase.updateData(updatedTestData);

        int testDataSize = testData.size();

        assertEquals(3, testDataSize,
            "Expected size of test database is 3, but was " + testDataSize);

        assertTrue(testData.contains(
                Account.fromCSV("0;test3;9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08;0.0;PLC;1406.3994687999493;50.0")),
            "Expected loaded data to contain account with name test3 but it did not");
    }

    @Test
    void testUpdateData() {
        Set<Account> updatedTestData = new HashSet<>(testData);
        updatedTestData.add(
            Account.fromCSV("0;test3;9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08;0.0;PLC;1406.3994687999493;50.0"));

        testDatabase.updateData(updatedTestData);

        Set<Account> updatedDataFromDatabase = testDatabase.getDatabase();

        assertIterableEquals(updatedTestData, updatedDataFromDatabase,
            "Expected updated data to be equal to the data in the database");
    }

    @Test
    void testShutdownScheduler() {
        Set<Account> updatedTestData = new HashSet<>(testData);
        updatedTestData.add(
            Account.fromCSV("0;test3;9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08;0.0;PLC;1406.3994687999493;50.0"));

        testDatabase.shutdownScheduler(updatedTestData);

        assertTrue(Files.exists(TEST_FILE_PATH));

        try {
            String fileContent = Files.readString(TEST_FILE_PATH, StandardCharsets.UTF_8);
            assertTrue(fileContent.contains(
                "0;test3;9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08;0.0;PLC;1406.3994687999493;50.0"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
