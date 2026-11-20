package view;

import dao.OrderDAO;
import model.Order;
import model.OrderItem;
import model.OrderStatusHistory;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;

public class OrderDetailFrm extends JFrame implements ActionListener {
    private JLabel lblOrderCode;
    private JLabel lblStatus;
    private JLabel lblFulfillment;
    private JLabel lblPayment;
    private JLabel lblTotal;
    private JTable tblItems;
    private JPanel pnlTimeline;
    private JLabel lblError;
    private JButton btnBack;
    private JButton btnConfirm;
    private JButton btnMarkDelivering;
    private JButton btnCompleteOrder;
    private User u;
    private Order o;

    public OrderDetailFrm(User u, Order o) {
        super("Chi tiết đơn hàng");

        this.u = u;
        this.o = o;

        lblOrderCode = new JLabel();
        lblStatus = new JLabel();
        lblFulfillment = new JLabel();
        lblPayment = new JLabel();
        lblTotal = new JLabel();

        lblError = new JLabel(" ");
        lblError.setForeground(Color.RED);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{
                        "Item ID",
                        "Product ID",
                        "Sản phẩm",
                        "Số lượng",
                        "Đơn giá"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblItems = new JTable(model);

        pnlTimeline = new JPanel();
        pnlTimeline.setLayout(
                new BoxLayout(pnlTimeline, BoxLayout.Y_AXIS)
        );
        pnlTimeline.setBorder(
                BorderFactory.createTitledBorder("Lịch sử trạng thái")
        );

        btnBack = new JButton("Quay lại");
        btnConfirm = new JButton("Chấp nhận đơn");
        btnMarkDelivering = new JButton("Đánh dấu đang giao");
        btnCompleteOrder = new JButton("Hoàn tất đơn");

        btnBack.addActionListener(this);
        btnConfirm.addActionListener(this);
        btnMarkDelivering.addActionListener(this);
        btnCompleteOrder.addActionListener(this);

        JPanel info = new JPanel(new GridLayout(5, 2, 8, 6));
        info.add(new JLabel("Mã đơn:"));
        info.add(lblOrderCode);
        info.add(new JLabel("Trạng thái:"));
        info.add(lblStatus);
        info.add(new JLabel("Giao nhận:"));
        info.add(lblFulfillment);
        info.add(new JLabel("Thanh toán:"));
        info.add(lblPayment);
        info.add(new JLabel("Tổng tiền:"));
        info.add(lblTotal);

        JSplitPane center = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tblItems),
                new JScrollPane(pnlTimeline)
        );
        center.setResizeWeight(0.65);

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );
        buttons.add(btnConfirm);
        buttons.add(btnMarkDelivering);
        buttons.add(btnCompleteOrder);
        buttons.add(btnBack);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.add(lblError, BorderLayout.CENTER);
        bottom.add(buttons, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );
        content.add(info, BorderLayout.NORTH);
        content.add(center, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);

        setContentPane(content);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        displayOrder();
    }

    private void displayOrder() {
        btnConfirm.setVisible(false);
        btnMarkDelivering.setVisible(false);
        btnCompleteOrder.setVisible(false);

        if (u == null || u.getUserId() <= 0) {
            lblError.setForeground(Color.RED);
            lblError.setText("Người dùng chưa đăng nhập.");
            return;
        }

        if (o == null || o.getOrderId() <= 0) {
            lblError.setForeground(Color.RED);
            lblError.setText("Đơn hàng không hợp lệ.");
            return;
        }

        boolean isSeller = u.getUserId() == o.getSellerId();
        boolean isBuyer = u.getUserId() == o.getBuyerId();

        if (!isSeller && !isBuyer) {
            lblError.setForeground(Color.RED);
            lblError.setText("Bạn không có quyền xem đơn hàng này.");
            return;
        }

        lblOrderCode.setText("#" + o.getOrderId());

        String[] statusCodes = {
                "PENDING",
                "CONFIRMED",
                "DELIVERING",
                "COMPLETED",
                "CANCELLED"
        };

        String[] statusNames = {
                "Chờ xác nhận",
                "Đã xác nhận",
                "Đang giao",
                "Đã hoàn tất",
                "Đã hủy"
        };

        String statusText = o.getStatus();

        for (int i = 0; i < statusCodes.length; i++) {
            if (statusCodes[i].equalsIgnoreCase(o.getStatus())) {
                statusText = statusNames[i];
                break;
            }
        }

        lblStatus.setText(statusText);

        String fulfillmentText = o.getFulfillmentMethod();

        if ("MEETUP".equalsIgnoreCase(o.getFulfillmentMethod())) {
            fulfillmentText = "Gặp trực tiếp";

            if (o.getMeetupPlace() != null
                    && !o.getMeetupPlace().trim().isEmpty()) {
                fulfillmentText += " - " + o.getMeetupPlace();
            }
        } else if ("SHIPPING".equalsIgnoreCase(
                o.getFulfillmentMethod())) {
            fulfillmentText = "Giao hàng";
        }

        lblFulfillment.setText(fulfillmentText);

        String paymentMethodText = o.getPaymentMethod();

        if ("CASH_ON_MEETUP".equalsIgnoreCase(o.getPaymentMethod())) {
            paymentMethodText = "Tiền mặt khi gặp";
        }

        String paymentStatusText = o.getPaymentStatus();

        if ("PAID".equalsIgnoreCase(o.getPaymentStatus())) {
            paymentStatusText = "Đã thanh toán";
        } else if ("UNPAID".equalsIgnoreCase(o.getPaymentStatus())) {
            paymentStatusText = "Chưa thanh toán";
        }

        lblPayment.setText(
                paymentMethodText + " (" + paymentStatusText + ")"
        );

        lblTotal.setText(
                String.format("%,.0f đ", o.getTotalAmount())
        );

        DefaultTableModel model =
                (DefaultTableModel) tblItems.getModel();
        model.setRowCount(0);

        if (o.getItems() != null) {
            for (OrderItem item : o.getItems()) {
                model.addRow(new Object[]{
                        item.getOrderItemId(),
                        item.getProductId(),
                        item.getProductTitle(),
                        item.getQuantity(),
                        String.format(
                                "%,.0f đ",
                                item.getPriceAtPurchase()
                        )
                });
            }
        }

        pnlTimeline.removeAll();

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        if (o.getStatusHistory() != null) {
            for (OrderStatusHistory history : o.getStatusHistory()) {
                String timeText = history.getCreatedAt() != null
                        ? dateFormat.format(history.getCreatedAt())
                        : "";

                String fromStatus = history.getFromStatus() != null
                        ? history.getFromStatus()
                        : "INIT";

                String changedBy = history.getChangedBy() > 0
                        ? "User #" + history.getChangedBy()
                        : "Hệ thống";

                String line = String.format(
                        "[%s] %s -> %s (%s)",
                        timeText,
                        fromStatus,
                        history.getToStatus(),
                        changedBy
                );

                pnlTimeline.add(new JLabel(line));
            }
        }

        pnlTimeline.revalidate();
        pnlTimeline.repaint();

        btnConfirm.setVisible(
                isSeller
                        && "PENDING".equalsIgnoreCase(o.getStatus())
        );

        btnMarkDelivering.setVisible(
                isSeller
                        && "CONFIRMED".equalsIgnoreCase(o.getStatus())
                        && "SHIPPING".equalsIgnoreCase(
                        o.getFulfillmentMethod()
                )
        );

        btnCompleteOrder.setVisible(
                (isBuyer || isSeller)
                        && "CONFIRMED".equalsIgnoreCase(o.getStatus())
                        && "MEETUP".equalsIgnoreCase(
                        o.getFulfillmentMethod()
                )
                        && "PAID".equalsIgnoreCase(o.getPaymentStatus())
        );

        revalidate();
        repaint();
    }

    private void btnBackClick() {
        dispose();
    }

    private void btnConfirmClick() {
        ConfirmOrderFrm frm = new ConfirmOrderFrm(u, o, this);
        frm.setVisible(true);
    }

    private void btnMarkDeliveringClick() {
        lblError.setText(" ");
        btnMarkDelivering.setEnabled(false);

        try {
            OrderDAO orderDAO = new OrderDAO();

            if (orderDAO.markDelivering(u, o)) {
                reloadOrder();
            } else {
                lblError.setForeground(Color.RED);
                lblError.setText("Không thể đánh dấu đơn đang giao.");
            }
        } catch (Exception ex) {
            lblError.setForeground(Color.RED);
            lblError.setText(ex.getMessage());
        } finally {
            btnMarkDelivering.setEnabled(true);
        }
    }

    private void btnCompleteOrderClick() {
        lblError.setText(" ");
        btnCompleteOrder.setEnabled(false);

        try {
            OrderDAO orderDAO = new OrderDAO();

            if (orderDAO.completeOrder(u, o)) {
                reloadOrder();
            } else {
                lblError.setForeground(Color.RED);
                lblError.setText("Không thể hoàn tất đơn hàng.");
            }
        } catch (Exception ex) {
            lblError.setForeground(Color.RED);
            lblError.setText(ex.getMessage());
        } finally {
            btnCompleteOrder.setEnabled(true);
        }
    }

    public void reloadOrder() {
        btnConfirm.setVisible(false);
        btnMarkDelivering.setVisible(false);
        btnCompleteOrder.setVisible(false);

        if (u == null || u.getUserId() <= 0) {
            lblError.setForeground(Color.RED);
            lblError.setText("Người dùng chưa đăng nhập.");
            return;
        }

        if (o == null || o.getOrderId() <= 0) {
            lblError.setForeground(Color.RED);
            lblError.setText("Đơn hàng không hợp lệ.");
            return;
        }

        try {
            String previousStatus = o.getStatus();

            OrderDAO orderDAO = new OrderDAO();
            this.o = orderDAO.getOrderDetail(u, o.getOrderId());

            displayOrder();

            lblError.setForeground(new Color(0, 128, 0));

            if ("PENDING".equalsIgnoreCase(previousStatus)
                    && "CONFIRMED".equalsIgnoreCase(o.getStatus())) {
                lblError.setText("Chấp nhận đơn hàng thành công.");
            } else if ("CONFIRMED".equalsIgnoreCase(previousStatus)
                    && "DELIVERING".equalsIgnoreCase(o.getStatus())) {
                lblError.setText(
                        "Đã đánh dấu đơn đang giao thành công."
                );
            } else if ("CONFIRMED".equalsIgnoreCase(previousStatus)
                    && "COMPLETED".equalsIgnoreCase(o.getStatus())) {
                lblError.setText("Hoàn tất đơn hàng thành công.");
            } else {
                lblError.setText("Đã tải lại thông tin đơn hàng.");
            }
        } catch (Exception ex) {
            lblError.setForeground(Color.RED);
            lblError.setText(
                    "Không thể tải lại đơn hàng: " + ex.getMessage()
            );
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnBack) {
            btnBackClick();
        } else if (e.getSource() == btnConfirm) {
            btnConfirmClick();
        } else if (e.getSource() == btnMarkDelivering) {
            btnMarkDeliveringClick();
        } else if (e.getSource() == btnCompleteOrder) {
            btnCompleteOrderClick();
        }
    }
}