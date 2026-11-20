package view;

import dao.OrderDAO;
import model.Order;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CancelOrderFrm extends JFrame implements ActionListener {
    private JLabel lblOrderCode;
    private JLabel lblProductSummary;
    private JLabel lblMessage;
    private JTextArea txtReason;
    private JButton btnConfirmCancel;
    private JButton btnKeepOrder;
    private User u;
    private Order o;
    private boolean cancelled;

    private final OrderDetailFrm owner;

    public CancelOrderFrm(OrderDetailFrm owner, User u, Order o) {
        super("Xác nhận hủy đơn hàng");
        this.owner = owner;
        this.u = u;
        this.o = o;
        this.cancelled = false;

        lblOrderCode = new JLabel();
        lblProductSummary = new JLabel();
        lblMessage = new JLabel();
        lblMessage.setForeground(Color.RED);

        txtReason = new JTextArea(4, 26);
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);

        btnConfirmCancel = new JButton("Xác nhận hủy");
        btnConfirmCancel.addActionListener(this);

        btnKeepOrder = new JButton("Giữ đơn");
        btnKeepOrder.addActionListener(this);

        if (o != null) {
            lblOrderCode.setText("#" + o.getOrderId());
            String summary = (o.getItems() != null && !o.getItems().isEmpty())
                    ? o.getItems().get(0).getProductTitle() + " - " + String.format("%,.0f đ", o.getTotalAmount())
                    : String.format("%,.0f đ", o.getTotalAmount());
            lblProductSummary.setText(summary);
        }

        JPanel info = new JPanel(new GridLayout(2, 2, 8, 6));
        info.add(new JLabel("Mã đơn:"));
        info.add(lblOrderCode);
        info.add(new JLabel("Sản phẩm / Tổng tiền:"));
        info.add(lblProductSummary);

        JPanel reasonPanel = new JPanel(new BorderLayout(6, 6));
        reasonPanel.add(new JLabel("Lý do hủy đơn:"), BorderLayout.NORTH);
        reasonPanel.add(new JScrollPane(txtReason), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(btnKeepOrder);
        buttons.add(btnConfirmCancel);

        JPanel bottom = new JPanel(new BorderLayout(6, 6));
        bottom.add(lblMessage, BorderLayout.CENTER);
        bottom.add(buttons, BorderLayout.EAST);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        root.add(info, BorderLayout.NORTH);
        root.add(reasonPanel, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
        setSize(460, 320);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    private void btnConfirmCancelClick() {
        String reason = txtReason.getText().trim();
        if (reason.isEmpty()) {
            showMessage("Vui lòng nhập lý do hủy đơn.");
            return;
        }
        try {
            OrderDAO orderDAO = new OrderDAO();
            boolean ok = orderDAO.cancelOrder(u, o.getOrderId(), reason);
            if (ok) {
                this.cancelled = true;
                if (owner != null) {
                    owner.reloadOrder();
                }
                dispose();
            }
        } catch (Exception ex) {
            showMessage(ex.getMessage());
        }
    }

    private void btnKeepOrderClick() {
        this.cancelled = false;
        dispose();
    }

    private void showMessage(String msg) {
        lblMessage.setText(msg != null ? msg : "");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnConfirmCancel) {
            btnConfirmCancelClick();
        } else if (e.getSource() == btnKeepOrder) {
            btnKeepOrderClick();
        }
    }
}