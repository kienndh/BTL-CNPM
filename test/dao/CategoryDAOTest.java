package dao;

import dao.CategoryDAO;
import model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryDAOTest extends BaseDAOTest {
    private CategoryDAO categoryDAO;

    @BeforeEach
    public void setUp() {
        categoryDAO = new CategoryDAO();
    }


    @Test
    public void testGetCategoriesStandard() {
        List<Category> list = categoryDAO.getCategories();
        assertEquals(5, list.size());
        for (Category c : list) {
            assertFalse(c.isDeleted());
        }
    }

    @Test
    public void testGetCategoriesException() throws Exception {
        TestDB.exec("UPDATE tblCategory SET isDeleted = TRUE WHERE slug = ?", "khac");

        List<Category> list = categoryDAO.getCategories();
        assertEquals(4, list.size(), "Danh mục đã xóa không được trả về");
        for (Category c : list) {
            assertNotEquals("khac", c.getSlug());
        }
    }
}
