package com.ptit.choptiters.dao;

import com.ptit.choptiters.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO extends DAO {
    private static final String PHONE_REGEX = "^0\\d{9}$";
    public UserDAO() {
        super();
    }
    public User getMyProfile(int userId){
        User u = findById(userId);
        if (u == null) {
            throw new RuntimeException("Không tìm thấy người dùng");
        }
        if (!u.checkActive()) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }
        return u;
    }
    /** UC03: kiểm tra và cập nhật số điện thoại, trả về hồ sơ sau cập nhật. */
    public User updateProfile(int userId, String phone){
        if (phone == null || !phone.trim().matches(PHONE_REGEX)) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (gồm 10 chữ số, bắt đầu bằng 0)");
        }
        getMyProfile(userId);
        String sql = "UPDATE tblUser SET phone = ? WHERE userId = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, phone.trim());
            ps.setInt(2, userId);
            if (ps.executeUpdate() == 0) {
                throw new RuntimeException("Cập nhật hồ sơ thất bại");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật hồ sơ: " + e.getMessage(), e);

        }
        return findById(userId);
    }
    /** UC06, UC07, UC08: kiểm tra điều kiện người bán. */
    public boolean checkSellerEligibility(int userId) {
        User u = findById(userId);
        return u != null && u.checkSellerEligibility();
    }
    private User findById(int userId) {
        String sql = "SELECT userId, publicId, email, fullName, phone, status, role "
                + "FROM tblUser WHERE userId = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("userId"),
                            rs.getString("publicId"),
                            rs.getString("email"),
                            rs.getString("fullName"),
                            rs.getString("phone"),
                            rs.getString("status"),
                            rs.getString("role"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn người dùng: " + e.getMessage(), e);
        }
        return null;
    }
}
