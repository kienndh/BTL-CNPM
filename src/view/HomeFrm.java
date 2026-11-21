package view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import model.User;

public class HomeFrm extends JFrame implements ActionListener {
    private JLabel lblWelcome;
    private JButton btnFindListings;
    private JButton btnCreateListing;
    private JButton btnProfile;
    private JButton btnBuyingOrders;
    private JButton btnSellingOrders;
    private JButton btnLogout;
    private User u;

    public HomeFrm(User u) {
        this.u = u;
        setTitle("Chợ PTITers - Trang chủ");
        setSize(580, 320);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        lblWelcome = new JLabel("", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 16));
        lblWelcome.setBorder(BorderFactory.createEmptyBorder(20, 15, 10, 15));
        displayWelcome();
        add(lblWelcome, BorderLayout.NORTH);

        JPanel pnlActions = new JPanel(new GridLayout(3, 2, 12, 12));
        pnlActions.setBorder(BorderFactory.createEmptyBorder(10, 25, 25, 25));

        btnFindListings = new JButton("Tìm kiếm & Mua hàng");
        btnCreateListing = new JButton("Đăng tin bán mới");
        btnProfile = new JButton("Hồ sơ & Tin đăng của tôi");
        btnBuyingOrders = new JButton("Đơn mua của tôi");
        btnSellingOrders = new JButton("Đơn bán của tôi");
        btnLogout = new JButton("Đăng xuất");

        btnFindListings.addActionListener(this);
        btnCreateListing.addActionListener(this);
        btnProfile.addActionListener(this);
        btnBuyingOrders.addActionListener(this);
        btnSellingOrders.addActionListener(this);
        btnLogout.addActionListener(this);

        pnlActions.add(btnFindListings);
        pnlActions.add(btnCreateListing);
        pnlActions.add(btnProfile);
        pnlActions.add(btnBuyingOrders);
        pnlActions.add(btnSellingOrders);
        pnlActions.add(btnLogout);
        add(pnlActions, BorderLayout.CENTER);
    }

    public void displayWelcome() {
        if (u != null) {
            lblWelcome.setText("Xin chào, " + u.getFullName() + " (@" + u.getUsername() + ")");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnFindListings) {
            new FindListingsFrm(u).setVisible(true);
        } else if (e.getSource() == btnCreateListing) {
            new CreateListingFrm(u.getUserId()).setVisible(true);
        } else if (e.getSource() == btnProfile) {
            new ProfileFrm(u).setVisible(true);
        } else if (e.getSource() == btnBuyingOrders) {
            new BuyingOrdersFrm(u).setVisible(true);
        } else if (e.getSource() == btnSellingOrders) {
            new SellingOrdersFrm(u).setVisible(true);
        } else if (e.getSource() == btnLogout) {
            btnLogoutClick();
        }
    }

    private void btnLogoutClick() {
        this.u = null;
        JOptionPane.showMessageDialog(this, "Đăng xuất thành công!");
        // Đóng các cửa sổ thuộc phiên cũ để không thể tiếp tục thao tác sau đăng xuất.
        for (Window window : Window.getWindows()) {
            if (window != this && window.isDisplayable()) {
                window.dispose();
            }
        }
        dispose();
        new LoginFrm().setVisible(true);
    }
}
