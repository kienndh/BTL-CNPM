package view;

import dao.OrderDAO;
import model.Order;
import model.OrderItem;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class BuyingOrdersFrm extends JFrame implements ActionListener {
    private JComboBox<String> cboStatus;
    private JTable tblOrders;
    private JLabel lblMessage;
    private JButton btnViewDetails;
    private JButton btnRetry;
    private User u;

    public BuyingOrdersFrm(User u) {
        super("Đơn mua");
        this.u = u;

        cboStatus = new JComboBox<>(new String[]{
                "Chờ xác nhận",
                "Đã xác nhận",
                "Đang giao",
                "Đã hoàn tất",
                "Đã hủy",
                "Tất cả"
        });
        cboStatus.setSelectedIndex(5); // Mặc định hiển thị Tất cả, gồm đơn PENDING vừa tạo.

        DefaultTableModel model = new DefaultTableModel(
                new String[]{
                        "Mã đơn", "Sản phẩm", "Tổng tiền", "Trạng thái"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblOrders = new JTable(model);
        tblOrders.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        lblMessage = new JLabel(" ");
        btnViewDetails = new JButton("Xem chi tiết");
        btnRetry = new JButton("Thử lại");

        cboStatus.addActionListener(this);
        btnViewDetails.addActionListener(this);
        btnRetry.addActionListener(this);

        JPanel filterPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );
        filterPanel.add(new JLabel("Trạng thái:"));
        filterPanel.add(cboStatus);

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );
        buttons.add(btnRetry);
        buttons.add(btnViewDetails);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.add(lblMessage, BorderLayout.CENTER);
        bottom.add(buttons, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );
        content.add(filterPanel, BorderLayout.NORTH);
        content.add(new JScrollPane(tblOrders), BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);

        setContentPane(content);
        setSize(850, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        cboStatusChanged();
    }

    public void cboStatusChanged() {
        DefaultTableModel model =
                (DefaultTableModel) tblOrders.getModel();

        model.setRowCount(0);
        btnViewDetails.setEnabled(false);
        lblMessage.setText(" ");

        if (u == null || u.getUserId() <= 0) {
            lblMessage.setForeground(Color.RED);
            lblMessage.setText("Người dùng chưa đăng nhập.");
            return;
        }

        String[] statusCodes = {
                "PENDING",
                "CONFIRMED",
                "DELIVERING",
                "COMPLETED",
                "CANCELLED",
                "ALL"
        };

        String status =
                statusCodes[cboStatus.getSelectedIndex()];

        try {
            OrderDAO orderDAO = new OrderDAO();
            ArrayList<Order> orders =
                    orderDAO.listBuyingOrders(u, status);

            for (Order order : orders) {
                StringBuilder products = new StringBuilder();

                if (order.getItems() != null) {
                    for (OrderItem item : order.getItems()) {
                        if (products.length() > 0) {
                            products.append("; ");
                        }

                        products.append(item.getProductTitle())
                                .append(" x")
                                .append(item.getQuantity());
                    }
                }

                String statusText = order.getStatus();

                for (int i = 0; i < statusCodes.length - 1; i++) {
                    if (statusCodes[i].equalsIgnoreCase(statusText)) {
                        statusText = cboStatus.getItemAt(i);
                        break;
                    }
                }

                model.addRow(new Object[]{
                        order.getOrderId(),
                        products.toString(),
                        String.format("%,.0f đ", order.getTotalAmount()),
                        statusText
                });
            }

            lblMessage.setForeground(Color.BLACK);
            lblMessage.setText(
                    orders.isEmpty()
                            ? "Không có đơn mua phù hợp."
                            : " "
            );
            btnViewDetails.setEnabled(!orders.isEmpty());
        } catch (Exception ex) {
            lblMessage.setForeground(Color.RED);
            lblMessage.setText("Không thể tải đơn mua: " + ex.getMessage());
        }
    }

    public void btnViewDetailsClick() {
        int selectedRow = tblOrders.getSelectedRow();

        if (selectedRow < 0) {
            lblMessage.setForeground(Color.RED);
            lblMessage.setText("Vui lòng chọn một đơn hàng.");
            return;
        }

        int orderId = ((Number) tblOrders.getValueAt(
                selectedRow, 0
        )).intValue();

        try {
            OrderDAO orderDAO = new OrderDAO();
            Order o = orderDAO.getOrderDetail(u, orderId);

            OrderDetailFrm frm = new OrderDetailFrm(u, o);
            frm.setVisible(true);

            lblMessage.setText(" ");
        } catch (Exception ex) {
            lblMessage.setForeground(Color.RED);
            lblMessage.setText(
                    "Không thể mở chi tiết đơn: " + ex.getMessage()
            );
        }
    }

    public void btnRetryClick() {
        cboStatusChanged();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cboStatus) {
            cboStatusChanged();
        } else if (e.getSource() == btnViewDetails) {
            btnViewDetailsClick();
        } else if (e.getSource() == btnRetry) {
            btnRetryClick();
        }
    }
}