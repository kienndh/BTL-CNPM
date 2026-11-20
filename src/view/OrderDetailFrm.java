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

public class OrderDetailFrm extends JFrame implements ActionListener {
    private JLabel lblOrderCode;
    private JLabel lblSeller;
    private JLabel lblStatus;
    private JLabel lblFulfillment;
    private JLabel lblPayment;
    private JLabel lblMeetupPlace;
    private JLabel lblTotal;
    private JTable tblItems;
    private JPanel pnlTimeline;
    private JLabel lblMessage;
    private JButton btnBack;
    private User u;
    private Order o;
    private JLabel lblCreatedAt;
    private JLabel lblContactName;
    private JLabel lblContactPhone;
    private JLabel lblCancellationReason;
    private JButton btnCancel;

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
        lblContactName = new JLabel(u != null && u.getFullName() != null ? u.getFullName() : "");
        lblContactPhone = new JLabel(u != null && u.getPhone() != null ? u.getPhone() : "");
        lblCancellationReason = new JLabel();
        lblMessage = new JLabel();
        lblMessage.setForeground(Color.RED);

        DefaultTableModel itemsModel = new DefaultTableModel(
                new String[]{"Item ID", "Product ID", "Sản phẩm", "Số lượng", "Đơn giá"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblItems = new JTable(itemsModel);

        pnlTimeline = new JPanel();
        pnlTimeline.setLayout(new BoxLayout(pnlTimeline, BoxLayout.Y_AXIS));
        pnlTimeline.setBorder(BorderFactory.createTitledBorder("Lịch sử trạng thái"));

        btnCancel = new JButton("Hủy đơn");
        btnCancel.addActionListener(this);

        btnBack = new JButton("Quay lại");
        btnBack.addActionListener(this);

        JPanel info = new JPanel(new GridLayout(6, 4, 8, 6));
        info.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(new JScrollPane(tblItems), BorderLayout.CENTER);
        center.add(new JScrollPane(pnlTimeline), BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(btnCancel);
        buttons.add(btnBack);
        bottom.add(lblMessage, BorderLayout.CENTER);
        bottom.add(buttons, BorderLayout.EAST);

        setLayout(new BorderLayout(8, 8));
        add(info, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        displayOrder();

        setSize(740, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void displayOrder() {
        if (this.o == null) {
            return;
        }
        lblOrderCode.setText("#" + o.getOrderId());
        lblSeller.setText("User #" + o.getSellerId());
        lblStatus.setText(o.getStatus());
        lblFulfillment.setText(o.getFulfillmentMethod());
        lblPayment.setText(o.getPaymentMethod() + " (" + o.getPaymentStatus() + ")");
        lblMeetupPlace.setText(o.getMeetupPlace() != null ? o.getMeetupPlace() : "");
        lblTotal.setText(String.format("%,.0f đ", o.getTotalAmount()));
        lblCancellationReason.setText(o.getCancelledBy() > 0 ? "Đã hủy bởi User #" + o.getCancelledBy() : "");

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
        String latestTime = "";
        if (o.getStatusHistory() != null) {
            for (OrderStatusHistory h : o.getStatusHistory()) {
                String timeText = h.getCreatedAt() != null ? h.getCreatedAt().toString() : "";
                if (!timeText.isEmpty()) {
                    latestTime = timeText;
                }
                String line = String.format("[%s] %s -> %s (by User #%d)",
                        timeText,
                        h.getFromStatus() != null ? h.getFromStatus() : "INIT",
                        h.getToStatus(),
                        h.getChangedBy());
                pnlTimeline.add(new JLabel(line));
            }
        }
        lblCreatedAt.setText(latestTime);
        pnlTimeline.revalidate();
        pnlTimeline.repaint();

        OrderDAO orderDAO = new OrderDAO();
        btnCancel.setEnabled(orderDAO.canCancelOrder(u, o.getOrderId()));
    }

    private void btnBackClick() {
        dispose();
    }

    private void btnCancelClick() {
        CancelOrderFrm cancelFrm = new CancelOrderFrm(this, u, o);
        cancelFrm.setVisible(true);
    }

    public void reloadOrder() {
        try {
            OrderDAO orderDAO = new OrderDAO();
            this.o = orderDAO.getOrderDetail(u, o.getOrderId());
            displayOrder();
            showMessage("Đã cập nhật trạng thái đơn hàng.");
        } catch (Exception ex) {
            showMessage(ex.getMessage());
        }
    }

    private void showMessage(String msg) {
        lblMessage.setText(msg != null ? msg : "");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnCancel) {
            btnCancelClick();
        } else if (e.getSource() == btnBack) {
            btnBackClick();
        }
    }
}