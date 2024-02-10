package bg.sofia.uni.fmi.mjt.wallet.crypto.command;

import bg.sofia.uni.fmi.mjt.wallet.crypto.account.Account;
import bg.sofia.uni.fmi.mjt.wallet.crypto.database.Database;
import bg.sofia.uni.fmi.mjt.wallet.crypto.exception.FailedRequestException;
import bg.sofia.uni.fmi.mjt.wallet.crypto.response.ApiCall;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CommandExecutorTest {

    private static final String DUMMY_API_KEY = "dummy-api-key";
    @Mock
    private ApiCall mockApiCall = mock(ApiCall.class);
    @Mock
    private Database mockDatabase = mock(Database.class);
    @Mock
    private SelectionKey mockKey = mock(SelectionKey.class);
    @InjectMocks
    private CommandExecutor executor;

    @BeforeEach
    void setUp() {
        Mockito.reset(mockApiCall, mockDatabase, mockKey);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoginSuccessful() {
        String testInput = "login test1 test";
        Account account = new Account("test1", "test");

        when(mockDatabase.getDatabase()).thenReturn(Set.of(account));

        assertNull(mockKey.attachment());

        Command testLoginCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testLoginCommand, mockKey);

        assertEquals("Logged in successfully", result,
            "Expected successful login message but was " + result);
    }

    @Test
    void testLoginWithInvalidNumberOfArgs() {
        String testInput = "login test1 test2 test";

        Command testLoginCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testLoginCommand, mockKey);

        assertTrue(result.contains("Invalid count of arguments"),
            "Expected invalid count of arguments message but was " + result);
        assertNull(mockKey.attachment());
    }

    @Test
    void testLoginWhileAlreadyLoggedIn() {
        String testInput = "login test2 test";
        Account account = new Account("test1", "test");

        when(mockKey.attachment()).thenReturn(account);

        Command testLoginCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testLoginCommand, mockKey);

        assertEquals("You are already logged in, log out before using this command", result,
            "Expected already logged in message but was " + result);
    }

    @Test
    void testLoginWithUnexistingAccount() {
        String testInput = "login test2 test1";

        when(mockDatabase.getDatabase()).thenReturn(new HashSet<>());
        when(mockKey.attachment()).thenReturn(null);

        Command testLoginCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testLoginCommand, mockKey);

        assertTrue(result.contains("does not exist"),
            "Expected account does not exist message but was " + result);
    }

    @Test
    void testRegisterSuccessful() {
        String testInput = "register test test1";

        doNothing().when(mockDatabase).updateData(any());

        assertNull(mockKey.attachment());

        Command testRegisterCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testRegisterCommand, mockKey);

        assertEquals("Registered successfully", result,
            "Expected successful registration message but was " + result);
    }

    @Test
    void testRegisterWithInvalidNumberOfArgs() {
        String testInput = "register test1 test2 test1";

        Command testRegisterCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testRegisterCommand, mockKey);

        assertTrue(result.contains("Invalid count of arguments"),
            "Expected invalid count of arguments message but was " + result);
        assertNull(mockKey.attachment());
    }

    @Test
    void testRegisterWhileLoggedIn() {
        String testInput = "register test2 test1";
        Account account = new Account("test1", "test1");

        when(mockKey.attachment()).thenReturn(account);

        Command testRegisterCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testRegisterCommand, mockKey);

        assertEquals("You are already logged in, log out before using this command", result,
            "Expected already logged in message during registration but was " + result);
        assertNotNull(mockKey.attachment());
    }

    @Test
    void testRegisterWithAlreadyExistingUsername() {
        String testInput = "register test1 test1";
        Account account = new Account("test1", "test1");

        when(mockDatabase.getDatabase()).thenReturn(Set.of(account));

        Command testRegisterCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testRegisterCommand, mockKey);

        assertEquals("Account with such username already exists", result,
            "Expected account already exists message but was " + result);
    }

    @Test
    void testRegisterWithInvalidPassword() {
        String testInput = "register test1 test";

        doNothing().when(mockDatabase).updateData(any());

        assertNull(mockKey.attachment());

        Command testRegisterCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testRegisterCommand, mockKey);

        assertTrue(result.contains("try again"),
            "Expected invalid password message but was " + result);
    }

    @Test
    void testHelpMessage() {
        String testInput = "help";

        Command testHelpCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testHelpCommand, mockKey);

        assertTrue(result.contains("THE FOLLOWING COMMANDS REQUIRE YOU TO BE LOGGED IN"),
            "Expected help message but was " + result);
    }

    @Test
    void testDepositMoney() {
        String testInput = "deposit-money 50";
        Account account = new Account("test1", "test");

        when(mockKey.attachment()).thenReturn(account);

        Command testDepositCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testDepositCommand, mockKey);

        assertEquals("Deposited successfully", result,
            "Expected successful deposit message but was " + result);
    }

    @Test
    void testDepositNegativeAmountMoney() {
        String testInput = "deposit-money -1";
        Account account = new Account("test1", "test");

        when(mockKey.attachment()).thenReturn(account);

        Command testDepositCommand = CommandCreator.newCommand(testInput);

        assertThrows(IllegalArgumentException.class,
            () -> executor.execute(testDepositCommand, mockKey),
            "Expected IllegalArgumentException but was not thrown");
    }

    @Test
    void testDepositNoMoney() {
        String testInput = "deposit-money 0";
        Account account = new Account("test1", "test");

        when(mockKey.attachment()).thenReturn(account);

        Command testDepositCommand = CommandCreator.newCommand(testInput);

        assertThrows(IllegalArgumentException.class,
            () -> executor.execute(testDepositCommand, mockKey),
            "Expected IllegalArgumentException but was not thrown");
    }

    @Test
    void testDepositWithInvalidNumberOfArgs() {
        String testInput = "deposit-money 1 1";
        Account account = new Account("test1", "test");

        when(mockKey.attachment()).thenReturn(account);

        Command testDepositCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testDepositCommand, mockKey);

        assertTrue(result.contains("Invalid count of arguments"),
            "Expected invalid count of arguments message but was " + result);
    }

    @Test
    void testListOfferings() throws FailedRequestException {
        String testInput = "list-offerings";
        Account account = new Account("0;test1", "test");

        when(mockKey.attachment()).thenReturn(account);

        Map<String, Double> dummyMap = new HashMap<>();
        dummyMap.put("DUMMY", 0.001);
        dummyMap.put("MUMMY", 0.002);
        dummyMap.put("TUMMY", 1242.222);

        when(mockApiCall.getMarketChart()).thenReturn(dummyMap);

        Command testListOfferingsCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testListOfferingsCommand, mockKey);

        assertTrue(result.contains("DUMMY"),
            "Expected entry DUMMY but was not there");
        verify(mockApiCall, times(1)).getMarketChart();
    }

    @Test
    void testBuySuccessfully() throws FailedRequestException {
        String testInput = "buy DUMMY 50";
        Account account = Account.fromCSV("0;test1;test;60.0");

        when(mockKey.attachment()).thenReturn(account);

        Map<String, Double> dummyMap = new HashMap<>();
        dummyMap.put("DUMMY", 0.001);
        dummyMap.put("MUMMY", 0.002);
        dummyMap.put("TUMMY", 1242.222);

        when(mockApiCall.getMarketChart()).thenReturn(dummyMap);

        Command testBuyCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testBuyCommand, mockKey);

        assertTrue(result.contains("Successfully purchased"),
            "Expected successfully purchased message but was" + result);
    }

    @Test
    void testBuyForInvalidAmountOfMoney() throws FailedRequestException {
        String testInput = "buy DUMMY -1";
        Account account = Account.fromCSV("0;test1;test;60.0");

        when(mockKey.attachment()).thenReturn(account);

        Map<String, Double> dummyMap = new HashMap<>();
        dummyMap.put("DUMMY", 0.001);
        dummyMap.put("MUMMY", 0.002);
        dummyMap.put("TUMMY", 1242.222);

        when(mockApiCall.getMarketChart()).thenReturn(dummyMap);

        Command testBuyCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testBuyCommand, mockKey);

        assertEquals("Amount of money must be positive", result,
            "Expected invalid amount of money message but was " + result);
    }

    @Test
    void testBuyForInvalidAsset() throws FailedRequestException {
        String testInput = "buy BUMPY 50";
        Account account = Account.fromCSV("0;test1;test;60.0");

        when(mockKey.attachment()).thenReturn(account);

        Map<String, Double> dummyMap = new HashMap<>();
        dummyMap.put("DUMMY", 0.001);
        dummyMap.put("MUMMY", 0.002);
        dummyMap.put("TUMMY", 1242.222);

        when(mockApiCall.getMarketChart()).thenReturn(dummyMap);

        Command testBuyCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testBuyCommand, mockKey);

        assertEquals("You are trying to buy an asset that does not exist", result,
            "Expected invalid asset message but was " + result);
    }

    @Test
    void testBuyWithInsufficientBalance() throws FailedRequestException {
        String testInput = "buy DUMMY 50";
        Account account = Account.fromCSV("0;test1;test;40.0");

        when(mockKey.attachment()).thenReturn(account);

        Map<String, Double> dummyMap = new HashMap<>();
        dummyMap.put("DUMMY", 0.001);
        dummyMap.put("MUMMY", 0.002);
        dummyMap.put("TUMMY", 1242.222);

        when(mockApiCall.getMarketChart()).thenReturn(dummyMap);

        Command testBuyCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testBuyCommand, mockKey);

        assertEquals("Insufficient balance in account wallet", result,
            "Expected insufficient balance message but was " + result);
    }

    @Test
    void testBuyWhenNotLoggedIn() {
        String testInput = "buy DUMMY 50";

        when(mockKey.attachment()).thenReturn(null);

        Command testBuyCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testBuyCommand, mockKey);

        assertEquals("You need to log in to use this command", result,
            "Expected not logged in message but was " + result);
    }

    @Test
    void testBuyWithInvalidNumberOfArgs() {
        String testInput = "buy DUMMY 50 50";
        Account account = Account.fromCSV("0;test1;test;40.0");

        when(mockKey.attachment()).thenReturn(account);

        Command testBuyCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testBuyCommand, mockKey);

        assertTrue(result.contains("Invalid count of arguments"),
            "Expected invalid count of arguments message but was " + result);
    }

    @Test
    void testSellSuccessfully() throws FailedRequestException {
        String testInput = "sell DUMMY";
        Account account = Account.fromCSV("0;test1;test;40.0;DUMMY;1;1");

        when(mockKey.attachment()).thenReturn(account);

        Map<String, Double> dummyMap = new HashMap<>();
        dummyMap.put("DUMMY", 0.001);
        dummyMap.put("MUMMY", 0.002);
        dummyMap.put("TUMMY", 1242.222);

        when(mockApiCall.getMarketChart()).thenReturn(dummyMap);

        Command testSellCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testSellCommand, mockKey);

        assertTrue(result.contains("Successfully sold"),
            "Expected successfully sold message but was " + result);
    }

    @Test
    void testSellWhenNotLoggedIn() {
        String testInput = "sell DUMMY";

        when(mockKey.attachment()).thenReturn(null);

        Command testBuyCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testBuyCommand, mockKey);

        assertEquals("You need to log in to use this command", result,
            "Expected not logged in message but was " + result);
    }

    @Test
    void testSellWithInvalidNumberOfArgs() {
        String testInput = "sell DUMMY 50";
        Account account = Account.fromCSV("0;test1;test;40.0;DUMMY;1;1");

        when(mockKey.attachment()).thenReturn(account);

        Command testBuyCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testBuyCommand, mockKey);

        assertTrue(result.contains("Invalid count of arguments"),
            "Expected invalid count of arguments message but was " + result);
    }

    @Test
    void testSellInvalidAsset() throws FailedRequestException {
        String testInput = "sell BUMPY";
        Account account = Account.fromCSV("0;test1;test;40.0;DUMMY;1;1");

        when(mockKey.attachment()).thenReturn(account);

        Map<String, Double> dummyMap = new HashMap<>();
        dummyMap.put("DUMMY", 0.001);
        dummyMap.put("MUMMY", 0.002);
        dummyMap.put("TUMMY", 1242.222);

        when(mockApiCall.getMarketChart()).thenReturn(dummyMap);

        Command testBuyCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testBuyCommand, mockKey);

        assertEquals("You are trying to buy an asset that does not exist", result,
            "Expected invalid asset message but was " + result);
    }

    @Test
    void testSellNonPossessedAsset() throws FailedRequestException {
        String testInput = "sell DUMMY";
        Account account = Account.fromCSV("0;test1;test;40.0");

        when(mockKey.attachment()).thenReturn(account);

        Map<String, Double> dummyMap = new HashMap<>();
        dummyMap.put("DUMMY", 0.001);
        dummyMap.put("MUMMY", 0.002);
        dummyMap.put("TUMMY", 1242.222);

        when(mockApiCall.getMarketChart()).thenReturn(dummyMap);

        Command testBuyCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testBuyCommand, mockKey);

        assertEquals("You are trying to sell an asset that you do not possess", result,
            "Expected non-possessed asset message but was " + result);
    }

    @Test
    void testChangePassword() {
        String testInput = "change-password old1 new1";
        Account account = new Account("test1", "old1");

        when(mockKey.attachment()).thenReturn(account);

        Command testChangePasswordCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testChangePasswordCommand, mockKey);

        assertTrue(result.contains("changed"),
            "Expected password changed successfully message but was " + result);
    }

    @Test
    void testChangePasswordWithInvalidPassword() {
        String testInput = "change-password old1 new";
        Account account = new Account("test1", "old1");

        when(mockKey.attachment()).thenReturn(account);

        Command testChangePasswordCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testChangePasswordCommand, mockKey);

        assertTrue(result.contains("try again"),
            "Expected invalid password message but was " + result);
    }

    @Test
    void testGetWalletSummary() {
        String testInput = "get-wallet-summary";
        Account account = Account.fromCSV("0;test1;test;40.0;DUMMY;1;1");

        when(mockKey.attachment()).thenReturn(account);

        Command testWalletSummaryCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testWalletSummaryCommand, mockKey);

        assertTrue(result.contains("Current balance"),
            "Expected current balance info but was not present");
        assertTrue(result.contains("DUMMY"),
            "Expected asset info but was not present");
    }

    @Test
    void testGetWalletSummaryWhenNotLoggedIn() {
        String testInput = "get-wallet-summary";

        when(mockKey.attachment()).thenReturn(null);

        Command testWalletSummaryCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testWalletSummaryCommand, mockKey);

        assertEquals("You need to log in to use this command", result,
            "Expected not logged in message but was " + result);
    }

    @Test
    void testChangePasswordWithTheSamePassword() {
        String testInput = "change-password old1 old1";
        Account account = new Account("test1", "old1");

        when(mockKey.attachment()).thenReturn(account);

        Command testChangePasswordCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testChangePasswordCommand, mockKey);

        assertTrue(result.contains("same as the"),
            "Expected same password message but was " + result);
    }

    @Test
    void testChangePasswordWithTheWrongOldPassword() {
        String testInput = "change-password old2 new1";
        Account account = new Account("test1", "old1");

        when(mockKey.attachment()).thenReturn(account);

        Command testChangePasswordCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testChangePasswordCommand, mockKey);

        assertTrue(result.contains("Wrong password"),
            "Expected wrong password message but was " + result);
    }

    @Test
    void testChangePasswordWhenNotLoggedIn() {
        String testInput = "change-password old2 new1";

        when(mockKey.attachment()).thenReturn(null);

        Command testChangePasswordCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testChangePasswordCommand, mockKey);

        assertTrue(result.contains("You need to log in"),
            "Expected not logged in message but was " + result);
    }

    @Test
    void testGetWalletOverallSummary() throws FailedRequestException {
        String testInput = "get-wallet-overall-summary";
        Account account = Account.fromCSV("0;test1;test;40.0;DUMMY;1000;1");

        when(mockKey.attachment()).thenReturn(account);

        Map<String, Double> dummyMap = new HashMap<>();
        dummyMap.put("DUMMY", 0.001);
        dummyMap.put("MUMMY", 0.002);
        dummyMap.put("TUMMY", 1242.222);

        when(mockApiCall.getMarketChart()).thenReturn(dummyMap);

        Command testWalletSummaryCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testWalletSummaryCommand, mockKey);

        assertTrue(result.contains("Total winnings/losses"),
            "Expected total winnings/losses message but was " + result);
    }

    @Test
    void testGetWalletOverallSummaryWhenNotLoggedIn() {
        String testInput = "get-wallet-overall-summary";

        when(mockKey.attachment()).thenReturn(null);

        Command testWalletOverallSummaryCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testWalletOverallSummaryCommand, mockKey);

        assertEquals("You need to log in to use this command", result,
            "Expected not logged in message but was " + result);
    }

    @Test
    void testLogout() {
        String testInput = "logout";
        Account account = Account.fromCSV("0;test1;test;40.0");

        when(mockKey.attachment()).thenReturn(account);

        Command testLogoutCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testLogoutCommand, mockKey);

        assertEquals("Logged out successfully", result,
            "Expected logged out message but was " + result);
    }

    @Test
    void testLogoutWhenNotLoggedIn() {
        String testInput = "logout";

        when(mockKey.attachment()).thenReturn(null);

        Command testLogoutCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testLogoutCommand, mockKey);

        assertEquals("You need to log in to use this command", result,
            "Expected not logged in message but was " + result);
    }

    @Test
    void testDisconnectWhenLoggedIn() {
        String testInput = "disconnect";
        Account account = Account.fromCSV("0;test1;test;40.0");

        mockKey.attach(account);

        Command testDisconnectCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testDisconnectCommand, mockKey);

        assertEquals("Disconnected successfully", result,
            "Expected disconnected successfully message but was " + result);
    }

    @Test
    void testDisconnectWhenNotLoggedIn() {
        String testInput = "disconnect";

        when(mockKey.attachment()).thenReturn(null);

        Command testDisconnectCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testDisconnectCommand, mockKey);

        assertEquals("Disconnected successfully", result,
            "Expected disconnected successfully message but was " + result);
    }

    @Test
    void testMakeAdmin() {
        String testInput = "make-admin test2";
        Account accountAdmin = Account.fromCSV("1;test1;test1;40.0");
        Account accountToMakeAdmin = Account.fromCSV("0;test2;test1;20.0");

        Set<Account> database = new HashSet<>();
        database.add(accountAdmin);
        database.add(accountToMakeAdmin);

        when(mockDatabase.getDatabase()).thenReturn(database);
        when(mockKey.attachment()).thenReturn(accountAdmin);

        Command testMakeAdminCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testMakeAdminCommand, mockKey);

        assertTrue(result.contains("admin rights"),
            "Expected successfully made admin message but was " + result);
    }

    @Test
    void testMakeAdminWhenNotLoggedIn() {
        String testInput = "make-admin test2";

        when(mockKey.attachment()).thenReturn(null);

        Command testMakeAdminCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testMakeAdminCommand, mockKey);

        assertEquals("You need to log in to use this command", result,
            "Expected not logged in message but was " + result);
    }

    @Test
    void testMakeAdminWithInvalidNumberOfArgs() {
        String testInput = "make-admin test2 test2";
        Account accountAdmin = Account.fromCSV("1;test1;test1;40.0");

        when(mockKey.attachment()).thenReturn(accountAdmin);

        Command testMakeAdminCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testMakeAdminCommand, mockKey);

        assertTrue(result.contains("Invalid count of arguments"),
            "Expected invalid count of arguments message but was " + result);
    }

    @Test
    void testShutdown() {
        String testInput = "shutdown";
        Account account = Account.fromCSV("1;test1;test1;40.0");

        when(mockKey.attachment()).thenReturn(account);

        doNothing().when(mockApiCall).shutdownScheduler();
        doNothing().when(mockDatabase).shutdownScheduler(any());

        Command testShutdownCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testShutdownCommand, mockKey);

        assertTrue(result.contains("Server was shut down"),
            "Expected server shutdown message but was " + result);
    }

    @Test
    void testShutdownWithNonAdminAccount() {
        String testInput = "shutdown";
        Account account = Account.fromCSV("0;test1;test1;40.0");

        when(mockKey.attachment()).thenReturn(account);

        doNothing().when(mockApiCall).shutdownScheduler();
        doNothing().when(mockDatabase).shutdownScheduler(any());

        Command testShutdownCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testShutdownCommand, mockKey);

        assertTrue(result.contains("You don't have the rights"),
            "Expected non-admin account message but was " + result);
    }

    @Test
    void testShutdownWhenNotLoggedIn() {
        String testInput = "shutdown";

        when(mockKey.attachment()).thenReturn(null);

        doNothing().when(mockApiCall).shutdownScheduler();
        doNothing().when(mockDatabase).shutdownScheduler(any());

        Command testShutdownCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testShutdownCommand, mockKey);

        assertEquals("You need to log in to use this command", result,
            "Expected not logged in message but was " + result);
    }

    @Test
    void testUnknownCommand() {
        String testInput = "dummy";

        Command testUnknownCommand = CommandCreator.newCommand(testInput);
        String result = executor.execute(testUnknownCommand, mockKey);

        assertTrue(result.contains("Unknown command"),
            "Expected unknown command message but was " + result);
    }
}