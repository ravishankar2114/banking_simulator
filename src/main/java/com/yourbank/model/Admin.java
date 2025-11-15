package com.yourbank.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "adminId")
public class Admin {

    private String adminId;
    private String username;
    private String passwordHash;
    private String email;
    private String phoneNumber;
    private String role;
    private String assignedBank;
    private String bankBranchIfsc;
}