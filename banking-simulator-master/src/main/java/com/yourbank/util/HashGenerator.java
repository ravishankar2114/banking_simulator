package com.yourbank.util;

public class HashGenerator {

    public static void main(String[] args) {
        String passwordToHash = "admin@123";
        String correctHash = SecurityUtil.hashPassword(passwordToHash);

        System.out.println("--- Your Correct Admin Hash ---");
        System.out.println("Copy this entire line (including the $2a$):");
        System.out.println(correctHash);
        System.out.println("---------------------------------");
    }
}
