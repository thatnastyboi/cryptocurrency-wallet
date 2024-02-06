package bg.sofia.uni.fmi.mjt.wallet.crypto.database;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerLoggerTest {

    @Test
    void testLoggerWritesInFile() {
        String testLogDir = "test/test-log-path";
        Path testLogPath = Paths.get(testLogDir);

        ServerLogger logger = new ServerLogger(testLogPath.toString());

        ByteArrayOutputStream consoleOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(consoleOutput));

        Exception testException = new RuntimeException("Test exception");
        logger.logError(testException.getMessage(), testException.getStackTrace());

        assertTrue(Files.exists(testLogPath));
        try {
            String logContent = Files.readString(testLogPath);
            assertTrue(logContent.contains(testException.getMessage()));

            Files.delete(testLogPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
