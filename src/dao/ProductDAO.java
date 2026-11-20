package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Product;

public class ProductDAO extends DAO {

    public ProductDAO() {
        super();
    }

    public List<Product> findPublicListings(String keyword) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT productId, sellerId, categoryId, title, price, stock, reservedStock, "
                   + "status, approvalStatus, moderationStatus, isDeleted "
                   + "FROM tblProduct "
                   + "WHERE isDeleted = 0 "
                   + "AND status = 'AVAILABLE' "
                   + "AND approvalStatus = 'APPROVED' "
                   + "AND moderationStatus = 'CLEAR' "
                   + "AND (stock - reservedStock) > 0 "
                   + "AND title LIKE ? "
                   + "ORDER BY productId DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String kw = (keyword == null) ? "" : keyword.trim();
            ps.setString(1, "%" + kw + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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
                    p.setDeleted(rs.getInt("isDeleted") == 1);
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}