package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
    public static byte[] toHash(String toHash)  {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(toHash.getBytes(StandardCharsets.UTF_8));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
