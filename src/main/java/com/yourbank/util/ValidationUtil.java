package com.yourbank.util;

import java.util.regex.Pattern;

public class ValidationUtil {


    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE
    );


    private static final Pattern PAN_PATTERN = Pattern.compile(
            "[A-Z]{5}[0-9]{4}[A-Z]{1}"
    );


    private static final Pattern IFSC_PATTERN = Pattern.compile(
            "^[A-Z]{4}0[A-Z0-9]{6}$"
    );


    private static final Pattern AADHAR_PATTERN = Pattern.compile(
            "^[0-9]{12}$"
    );


    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[6-9][0-9]{9}$"
    );


    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }


    public static boolean isValidPan(String pan) {
        if (pan == null) return false;
        return PAN_PATTERN.matcher(pan).matches();
    }


    public static boolean isValidIfsc(String ifsc) {
        if (ifsc == null) return false;
        return IFSC_PATTERN.matcher(ifsc).matches();
    }


    public static boolean isValidAadhar(String aadhar) {
        if (aadhar == null) return false;
        return AADHAR_PATTERN.matcher(aadhar).matches();
    }


    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null) return false;
        return PHONE_PATTERN.matcher(phone).matches();
    }


    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;

        boolean hasNumber = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()].*");

        return hasNumber && hasSpecial;
    }
}