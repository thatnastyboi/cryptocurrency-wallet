package bg.sofia.uni.fmi.mjt.wallet.crypto.account;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Wallet {
    private static final int BALANCE_INDEX = 2;
    private static final int FIRST_CRYPTO_INDEX = 3;
    private static final int WALLET_FROM_CSV_STEP = 3;
    private static final String NEW_LINE = System.lineSeparator();

    private double balance;
    private Map<String, Double> totalDepositedInCrypto;
    private Map<String, Double> cryptoMap;
    private static final String DELIMITER = ";";

    public Wallet() {
        this.balance = 0.0;
        this.totalDepositedInCrypto = new HashMap<>();
        this.cryptoMap = new HashMap<>();
    }

    private Wallet(double balance, Map<String, Double> totalDepositedInCrypto, Map<String, Double> cryptoMap) {
        this.balance = balance;
        this.totalDepositedInCrypto = totalDepositedInCrypto;
        this.cryptoMap = cryptoMap;
    }

    public static Wallet fromCSV(String[] tokens) {
        Map<String, Double> putInWallet = new HashMap<>();
        Map<String, Double> putInWalletDeposited = new HashMap<>();

        for (int i = FIRST_CRYPTO_INDEX; i < tokens.length; i += WALLET_FROM_CSV_STEP) {
            putInWallet.put(tokens[i], Double.parseDouble(tokens[i + 1]));
            putInWalletDeposited.put(tokens[i], Double.parseDouble(tokens[i + 2]));
        }

        double newBalance = Double.parseDouble(tokens[BALANCE_INDEX]);

        return new Wallet(newBalance, putInWalletDeposited, putInWallet);
    }

    public String toCSV() {
        StringBuilder result = new StringBuilder();

        result.append(DELIMITER).append(balance);

        for (var entry : cryptoMap.entrySet()) {
            result.append(DELIMITER).append(entry.getKey()).append(DELIMITER).append(entry.getValue())
                .append(DELIMITER).append(totalDepositedInCrypto.get(entry.getKey()));
        }

        return result.toString();
    }

    public double getBalance() {
        return this.balance;
    }

    public Map<String, Double> getTotalDepositedInCrypto() {
        return Collections.unmodifiableMap(this.totalDepositedInCrypto);
    }

    public Map<String, Double> getCryptoInWallet() {
        return Collections.unmodifiableMap(this.cryptoMap);
    }

    public void depositMoney(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be bigger than zero");
        }

        balance += amount;
    }

    public void buyCrypto(String cryptoCode, double money, double pricePerOne) {
        balance -= money;

        cryptoMap.merge(cryptoCode,
            money / pricePerOne, Double::sum);
        totalDepositedInCrypto.merge(cryptoCode, money, Double::sum);
    }

    public double sellCrypto(String cryptoCode, double pricePerOne) {
        double soldFor = cryptoMap.get(cryptoCode) * pricePerOne;

        balance += soldFor;

        cryptoMap.remove(cryptoCode);
        totalDepositedInCrypto.remove(cryptoCode);

        return soldFor;
    }

    public String getWalletInformation() {
        StringBuilder result = new StringBuilder();

        result.append("Current balance: ").append(String.format("%.2f", balance)).append(NEW_LINE);

        for (var entry : cryptoMap.entrySet()) {
            result.append(String.format("%-6s %-10.2f", entry.getKey(), entry.getValue())).append(NEW_LINE);
        }

        return result.toString();
    }
}