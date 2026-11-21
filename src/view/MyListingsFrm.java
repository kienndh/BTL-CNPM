package view;

import dao.ProductDAO;
import model.Product;
import model.User;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Danh sách tin thuộc tài khoản hiện tại: cầu nối tới UC07 và UC08 đã có. */
public class MyListingsFrm extends JFrame {
    private final User user;
    private final ProductDAO productDAO = new ProductDAO();
    private final JTable tblListings;
    private final JButton btnDetail = new JButton("Xem chi tiết");
    private final JButton btnEdit = new JButton("Chỉnh sửa giá");
    private final JButton btnRemove = new JButton("Rút tin");
    private final JButton btnRefresh = new JButton("Làm mới");
    private final JLabel lblMessage = new JLabel(" ");
    private List<Product> listings = new ArrayList<>();

    public MyListingsFrm(User user) {
        super("Chợ PTITers - Tin đăng của tôi");
        this.user = user;

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Mã tin", "Tên tin", "Giá bán (VNĐ)", "Tồn kho", "Đã giữ", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblListings = new JTable(model);
        tblListings.setRowHeight(26);
        tblListings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblListings.getSelectionModel().addListSelectionListener(this::selectionChanged);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(btnRefresh);
        buttons.add(btnDetail);
        buttons.add(btnEdit);
        buttons.add(btnRemove);
        btnRefresh.addActionListener(e -> loadListings());
        btnDetail.addActionListener(e -> openDetail());
        btnEdit.addActionListener(e -> openEdit());
        btnRemove.addActionListener(e -> openRemove());

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.add(lblMessage, BorderLayout.CENTER);
        bottom.add(buttons, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(new JLabel("Chọn một tin để xem, chỉnh sửa giá hoặc rút khỏi danh sách công khai."),
                BorderLayout.NORTH);
        content.add(new JScrollPane(tblListings), BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);
        setContentPane(content);
        setSize(900, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        updateButtons();
        loadListings();
    }

    private void loadListings() {
        DefaultTableModel model = (DefaultTableModel) tblListings.getModel();
        model.setRowCount(0);
        listings = new ArrayList<>();
        updateButtons();
        if (user == null || user.getUserId() <= 0) {
            lblMessage.setText("Bạn cần đăng nhập để quản lý tin đăng.");
            return;
        }
        try {
            listings = productDAO.getMyListings(user.getUserId());
            NumberFormat currency = NumberFormat.getInstance(new Locale("vi", "VN"));
            for (Product p : listings) {
                model.addRow(new Object[]{
                        p.getProductId(), p.getTitle(), currency.format(p.getPrice()),
                        p.getStock(), p.getReservedStock(), statusText(p)
                });
            }
            lblMessage.setText(listings.isEmpty() ? "Bạn chưa có tin đăng nào." :
                    "Tổng cộng " + listings.size() + " tin đăng (bao gồm tin đã rút).");
        } catch (RuntimeException ex) {
            lblMessage.setText(ex.getMessage());
        }
        updateButtons();
    }

    private Product selectedProduct() {
        int row = tblListings.getSelectedRow();
        return row >= 0 && row < listings.size() ? listings.get(row) : null;
    }

    private void selectionChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            updateButtons();
        }
    }

    private void updateButtons() {
        Product p = selectedProduct();
        boolean chosen = p != null;
        boolean hidden = chosen && "HIDDEN".equalsIgnoreCase(p.getStatus());
        btnDetail.setEnabled(chosen);
        btnEdit.setEnabled(chosen && !hidden);
        btnRemove.setEnabled(chosen && !hidden && p.getReservedStock() == 0);
        if (chosen && !hidden && p.getReservedStock() > 0) {
            lblMessage.setText("Tin đang có đơn chưa xử lý xong nên chưa thể rút.");
        } else if (lblMessage.getText().startsWith("Tin đang có đơn")) {
            lblMessage.setText("Tổng cộng " + listings.size() + " tin đăng (bao gồm tin đã rút).");
        }
    }

    private void openDetail() {
        Product p = selectedProduct();
        if (p != null) {
            new ProductDetailFrm(user, p.getProductId()).setVisible(true);
        }
    }

    private void openEdit() {
        Product p = selectedProduct();
        if (p != null) {
            showAndRefreshOnClose(new UpdateListingFrm(p.getProductId(), user.getUserId()));
        }
    }

    private void openRemove() {
        Product p = selectedProduct();
        if (p != null) {
            showAndRefreshOnClose(new RemoveListingFrm(p.getProductId(), user.getUserId()));
        }
    }

    private void showAndRefreshOnClose(JFrame frame) {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                loadListings();
            }
        });
        frame.setVisible(true);
    }

    private String statusText(Product p) {
        if (p.isDeleted()) return "Đã xóa";
        if ("HIDDEN".equalsIgnoreCase(p.getStatus())) return "Đã rút";
        if (!"APPROVED".equalsIgnoreCase(p.getApprovalStatus())) return "Chưa công khai";
        if ("SOLD_OUT".equalsIgnoreCase(p.getStatus())) return "Hết hàng";
        return "Đang công khai";
    }
}
