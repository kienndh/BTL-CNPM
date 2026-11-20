package view;

import dao.ProductDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import model.Product;

public class FindListingsFrm extends JFrame implements ActionListener {
    private JTextField txtKeyword;
    private JButton btnSearch;
    private JTable tblListings;
    private JLabel lblMessage;

    public FindListingsFrm() {
        setTitle("Chợ PTITers - Tìm kiếm tin đăng");
        setSize(720, 440);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        pnlSearch.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));

        pnlSearch.add(new JLabel("Từ khóa:"));
        txtKeyword = new JTextField(28);
        btnSearch = new JButton("Tìm kiếm");
        btnSearch.addActionListener(this);

        pnlSearch.add(txtKeyword);
        pnlSearch.add(btnSearch);
        add(pnlSearch, BorderLayout.NORTH);

        String[] cols = {"Mã tin", "Tên sản phẩm", "Giá bán (VNĐ)", "Số lượng còn lại", "Trạng thái"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblListings = new JTable(model);
        tblListings.setRowHeight(26);

        JScrollPane scrollPane = new JScrollPane(tblListings);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        add(scrollPane, BorderLayout.CENTER);

        lblMessage = new JLabel(" ", SwingConstants.CENTER);
        lblMessage.setForeground(Color.RED);
        lblMessage.setFont(new Font("Arial", Font.ITALIC, 12));
        lblMessage.setBorder(BorderFactory.createEmptyBorder(0, 10, 12, 10));
        add(lblMessage, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnSearch) {
            btnSearchClick();
        }
    }

    private void btnSearchClick() {
        String keyword = txtKeyword.getText();
        ProductDAO productDAO = new ProductDAO();
        List<Product> list = productDAO.findPublicListings(keyword);

        displayListings(list);
        if (list.isEmpty()) {
            showMessage("Không tìm thấy tin đăng phù hợp!");
        } else {
            showMessage("Tìm thấy " + list.size() + " tin đăng phù hợp.");
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
                p.getStatus()
            });
        }
    }

    private void showMessage(String msg) {
        lblMessage.setText(msg);
    }
}