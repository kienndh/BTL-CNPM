package dao;

import dao.OrderDAO;
import model.Order;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderDAOTest extends BaseDAOTest {
    private OrderDAO orderDAO;
    private User buyer;

    @BeforeEach
    public void setUp() {
        orderDAO = new OrderDAO();
        buyer = new User(2, "U002", "buyer", "123456", "buyer@ptit.edu.vn",
                "Tran Thi Mua", "0922222222", "ACTIVE", "STUDENT");
    }

    private Order placeOrder(int quantity) {
        return orderDAO.createOrder(buyer, 1, quantity, "Tran Thi Mua", "0922222222",
                "Cong Hoc vien PTIT", "Giao buoi sang", "MEETUP", "CASH_ON_MEETUP");
    }


    @Test
    public void testCreateOrderStandard() throws Exception {
        Order o = placeOrder(2);

        assertTrue(o.getOrderId() > 0);
        assertEquals(2, o.getBuyerId());
        assertEquals(1, o.getSellerId());
        assertEquals("PENDING", o.getStatus());
        assertEquals(1, o.getItems().size());
        assertEquals(100000f, o.getTotalAmount(), 0.01f);
        assertEquals(1, o.getStatusHistory().size());
        assertEquals("PENDING", o.getStatusHistory().get(0).getToStatus());

        assertEquals(2, TestDB.queryInt("SELECT reservedStock FROM tblProduct WHERE productId = 1"),
                "Phải giữ chỗ tồn kho khi đặt đơn");
    }

    @Test
    public void testCreateOrderException() throws Exception {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> placeOrder(4));
        assertTrue(ex.getMessage().contains("vượt quá tồn kho"));

        assertEquals(0, TestDB.queryInt("SELECT COUNT(*) FROM tblOrder"), "Không được tạo đơn khi lỗi");
        assertEquals(0, TestDB.queryInt("SELECT reservedStock FROM tblProduct WHERE productId = 1"));
    }
}
