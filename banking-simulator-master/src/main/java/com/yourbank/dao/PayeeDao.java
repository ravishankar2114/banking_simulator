package com.yourbank.dao;

import com.yourbank.model.Payee;
import com.yourbank.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class PayeeDao {

    private Connection connection;

    public PayeeDao() {
        try {
            this.connection = DatabaseUtil.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to the database");
        }
    }


    public void createPayee(Payee payee) throws SQLException {
        String sql = "INSERT INTO payees (owner_account_number, payee_name, " +
                "payee_account_number, payee_ifsc_code) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, payee.getOwnerAccountNumber());
            statement.setString(2, payee.getPayeeName());
            statement.setString(3, payee.getPayeeAccountNumber());
            statement.setString(4, payee.getPayeeIfscCode());

            statement.executeUpdate();
        }
    }


    public boolean deletePayee(int payeeId, String ownerAccountNumber) throws SQLException {
        String sql = "DELETE FROM payees WHERE payee_id = ? AND owner_account_number = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, payeeId);
            statement.setString(2, ownerAccountNumber);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public Payee findPayeeById(int payeeId) throws SQLException {
        String sql = "SELECT * FROM payees WHERE payee_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, payeeId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPayee(rs);
                }
            }
        }
        return null;
    }

    public List<Payee> findPayeesForAccount(String ownerAccountNumber) throws SQLException {
        List<Payee> payees = new ArrayList<>();
        String sql = "SELECT * FROM payees WHERE owner_account_number = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ownerAccountNumber);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    payees.add(mapResultSetToPayee(rs));
                }
            }
        }
        return payees;
    }


    private Payee mapResultSetToPayee(ResultSet rs) throws SQLException {
        return new Payee(
                rs.getInt("payee_id"),
                rs.getString("owner_account_number"),
                rs.getString("payee_name"),
                rs.getString("payee_account_number"),
                rs.getString("payee_ifsc_code")
        );
    }
}