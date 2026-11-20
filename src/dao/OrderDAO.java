package dao;

import model.Order;
import model.OrderItem;
import model.OrderStatusHistory;
import model.Product;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class OrderDAO extends DAO {
    public OrderDAO() {
        super();
    }

    public ArrayList<Order> listSellingOrders(User u, String status) {
        requireUser(u);
        ArrayList<Order> list = new ArrayList<>();
        boolean filterByStatus = status != null
                && !status.trim().isEmpty()
                && !"ALL".equalsIgnoreCase(status.trim());

        StringBuilder sql = new StringBuilder(
                "SELECT orderId, buyerId, sellerId, status, fulfillmentMethod, "
                        + "paymentMethod, paymentStatus, meetupPlace, cancelledBy "
                        + "FROM tblOrder WHERE sellerId = ? ");
        if (filterByStatus) {
            sql.append("AND status = ? ");
        }
        sql.append("ORDER BY orderId DESC");
        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            ps.setInt(1, u.getUserId());

            if (filterByStatus) {
                ps.setString(2, status.trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = mapOrder(rs);

                    List<OrderItem> items = fetchOrderItems(o.getOrderId());
                    o.setItems(items);
                    o.setTotalAmount(calcTotal(items));

                    list.add(o);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Lỗi tải danh sách đơn bán: " + e.getMessage(), e);
        }
        return list;
    }

    public Order getOrderDetail(User u, int orderId) throws SQLException {
        if (u == null || u.getUserId() <= 0) {
            throw new IllegalStateException("Người dùng chưa đăng nhập.");
        }

        String sql = "SELECT orderId, buyerId, sellerId, status, fulfillmentMethod, "
                + "paymentMethod, paymentStatus, meetupPlace, cancelledBy "
                + "FROM tblOrder WHERE orderId = ?";

        Order order;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "Không tìm thấy đơn hàng #" + orderId);
                }
                order = mapOrder(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Lỗi tải chi tiết đơn hàng: " + e.getMessage(), e);
        }
        if (order.getBuyerId() != u.getUserId()
                && order.getSellerId() != u.getUserId()) {
            throw new SecurityException(
                    "Bạn không có quyền xem đơn hàng này.");
        }
        List<OrderItem> items = fetchOrderItems(orderId);
        order.setItems(items);
        order.setTotalAmount(calcTotal(items));
        order.setStatusHistory(fetchStatusHistory(orderId));
        return order;
    }

    public boolean confirmOrder(User u, Order o, String note) {
        requireOrder(u, o);
        Connection db = con;
        beginTransaction(db);
        try {
            Order current = lockOrder(db, o.getOrderId());
            if (current.getSellerId() != u.getUserId()) {
                throw new SecurityException(
                        "Chỉ người bán của đơn mới được chấp nhận đơn.");
            }
            if (!"PENDING".equalsIgnoreCase(current.getStatus())) {
                throw new IllegalStateException(
                        "Chỉ có thể chấp nhận đơn đang chờ xác nhận.");
            }
            String sql = "UPDATE tblOrder SET status = 'CONFIRMED' "
                    + "WHERE orderId = ? AND status = 'PENDING'";
            try (PreparedStatement ps = db.prepareStatement(sql)) {
                ps.setInt(1, current.getOrderId());
                requireOneRow(ps.executeUpdate());
            }
            Timestamp now = new Timestamp(System.currentTimeMillis());

            OrderStatusHistory history = insertStatusHistory(
                    db,
                    current.getOrderId(),
                    current.getStatus(),
                    "CONFIRMED",
                    u.getUserId(),
                    now
            );
            db.commit();
            return true;
        } catch (SQLException | RuntimeException e) {
            throw rollbackAndWrap(db, e, "Lỗi chấp nhận đơn hàng");
        } finally {
            finishTransaction(db);
        }
    }

    public boolean markDelivering(User u, Order o) {
        requireOrder(u, o);
        Connection db = con;
        beginTransaction(db);
        try {
            Order current = lockOrder(db, o.getOrderId());

            if (current.getSellerId() != u.getUserId()) {
                throw new SecurityException(
                        "Chỉ người bán của đơn mới được đánh dấu đang giao.");
            }

            if (!"CONFIRMED".equalsIgnoreCase(current.getStatus())) {
                throw new IllegalStateException(
                        "Đơn hàng phải ở trạng thái đã xác nhận.");
            }

            if (!"SHIPPING".equalsIgnoreCase(current.getFulfillmentMethod())) {
                throw new IllegalStateException(
                        "Chỉ áp dụng cho đơn sử dụng hình thức giao hàng.");
            }

            String sql = "UPDATE tblOrder SET status = 'DELIVERING' "
                    + "WHERE orderId = ? AND status = 'CONFIRMED'";

            try (PreparedStatement ps = db.prepareStatement(sql)) {
                ps.setInt(1, current.getOrderId());
                requireOneRow(ps.executeUpdate());
            }
            Timestamp now = new Timestamp(System.currentTimeMillis());
            OrderStatusHistory history = insertStatusHistory(
                    db,
                    current.getOrderId(),
                    current.getStatus(),
                    "DELIVERING",
                    u.getUserId(),
                    now
            );
            db.commit();
            return true;
        } catch (SQLException | RuntimeException e) {
            throw rollbackAndWrap(db, e, "Lỗi đánh dấu đơn đang giao");

        } finally {
            finishTransaction(db);
        }
    }

    public ArrayList<Order> listBuyingOrders(User u, String status) {
        requireUser(u);
        ArrayList<Order> list = new ArrayList<>();
        boolean filterByStatus = status != null
                && !status.trim().isEmpty()
                && !"ALL".equalsIgnoreCase(status.trim());

        StringBuilder sql = new StringBuilder(
                "SELECT orderId, buyerId, sellerId, status, fulfillmentMethod, "
                        + "paymentMethod, paymentStatus, meetupPlace, cancelledBy "
                        + "FROM tblOrder WHERE buyerId = ? ");

        if (filterByStatus) {
            sql.append("AND status = ? ");
        }
        sql.append("ORDER BY orderId DESC");
        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            ps.setInt(1, u.getUserId());
            if (filterByStatus) {
                ps.setString(2, status.trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = mapOrder(rs);
                    List<OrderItem> items = fetchOrderItems(o.getOrderId());
                    o.setItems(items);
                    o.setTotalAmount(calcTotal(items));
                    list.add(o);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Lỗi tải danh sách đơn mua: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean completeOrder(User u, Order o) {
        requireOrder(u, o);
        Connection db = con;
        beginTransaction(db);
        try {
            Order current = lockOrder(db, o.getOrderId());
            if (current.getBuyerId() != u.getUserId()
                    && current.getSellerId() != u.getUserId()) {
                throw new SecurityException(
                        "Chỉ người mua hoặc người bán của đơn được hoàn tất."
                );
            }
            if (!"CONFIRMED".equalsIgnoreCase(current.getStatus())
                    || !"MEETUP".equalsIgnoreCase(current.getFulfillmentMethod())
                    || !"PAID".equalsIgnoreCase(current.getPaymentStatus())) {
                throw new IllegalStateException(
                        "Đơn phải gặp trực tiếp, đã xác nhận và đã thanh toán để hoàn tất."
                );
            }
            List<OrderItem> items = fetchOrderItems(current.getOrderId());
            if (items.isEmpty()) {
                throw new IllegalStateException(
                        "Đơn hàng không có dòng sản phẩm."
                );
            }
            Map<Integer, Integer> quantities = new TreeMap<>();
            for (OrderItem item : items) {
                if (item.getProductId() <= 0 || item.getQuantity() <= 0) {
                    throw new IllegalStateException(
                            "Dữ liệu dòng sản phẩm không hợp lệ."
                    );
                }
                int previousQuantity =
                        quantities.getOrDefault(item.getProductId(), 0);
                quantities.put(
                        item.getProductId(),
                        Math.addExact(previousQuantity, item.getQuantity())
                );
            }
            for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
                int productId = entry.getKey();
                int quantity = entry.getValue();
                Product product;
                String lockProductSql =
                        "SELECT productId, stock, reservedStock "
                                + "FROM tblProduct "
                                + "WHERE productId = ? FOR UPDATE";

                try (PreparedStatement ps =
                             db.prepareStatement(lockProductSql)) {

                    ps.setInt(1, productId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException(
                                    "Không tìm thấy sản phẩm #" + productId
                            );
                        }
                        product = new Product();
                        product.setProductId(rs.getInt("productId"));
                        product.setStock(rs.getInt("stock"));
                        product.setReservedStock(
                                rs.getInt("reservedStock")
                        );
                    }
                }
                if (product.getStock() < quantity
                        || product.getReservedStock() < quantity) {
                    throw new IllegalStateException(
                            "Tồn kho hoặc số lượng giữ chỗ không đủ "
                                    + "cho sản phẩm #" + productId
                    );
                }
                String updateProductSql =
                        "UPDATE tblProduct "
                                + "SET stock = stock - ?, "
                                + "reservedStock = reservedStock - ? "
                                + "WHERE productId = ? "
                                + "AND stock >= ? "
                                + "AND reservedStock >= ?";

                try (PreparedStatement ps =
                             db.prepareStatement(updateProductSql)) {
                    ps.setInt(1, quantity);
                    ps.setInt(2, quantity);
                    ps.setInt(3, productId);
                    ps.setInt(4, quantity);
                    ps.setInt(5, quantity);
                    requireOneRow(ps.executeUpdate());
                }
            }
            String updateOrderSql =
                    "UPDATE tblOrder SET status = 'COMPLETED' "
                            + "WHERE orderId = ? AND status = 'CONFIRMED'";
            try (PreparedStatement ps =
                         db.prepareStatement(updateOrderSql)) {

                ps.setInt(1, current.getOrderId());
                requireOneRow(ps.executeUpdate());
            }
            Timestamp now = new Timestamp(System.currentTimeMillis());
            insertStatusHistory(
                    db,
                    current.getOrderId(),
                    current.getStatus(),
                    "COMPLETED",
                    u.getUserId(),
                    now
            );
            db.commit();
            return true;
        } catch (SQLException | RuntimeException e) {
            throw rollbackAndWrap(
                    db,
                    e,
                    "Lỗi hoàn tất đơn hàng"
            );
        } finally {
            finishTransaction(db);
        }
    }
    private List<OrderItem> fetchOrderItems(int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();

        String sql = "SELECT orderItemId, orderId, productId, productTitle, "
                + "quantity, priceAtPurchase "
                + "FROM tblOrderItem WHERE orderId = ? "
                + "ORDER BY orderItemId ASC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setOrderItemId(rs.getInt("orderItemId"));
                    item.setOrderId(rs.getInt("orderId"));
                    item.setProductId(rs.getInt("productId"));
                    item.setProductTitle(rs.getString("productTitle"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPriceAtPurchase(rs.getFloat("priceAtPurchase"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private List<OrderStatusHistory> fetchStatusHistory(int orderId)
            throws SQLException {
        List<OrderStatusHistory> list = new ArrayList<>();

        String sql = "SELECT historyId, orderId, fromStatus, toStatus, "
                + "changedBy, createdAt "
                + "FROM tblOrderStatusHistory WHERE orderId = ? "
                + "ORDER BY createdAt ASC, historyId ASC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderStatusHistory history = new OrderStatusHistory();
                    history.setHistoryId(rs.getInt("historyId"));
                    history.setOrderId(rs.getInt("orderId"));
                    history.setFromStatus(rs.getString("fromStatus"));
                    history.setToStatus(rs.getString("toStatus"));

                    int changedBy = rs.getInt("changedBy");
                    history.setChangedBy(rs.wasNull() ? 0 : changedBy);

                    history.setCreatedAt(rs.getTimestamp("createdAt"));
                    list.add(history);
                }
            }
        }

        return list;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("orderId"));
        order.setBuyerId(rs.getInt("buyerId"));
        order.setSellerId(rs.getInt("sellerId"));
        order.setStatus(rs.getString("status"));
        order.setFulfillmentMethod(rs.getString("fulfillmentMethod"));
        order.setPaymentMethod(rs.getString("paymentMethod"));
        order.setPaymentStatus(rs.getString("paymentStatus"));
        order.setMeetupPlace(rs.getString("meetupPlace"));
        int cancelledBy = rs.getInt("cancelledBy");
        order.setCancelledBy(rs.wasNull() ? 0 : cancelledBy);

        return order;
    }

    private float calcTotal(List<OrderItem> items) {
        float total = 0f;

        for (OrderItem item : items) {
            total += item.getQuantity() * item.getPriceAtPurchase();
        }
        return total;
    }

    private void requireUser(User u) {
        if (u == null || u.getUserId() <= 0) {
            throw new IllegalStateException("Người dùng chưa đăng nhập.");
        }
    }

    private void requireOrder(User u, Order o) {
        requireUser(u);

        if (o == null || o.getOrderId() <= 0) {
            throw new IllegalArgumentException("Đơn hàng không hợp lệ.");
        }
    }

    private Order lockOrder(Connection db, int orderId) throws SQLException {
        String sql = "SELECT orderId, buyerId, sellerId, status, "
                + "fulfillmentMethod, paymentMethod, paymentStatus, "
                + "meetupPlace, cancelledBy "
                + "FROM tblOrder WHERE orderId = ? FOR UPDATE";

        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "Không tìm thấy đơn hàng #" + orderId
                    );
                }

                return mapOrder(rs);
            }
        }
    }

    private OrderStatusHistory insertStatusHistory(
            Connection db,
            int orderId,
            String fromStatus,
            String toStatus,
            int userId,
            Timestamp now
    ) throws SQLException {
        String sql = "INSERT INTO tblOrderStatusHistory "
                + "(orderId, fromStatus, toStatus, changedBy, createdAt, "
                + "tblOrderId, tblUserId) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setString(2, fromStatus);
            ps.setString(3, toStatus);
            ps.setInt(4, userId);
            ps.setTimestamp(5, now);
            ps.setInt(6, orderId);
            ps.setInt(7, userId);
            requireOneRow(ps.executeUpdate());
        }

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrderId(orderId);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setChangedBy(userId);
        history.setCreatedAt(now);
        return history;
    }

    private void requireOneRow(int rows) throws SQLException {
        if (rows != 1) {
            throw new SQLException(
                    "Số bản ghi cập nhật không hợp lệ: " + rows
            );
        }
    }

    private void beginTransaction(Connection db) {
        if (db == null) {
            throw new IllegalStateException("Chưa có kết nối CSDL.");
        }

        try {
            if (db.isClosed()) {
                throw new IllegalStateException("Kết nối CSDL đã đóng.");
            }

            if (!db.getAutoCommit()) {
                throw new IllegalStateException(
                        "Kết nối đang có giao dịch khác chưa kết thúc."
                );
            }

            db.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("Không thể bắt đầu giao dịch.", e);
        }
    }

    private RuntimeException rollbackAndWrap(
            Connection db,
            Exception cause,
            String message
    ) {
        try {
            db.rollback();
        } catch (SQLException rollbackError) {
            cause.addSuppressed(rollbackError);

            try {
                db.close();
            } catch (SQLException closeError) {
                cause.addSuppressed(closeError);
            }

            if (con == db) {
                con = null;
            }
        }
        if (cause instanceof RuntimeException) {
            return (RuntimeException) cause;
        }

        return new RuntimeException(
                message + ": " + cause.getMessage(),
                cause
        );
    }
    private void finishTransaction(Connection db) {
        try {
            if (!db.isClosed()) {
                db.setAutoCommit(true);
            }
        } catch (SQLException e) {
            try {
                db.close();
            } catch (SQLException closeError) {
                e.addSuppressed(closeError);
            }
            if (con == db) {
                con = null;
            }
            throw new RuntimeException(
                    "Không thể khôi phục chế độ kết nối; cần mở lại kết nối.",
                    e
            );
        }
    }
}