package com.studio.api.domain.member.client;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class NaverSignatureGenerator {

    public static String generate(String clientId, String clientSecret, long timestamp) {
        String password = clientId + "_" + timestamp;
        String hashed = BCrypt.hashpw(password, clientSecret);
        return Base64.getEncoder().encodeToString(hashed.getBytes(StandardCharsets.UTF_8));
    }
}
