package com.ptit.choptiters.dao;

import com.ptit.choptiters.model.ProductImage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class ProductImageDAO extends DAO {

    public ProductImageDAO() {
        super();
    }

    /** UC06: gán productId cho các ảnh và lưu vào tblProductImage. */
    public void attachImages(int productId, List<ProductImage> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO tblProductImage (productId, imageUrl, isThumbnail, sortOrder) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (ProductImage img : images) {
                img.setProductId(productId);
                ps.setInt(1, productId);
                ps.setString(2, img.getImageUrl());
                ps.setBoolean(3, img.isThumbnail());
                ps.setInt(4, img.getSortOrder());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lưu hình ảnh: " + e.getMessage(), e);
        }
    }
}