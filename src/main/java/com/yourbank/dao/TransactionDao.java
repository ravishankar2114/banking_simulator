package com.yourbank.dao;

import com.yourbank.model.TransactionRecord;
import com.yourbank.model.TransactionRecord.TxType;
import com.yourbank.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class TransactionDao {

    private Connection connection;

    public TransactionDao() {
        try {
            this.connection = DatabaseUtil.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to the database");
        }
    }

   public void createTransaction(TransactionRecord tx) throws SQLException {
        String sql = "INSERT INTO transactions (tx_id, tx_type, amount, from_account_number, " +
                "to_account_number, created_at) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tx.getTxId());
            statement.setString(2, tx.getTxType().name());
            statement.setBigDecimal(3, tx.getAmount());
            statement.setString(4, tx.getFromAccountNumber());
            statement.setString(5, tx.getToAccountNumber());
            statement.setObject(6, tx.getCreatedAt());

            statement.executeUpdate();
        }
    }

    public List<TransactionRecord> findTransactionsForAccount(String accountNumber) throws SQLException {
        List<TransactionRecord> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE from_account_number = ? OR to_account_number = ? " +
                "ORDER BY created_at DESC";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountNumber);
            statement.setString(2, accountNumber);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return transactions;
    }


    public List<TransactionRecord> findMiniStatement(String accountNumber, int limit) throws SQLException {
        List<TransactionRecord> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE from_account_number = ? OR to_account_number = ? " +
                "ORDER BY created_at DESC LIMIT ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountNumber);
            statement.setString(2, accountNumber);
            statement.setInt(3, limit);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return transactions;
    }



    private TransactionRecord mapResultSetToTransaction(ResultSet rs) throws SQLException {
        return new TransactionRecord(
                rs.getString("tx_id"),
                TxType.valueOf(rs.getString("tx_type")),
                rs.getBigDecimal("amount"),
                rs.getString("from_account_number"),
                rs.getString("to_account_number"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

    public List<TransactionRecord> findAllTransactions() throws SQLException {
        List<TransactionRecord> transactions = new ArrayList<>();
        // Note: No 'WHERE' clause
        String sql = "SELECT * FROM transactions ORDER BY created_at DESC";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }
}