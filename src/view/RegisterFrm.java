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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import model.User;

public class RegisterFrm extends JFrame implements ActionListener {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JButton btnRegister;
    private JButton btnBack;
    private JLabel lblMessage;

    public RegisterFrm() {
        setTitle("Chợ PTITers - Đăng ký tài khoản");
        setSize(460, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel lblTitle = new JLabel("ĐĂNG KÝ TÀI KHOẢN MỚI", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        add(lblTitle, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridLayout(7, 2, 10, 10));
        pnlForm.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        pnlForm.add(new JLabel("Tên đăng nhập (*):"));
        txtUsername = new JTextField();
        pnlForm.add(txtUsername);

        pnlForm.add(new JLabel("Mật khẩu (*):"));
        txtPassword = new JPasswordField();
        pnlForm.add(txtPassword);

        pnlForm.add(new JLabel("Xác nhận mật khẩu (*):"));
        txtConfirmPassword = new JPasswordField();
        pnlForm.add(txtConfirmPassword);

        pnlForm.add(new JLabel("Họ và tên (*):"));
        txtFullName = new JTextField();
        pnlForm.add(txtFullName);

        pnlForm.add(new JLabel("Email (*):"));
        txtEmail = new JTextField();
        pnlForm.add(txtEmail);

        pnlForm.add(new JLabel("Số điện thoại:"));
        txtPhone = new JTextField();
        pnlForm.add(txtPhone);

        btnRegister = new JButton("Đăng ký");
        btnBack = new JButton("Quay lại");
        btnRegister.addActionListener(this);
        btnBack.addActionListener(this);

        pnlForm.add(btnRegister);
        pnlForm.add(btnBack);
        add(pnlForm, BorderLayout.CENTER);

        lblMessage = new JLabel(" ", SwingConstants.CENTER);
        lblMessage.setForeground(Color.RED);
        lblMessage.setBorder(BorderFactory.createEmptyBorder(0, 10, 15, 10));
        add(lblMessage, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRegister) {
            btnRegisterClick();
        } else if (e.getSource() == btnBack) {
            btnBackClick();
        }
    }

    private boolean validateInput() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String confirm = new String(txtConfirmPassword.getPassword()).trim();
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
            showMessage("Vui lòng nhập đầy đủ các trường bắt buộc (*)");
            return false;
        }
        if (!password.equals(confirm)) {
            showMessage("Mật khẩu xác nhận không khớp!");
            return false;
        }
        if (!email.contains("@")) {
            showMessage("Địa chỉ email không hợp lệ!");
            return false;
        }
        return true;
    }

    private void btnRegisterClick() {
        if (!validateInput()) {
            return;
        }

        User u = new User();
        u.setUsername(txtUsername.getText().trim());
        u.setPassword(new String(txtPassword.getPassword()).trim());
        u.setFullName(txtFullName.getText().trim());
        u.setEmail(txtEmail.getText().trim());
        u.setPhone(txtPhone.getText().trim());

        UserDAO userDAO = new UserDAO();
        if (userDAO.register(u)) {
            JOptionPane.showMessageDialog(this, "Đăng ký thành công! Vui lòng đăng nhập.");
            new LoginFrm().setVisible(true);
            dispose();
        } else {
            showMessage("Tên đăng nhập hoặc email đã tồn tại!");
        }
    }

    private void btnBackClick() {
        new LoginFrm().setVisible(true);
        dispose();
    }

    private void showMessage(String msg) {
        lblMessage.setText(msg);
    }
}