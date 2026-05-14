package com.services.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String rawPassword = "FleetMaster123";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("HASH_START:" + encodedPassword + ":HASH_END");
    }
}
