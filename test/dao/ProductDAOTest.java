package dao;

import dao.ProductDAO;
import model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProductDAOTest extends BaseDAOTest {
    private static final int SELLER = 1;
    private ProductDAO productDAO;

    @BeforeEach
    public void setUp() {
        productDAO = new ProductDAO();
    }

    private Product newListing(String title) {
        Product p = new Product();
        p.setTitle(title);
        p.setCategoryId(2);
        p.setPrice(250000f);
        p.setStock(2);
        p.setDescription("Mo ta");
        return p;
    }


    @Test
    public void testCreateListingStandard() throws Exception {
        Product created = productDAO.createListing(SELLER, newListing("  Máy tính Casio fx-580  "));

        assertTrue(created.getProductId() > 2);
        assertEquals(SELLER, created.getSellerId());
        assertEquals("Máy tính Casio fx-580", created.getTitle(), "Tên tin phải được trim");
        assertEquals(250000f, created.getPrice(), 0.01f);
        assertEquals(2, created.getStock());
        assertEquals("AVAILABLE", created.getStatus());
        assertEquals("APPROVED", created.getApprovalStatus());
        assertEquals(3, TestDB.queryInt("SELECT COUNT(*) FROM tblProduct"));
    }

    @Test
    public void testCreateListingException() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> productDAO.createListing(SELLER, newListing("   ")));
        assertEquals(2, TestDB.queryInt("SELECT COUNT(*) FROM tblProduct"),
                "Dữ liệu không hợp lệ không được lưu vào DB");
    }
}
