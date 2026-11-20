package view;

import dao.OrderDAO;
import model.Order;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class MyOrdersFrm extends JFrame implements ActionListener {
    private JComboBox<String> cboStatus;
    private JButton btnFilter;
    private JTable tblOrders;
    private JLabel lblMessage;
    private User u;
    private JLabel lblTotalOrders;
    private ArrayList<Order> orders;

    public MyOrdersFrm(User u) {
        super("Đơn mua của tôi");
        this.u = u;
        this.orders = new ArrayList<>();

        cboStatus = new JComboBox<>(new String[]{"ALL", "PENDING", "CONFIRMED", "DELIVERING", "COMPLETED", "CANCELLED"});
        btnFilter = new JButton("Lọc");
        btnFilter.addActionListener(this);

        lblTotalOrders = new JLabel("Tổng số đơn: 0");
        lblMessage = new JLabel();
        lblMessage.setForeground(Color.RED);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Mã đơn", "Sản phẩm", "Người bán (ID)", "Giao nhận", "Tổng tiền", "Trạng thái"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblOrders = new JTable(model);
        tblOrders.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblOrders.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tblOrders.getSelectedRow();
                    if (row >= 0 && row < orders.size()) {
                        btnViewDetailsClick(orders.get(row).getOrderId());
                    }
                }
            }
        });

        JButton btnViewDetail = new JButton("Xem chi tiết");
        btnViewDetail.addActionListener(e -> {
            int row = tblOrders.getSelectedRow();
            if (row >= 0 && row < orders.size()) {
                btnViewDetailsClick(orders.get(row).getOrderId());
            } else {
                lblMessage.setText("Vui lòng chọn một đơn hàng để xem chi tiết.");
            }
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.add(new JLabel("Trạng thái:"));
        top.add(cboStatus);
        top.add(btnFilter);
        top.add(lblTotalOrders);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        bottom.add(lblMessage, BorderLayout.CENTER);
        bottom.add(btnViewDetail, BorderLayout.EAST);

        setLayout(new BorderLayout(8, 8));
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(tblOrders), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        loadOrders("ALL");

        setSize(760, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void loadOrders(String status) {
        try {
            OrderDAO orderDAO = new OrderDAO();
            this.orders = orderDAO.getOrdersByBuyer(u, status);

            DefaultTableModel model = (DefaultTableModel) tblOrders.getModel();
            model.setRowCount(0);
            for (Order o : this.orders) {
                String productSummary = (o.getItems() != null && !o.getItems().isEmpty())
                        ? o.getItems().get(0).getProductTitle()
                        : "";
                model.addRow(new Object[]{
                        "#" + o.getOrderId(),
                        productSummary,
                        "User #" + o.getSellerId(),
                        o.getFulfillmentMethod(),
                        String.format("%,.0f đ", o.getTotalAmount()),
                        o.getStatus()
                });
            }
            lblTotalOrders.setText("Tổng số đơn: " + this.orders.size());
            lblMessage.setText("");
        } catch (Exception ex) {
            lblMessage.setText(ex.getMessage());
        }
    }

    private void btnFilterClick() {
        String selectedStatus = (String) cboStatus.getSelectedItem();
        loadOrders(selectedStatus);
    }

    private void btnViewDetailsClick(int orderId) {
        try {
            OrderDAO orderDAO = new OrderDAO();
            Order detail = orderDAO.getOrderDetail(u, orderId);
            new OrderDetailFrm(u, detail).setVisible(true);
        } catch (Exception ex) {
            lblMessage.setText(ex.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnFilter) {
            btnFilterClick();
        }
    }
}