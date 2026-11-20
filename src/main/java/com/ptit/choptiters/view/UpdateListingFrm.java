package com.ptit.choptiters.view;

import com.ptit.choptiters.dao.ProductDAO;
import com.ptit.choptiters.dao.UserDAO;
import com.ptit.choptiters.model.Product;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Locale;

public class UpdateListingFrm extends JFrame implements ActionListener {
    private JTextField txtPrice;
    private JButton btnSave;
    private JLabel lblTitle, lblCurrentPrice, lblStatus, lblMessage;

    private final int productId;
    private final int sellerId;
    private final UserDAO userDAO = new UserDAO();
    private final ProductDAO productDAO = new ProductDAO();

    public UpdateListingFrm(int productId, int sellerId) {
        super("Chỉnh sửa tin đăng");
        this.productId = productId;
        this.sellerId = sellerId;
        initComponents();

        try {
            if (!userDAO.checkSellerEligibility(sellerId)) {
                btnSave.setEnabled(false);
                showMessage("Tài khoản chưa đủ điều kiện chỉnh sửa tin (bị khóa hoặc chưa có số điện thoại)");
                return;
            }
            Product p = productDAO.getMyListingDetail(productId, sellerId);
            displayProduct(p);
            if ("HIDDEN".equalsIgnoreCase(p.getStatus())) {
                btnSave.setEnabled(false);
                showMessage("Tin đã bị rút, không thể chỉnh sửa");
            }
        } catch (RuntimeException ex) {
            btnSave.setEnabled(false);
            showMessage(ex.getMessage());
        }
    }

    private void initComponents() {
        lblTitle = new JLabel("-");
        lblCurrentPrice = new JLabel("-");
        lblStatus = new JLabel("-");
        txtPrice = new JTextField(15);
        btnSave = new JButton("Lưu thay đổi");
        lblMessage = new JLabel(" ");
        btnSave.addActionListener(this);

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.add(new JLabel("Tên tin:"));
        form.add(lblTitle);
        form.add(new JLabel("Giá bán hiện tại:"));
        form.add(lblCurrentPrice);
        form.add(new JLabel("Trạng thái:"));
        form.add(lblStatus);
        form.add(new JLabel("Giá bán mới (đ):"));
        form.add(txtPrice);

        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.add(btnSave, BorderLayout.NORTH);
        bottom.add(lblMessage, BorderLayout.SOUTH);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        root.add(form, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
        setSize(420, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnSave) {
            btnSaveClick();
        }
    }

    private void displayProduct(Product p) {
        lblTitle.setText(p.getTitle());
        lblCurrentPrice.setText(formatPrice(p.getPrice()) + " đ");
        lblStatus.setText(getStatusText(p));
    }

    private void btnSaveClick() {
        String input = txtPrice.getText().trim().replace(".", "").replace(",", "");
        if (input.isEmpty()) {
            showMessage("Vui lòng nhập giá bán mới");
            return;
        }
        float price;
        try {
            price = Float.parseFloat(input);
        } catch (NumberFormatException ex) {
            showMessage("Giá bán phải là số");
            return;
        }

        try {
            Product pUpdated = productDAO.updateListing(productId, sellerId, price);
            displayProduct(pUpdated);
            txtPrice.setText("");
            showMessage("Cập nhật tin đăng thành công");
        } catch (RuntimeException ex) {
            showMessage(ex.getMessage());
        }
    }

    private String formatPrice(float price) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(price);
    }

    private String getStatusText(Product p) {
        if (p.isDeleted()) return "Đã xóa";
        if ("HIDDEN".equalsIgnoreCase(p.getStatus())) return "Đã rút";
        if (!"APPROVED".equalsIgnoreCase(p.getApprovalStatus())) return "Chưa công khai";
        if ("SOLD_OUT".equalsIgnoreCase(p.getStatus())) return "Hết hàng";
        return "Đang công khai";
    }

    private void showMessage(String msg) {
        lblMessage.setText(msg);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String seller = JOptionPane.showInputDialog(null,
                    "Nhập sellerId để giả lập đăng nhập (VD: 101):", "101");
            if (seller == null) return;
            String product = JOptionPane.showInputDialog(null,
                    "Nhập productId của tin cần chỉnh sửa (VD: 1):", "1");
            if (product == null) return;
            try {
                new UpdateListingFrm(Integer.parseInt(product.trim()),
                        Integer.parseInt(seller.trim())).setVisible(true);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "sellerId và productId phải là số");
            }
        });
    }
}