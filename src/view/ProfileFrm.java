package view;

import dao.UserDAO;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProfileFrm extends JFrame implements ActionListener {
    private JTextField txtPhone;
    private JButton btnSave;
    private JButton btnMyListings;
    private JLabel lblName, lblEmail, lblMessage;
    private User u;
    private final UserDAO userDAO = new UserDAO();

    public ProfileFrm(User u) {
        super("Hồ sơ cá nhân");
        this.u = u;
        initComponents();

        try {
            this.u = userDAO.getMyProfile(u.getUserId());
            displayProfile(this.u);
        } catch (RuntimeException ex) {
            btnSave.setEnabled(false);
            btnMyListings.setEnabled(false);
            showMessage(ex.getMessage());
        }
    }

    private void initComponents() {
        lblName = new JLabel();
        lblEmail = new JLabel();
        txtPhone = new JTextField(15);
        btnSave = new JButton("Lưu");
        btnMyListings = new JButton("Tin đăng của tôi");
        lblMessage = new JLabel(" ");
        btnSave.addActionListener(this);
        btnMyListings.addActionListener(this);

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Họ tên:"));
        form.add(lblName);
        form.add(new JLabel("Email:"));
        form.add(lblEmail);
        form.add(new JLabel("Số điện thoại:"));
        form.add(txtPhone);

        JPanel actions = new JPanel(new GridLayout(1, 2, 8, 0));
        actions.add(btnSave);
        actions.add(btnMyListings);

        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.add(actions, BorderLayout.NORTH);
        bottom.add(lblMessage, BorderLayout.SOUTH);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        root.add(form, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
        setSize(400, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnSave) {
            btnSaveClick();
        } else if (e.getSource() == btnMyListings) {
            new MyListingsFrm(u).setVisible(true);
        }
    }

    private void displayProfile(User u) {
        lblName.setText(u.getFullName());
        lblEmail.setText(u.getEmail());
        txtPhone.setText(u.getPhone() == null ? "" : u.getPhone());
    }

    private void btnSaveClick() {
        String phone = txtPhone.getText().trim();
        try {
            User uUpdated = userDAO.updateProfile(u.getUserId(), phone);
            this.u = uUpdated;
            displayProfile(uUpdated);
            showMessage("Cập nhật hồ sơ thành công");
        } catch (RuntimeException ex) {
            showMessage(ex.getMessage());
        }
    }

    private void showMessage(String msg) {
        lblMessage.setText(msg);
    }


    public static void main(String[] args) {
        User current = new User();
        current.setUserId(101);
        SwingUtilities.invokeLater(() -> new ProfileFrm(current).setVisible(true));
    }
}