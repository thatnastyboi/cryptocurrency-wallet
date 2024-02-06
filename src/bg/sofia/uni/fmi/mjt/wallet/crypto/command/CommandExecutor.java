package bg.sofia.uni.fmi.mjt.wallet.crypto.command;

import bg.sofia.uni.fmi.mjt.wallet.crypto.account.Account;
import bg.sofia.uni.fmi.mjt.wallet.crypto.database.Database;
import bg.sofia.uni.fmi.mjt.wallet.crypto.database.ServerLogger;
import bg.sofia.uni.fmi.mjt.wallet.crypto.response.ApiCall;

import java.net.http.HttpClient;
import java.nio.channels.SelectionKey;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommandExecutor {
    private static final String OUTPUT_DIRECTORY = "database";
    private static final String DATABASE_FILE_NAME = "accounts.txt";
    private static final String LOG_PATH = "server.log";
    private static final Path FILE_PATH = Paths.get(OUTPUT_DIRECTORY, DATABASE_FILE_NAME);

    private Set<Account> accounts = new HashSet<>();
    private ApiCall apiCall;
    private Database database;
    private ServerLogger logger;
    private static Set<Account> currentlyUsedAccounts = new HashSet<>();

    public CommandExecutor(String apiKey) {
        this.database = new Database(FILE_PATH);
        this.logger = new ServerLogger(LOG_PATH);
        this.accounts.addAll(database.getDatabase());
        this.apiCall = new ApiCall(HttpClient.newBuilder().build(), apiKey);
    }

    public CommandExecutor(String apiKey, Database database, ApiCall apiCall) {
        this.database = database;
        this.accounts.addAll(database.getDatabase());
        this.apiCall = apiCall;
    }

    public String execute(Command command, SelectionKey key) {
        return switch (command.command()) {
            case HELP -> help();
            case LOGIN -> login(command.arguments(), key);
            case REGISTER -> register(command.arguments(), key);
            case DISCONNECT -> disconnect(key);
            case DEPOSIT_MONEY -> depositMoney(command.arguments(), key);
            case LIST_OFFERINGS -> listOfferings(key);
            case BUY -> buy(command.arguments(), key);
            case SELL -> sell(command.arguments(), key);
            case GET_WALLET_SUMMARY -> getWalletSummary(key);
            case GET_WALLET_OVERALL_SUMMARY -> getWalletOverallSummary(key);
            case LOGOUT -> logout(key);
            case SHUTDOWN -> shutdown();
            default -> UNKNOWN_COMMAND;
        };
    }

    private String help() {
        StringBuilder result = new StringBuilder();

        result.append(HELP_MESSAGE_COMMANDS).append(NEW_LINE)
            .append(MESSAGE_SEPARATOR).append(NEW_LINE)
            .append(LOGIN).append(HELP_LOGIN_MESSAGE).append(NEW_LINE)
            .append(REGISTER).append(HELP_REGISTER_MESSAGE).append(NEW_LINE)
            .append(DISCONNECT).append(HELP_DISCONNECT_MESSAGE).append(NEW_LINE)
            .append(SHUTDOWN).append(HELP_SHUTDOWN_MESSAGE).append(NEW_LINE)
            .append(MESSAGE_SEPARATOR).append(NEW_LINE)
            .append(HELP_MESSAGE_LOGGED_IN_COMMANDS).append(NEW_LINE)
            .append(MESSAGE_SEPARATOR).append(NEW_LINE)
            .append(DEPOSIT_MONEY).append(HELP_DEPOSIT_MONEY_MESSAGE).append(NEW_LINE)
            .append(LIST_OFFERINGS).append(HELP_LIST_OFFERINGS_MESSAGE).append(NEW_LINE)
            .append(BUY).append(HELP_BUY_MESSAGE).append(NEW_LINE)
            .append(SELL).append(HELP_SELL_MESSAGE).append(NEW_LINE)
            .append(LOGOUT).append(HELP_LOGOUT_MESSAGE).append(NEW_LINE)
            .append(GET_WALLET_SUMMARY).append(HELP_GET_WALLET_SUMMARY_MESSAGE).append(NEW_LINE)
            .append(GET_WALLET_OVERALL_SUMMARY).append(HELP_GET_WALLET_OVERALL_SUMMARY_MESSAGE);

        return result.toString();
    }

    private String register(String[] accountData, SelectionKey key) {
        if (key.attachment() != null) {
            return ALREADY_LOGGED_IN_MESSAGE;
        }
        if (accountData.length != NUMBER_OF_ARGS_BUY_LOGIN_REGISTER) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, REGISTER, 2, REGISTER + HELP_REGISTER_MESSAGE);
        }

        String username = accountData[0];
        String password = accountData[1];

        Account current = findAccount(username);

        if (database.getDatabase().contains(current)) {
            return ACCOUNT_WITH_SUCH_USERNAME_ALREADY_EXISTS;
        }

        Account newAccount = Account.register(username, password);
        accounts.add(newAccount);
        database.updateData(accounts);

        return REGISTERED_SUCCESSFULLY;
    }

    private String login(String[] accountData, SelectionKey key) {
        if (accountData.length != NUMBER_OF_ARGS_BUY_LOGIN_REGISTER) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, LOGIN, 2, LOGIN + HELP_LOGIN_MESSAGE);
        }
        if (key.attachment() != null) {
            return ALREADY_LOGGED_IN_MESSAGE;
        }

        String username = accountData[0];
        String password = accountData[1];

        Account current = findAccount(username);

        if (!database.getDatabase().contains(current)) {
            return ACCOUNT_DOES_NOT_EXIST_MESSAGE;
        }
        if (currentlyUsedAccounts.contains(current)) {
            return ACCOUNT_ALREADY_IN_USAGE;
        }
        if (!current.passwordsMatch(password)) {
            return WRONG_PASSWORD_MESSAGE;
        }

        key.attach(current);
        currentlyUsedAccounts.add(current);

        return LOGGED_IN_SUCCESSFULLY;
    }

    private Account findAccount(String username) {
        if (username == null) {
            return null;
        }

        for (Account acc : database.getDatabase()) {
            if (username.equals(acc.getUsername())) {
                return acc;
            }
        }

        return null;
    }

    private String depositMoney(String[] moneyDeposit, SelectionKey key) {
        if (key.attachment() == null) {
            return NOT_LOGGED_IN_MESSAGE;
        }
        if (moneyDeposit.length != NUMBER_OF_ARGS_DEPOSIT_SELL) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, DEPOSIT_MONEY, 1,
                DEPOSIT_MONEY + HELP_DEPOSIT_MONEY_MESSAGE);
        }

        Account current = (Account) key.attachment();
        double amount = Double.parseDouble(moneyDeposit[0]);

        current.getWallet().depositMoney(amount);

        return DEPOSITED_SUCCESSFULLY;
    }

    private String listOfferings(SelectionKey key) {
        if (key.attachment() == null) {
            return NOT_LOGGED_IN_MESSAGE;
        }

        StringBuilder result = new StringBuilder();

        try {
            Map<String, Double> map = apiCall.getMarketChart();
            for (var entry : map.entrySet()) {
                result.append(String.format("%-6s %10.4f", entry.getKey(), entry.getValue())).append(NEW_LINE);
            }

        } catch (RuntimeException e) {
            logger.logError(FAILED_REQUEST_MESSAGE, e.getStackTrace());
            return FAILED_REQUEST_MESSAGE;
        }

        return result.toString();
    }

    private String buy(String[] buyData, SelectionKey key) {
        if (key.attachment() == null) {
            return NOT_LOGGED_IN_MESSAGE;
        }
        if (buyData.length != NUMBER_OF_ARGS_BUY_LOGIN_REGISTER) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, BUY, 2, BUY + HELP_BUY_MESSAGE);
        }

        String cryptoCode = buyData[0];
        Double moneyAmount = Double.parseDouble(buyData[1]);

        if (moneyAmount <= 0) {
            return INVALID_MONEY_AMOUNT;
        }
        if (!apiCall.getMarketChart().containsKey(cryptoCode)) {
            return ASSET_DOES_NOT_EXIST;
        }

        Account current = (Account) key.attachment();

        if (!(Double.compare(moneyAmount, current.getWallet().getBalance()) < 1)) {
            return INSUFFICIENT_BALANCE_MESSAGE;
        }

        double pricePerOne = apiCall.getMarketChart().get(cryptoCode);
        current.getWallet().buyCrypto(cryptoCode, moneyAmount, pricePerOne);

        return String.format(ASSET_PURCHASED_SUCCESSFULLY, cryptoCode, moneyAmount);
    }

    private String sell(String[] sellData, SelectionKey key) {
        if (key.attachment() == null) {
            return NOT_LOGGED_IN_MESSAGE;
        }
        if (sellData.length != NUMBER_OF_ARGS_DEPOSIT_SELL) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, SELL, 1, SELL + HELP_SELL_MESSAGE);
        }

        String cryptoCode = sellData[0];

        if (!apiCall.getMarketChart().containsKey(cryptoCode)) {
            return ASSET_DOES_NOT_EXIST;
        }

        Account current = (Account) key.attachment();

        if (!current.getWallet().getCryptoInWallet().containsKey(cryptoCode)) {
            return ASSET_NOT_IN_POSSESSION;
        }

        double pricePerOne = apiCall.getMarketChart().get(cryptoCode);

        double soldFor = current.getWallet().sellCrypto(cryptoCode, pricePerOne);

        return String.format(ASSET_SOLD_SUCCESSFULLY, cryptoCode, soldFor);
    }

    private String getWalletSummary(SelectionKey key) {
        if (key.attachment() == null) {
            return NOT_LOGGED_IN_MESSAGE;
        }

        Account current = (Account) key.attachment();

        return current.getWallet().getWalletInformation().trim();
    }

    private String getWalletOverallSummary(SelectionKey key) {
        if (key.attachment() == null) {
            return NOT_LOGGED_IN_MESSAGE;
        }

        Account current = (Account) key.attachment();

        double currentAccountBalance = current.getWallet().getBalance();
        Map<String, Double> currentAccountTotalDeposited = current.getWallet().getTotalDepositedInCrypto();
        Map<String, Double> currentAccountCryptoMap = current.getWallet().getCryptoInWallet();

        double moneyInCrypto = 0.0;
        double moneyDepositedInCrypto = 0.0;

        for (var entry : currentAccountCryptoMap.entrySet()) {
            moneyInCrypto += (apiCall.getMarketChart().get(entry.getKey()) * (entry.getValue()));
        }

        for (var entry : currentAccountTotalDeposited.entrySet()) {
            moneyDepositedInCrypto += entry.getValue();
        }

        StringBuilder result = new StringBuilder();

        double totalWinnings = currentAccountBalance - moneyDepositedInCrypto + moneyInCrypto;

        result.append("Total winnings/losses: ").append(String.format("%.2f", totalWinnings)).append(NEW_LINE);

        return result.toString().trim();
    }

    private String logout(SelectionKey key) {
        if (key.attachment() == null) {
            return NOT_LOGGED_IN_MESSAGE;
        }

        currentlyUsedAccounts.remove((Account) key.attachment());
        key.attach(null);

        return LOGGED_OUT_SUCCESSFULLY;
    }

    private String disconnect(SelectionKey key) {
        if (key == null || key.attachment() == null) {
            return DISCONNECTED_SUCCESSFULLY;
        }

        Account account = (Account) key.attachment();
        currentlyUsedAccounts.remove(account);
        key.attach(null);

        return disconnect(key);
    }

    private String shutdown() {
        apiCall.shutdownScheduler();
        database.shutdownScheduler(accounts);

        return SHUTTING_DOWN_MESSAGE;
    }

    private static final String HELP = "help";
    private static final String REGISTER = "register";
    private static final String LOGIN = "login";
    private static final String LOGOUT = "logout";
    private static final String DISCONNECT = "disconnect";
    private static final String SHUTDOWN = "shutdown";
    private static final String DEPOSIT_MONEY = "deposit-money";
    private static final String LIST_OFFERINGS = "list-offerings";
    private static final String BUY = "buy";
    private static final String SELL = "sell";
    private static final String GET_WALLET_SUMMARY = "get-wallet-summary";
    private static final String GET_WALLET_OVERALL_SUMMARY = "get-wallet-overall-summary";
    private static final String UNKNOWN_COMMAND = "unknown command";
    private static final String HELP_MESSAGE_COMMANDS = "LIST OF COMMANDS:";
    private static final String HELP_LOGIN_MESSAGE = " <username> <password>: logs in with existing account";
    private static final String HELP_LOGOUT_MESSAGE = " : logs out of account";
    private static final String HELP_REGISTER_MESSAGE = " <username> <password>: creates new account";
    private static final String HELP_MESSAGE_LOGGED_IN_COMMANDS =
        "THE FOLLOWING COMMANDS REQUIRE YOU TO BE LOGGED IN:";
    private static final String HELP_DEPOSIT_MONEY_MESSAGE = " <amount>: deposits amount in wallet";
    private static final String HELP_LIST_OFFERINGS_MESSAGE = ": lists all assets available for purchase";
    private static final String HELP_BUY_MESSAGE = " <asset_id> <amount>: buy asset for desired quantity of money";
    private static final String HELP_SELL_MESSAGE = " <asset_id>: sell asset";
    private static final String HELP_GET_WALLET_SUMMARY_MESSAGE =
        ": gives information about wallet, namely current balance and currently possessed assets";
    private static final String HELP_GET_WALLET_OVERALL_SUMMARY_MESSAGE =
        ": gives information about total winnings/losses from investments";
    private static final String HELP_DISCONNECT_MESSAGE =
        ": disconnects you from the server";
    private static final String HELP_SHUTDOWN_MESSAGE =
        ": shuts down the server, disconnecting all other connected accounts";
    private static final String MESSAGE_SEPARATOR =
        "--------------------------------------------------------------------------------------------------------";
    private static final String NEW_LINE = System.lineSeparator();
    private static final String INVALID_ARGS_COUNT_MESSAGE_FORMAT =
        "Invalid count of arguments: \"%s\" expects %d arguments. Example: \"%s\"";
    public static final int NUMBER_OF_ARGS_DEPOSIT_SELL = 1;
    public static final int NUMBER_OF_ARGS_BUY_LOGIN_REGISTER = 2;
    private static final String ALREADY_LOGGED_IN_MESSAGE =
        "You are already logged in, log out before using this command";
    private static final String NOT_LOGGED_IN_MESSAGE =
        "You need to log in to use this command";
    private static final String ACCOUNT_ALREADY_IN_USAGE =
        "This account is already logged in elsewhere";
    private static final String ACCOUNT_DOES_NOT_EXIST_MESSAGE =
        "Account with such username does not exist, try registering it first";
    private static final String ACCOUNT_WITH_SUCH_USERNAME_ALREADY_EXISTS =
        "Account with such username already exists";
    private static final String FAILED_REQUEST_MESSAGE =
        "An error has occurred when requesting from API";
    private static final String INVALID_MONEY_AMOUNT =
        "Amount of money must be positive";
    private static final String INSUFFICIENT_BALANCE_MESSAGE =
        "Insufficient balance in account wallet";
    private static final String ASSET_DOES_NOT_EXIST =
        "You are trying to buy an asset that does not exist";
    private static final String ASSET_NOT_IN_POSSESSION =
        "You are trying to sell an asset that you do not possess";
    private static final String WRONG_PASSWORD_MESSAGE =
        "Wrong password";
    private static final String SHUTTING_DOWN_MESSAGE =
        "Server was shut down";
    private static final String LOGGED_IN_SUCCESSFULLY =
        "Logged in successfully";
    private static final String LOGGED_OUT_SUCCESSFULLY =
        "Logged out successfully";
    private static final String DISCONNECTED_SUCCESSFULLY =
        "Disconnected successfully";
    private static final String DEPOSITED_SUCCESSFULLY =
        "Deposited successfully";
    private static final String REGISTERED_SUCCESSFULLY =
        "Registered successfully";
    private static final String ASSET_PURCHASED_SUCCESSFULLY =
        "Successfully purchased \"%s\" for \"%s\" dollars";
    private static final String ASSET_SOLD_SUCCESSFULLY =
        "Successfully sold \"%s\" for \"%s\" dollars";
    private static final String NOT_YET_IMPLEMENTED =
        "Implementation not yet made";
}
