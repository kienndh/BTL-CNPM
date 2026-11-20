package dao;

import model.Category;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO extends DAO {

    public CategoryDAO() {
        super();
    }

    public List<Category> getCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT categoryId, slug, name, isDeleted FROM tblCategory "
                + "WHERE isDeleted = FALSE ORDER BY name";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Category(
                        rs.getInt("categoryId"),
                        rs.getString("slug"),
                        rs.getString("name"),
                        rs.getBoolean("isDeleted")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải danh mục: " + e.getMessage(), e);
        }
        return list;
    }
}