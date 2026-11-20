package view;

import dao.OrderDAO;
import model.Order;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConfirmOrderFrm extends JFrame implements ActionListener {
    private JLabel lblOrderCode;
    private JTextArea txtPaymentNote;
    private JLabel lblError;
    private JButton btnClose;
    private JButton btnConfirm;
    private User u;
    private Order o;
    private OrderDetailFrm frmDetail;

    public ConfirmOrderFrm(User u, Order o, OrderDetailFrm f) {
        super("Chấp nhận đơn hàng");

        this.u = u;
        this.o = o;
        this.frmDetail = f;

        lblOrderCode = new JLabel(
                o != null ? "Mã đơn: #" + o.getOrderId() : "Mã đơn: --"
        );

        txtPaymentNote = new JTextArea(4, 30);
        txtPaymentNote.setLineWrap(true);
        txtPaymentNote.setWrapStyleWord(true);

        lblError = new JLabel(" ");
        lblError.setForeground(Color.RED);

        btnClose = new JButton("Đóng");
        btnConfirm = new JButton("Xác nhận");

        btnClose.addActionListener(this);
        btnConfirm.addActionListener(this);

        JPanel notePanel = new JPanel(new BorderLayout(0, 6));
        notePanel.add(
                new JLabel("Ghi chú thanh toán (không bắt buộc):"),
                BorderLayout.NORTH
        );
        notePanel.add(
                new JScrollPane(txtPaymentNote),
                BorderLayout.CENTER
        );

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );
        buttons.add(btnClose);
        buttons.add(btnConfirm);

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.add(lblError, BorderLayout.CENTER);
        bottom.add(buttons, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(10, 12));
        content.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );
        content.add(lblOrderCode, BorderLayout.NORTH);
        content.add(notePanel, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);

        setContentPane(content);
        setSize(580, 300);
        setLocationRelativeTo(frmDetail);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void btnCloseClick() {
        dispose();
    }

    private void btnConfirmClick() {
        lblError.setText(" ");

        if (u == null || u.getUserId() <= 0) {
            lblError.setText("Người dùng chưa đăng nhập.");
            return;
        }

        if (o == null || o.getOrderId() <= 0) {
            lblError.setText("Đơn hàng không hợp lệ.");
            return;
        }

        String note = txtPaymentNote.getText().trim();

        if (!note.isEmpty()) {
            lblError.setText(
                    "CSDL chưa hỗ trợ lưu ghi chú. Vui lòng để trống."
            );
            return;
        }

        btnConfirm.setEnabled(false);

        try {
            OrderDAO orderDAO = new OrderDAO();
            boolean success = orderDAO.confirmOrder(u, o, note);

            if (!success) {
                lblError.setText("Không thể chấp nhận đơn hàng.");
                btnConfirm.setEnabled(true);
                return;
            }

            dispose();

            if (frmDetail != null) {
                frmDetail.reloadOrder();
            }
        } catch (Exception ex) {
            lblError.setText(
                    ex.getMessage() != null
                            ? ex.getMessage()
                            : "Có lỗi khi chấp nhận đơn hàng."
            );
            btnConfirm.setEnabled(true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnClose) {
            btnCloseClick();
        } else if (e.getSource() == btnConfirm) {
            btnConfirmClick();
        }
    }
}