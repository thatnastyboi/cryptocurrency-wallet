package bg.sofia.uni.fmi.mjt.wallet.crypto.account;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordChecker {

    private static final String PASSWORD_PATTERN_NUMBERS = ".*[0-9].*";
    private static final String PASSWORD_PATTERN_LETTERS = ".*[a-zA-Z].*";
    private static final String NO_NUMBERS_IN_PASSWORD =
        "You must have at least one number in password" + System.lineSeparator();
    private static final String NO_LETTERS_IN_PASSWORD =
        "You must have at least one letter in password" + System.lineSeparator();
    private static final String TRY_AGAIN_MESSAGE =
        "Please try again with a new password";

    public static String validatePassword(String password) {
        Matcher matcherNumbers = Pattern.compile(PASSWORD_PATTERN_NUMBERS).matcher(password);
        Matcher matcherLetters = Pattern.compile(PASSWORD_PATTERN_LETTERS).matcher(password);

        System.out.println(password);

        StringBuilder result = new StringBuilder();

        if (!matcherNumbers.matches()) {
            result.append(NO_NUMBERS_IN_PASSWORD);
        }

        if (!matcherLetters.matches()) {
            result.append(NO_LETTERS_IN_PASSWORD);
        }

        if (!result.isEmpty()) {
            result.append(TRY_AGAIN_MESSAGE);
        }

        return result.toString();
    }
}
