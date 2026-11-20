package view;

import dao.OrderDAO;
import model.Order;
import model.Product;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PlaceOrderFrm extends JFrame implements ActionListener {
    private JSpinner spnQuantity;
    private JTextField txtContactName;
    private JTextField txtContactPhone;
    private JTextField txtMeetUpPlace;
    private JTextArea txtNote;
    private JComboBox<String> cboFulfillmentMethod;
    private JComboBox<String> cboPaymentMethod;
    private JLabel lblTotal;
    private JLabel lblMessage;
    private JButton btnPlaceOrder;
    private JButton btnBack;
    private User u;
    private Product p;

    public PlaceOrderFrm(User u, Product p) {
        super("Đặt mua sản phẩm");
        this.u = u;
        this.p = p;

        int maxAvailable = (p != null && (p.getStock() - p.getReservedStock()) > 0)
                ? (p.getStock() - p.getReservedStock())
                : 1;
        spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, maxAvailable, 1));
        spnQuantity.addChangeListener(e -> calculateTotal());

        txtContactName = new JTextField(u != null && u.getFullName() != null ? u.getFullName() : "", 24);
        txtContactPhone = new JTextField(u != null && u.getPhone() != null ? u.getPhone() : "", 24);
        txtMeetUpPlace = new JTextField(24);
        txtNote = new JTextArea(3, 24);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);

        cboFulfillmentMethod = new JComboBox<>(new String[]{"MEETUP", "SHIPPING"});
        cboPaymentMethod = new JComboBox<>(new String[]{"CASH_ON_MEETUP", "BANK_TRANSFER"});

        lblTotal = new JLabel("0 đ");
        lblMessage = new JLabel();
        lblMessage.setForeground(Color.RED);

        btnPlaceOrder = new JButton("Xác nhận đặt hàng");
        btnPlaceOrder.addActionListener(this);

        btnBack = new JButton("Quay lại");
        btnBack.addActionListener(this);

        JPanel form = new JPanel(new GridLayout(9, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(14, 14, 8, 14));

        form.add(new JLabel("Số lượng:"));
        form.add(spnQuantity);
        form.add(new JLabel("Tên người liên hệ:"));
        form.add(txtContactName);
        form.add(new JLabel("Số điện thoại:"));
        form.add(txtContactPhone);
        form.add(new JLabel("Hình thức giao nhận:"));
        form.add(cboFulfillmentMethod);
        form.add(new JLabel("Địa điểm gặp mặt:"));
        form.add(txtMeetUpPlace);
        form.add(new JLabel("Phương thức thanh toán:"));
        form.add(cboPaymentMethod);
        form.add(new JLabel("Ghi chú:"));
        form.add(new JScrollPane(txtNote));
        form.add(new JLabel("Tổng tiền:"));
        form.add(lblTotal);
        form.add(btnBack);
        form.add(btnPlaceOrder);

        JPanel root = new JPanel(new BorderLayout(6, 6));
        root.add(form, BorderLayout.CENTER);
        root.add(lblMessage, BorderLayout.SOUTH);

        setContentPane(root);
        calculateTotal();

        setSize(540, 440);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private boolean validateInput() {
        if (p == null) {
            lblMessage.setText("Không có thông tin sản phẩm.");
            return false;
        }
        int qty = (Integer) spnQuantity.getValue();
        int available = p.getStock() - p.getReservedStock();
        if (qty <= 0 || qty > available) {
            lblMessage.setText("Số lượng đặt phải từ 1 đến " + available + ".");
            return false;
        }
        if (txtContactName.getText().trim().isEmpty()) {
            lblMessage.setText("Vui lòng nhập tên người liên hệ.");
            return false;
        }
        if (txtContactPhone.getText().trim().isEmpty()) {
            lblMessage.setText("Vui lòng nhập số điện thoại liên hệ.");
            return false;
        }
        String fulfillment = (String) cboFulfillmentMethod.getSelectedItem();
        if ("MEETUP".equalsIgnoreCase(fulfillment) && txtMeetUpPlace.getText().trim().isEmpty()) {
            lblMessage.setText("Vui lòng nhập địa điểm gặp mặt.");
            return false;
        }
        lblMessage.setText("");
        return true;
    }

    private void calculateTotal() {
        if (p == null) {
            lblTotal.setText("0 đ");
            return;
        }
        int qty = (Integer) spnQuantity.getValue();
        float total = p.getPrice() * qty;
        lblTotal.setText(String.format("%,.0f đ", total));
    }

    private void btnPlaceOrderClick() {
        if (!validateInput()) {
            return;
        }
        try {
            OrderDAO orderDAO = new OrderDAO();
            Order created = orderDAO.createOrder(
                    u,
                    p.getProductId(),
                    (Integer) spnQuantity.getValue(),
                    txtContactName.getText().trim(),
                    txtContactPhone.getText().trim(),
                    txtMeetUpPlace.getText().trim(),
                    txtNote.getText().trim(),
                    (String) cboFulfillmentMethod.getSelectedItem(),
                    (String) cboPaymentMethod.getSelectedItem()
            );
            JOptionPane.showMessageDialog(this, "Đặt hàng thành công! Mã đơn: #" + created.getOrderId());
            new OrderDetailFrm(u, created).setVisible(true);
            dispose();
        } catch (Exception ex) {
            lblMessage.setText(ex.getMessage());
        }
    }

    private void btnBackClick() {
        if (p != null) {
            new ProductDetailFrm(u, p.getProductId()).setVisible(true);
        }
        dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnPlaceOrder) {
            btnPlaceOrderClick();
        } else if (e.getSource() == btnBack) {
            btnBackClick();
        }
    }
}