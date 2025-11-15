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
@EqualsAndHashCode(of = "payeeId")
public class Payee {


    private int payeeId;
    private String ownerAccountNumber;
    private String payeeName;
    private String payeeAccountNumber;
    private String payeeIfscCode;
}