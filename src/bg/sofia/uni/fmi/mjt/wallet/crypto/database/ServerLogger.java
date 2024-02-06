package bg.sofia.uni.fmi.mjt.wallet.crypto.database;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class ServerLogger {
    private static Logger logger = Logger.getLogger(ServerLogger.class.getName());
    private final String logPath;

    public ServerLogger(String logPath) {
        this.logPath = logPath;
    }

    public void logError(String errorMessage, StackTraceElement[] stackTraceElements) {
        try {
            FileHandler fh = new FileHandler(logPath, true);
            logger.addHandler(fh);

            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            StringBuilder result = new StringBuilder();

            result.append(errorMessage);

            for (StackTraceElement ste : stackTraceElements) {
                result.append(System.lineSeparator()).append(ste.toString());
            }

            logger.info(result.toString());
            fh.close();
        } catch (IOException e) {
            System.out.println("Could not log error");
        }
    }
}
