package com.yourbank.util;

import org.mindrot.jbcrypt.BCrypt;
import java.security.SecureRandom;

public class SecurityUtil {


    private static final SecureRandom random = new SecureRandom();


    public static String hashPassword(String plainTextPassword) {

        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }


    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (Exception e) {

            return false;
        }
    }


    public static String generateOtp() {
        int otpNumber = 100000 + random.nextInt(900000);
        return String.valueOf(otpNumber);
    }
}