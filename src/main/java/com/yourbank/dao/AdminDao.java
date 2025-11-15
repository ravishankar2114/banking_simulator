package com.yourbank.dao;

import com.yourbank.model.Admin;
import com.yourbank.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class AdminDao {

    private Connection connection;

    public AdminDao() {
        try {
            this.connection = DatabaseUtil.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to the database");
        }
    }


    public Admin findAdminByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM admins WHERE username = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {

                    return mapResultSetToAdmin(rs);
                }
            }
        }
        return null;
    }


    public void createAdmin(Admin admin) throws SQLException {
        String sql = "INSERT INTO admins (admin_id, username, password_hash, email, phone_number, " +
                "role, assigned_bank, bank_branch_ifsc) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, admin.getAdminId());
            statement.setString(2, admin.getUsername());
            statement.setString(3, admin.getPasswordHash());
            statement.setString(4, admin.getEmail());
            statement.setString(5, admin.getPhoneNumber());
            statement.setString(6, admin.getRole());
            statement.setString(7, admin.getAssignedBank());
            statement.setString(8, admin.getBankBranchIfsc());

            statement.executeUpdate();
        }
    }
    private Admin mapResultSetToAdmin(ResultSet rs) throws SQLException {
        return new Admin(
                rs.getString("admin_id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("email"),
                rs.getString("phone_number"),
                rs.getString("role"),
                rs.getString("assigned_bank"),
                rs.getString("bank_branch_ifsc")
        );
    }
}