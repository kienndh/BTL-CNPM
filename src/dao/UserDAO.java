package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import model.User;

public class UserDAO extends DAO {
    private static final String PHONE_REGEX = "^0\\d{9}$";

    public UserDAO() {
        super();
    }


    public User login(String username, String password) {
        String sql = "SELECT userId, publicId, username, password, email, fullName, phone, status, role "
                + "FROM tblUser WHERE username = ? AND password = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setUserId(rs.getInt("userId"));
                    u.setPublicId(rs.getString("publicId"));
                    u.setUsername(rs.getString("username"));
                    u.setPassword(rs.getString("password"));
                    u.setEmail(rs.getString("email"));
                    u.setFullName(rs.getString("fullName"));
                    u.setPhone(rs.getString("phone"));
                    u.setStatus(rs.getString("status"));
                    u.setRole(rs.getString("role"));

                    if (u.checkActive()) {
                        return u;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isUsernameExists(String username) {
        String sql = "SELECT 1 FROM tblUser WHERE username = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isEmailExists(String email) {
        String sql = "SELECT 1 FROM tblUser WHERE email = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean register(User u) {
        if (isUsernameExists(u.getUsername()) || isEmailExists(u.getEmail())) {
            return false;
        }
        String sql = "INSERT INTO tblUser (publicId, username, password, email, fullName, phone, status, role) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String publicId = (u.getPublicId() == null || u.getPublicId().isEmpty())
                    ? "USR_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                    : u.getPublicId();
            ps.setString(1, publicId);
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getPassword());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getFullName());
            ps.setString(6, u.getPhone());
            ps.setString(7, "ACTIVE");
            ps.setString(8, "STUDENT");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public User getMyProfile(int userId) {
        User u = findById(userId);
        if (u == null) {
            throw new RuntimeException("Không tìm thấy người dùng");
        }
        if (!u.checkActive()) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }
        return u;
    }

    public User updateProfile(int userId, String phone) {
        if (phone == null || !phone.trim().matches(PHONE_REGEX)) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (gồm 10 chữ số, bắt đầu bằng 0)");
        }
        getMyProfile(userId);
        String sql = "UPDATE tblUser SET phone = ? WHERE userId = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
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