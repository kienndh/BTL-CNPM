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
    private JLabel lblSeller;
    private JLabel lblStatus;
    private JLabel lblFulfillment;
    private JLabel lblPayment;
    private JLabel lblMeetupPlace;
    private JLabel lblTotal;
    private JLabel lblCreatedAt;
    private JLabel lblContactName;
    private JLabel lblContactPhone;
    private JLabel lblCancellationReason;
    private JTable tblItems;
    private JPanel pnlTimeline;
    private JLabel lblMessage;
    private JButton btnCancel;
    private JButton btnConfirm;
    private JButton btnMarkDelivering;
    private JButton btnCompleteOrder;
    private JButton btnBack;
    private User u;
    private Order o;

    public OrderDetailFrm(User u, Order o) {
        super("Chi tiết đơn hàng");
        this.u = u;
        this.o = o;

        lblOrderCode = new JLabel();
        lblSeller = new JLabel();
        lblStatus = new JLabel();
        lblFulfillment = new JLabel();
        lblPayment = new JLabel();
        lblMeetupPlace = new JLabel();
        lblTotal = new JLabel();
        lblCreatedAt = new JLabel();
        lblContactName = new JLabel();
        lblContactPhone = new JLabel();
        lblCancellationReason = new JLabel();

        lblMessage = new JLabel(" ");
        lblMessage.setForeground(Color.RED);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Item ID", "Product ID", "Sản phẩm", "Số lượng", "Đơn giá"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblItems = new JTable(model);

        pnlTimeline = new JPanel();
        pnlTimeline.setLayout(new BoxLayout(pnlTimeline, BoxLayout.Y_AXIS));
        pnlTimeline.setBorder(BorderFactory.createTitledBorder("Lịch sử trạng thái"));

        btnCancel = new JButton("Hủy đơn");
        btnConfirm = new JButton("Chấp nhận đơn");
        btnMarkDelivering = new JButton("Đánh dấu đang giao");
        btnCompleteOrder = new JButton("Hoàn tất đơn");
        btnBack = new JButton("Quay lại");

        btnCancel.addActionListener(this);
        btnConfirm.addActionListener(this);
        btnMarkDelivering.addActionListener(this);
        btnCompleteOrder.addActionListener(this);
        btnBack.addActionListener(this);

        JPanel info = new JPanel(new GridLayout(6, 4, 8, 6));
        info.add(new JLabel("Mã đơn:"));
        info.add(lblOrderCode);
        info.add(new JLabel("Người bán:"));
        info.add(lblSeller);
        info.add(new JLabel("Trạng thái:"));
        info.add(lblStatus);
        info.add(new JLabel("Giao nhận:"));
        info.add(lblFulfillment);
        info.add(new JLabel("Thanh toán:"));
        info.add(lblPayment);
        info.add(new JLabel("Địa điểm gặp:"));
        info.add(lblMeetupPlace);
        info.add(new JLabel("Người liên hệ:"));
        info.add(lblContactName);
        info.add(new JLabel("SĐT liên hệ:"));
        info.add(lblContactPhone);
        info.add(new JLabel("Thời điểm:"));
        info.add(lblCreatedAt);
        info.add(new JLabel("Thông tin hủy:"));
        info.add(lblCancellationReason);
        info.add(new JLabel("Tổng tiền:"));
        info.add(lblTotal);
        info.add(new JLabel());
        info.add(new JLabel());

        JSplitPane center = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tblItems),
                new JScrollPane(pnlTimeline)
        );
        center.setResizeWeight(0.65);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(btnConfirm);
        buttons.add(btnMarkDelivering);
        buttons.add(btnCompleteOrder);
        buttons.add(btnCancel);
        buttons.add(btnBack);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.add(lblMessage, BorderLayout.CENTER);
        bottom.add(buttons, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        content.add(info, BorderLayout.NORTH);
        content.add(center, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);

        setContentPane(content);
        setSize(900, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        displayOrder();
    }

    private void displayOrder() {
        btnCancel.setVisible(false);
        btnConfirm.setVisible(false);
        btnMarkDelivering.setVisible(false);
        btnCompleteOrder.setVisible(false);

        if (u == null || u.getUserId() <= 0) {
            showError("Người dùng chưa đăng nhập.");
            return;
        }
        if (o == null || o.getOrderId() <= 0) {
            showError("Đơn hàng không hợp lệ.");
            return;
        }

        boolean isSeller = u.getUserId() == o.getSellerId();
        boolean isBuyer = u.getUserId() == o.getBuyerId();
        if (!isSeller && !isBuyer) {
            showError("Bạn không có quyền xem đơn hàng này.");
            return;
        }

        lblOrderCode.setText("#" + o.getOrderId());
        lblSeller.setText("User #" + o.getSellerId());

        String[] statusCodes = {"PENDING", "CONFIRMED", "DELIVERING", "COMPLETED", "CANCELLED"};
        String[] statusNames = {"Chờ xác nhận", "Đã xác nhận", "Đang giao", "Đã hoàn tất", "Đã hủy"};
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
        } else if ("SHIPPING".equalsIgnoreCase(o.getFulfillmentMethod())) {
            fulfillmentText = "Giao hàng";
        }
        lblFulfillment.setText(fulfillmentText);
        lblMeetupPlace.setText(o.getMeetupPlace() != null ? o.getMeetupPlace() : "");

        String paymentMethodText = "CASH_ON_MEETUP".equalsIgnoreCase(o.getPaymentMethod())
                ? "Tiền mặt khi gặp" : o.getPaymentMethod();
        String paymentStatusText = "PAID".equalsIgnoreCase(o.getPaymentStatus())
                ? "Đã thanh toán"
                : ("UNPAID".equalsIgnoreCase(o.getPaymentStatus()) ? "Chưa thanh toán" : o.getPaymentStatus());
        lblPayment.setText(paymentMethodText + " (" + paymentStatusText + ")");

        lblTotal.setText(String.format("%,.0f đ", o.getTotalAmount()));
        lblContactName.setText(o.getContactName() != null ? o.getContactName() : "");
        lblContactPhone.setText(o.getContactPhone() != null ? o.getContactPhone() : "");
        if (o.getCancelledBy() > 0) {
            String reasonText = o.getCancelReason() != null && !o.getCancelReason().isEmpty()
                    ? ": " + o.getCancelReason() : "";
            lblCancellationReason.setText("Đã hủy bởi User #" + o.getCancelledBy() + reasonText);
        } else {
            lblCancellationReason.setText("");
        }

        DefaultTableModel model = (DefaultTableModel) tblItems.getModel();
        model.setRowCount(0);
        if (o.getItems() != null) {
            for (OrderItem item : o.getItems()) {
                model.addRow(new Object[]{
                        item.getOrderItemId(),
                        item.getProductId(),
                        item.getProductTitle(),
                        item.getQuantity(),
                        String.format("%,.0f đ", item.getPriceAtPurchase())
                });
            }
        }

        pnlTimeline.removeAll();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String latestTime = "";
        if (o.getStatusHistory() != null) {
            for (OrderStatusHistory history : o.getStatusHistory()) {
                String timeText = history.getCreatedAt() != null ? dateFormat.format(history.getCreatedAt()) : "";
                if (!timeText.isEmpty()) {
                    latestTime = timeText;
                }
                String fromStatus = history.getFromStatus() != null ? history.getFromStatus() : "INIT";
                String changedBy = history.getChangedBy() > 0 ? "User #" + history.getChangedBy() : "Hệ thống";
                String line = String.format("[%s] %s -> %s (%s)", timeText, fromStatus, history.getToStatus(), changedBy);
                pnlTimeline.add(new JLabel(line));
            }
        }
        lblCreatedAt.setText(latestTime);
        pnlTimeline.revalidate();
        pnlTimeline.repaint();

        OrderDAO orderDAO = new OrderDAO();
        if (isBuyer && orderDAO.canCancelOrder(u, o.getOrderId())) {
            btnCancel.setVisible(true);
            btnCancel.setEnabled(true);
        }
        btnConfirm.setVisible(isSeller && "PENDING".equalsIgnoreCase(o.getStatus()));
        btnMarkDelivering.setVisible(isSeller
                && "CONFIRMED".equalsIgnoreCase(o.getStatus())
                && "SHIPPING".equalsIgnoreCase(o.getFulfillmentMethod()));
        btnCompleteOrder.setVisible((isBuyer || isSeller) && OrderDAO.canComplete(o));

        revalidate();
        repaint();
    }

    private void btnBackClick() {
        dispose();
    }

    private void btnCancelClick() {
        CancelOrderFrm cancelFrm = new CancelOrderFrm(this, u, o);
        cancelFrm.setVisible(true);
    }

    private void btnConfirmClick() {
        ConfirmOrderFrm frm = new ConfirmOrderFrm(u, o, this);
        frm.setVisible(true);
    }

    private void btnMarkDeliveringClick() {
        lblMessage.setText(" ");
        btnMarkDelivering.setEnabled(false);
        try {
            OrderDAO orderDAO = new OrderDAO();
            if (orderDAO.markDelivering(u, o)) {
                reloadOrder();
            } else {
                showError("Không thể đánh dấu đơn đang giao.");
            }
        } catch (Exception ex) {
            showError(ex.getMessage());
        } finally {
            btnMarkDelivering.setEnabled(true);
        }
    }

    private void btnCompleteOrderClick() {
        lblMessage.setText(" ");
        btnCompleteOrder.setEnabled(false);
        try {
            OrderDAO orderDAO = new OrderDAO();
            if (orderDAO.completeOrder(u, o)) {
                reloadOrder();
            } else {
                showError("Không thể hoàn tất đơn hàng.");
            }
        } catch (Exception ex) {
            showError(ex.getMessage());
        } finally {
            btnCompleteOrder.setEnabled(true);
        }
    }

    public void reloadOrder() {
        btnCancel.setVisible(false);
        btnConfirm.setVisible(false);
        btnMarkDelivering.setVisible(false);
        btnCompleteOrder.setVisible(false);

        if (u == null || u.getUserId() <= 0) {
            showError("Người dùng chưa đăng nhập.");
            return;
        }
        if (o == null || o.getOrderId() <= 0) {
            showError("Đơn hàng không hợp lệ.");
            return;
        }

        try {
            String previousStatus = o.getStatus();
            OrderDAO orderDAO = new OrderDAO();
            this.o = orderDAO.getOrderDetail(u, o.getOrderId());

            displayOrder();

            lblMessage.setForeground(new Color(0, 128, 0));
            if ("PENDING".equalsIgnoreCase(previousStatus) && "CONFIRMED".equalsIgnoreCase(o.getStatus())) {
                lblMessage.setText("Chấp nhận đơn hàng thành công.");
            } else if ("CONFIRMED".equalsIgnoreCase(previousStatus) && "DELIVERING".equalsIgnoreCase(o.getStatus())) {
                lblMessage.setText("Đã đánh dấu đơn đang giao thành công.");
            } else if (("CONFIRMED".equalsIgnoreCase(previousStatus) || "DELIVERING".equalsIgnoreCase(previousStatus))
                    && "COMPLETED".equalsIgnoreCase(o.getStatus())) {
                lblMessage.setText("Hoàn tất đơn hàng thành công.");
            } else {
                lblMessage.setText("Đã cập nhật trạng thái đơn hàng.");
            }
        } catch (Exception ex) {
            showError("Không thể tải lại đơn hàng: " + ex.getMessage());
        }
    }

    private void showError(String msg) {
        lblMessage.setForeground(Color.RED);
        lblMessage.setText(msg != null ? msg : "");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnBack) {
            btnBackClick();
        } else if (e.getSource() == btnCancel) {
            btnCancelClick();
        } else if (e.getSource() == btnConfirm) {
            btnConfirmClick();
        } else if (e.getSource() == btnMarkDelivering) {
            btnMarkDeliveringClick();
        } else if (e.getSource() == btnCompleteOrder) {
            btnCompleteOrderClick();
        }
    }
}