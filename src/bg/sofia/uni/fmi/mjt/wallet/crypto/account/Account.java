package bg.sofia.uni.fmi.mjt.wallet.crypto.account;

import java.util.Objects;

public class Account {

    private final String username;
    private String password;
    private final Wallet wallet;
    private boolean isAdmin;
    private static final String VALID_PASSWORD = "Password is valid";
    private static final String DELIMITER = ";";

    public static Account register(String username, String password) {
        return new Account(username, password);
    }

    public Account(String username, String password) {
        this.username = username;
        this.password = PasswordHasher.hashString(password);
        this.wallet = new Wallet();
        this.isAdmin = false;
    }

    private Account(String username, String password, Wallet wallet, boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.wallet = wallet;
        this.isAdmin = isAdmin;
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

    public boolean getAdminStatus() {
        return this.isAdmin;
    }

    public void setPassword(String newPass) {
        this.password = newPass;
    }

    public void changeAdminStatus() {
        isAdmin = !isAdmin;
    }

    public boolean passwordsMatch(String password) {
        if (password == null) {
            throw new IllegalArgumentException();
        }

        return PasswordHasher.checkpw(password, this.password);
    }

    public static Account fromCSV(String line) {
        String[] tokens = line.split(DELIMITER);

        boolean isAdmin = (Integer.parseInt(tokens[0])) != 0;

        return new Account(tokens[1], tokens[2], Wallet.fromCSV(tokens), isAdmin);
    }

    public String toCSV() {
        StringBuilder result = new StringBuilder();

        int adminValue = isAdmin ? 1 : 0;

        result.append(adminValue).append(DELIMITER)
            .append(username).append(DELIMITER)
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