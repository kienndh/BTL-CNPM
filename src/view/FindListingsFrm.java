package view;

import dao.ProductDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import model.Product;
import model.User;

public class FindListingsFrm extends JFrame implements ActionListener {
    private User u;
    private JTextField txtKeyword;
    private JButton btnSearch;
    private JButton btnViewDetail;
    private JTable tblListings;
    private JLabel lblMessage;

    public FindListingsFrm() {
        this(null);
    }

    public FindListingsFrm(User u) {
        this.u = u;
        setTitle("Chợ PTITers - Tìm kiếm tin đăng");
        setSize(860, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        pnlSearch.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));

        pnlSearch.add(new JLabel("Từ khóa:"));
        txtKeyword = new JTextField(26);
        btnSearch = new JButton("Tìm kiếm");
        btnViewDetail = new JButton("Xem chi tiết / Đặt mua");

        btnSearch.addActionListener(this);
        btnViewDetail.addActionListener(this);

        pnlSearch.add(txtKeyword);
        pnlSearch.add(btnSearch);
        pnlSearch.add(btnViewDetail);
        add(pnlSearch, BorderLayout.NORTH);

        String[] cols = {"Mã tin", "Tên sản phẩm", "Giá bán (VNĐ)", "Số lượng còn lại", "Trạng thái", "Phân loại"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblListings = new JTable(model);
        tblListings.setRowHeight(26);
        tblListings.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblListings.getSelectedRow() != -1) {
                    openSelectedProductDetail();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblListings);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        add(scrollPane, BorderLayout.CENTER);

        lblMessage = new JLabel("Nháy đúp vào một dòng sản phẩm (hoặc chọn dòng rồi bấm Xem chi tiết) để đặt mua.", SwingConstants.CENTER);
        lblMessage.setForeground(Color.RED);
        lblMessage.setFont(new Font("Arial", Font.ITALIC, 12));
        lblMessage.setBorder(BorderFactory.createEmptyBorder(0, 10, 12, 10));
        add(lblMessage, BorderLayout.SOUTH);

        btnSearchClick();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnSearch) {
            btnSearchClick();
        } else if (e.getSource() == btnViewDetail) {
            openSelectedProductDetail();
        }
    }

    private void openSelectedProductDetail() {
        int row = tblListings.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tin đăng trong bảng trước!");
            return;
        }
        int productId = Integer.parseInt(tblListings.getValueAt(row, 0).toString());
        new ProductDetailFrm(u, productId).setVisible(true);
    }

    private void btnSearchClick() {
        String keyword = txtKeyword.getText();
        ProductDAO productDAO = new ProductDAO();
        List<Product> list = productDAO.findPublicListings(keyword);
        // Tin của người khác lên trước, tin của mình xuống cuối; trong mỗi nhóm: tin mới trước.
        if (u != null) {
            list.sort(Comparator
                    .comparingInt((Product p) -> p.getSellerId() == u.getUserId() ? 1 : 0)
                    .thenComparing(Comparator.comparingInt(Product::getProductId).reversed()));
        }

        displayListings(list);
        if (list.isEmpty()) {
            showMessage("Không tìm thấy tin đăng phù hợp!");
        } else {
            showMessage("Tìm thấy " + list.size() + " tin đăng. Tin của bạn nằm cuối danh sách và không thể tự đặt mua.");
        }
    }

    public void displayListings(List<Product> list) {
        DefaultTableModel model = (DefaultTableModel) tblListings.getModel();
        model.setRowCount(0);
        if (list == null) {
            return;
        }

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        for (Product p : list) {
            model.addRow(new Object[]{
                p.getProductId(),
                p.getTitle(),
                nf.format(p.getPrice()),
                p.getAvailableStock(),
                p.getStatus(),
                u != null && p.getSellerId() == u.getUserId() ? "Tin của bạn" : "Người khác"
            });
        }
    }

    private void showMessage(String msg) {
        lblMessage.setText(msg);
    }
}
