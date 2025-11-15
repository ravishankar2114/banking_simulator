package com.yourbank.util;


import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;


public class OtpService {


    private static final String ACCOUNT_SID = DatabaseUtil.getProperty("TWILIO_ACCOUNT_SID");
    private static final String AUTH_TOKEN = DatabaseUtil.getProperty("TWILIO_AUTH_TOKEN");
    private static final String TWILIO_PHONE_NUMBER = DatabaseUtil.getProperty("TWILIO_PHONE_NUMBER");


    private static boolean isInitialized = false;


    private static void initializeTwilio() {
        if (!isInitialized) {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            isInitialized = true;
        }
    }


    public static String generateAndSendOtp(String toPhoneNumber) {

        initializeTwilio();

        String otp = SecurityUtil.generateOtp();
        String messageBody = "Your " + DatabaseUtil.getProperty("BANK_NAME", "BankSim") + " verification code is: " + otp;

        try {

            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(TWILIO_PHONE_NUMBER),
                    messageBody
            ).create();


            System.out.println("OTP SMS sent successfully! SID: " + message.getSid());

            return otp;

        } catch (Exception e) {

            System.err.println("Error sending OTP SMS: " + e.getMessage());

            throw new RuntimeException("Could not send OTP. Please try again later.", e);
        }
    }
}