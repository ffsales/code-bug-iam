package br.com.sales.code.bug.iam.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class PasswordHasherDigest implements PasswordHasher {

    private static final String ALGORITHM_256= "SHA-256";

    public String hash(String pass) {

        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM_256);
            byte[] encodedHash = digest.digest(pass.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
