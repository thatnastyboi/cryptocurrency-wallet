package bg.sofia.uni.fmi.mjt.wallet.crypto.command;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CommandCreatorTest {

    @Test
    void testNewCommand() {
        String clientInput = "login test1 test";
        Command command = CommandCreator.newCommand(clientInput);

        String actualCommandType = command.command();

        assertEquals("login", actualCommandType,
            "Command should be of type login but is " + actualCommandType);
    }

    @Test
    void testNewCommandWithQuotes() {
        String clientInput = "register \"test1\" \"test\"";
        Command command = CommandCreator.newCommand(clientInput);

        String actualCommandType = command.command();

        assertEquals("register", actualCommandType,
            "Command should be of type register but is " + actualCommandType);
    }

    @Test
    void testNewCommandArguments() {
        String clientInput = "deposit-money 500";
        Command command = CommandCreator.newCommand(clientInput);

        List<String> tokens = Arrays.asList(command.arguments());

        assertEquals(1, tokens.size(),
            "Expected command with 1 argument, but is with " + tokens.size() + " arguments instead");
    }

    @Test
    void testNewCommandWithIncorrectInput() {
        String clientInput = "buy \"BTC 50";
        Command command = CommandCreator.newCommand(clientInput);

        List<String> tokens = Arrays.asList(command.arguments());

        assertNotEquals(2, tokens.size(),
            "Expected command with 1 argument, but is with " + tokens.size() + " arguments instead");
    }
}
