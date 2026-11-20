package com.ptit.choptiters.dao;

import com.ptit.choptiters.model.Product;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ProductDAO extends DAO {

    private static final String SELECT_COLUMNS =
            "SELECT productId, sellerId, categoryId, title, description, price, stock, reservedStock, "
                    + "status, approvalStatus, moderationStatus, isDeleted FROM tblProduct ";

    public ProductDAO() {
        super();
    }


    public Product getProductDetail(int productId) {
        String sql = SELECT_COLUMNS + "WHERE productId = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapProduct(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn chi tiết sản phẩm: " + e.getMessage(), e);
        }
        return null;
    }

    /** UC06: kiểm tra dữ liệu, lưu tin đăng mới và trả về tin vừa tạo. */
    public Product createListing(int sellerId, Product listingData) {
        validateListing(listingData);

        String sql = "INSERT INTO tblProduct (sellerId, categoryId, title, description, price, stock, "
                + "reservedStock, status, approvalStatus, moderationStatus, isDeleted) "
                + "VALUES (?, ?, ?, ?, ?, ?, 0, 'AVAILABLE', 'PENDING', 'CLEAR', FALSE)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sellerId);
            ps.setInt(2, listingData.getCategoryId());
            ps.setString(3, listingData.getTitle().trim());
            ps.setString(4, listingData.getDescription());
            ps.setFloat(5, listingData.getPrice());
            ps.setInt(6, listingData.getStock());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    Product p = getProductDetail(keys.getInt(1));
                    p.setImages(listingData.getImages());
                    return p;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tạo tin đăng: " + e.getMessage(), e);
        }
        throw new RuntimeException("Tạo tin đăng thất bại");
    }

    public Product getMyListingDetail(int productId, int sellerId) {
        Product p = getProductDetail(productId);
        if (p == null || p.isDeleted()) {
            throw new RuntimeException("Không tìm thấy tin đăng");
        }
        if (p.getSellerId() != sellerId) {
            throw new RuntimeException("Bạn không có quyền thao tác với tin đăng này");
        }
        return p;
    }

    public Product updateListing(int productId, int sellerId, float price) {
        Product p = getMyListingDetail(productId, sellerId);
        if ("HIDDEN".equalsIgnoreCase(p.getStatus())) {
            throw new RuntimeException("Tin đã bị rút, không thể chỉnh sửa");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Giá bán phải lớn hơn 0");
        }
        if (price == p.getPrice()) {
            throw new IllegalArgumentException("Giá mới trùng với giá hiện tại");
        }
        String sql = "UPDATE tblProduct SET price = ? "
                + "WHERE productId = ? AND sellerId = ? AND isDeleted = FALSE AND status <> 'HIDDEN'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setFloat(1, price);
            ps.setInt(2, productId);
            ps.setInt(3, sellerId);
            if (ps.executeUpdate() == 0) {
                throw new RuntimeException("Cập nhật tin đăng thất bại");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật tin đăng: " + e.getMessage(), e);
        }
        return getProductDetail(productId);
    }

    private void validateListing(Product p) {
        if (p.getTitle() == null || p.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên tin không được để trống");
        }
        if (p.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn danh mục");
        }
        if (p.getPrice() <= 0) {
            throw new IllegalArgumentException("Giá bán phải lớn hơn 0");
        }
        if (p.getStock() < 1) {
            throw new IllegalArgumentException("Số lượng phải từ 1 trở lên");
        }
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("productId"));
        p.setSellerId(rs.getInt("sellerId"));
        p.setCategoryId(rs.getInt("categoryId"));
        p.setTitle(rs.getString("title"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getFloat("price"));
        p.setStock(rs.getInt("stock"));
        p.setReservedStock(rs.getInt("reservedStock"));
        p.setStatus(rs.getString("status"));
        p.setApprovalStatus(rs.getString("approvalStatus"));
        p.setModerationStatus(rs.getString("moderationStatus"));
        p.setDeleted(rs.getBoolean("isDeleted"));
        return p;
    }
}