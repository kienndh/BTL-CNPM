package test;

import model.User;
import view.MyOrdersFrm;
import view.ProductDetailFrm;

import javax.swing.SwingUtilities;

public class Module3Test {
    public static void main(String[] args) {
        User buyer = new User();
        buyer.setUserId(2);
        buyer.setPublicId("U002");
        buyer.setEmail("buyer@ptit.edu.vn");
        buyer.setFullName("Tran Thi Mua");
        buyer.setPhone("0922222222");
        buyer.setStatus("ACTIVE");
        buyer.setRole("STUDENT");

        SwingUtilities.invokeLater(() -> {
            new ProductDetailFrm(buyer, 1).setVisible(true);
            new MyOrdersFrm(buyer).setVisible(true);
        });
    }
}