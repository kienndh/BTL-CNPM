package dao;

import dao.UserDAO;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTest extends BaseDAOTest {
    private UserDAO userDAO;

    @BeforeEach
    public void setUp() {
        userDAO = new UserDAO();
    }


    @Test
    public void testLoginStandard() {
        User u = userDAO.login("seller", "123456");
        assertNotNull(u);
        assertEquals(1, u.getUserId());
        assertEquals("seller", u.getUsername());
        assertEquals("Nguyen Van Ban", u.getFullName());
        assertEquals("ACTIVE", u.getStatus());
    }


    @Test
    public void testRegisterException() {
        User duplicate = new User();
        duplicate.setUsername("seller"); // username đã có trong dữ liệu mẫu
        duplicate.setPassword("abc123");
        duplicate.setEmail("another@ptit.edu.vn");
        duplicate.setFullName("Nguyen Van Trung");
        duplicate.setPhone("0988777666");

        assertFalse(userDAO.register(duplicate));
        assertFalse(userDAO.isEmailExists("another@ptit.edu.vn"), "Tài khoản trùng không được lưu vào DB");
    }
}
