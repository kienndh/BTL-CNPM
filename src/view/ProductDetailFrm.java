package view;

import dao.ProductDAO;
import model.Product;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProductDetailFrm extends JFrame implements ActionListener {
    private JLabel lblProductTitle;
    private JLabel lblSeller;
    private JLabel lblAvailableStock;
    private JLabel lblSaleType;
    private JLabel lblListingStatus;
    private JButton btnOrder;
    private User u;
    private Product p;

    private final int productId;

    public ProductDetailFrm(User u, int productId) {
        super("Chi tiết sản phẩm");
        this.u = u;
        this.productId = productId;

        lblProductTitle = new JLabel();
        lblSeller = new JLabel();
        lblAvailableStock = new JLabel();
        lblSaleType = new JLabel();
        lblListingStatus = new JLabel();
        btnOrder = new JButton("Đặt mua");
        btnOrder.addActionListener(this);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panel.add(new JLabel("Tên sản phẩm:"));
        panel.add(lblProductTitle);
        panel.add(new JLabel("Người bán (ID):"));
        panel.add(lblSeller);
        panel.add(new JLabel("Số lượng có thể đặt:"));
        panel.add(lblAvailableStock);
        panel.add(new JLabel("Đơn giá:"));
        panel.add(lblSaleType);
        panel.add(new JLabel("Trạng thái tin:"));
        panel.add(lblListingStatus);
        panel.add(new JLabel());
        panel.add(btnOrder);

        setContentPane(panel);
        loadProduct();

        setSize(520, 320);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void loadProduct() {
        ProductDAO productDAO = new ProductDAO();
        this.p = productDAO.getProductDetail(productId);

        if (this.p == null) {
            lblProductTitle.setText("Không tìm thấy sản phẩm");
            btnOrder.setEnabled(false);
            return;
        }

        int available = p.getStock() - p.getReservedStock();
        lblProductTitle.setText(p.getTitle());
        lblSeller.setText("User #" + p.getSellerId());
        lblAvailableStock.setText(String.valueOf(available));
        lblSaleType.setText(String.format("%,.0f đ", p.getPrice()));
        lblListingStatus.setText(p.getStatus());

        boolean canOrder = u != null
                && "ACTIVE".equalsIgnoreCase(u.getStatus())
                && u.getUserId() != p.getSellerId()
                && !p.isDeleted()
                && "AVAILABLE".equalsIgnoreCase(p.getStatus())
                && "APPROVED".equalsIgnoreCase(p.getApprovalStatus())
                && "CLEAR".equalsIgnoreCase(p.getModerationStatus())
                && available > 0;
        btnOrder.setEnabled(canOrder);
    }

    private void btnOrderClick() {
        if (this.p == null) {
            return;
        }
        new PlaceOrderFrm(u, p).setVisible(true);
        dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnOrder) {
            btnOrderClick();
        }
    }
}