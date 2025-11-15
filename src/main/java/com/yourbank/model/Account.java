package com.yourbank.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "accountNumber")
public class Account {

    private String accountNumber;
    private String holderName;
    private String passwordHash;
    private String email;
    private String phoneNumber;
    private String fullAddress;
    private String panCardNumber;
    private String aadharCardNumber;
    private String ifscCode;

    private AccountType accountType;
    private SecurityLevel securityLevel;
    private AccountStatus accountStatus;

    private BigDecimal balance;
    private LocalDateTime createdAt;

    public enum AccountType {
        SAVINGS,
        CHECKING
    }

    public enum SecurityLevel {
        STANDARD,
        SECURE_OTP
    }

    public enum AccountStatus {
        ACTIVE,
        FROZEN,
        CLOSED
    }
}