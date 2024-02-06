package bg.sofia.uni.fmi.mjt.wallet.crypto.database;

import bg.sofia.uni.fmi.mjt.wallet.crypto.account.Account;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Database implements DatabaseAPI {
    private static final int PERIOD_OF_SAVING = 5;
    private static final String NEW_LINE = System.lineSeparator();
    private Path filePath;

    private Set<Account> database = new HashSet<>();
    private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public Database(Path filePath) {
        this.filePath = filePath;
        initDatabase();
    }

    public Set<Account> getDatabase() {
        return Collections.unmodifiableSet(database);
    }

    private void initDatabase() {
        loadData();
        scheduler.schedule(() -> updateData(database), PERIOD_OF_SAVING, TimeUnit.SECONDS);
    }

    private void loadData() {
        if (!Files.exists(filePath)) {
            return;
        }

        try (var bufferedReader = Files.newBufferedReader(filePath)) {

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                Account account = Account.fromCSV(line);
                database.add(account);
            }

        } catch (IOException e) {
            throw new UncheckedIOException("An error has occurred while loading from file", e);
        }
    }

    public void updateData(Set<Account> accounts) {
        database.addAll(accounts);
        saveData(database);
        scheduler.schedule(() -> updateData(database), PERIOD_OF_SAVING, TimeUnit.SECONDS);
    }

    private void saveData(Set<Account> database) {
        try {
            StringBuilder result = new StringBuilder();

            for (Account acc : database) {
                result.append(acc.toCSV()).append(NEW_LINE);
            }

            if (!Files.exists(filePath.getParent())) {
                Files.createDirectory(filePath.getParent());
            }
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }

            Files.write(filePath, result.toString().getBytes(StandardCharsets.UTF_8));

        } catch (IOException e) {
            throw new UncheckedIOException("An error has occurred while saving to file", e);
        }
    }

    public void shutdownScheduler(Set<Account> accounts) {
        scheduler.shutdownNow();
        saveData(accounts);
    }
}
