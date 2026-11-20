package dao;

import model.Product;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductDAO extends DAO {

    public ProductDAO() {
        super();
    }

    public Product getProductDetail(int productId) {
        String sql = "SELECT productId, sellerId, categoryId, title, price, stock, reservedStock, "
                + "       status, approvalStatus, moderationStatus, isDeleted "
                + "FROM tblProduct WHERE productId = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product p = new Product();
                    p.setProductId(rs.getInt("productId"));
                    p.setSellerId(rs.getInt("sellerId"));
                    p.setCategoryId(rs.getInt("categoryId"));
                    p.setTitle(rs.getString("title"));
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
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn chi tiết sản phẩm: " + e.getMessage(), e);
        }
        return null;
    }
}