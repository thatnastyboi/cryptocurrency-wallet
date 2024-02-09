package bg.sofia.uni.fmi.mjt.wallet.crypto.account;

import java.util.Objects;

public class Account {

    private final String username;
    private final String password;
    private final Wallet wallet;
    private static final String VALID_PASSWORD = "Password is valid";
    private static final String DELIMITER = ";";

    public static Account register(String username, String password) {
        return new Account(username, password);
    }

    public Account(String username, String password) {
        this.username = username;
        this.password = PasswordHasher.hashString(password);
        this.wallet = new Wallet();
    }

    private Account(String username, String password, Wallet wallet) {
        this.username = username;
        this.password = password;
        this.wallet = wallet;
    }

    public String validatePassword(String password) {
        String result = PasswordChecker.validatePassword(password);

        if (!result.isBlank()) {
            return result;
        }

        return VALID_PASSWORD;
    }

    public String getUsername() {
        return this.username;
    }

    public Wallet getWallet() {
        return this.wallet;
    }

    public boolean passwordsMatch(String password) {
        if (password == null) {
            throw new IllegalArgumentException();
        }

        return PasswordHasher.checkpw(password, this.password);
    }

    public static Account fromCSV(String line) {
        String[] tokens = line.split(DELIMITER);

        return new Account(tokens[0], tokens[1], Wallet.fromCSV(tokens));
    }

    public String toCSV() {
        StringBuilder result = new StringBuilder();

        result.append(username).append(DELIMITER)
            .append(password);

        return result.append(wallet.toCSV()).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(username, account.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}