package test;

import model.Order;
import model.OrderItem;
import model.OrderStatusHistory;
import model.User;
import view.ConfirmOrderFrm;
import view.OrderDetailFrm;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class Module4Test {
    public static void main(String[] args) {
        User seller = new User();
        seller.setUserId(1);
        seller.setFullName("Người bán chạy thử");
        seller.setStatus("ACTIVE");
        seller.setRole("STUDENT");

        Order order = new Order();
        order.setOrderId(1001);
        order.setSellerId(1);
        order.setBuyerId(2);
        order.setStatus("PENDING");
        order.setFulfillmentMethod("MEETUP");
        order.setPaymentMethod("CASH_ON_MEETUP");
        order.setPaymentStatus("UNPAID");
        order.setMeetupPlace("Cổng Học viện PTIT");
        order.setTotalAmount(100000f);

        OrderItem item = new OrderItem();
        item.setOrderItemId(1);
        item.setOrderId(1001);
        item.setProductId(10);
        item.setProductTitle("Giáo trình Giải tích 1");
        item.setQuantity(2);
        item.setPriceAtPurchase(50000f);

        order.getItems().add(item);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setHistoryId(1);
        history.setOrderId(1001);
        history.setFromStatus(null);
        history.setToStatus("PENDING");
        history.setChangedBy(2);
        history.setCreatedAt(new Date());

        order.getStatusHistory().add(history);

        SwingUtilities.invokeLater(() -> {
            OrderDetailFrm detailFrm =
                    new OrderDetailFrm(seller, order);
            detailFrm.setLocation(50, 50);
            disableUpdateButtons(detailFrm.getContentPane());
            detailFrm.setVisible(true);

            ConfirmOrderFrm confirmFrm =
                    new ConfirmOrderFrm(seller, order, detailFrm);
            confirmFrm.setLocation(300, 200);
            disableUpdateButtons(confirmFrm.getContentPane());
            confirmFrm.setVisible(true);
        });
    }

    private static void disableUpdateButtons(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                String text = button.getText();

                if ("Chấp nhận đơn".equals(text)
                        || "Đánh dấu đang giao".equals(text)
                        || "Hoàn tất đơn".equals(text)
                        || "Xác nhận".equals(text)) {
                    button.setEnabled(false);
                }
            }

            if (component instanceof Container) {
                disableUpdateButtons((Container) component);
            }
        }
    }
}