package view;

import dao.UserDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import model.User;

public class LoginFrm extends JFrame implements ActionListener {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegister;
    private JLabel lblMessage;

    public LoginFrm() {
        setTitle("Chợ PTITers - Đăng nhập");
        setSize(420, 290);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel lblTitle = new JLabel("ĐĂNG NHẬP HỆ THỐNG", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        add(lblTitle, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridLayout(3, 2, 10, 12));
        pnlForm.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        pnlForm.add(new JLabel("Tên đăng nhập:"));
        txtUsername = new JTextField();
        pnlForm.add(txtUsername);

        pnlForm.add(new JLabel("Mật khẩu:"));
        txtPassword = new JPasswordField();
        pnlForm.add(txtPassword);

        btnLogin = new JButton("Đăng nhập");
        btnRegister = new JButton("Đăng ký");
        btnLogin.addActionListener(this);
        btnRegister.addActionListener(this);

        pnlForm.add(btnLogin);
        pnlForm.add(btnRegister);
        add(pnlForm, BorderLayout.CENTER);

        lblMessage = new JLabel(" ", SwingConstants.CENTER);
        lblMessage.setForeground(Color.RED);
        lblMessage.setBorder(BorderFactory.createEmptyBorder(0, 10, 15, 10));
        add(lblMessage, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnLogin) {
            btnLoginClick();
        } else if (e.getSource() == btnRegister) {
            btnRegisterClick();
        }
    }

    private void btnLoginClick() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        UserDAO userDAO = new UserDAO();
        User u = userDAO.login(username, password);

        if (u != null) {
            new HomeFrm(u).setVisible(true);
            dispose();
        } else {
            showMessage("Sai thông tin đăng nhập hoặc tài khoản bị khóa!");
        }
    }

    private void btnRegisterClick() {
        new RegisterFrm().setVisible(true);
        dispose();
    }

    private void showMessage(String msg) {
        lblMessage.setText(msg);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrm().setVisible(true));
    }
}