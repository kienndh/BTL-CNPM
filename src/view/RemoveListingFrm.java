package view;

import dao.ProductDAO;
import dao.UserDAO;
import model.Product;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Locale;

public class RemoveListingFrm extends JFrame implements ActionListener {
    private JLabel lblTitle, lblPrice, lblStatus, lblMessage;
    private JButton btnRemove, btnCancel;
    private Product p;

    private final int productId;
    private final int sellerId;
    private final UserDAO userDAO = new UserDAO();
    private final ProductDAO productDAO = new ProductDAO();

    public RemoveListingFrm(int productId, int sellerId) {
        super("Rút tin đăng");
        this.productId = productId;
        this.sellerId = sellerId;
        initComponents();

        try {
            if (!userDAO.checkSellerEligibility(sellerId)) {
                btnRemove.setEnabled(false);
                showMessage("Tài khoản chưa đủ điều kiện rút tin (bị khóa hoặc chưa có số điện thoại)");
                return;
            }
            p = productDAO.getMyListingDetail(productId, sellerId);
            displayProduct(p);
            if ("HIDDEN".equalsIgnoreCase(p.getStatus())) {
                btnRemove.setEnabled(false);
                showMessage("Tin đã được rút trước đó");
            } else {
                showMessage("Bạn có chắc muốn rút tin này khỏi danh sách công khai?");
            }
        } catch (RuntimeException ex) {
            btnRemove.setEnabled(false);
            showMessage(ex.getMessage());
        }
    }

    private void initComponents() {
        lblTitle = new JLabel("-");
        lblPrice = new JLabel("-");
        lblStatus = new JLabel("-");
        lblMessage = new JLabel(" ");
        btnRemove = new JButton("Rút tin");
        btnCancel = new JButton("Hủy");
        btnRemove.addActionListener(this);
        btnCancel.addActionListener(this);

        JPanel info = new JPanel(new GridLayout(3, 2, 8, 8));
        info.add(new JLabel("Tên tin:"));
        info.add(lblTitle);
        info.add(new JLabel("Giá bán:"));
        info.add(lblPrice);
        info.add(new JLabel("Trạng thái:"));
        info.add(lblStatus);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.add(btnRemove);
        buttons.add(btnCancel);

        JPanel bottom = new JPanel(new BorderLayout(5, 8));
        bottom.add(lblMessage, BorderLayout.NORTH);
        bottom.add(buttons, BorderLayout.SOUTH);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        root.add(info, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
        setSize(440, 230);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRemove) {
            btnRemoveClick();
        } else if (e.getSource() == btnCancel) {
            dispose();
        }
    }

    private void displayProduct(Product p) {
        lblTitle.setText(p.getTitle());
        lblPrice.setText(NumberFormat.getInstance(new Locale("vi", "VN")).format(p.getPrice()) + " đ");
        lblStatus.setText(getStatusText(p));
    }

    private void btnRemoveClick() {
        try {
            Product pRemoved = productDAO.removeListing(productId, sellerId);
            this.p = pRemoved;
            displayProduct(pRemoved);
            btnRemove.setEnabled(false);
            btnCancel.setText("Đóng");
            showMessage("Rút tin đăng thành công");
        } catch (RuntimeException ex) {
            showMessage(ex.getMessage());
        }
    }

    /** Đổi trạng thái trong CSDL sang chữ tiếng Việt để hiển thị. */
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
                    "Nhập productId của tin cần rút (VD: 1):", "1");
            if (product == null) return;
            try {
                new RemoveListingFrm(Integer.parseInt(product.trim()),
                        Integer.parseInt(seller.trim())).setVisible(true);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "sellerId và productId phải là số");
            }
        });
    }
}