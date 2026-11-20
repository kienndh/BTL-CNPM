package com.ptit.choptiters.view;

import com.ptit.choptiters.dao.CategoryDAO;
import com.ptit.choptiters.dao.ProductDAO;
import com.ptit.choptiters.dao.ProductImageDAO;
import com.ptit.choptiters.dao.UserDAO;
import com.ptit.choptiters.model.Category;
import com.ptit.choptiters.model.Product;
import com.ptit.choptiters.model.ProductImage;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CreateListingFrm extends JFrame implements ActionListener {
    private JTextField txtTitle, txtPrice, txtStock, txtImages;
    private JTextArea txtDescription;
    private JComboBox<Category> cboCategory;
    private JButton btnCreate, btnChooseImages;
    private JLabel lblMessage;

    private final int sellerId;
    private final UserDAO userDAO = new UserDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final ProductImageDAO productImageDAO = new ProductImageDAO();

    public CreateListingFrm(int sellerId) {
        super("Tạo tin đăng");
        this.sellerId = sellerId;
        initComponents();

        try {
            if (!userDAO.checkSellerEligibility(sellerId)) {
                btnCreate.setEnabled(false);
                showMessage("Tài khoản chưa đủ điều kiện tạo tin (bị khóa hoặc chưa có số điện thoại)");
                return;
            }
            loadCategories();
        } catch (RuntimeException ex) {
            btnCreate.setEnabled(false);
            showMessage(ex.getMessage());
        }
    }

    private void initComponents() {
        txtTitle = new JTextField(25);
        cboCategory = new JComboBox<>();
        txtPrice = new JTextField();
        txtStock = new JTextField("1");
        txtDescription = new JTextArea(4, 25);
        txtDescription.setLineWrap(true);
        txtImages = new JTextField();
        txtImages.setToolTipText("Bấm \"Chọn ảnh...\" để chọn. Ảnh đầu tiên là ảnh đại diện");
        btnCreate = new JButton("Tạo tin");
        lblMessage = new JLabel(" ");
        btnCreate.addActionListener(this);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        addRow(form, c, 0, "Tên tin:", txtTitle);
        addRow(form, c, 1, "Danh mục:", cboCategory);
        addRow(form, c, 2, "Giá bán (đ):", txtPrice);
        addRow(form, c, 3, "Số lượng:", txtStock);
        addRow(form, c, 4, "Mô tả:", new JScrollPane(txtDescription));
        btnChooseImages = new JButton("Chọn ảnh...");
        btnChooseImages.addActionListener(this);
        txtImages.setEditable(false);

        JPanel imagePanel = new JPanel(new BorderLayout(5, 0));
        imagePanel.add(txtImages, BorderLayout.CENTER);
        imagePanel.add(btnChooseImages, BorderLayout.EAST);
        addRow(form, c, 5, "Hình ảnh:", imagePanel);

        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.add(btnCreate, BorderLayout.NORTH);
        bottom.add(lblMessage, BorderLayout.SOUTH);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        root.add(form, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridy = row;
        c.gridx = 0; c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1; c.weightx = 1;
        panel.add(field, c);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnCreate) {
            btnCreateClick();
        } else if (e.getSource() == btnChooseImages) {
            btnChooseImagesClick();
        }
    }

    private void btnChooseImagesClick() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn hình ảnh sản phẩm");
        chooser.setMultiSelectionEnabled(true);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Ảnh (jpg, jpeg, png, gif)", "jpg", "jpeg", "png", "gif"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            StringBuilder paths = new StringBuilder();
            for (File f : chooser.getSelectedFiles()) {
                if (paths.length() > 0) paths.append(",");
                paths.append(f.getAbsolutePath());
            }
            txtImages.setText(paths.toString());   // ảnh đầu tiên là ảnh đại diện
        }
    }

    private void loadCategories() {
        List<Category> categories = categoryDAO.getCategories();
        cboCategory.removeAllItems();
        for (Category cat : categories) {
            cboCategory.addItem(cat);
        }
    }

    private void btnCreateClick() {
        // 1. Đọc và kiểm tra dữ liệu trên form
        Category category = (Category) cboCategory.getSelectedItem();
        if (txtTitle.getText().trim().isEmpty()) {
            showMessage("Vui lòng nhập tên tin");
            return;
        }
        if (category == null) {
            showMessage("Vui lòng chọn danh mục");
            return;
        }
        float price;
        int stock;
        try {
            price = Float.parseFloat(txtPrice.getText().trim().replace(".", "").replace(",", ""));
            stock = Integer.parseInt(txtStock.getText().trim());
        } catch (NumberFormatException ex) {
            showMessage("Giá bán và số lượng phải là số");
            return;
        }


        Product listingData = new Product();
        listingData.setSellerId(sellerId);
        listingData.setCategoryId(category.getCategoryId());
        listingData.setTitle(txtTitle.getText().trim());
        listingData.setPrice(price);
        listingData.setStock(stock);
        listingData.setDescription(txtDescription.getText().trim());


        List<ProductImage> images = new ArrayList<>();
        String[] urls = txtImages.getText().split(",");
        for (String url : urls) {
            if (!url.trim().isEmpty()) {
                images.add(new ProductImage(url.trim(), images.isEmpty(), images.size()));
            }
        }
        String imageError = validateImages(images);
        if (imageError != null) {
            showMessage(imageError);
            return;
        }
        listingData.setImages(images);

        try {
            Product p = productDAO.createListing(sellerId, listingData);
            productImageDAO.attachImages(p.getProductId(), images);
            displayProduct(p);
            showMessage("Tạo tin đăng thành công");
            resetForm();
        } catch (RuntimeException ex) {
            showMessage(ex.getMessage());
        }
    }

    private String validateImages(List<ProductImage> images) {
        if (images.isEmpty()) {
            return "Vui lòng chọn ít nhất 1 hình ảnh";
        }
        for (ProductImage img : images) {
            String path = img.getImageUrl();
            if (!path.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif)$")) {
                return "Ảnh không đúng định dạng (jpg, jpeg, png, gif): " + path;
            }
            if (!new File(path).isFile()) {
                return "Không tìm thấy file ảnh: " + path;
            }
        }
        return null;
    }

    private void resetForm() {
        txtTitle.setText("");
        txtPrice.setText("");
        txtStock.setText("1");
        txtDescription.setText("");
        txtImages.setText("");
        if (cboCategory.getItemCount() > 0) {
            cboCategory.setSelectedIndex(0);
        }
        txtTitle.requestFocus();
    }

    private void displayProduct(Product p) {
        String price = NumberFormat.getInstance(new Locale("vi", "VN")).format(p.getPrice());
        StringBuilder imgs = new StringBuilder();
        for (ProductImage img : p.getImages()) {
            imgs.append("\n  - ").append(img.getImageUrl()).append(img.isThumbnail() ? " (ảnh đại diện)" : "");
        }
        JOptionPane.showMessageDialog(this,
                "Mã tin: " + p.getProductId()
                        + "\nTên tin: " + p.getTitle()
                        + "\nGiá bán: " + price + " đ"
                        + "\nSố lượng: " + p.getStock()
                        + "\nHình ảnh:" + (imgs.length() == 0 ? " (không có)" : imgs),
                "Tin đăng vừa tạo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showMessage(String msg) {
        lblMessage.setText(msg);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String input = JOptionPane.showInputDialog(null,
                    "Nhập sellerId để giả lập đăng nhập (VD: 101):", "101");
            if (input == null) return;
            try {
                new CreateListingFrm(Integer.parseInt(input.trim())).setVisible(true);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "sellerId phải là số");
            }
        });
    }
}