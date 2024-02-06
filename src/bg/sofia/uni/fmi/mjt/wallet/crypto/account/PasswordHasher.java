package bg.sofia.uni.fmi.mjt.wallet.crypto.account;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHasher {

    private static final String HASHING_ALGORITHM = "SHA-256";

    public static String hashString(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASHING_ALGORITHM);

            md.update(password.getBytes());

            byte[] hashedBytes = md.digest();

            StringBuilder result = new StringBuilder();
            for (byte b : hashedBytes) {
                result.append(String.format("%02x", b));
            }

            return result.toString();
        } catch (NoSuchAlgorithmException e) {

            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static boolean checkpw(String toCheck, String hashed) {
        String toCheckHashed = hashString(toCheck);

        return hashed.equals(toCheckHashed);
    }
}
