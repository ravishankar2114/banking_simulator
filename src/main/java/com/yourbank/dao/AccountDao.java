package com.yourbank.dao;

import com.yourbank.model.Account;
import com.yourbank.model.Account.AccountStatus;
import com.yourbank.model.Account.AccountType;
import com.yourbank.model.Account.SecurityLevel;
import com.yourbank.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccountDao {

    private Connection connection;

    public AccountDao() {
        try {
            this.connection = DatabaseUtil.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to the database");
        }
    }


    public void createAccount(Account account) throws SQLException {
        // The SQL query with placeholders (?)
        String sql = "INSERT INTO users (account_number, holder_name, password_hash, email, " +
                "phone_number, full_address, pan_card_number, aadhar_card_number, " +
                "ifsc_code, account_type, security_level, balance, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, account.getAccountNumber());
            statement.setString(2, account.getHolderName());
            statement.setString(3, account.getPasswordHash());
            statement.setString(4, account.getEmail());
            statement.setString(5, account.getPhoneNumber());
            statement.setString(6, account.getFullAddress());
            statement.setString(7, account.getPanCardNumber());
            statement.setString(8, account.getAadharCardNumber());
            statement.setString(9, account.getIfscCode());
            statement.setString(10, account.getAccountType().name());
            statement.setString(11, account.getSecurityLevel().name());
            statement.setBigDecimal(12, account.getBalance());
            statement.setObject(13, account.getCreatedAt());


            statement.executeUpdate();
        }
    }


    public Account findAccountByNumber(String accountNumber) throws SQLException {
        String sql = "SELECT * FROM users WHERE account_number = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountNumber);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {

                    return mapResultSetToAccount(rs);
                }
            }
        }
        return null;
    }


    public void updateAccount(Account account) throws SQLException {

        String sql = "UPDATE users SET " +
                "holder_name = ?, email = ?, phone_number = ?, full_address = ?, " +
                "password_hash = ?, account_status = ?, balance = ?, security_level = ? " +
                "WHERE account_number = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, account.getHolderName());
            statement.setString(2, account.getEmail());
            statement.setString(3, account.getPhoneNumber());
            statement.setString(4, account.getFullAddress());
            statement.setString(5, account.getPasswordHash());
            statement.setString(6, account.getAccountStatus().name());
            statement.setBigDecimal(7, account.getBalance());
            statement.setString(8, account.getSecurityLevel().name());
            statement.setString(9, account.getAccountNumber());

            statement.executeUpdate();
        }
    }

    public List<Account> listAllAccounts() throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
        }
        return accounts;
    }


    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        return new Account(
                rs.getString("account_number"),
                rs.getString("holder_name"),
                rs.getString("password_hash"),
                rs.getString("email"),
                rs.getString("phone_number"),
                rs.getString("full_address"),
                rs.getString("pan_card_number"),
                rs.getString("aadhar_card_number"),
                rs.getString("ifsc_code"),
                AccountType.valueOf(rs.getString("account_type")),
                SecurityLevel.valueOf(rs.getString("security_level")),
                AccountStatus.valueOf(rs.getString("account_status")),
                rs.getBigDecimal("balance"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}