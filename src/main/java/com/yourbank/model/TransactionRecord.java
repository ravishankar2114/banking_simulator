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
@EqualsAndHashCode(of = "txId")
public class TransactionRecord {

    private String txId;
    private TxType txType;
    private BigDecimal amount;
    private String fromAccountNumber;
    private String toAccountNumber;
    private LocalDateTime createdAt;


    public enum TxType {
        DEPOSIT,
        WITHDRAW,
        TRANSFER
    }
}