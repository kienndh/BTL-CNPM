package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
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
    private JButton btnLogout;
    private User u;

    public HomeFrm(User u) {
        this.u = u;
        setTitle("Chợ PTITers - Trang chủ");
        setSize(520, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        lblWelcome = new JLabel("", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 16));
        lblWelcome.setBorder(BorderFactory.createEmptyBorder(30, 15, 15, 15));
        displayWelcome();
        add(lblWelcome, BorderLayout.CENTER);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        btnFindListings = new JButton("Tìm kiếm tin đăng");
        btnLogout = new JButton("Đăng xuất");

        btnFindListings.addActionListener(this);
        btnLogout.addActionListener(this);

        pnlActions.add(btnFindListings);
        pnlActions.add(btnLogout);
        add(pnlActions, BorderLayout.SOUTH);
    }

    public void displayWelcome() {
        if (u != null) {
            lblWelcome.setText("Xin chào, " + u.getFullName() + " (@" + u.getUsername() + ")");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnFindListings) {
            new FindListingsFrm().setVisible(true);
        } else if (e.getSource() == btnLogout) {
            btnLogoutClick();
        }
    }

    private void btnLogoutClick() {
        this.u = null;
        JOptionPane.showMessageDialog(this, "Đăng xuất thành công!");
        new LoginFrm().setVisible(true);
        dispose();
    }
}