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
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class OrderDAO extends DAO {

    public OrderDAO() {
        super();
    }


    public Order createOrder(User u, int productId, int quantity,
                             String contactName, String contactPhone,
                             String meetupPlace, String note,
                             String fulfillmentMethod, String paymentMethod) {
        if (u == null || u.getUserId() <= 0) {
            throw new IllegalStateException("Người dùng chưa đăng nhập.");
        }
        if (!"ACTIVE".equalsIgnoreCase(u.getStatus())) {
            throw new IllegalStateException("Tài khoản người mua không hoạt động.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng đặt phải lớn hơn 0.");
        }
        if (contactName == null || contactName.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên liên hệ.");
        }
        if (contactPhone == null || contactPhone.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập số điện thoại liên hệ.");
        }
        if ("MEETUP".equalsIgnoreCase(fulfillmentMethod)
                && (meetupPlace == null || meetupPlace.trim().isEmpty())) {
            throw new IllegalArgumentException("Vui lòng nhập địa điểm gặp mặt.");
        }

        boolean prevAutoCommit = true;
        try {
            prevAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            String lockSql = "SELECT p.productId, p.sellerId, p.title, p.price, p.stock, p.reservedStock, "
                    + "       p.status, p.approvalStatus, p.moderationStatus, p.isDeleted, "
                    + "       seller.status AS sellerStatus "
                    + "FROM tblProduct p "
                    + "JOIN tblUser seller ON p.sellerId = seller.userId "
                    + "WHERE p.productId = ? FOR UPDATE";

            int sellerId;
            String title;
            float priceAtPurchase;

            try (PreparedStatement psLock = con.prepareStatement(lockSql)) {
                psLock.setInt(1, productId);
                try (ResultSet rs = psLock.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalStateException("Sản phẩm không tồn tại.");
                    }
                    sellerId = rs.getInt("sellerId");
                    title = rs.getString("title");
                    priceAtPurchase = rs.getFloat("price");

                    int stock = rs.getInt("stock");
                    int reservedStock = rs.getInt("reservedStock");
                    boolean isDeleted = rs.getBoolean("isDeleted");
                    String status = rs.getString("status");
                    String approvalStatus = rs.getString("approvalStatus");
                    String moderationStatus = rs.getString("moderationStatus");
                    String sellerStatus = rs.getString("sellerStatus");

                    if (sellerId == u.getUserId()) {
                        throw new IllegalStateException("Không thể tự đặt mua sản phẩm của chính mình.");
                    }
                    if (isDeleted
                            || !"AVAILABLE".equalsIgnoreCase(status)
                            || !"APPROVED".equalsIgnoreCase(approvalStatus)
                            || !"CLEAR".equalsIgnoreCase(moderationStatus)
                            || !"ACTIVE".equalsIgnoreCase(sellerStatus)) {
                        throw new IllegalStateException("Tin đăng không khả dụng để đặt mua.");
                    }
                    if (quantity > (stock - reservedStock)) {
                        throw new IllegalStateException("Số lượng vượt quá tồn kho khả dụng (" + (stock - reservedStock) + ").");
                    }
                }
            }

            String reserveSql = "UPDATE tblProduct SET reservedStock = reservedStock + ? WHERE productId = ?";
            try (PreparedStatement psReserve = con.prepareStatement(reserveSql)) {
                psReserve.setInt(1, quantity);
                psReserve.setInt(2, productId);
                psReserve.executeUpdate();
            }

            String insertOrderSql = "INSERT INTO tblOrder (buyerId, sellerId, status, fulfillmentMethod, "
                    + "paymentMethod, paymentStatus, meetupPlace, cancelledBy, contactName, contactPhone, note) "
                    + "VALUES (?, ?, 'PENDING', ?, ?, 'UNPAID', ?, NULL, ?, ?, ?)";
            int newOrderId;
            try (PreparedStatement psOrder = con.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setInt(1, u.getUserId());
                psOrder.setInt(2, sellerId);
                psOrder.setString(3, fulfillmentMethod);
                psOrder.setString(4, paymentMethod);
                if (meetupPlace == null || meetupPlace.trim().isEmpty()) {
                    psOrder.setNull(5, Types.VARCHAR);
                } else {
                    psOrder.setString(5, meetupPlace.trim());
                }
                psOrder.setString(6, contactName.trim());
                psOrder.setString(7, contactPhone.trim());
                if (note == null || note.trim().isEmpty()) {
                    psOrder.setNull(8, Types.VARCHAR);
                } else {
                    psOrder.setString(8, note.trim());
                }
                psOrder.executeUpdate();

                try (ResultSet keys = psOrder.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Không lấy được orderId vừa tạo.");
                    }
                    newOrderId = keys.getInt(1);
                }
            }

            String insertItemSql = "INSERT INTO tblOrderItem (orderId, productId, productTitle, quantity, priceAtPurchase) "
                    + "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement psItem = con.prepareStatement(insertItemSql)) {
                psItem.setInt(1, newOrderId);
                psItem.setInt(2, productId);
                psItem.setString(3, title);
                psItem.setInt(4, quantity);
                psItem.setFloat(5, priceAtPurchase);
                psItem.executeUpdate();
            }

            String insertHistSql = "INSERT INTO tblOrderStatusHistory (orderId, fromStatus, toStatus, changedBy, createdAt, tblUserId) "
                    + "VALUES (?, NULL, 'PENDING', ?, NOW(), ?)";
            try (PreparedStatement psHist = con.prepareStatement(insertHistSql)) {
                psHist.setInt(1, newOrderId);
                psHist.setInt(2, u.getUserId());
                psHist.setInt(3, u.getUserId());
                psHist.executeUpdate();
            }

            con.commit();
            return getOrderDetail(u, newOrderId);
        } catch (Exception e) {
            try {
                con.rollback();
            } catch (SQLException rollbackEx) {
                e.addSuppressed(rollbackEx);
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Lỗi tạo đơn hàng: " + e.getMessage(), e);
        } finally {
            try {
                con.setAutoCommit(prevAutoCommit);
            } catch (SQLException ignored) {
            }
        }
    }

    public ArrayList<Order> getOrdersByBuyer(User u, String status) {
        return listBuyingOrders(u, status);
    }

    public Order getOrderDetail(User u, int orderId) throws SQLException {
        if (u == null || u.getUserId() <= 0) {
            throw new IllegalStateException("Người dùng chưa đăng nhập.");
        }
        String sql = "SELECT orderId, buyerId, sellerId, status, fulfillmentMethod, "
                + "       paymentMethod, paymentStatus, meetupPlace, cancelledBy, contactName, contactPhone, note, cancelReason "
                + "FROM tblOrder WHERE orderId = ?";
        Order order;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Không tìm thấy đơn hàng #" + orderId);
                }
                order = mapOrder(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tải chi tiết đơn hàng: " + e.getMessage(), e);
        }

        if (order.getBuyerId() != u.getUserId() && order.getSellerId() != u.getUserId()) {
            throw new SecurityException("Bạn không có quyền xem đơn hàng này.");
        }

        List<OrderItem> items = fetchOrderItems(orderId);
        order.setItems(items);
        order.setTotalAmount(calcTotal(items));
        order.setStatusHistory(fetchStatusHistory(orderId));
        return order;
    }

    public boolean canCancelOrder(User u, int orderId) {
        if (u == null || u.getUserId() <= 0) {
            return false;
        }
        String sql = "SELECT buyerId, status, paymentStatus FROM tblOrder WHERE orderId = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                int buyerId = rs.getInt("buyerId");
                String status = rs.getString("status");
                String paymentStatus = rs.getString("paymentStatus");
                return buyerId == u.getUserId()
                        && ("PENDING".equalsIgnoreCase(status) || "CONFIRMED".equalsIgnoreCase(status))
                        && !"PAID".equalsIgnoreCase(paymentStatus);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kiểm tra điều kiện hủy đơn: " + e.getMessage(), e);
        }
    }

    public boolean cancelOrder(User u, int orderId, String reason) {
        if (u == null || u.getUserId() <= 0) {
            throw new IllegalStateException("Người dùng chưa đăng nhập.");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do hủy đơn.");
        }

        boolean prevAutoCommit = true;
        try {
            prevAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            String lockOrderSql = "SELECT buyerId, status, paymentStatus FROM tblOrder WHERE orderId = ? FOR UPDATE";
            String currentStatus;
            String paymentStatus;

            try (PreparedStatement psLock = con.prepareStatement(lockOrderSql)) {
                psLock.setInt(1, orderId);
                try (ResultSet rs = psLock.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalStateException("Đơn hàng không tồn tại.");
                    }
                    if (rs.getInt("buyerId") != u.getUserId()) {
                        throw new SecurityException("Bạn không có quyền hủy đơn hàng này.");
                    }
                    currentStatus = rs.getString("status");
                    paymentStatus = rs.getString("paymentStatus");
                }
            }

            if ("CANCELLED".equalsIgnoreCase(currentStatus)) {
                throw new IllegalStateException("Đơn hàng đã được hủy trước đó.");
            }
            if (!"PENDING".equalsIgnoreCase(currentStatus) && !"CONFIRMED".equalsIgnoreCase(currentStatus)) {
                throw new IllegalStateException("Không thể hủy đơn ở trạng thái: " + currentStatus);
            }
            if ("PAID".equalsIgnoreCase(paymentStatus)) {
                throw new IllegalStateException("Đơn hàng đã thanh toán, không thể hủy trực tiếp.");
            }

            String itemsSql = "SELECT productId, quantity FROM tblOrderItem WHERE orderId = ?";
            String releaseSql = "UPDATE tblProduct SET reservedStock = GREATEST(0, reservedStock - ?) WHERE productId = ?";
            try (PreparedStatement psItems = con.prepareStatement(itemsSql);
                 PreparedStatement psRelease = con.prepareStatement(releaseSql)) {
                psItems.setInt(1, orderId);
                try (ResultSet rs = psItems.executeQuery()) {
                    while (rs.next()) {
                        psRelease.setInt(1, rs.getInt("quantity"));
                        psRelease.setInt(2, rs.getInt("productId"));
                        psRelease.executeUpdate();
                    }
                }
            }

            String updateOrderSql = "UPDATE tblOrder SET status = 'CANCELLED', cancelledBy = ?, cancelReason = ? WHERE orderId = ?";
            try (PreparedStatement psUpdate = con.prepareStatement(updateOrderSql)) {
                psUpdate.setInt(1, u.getUserId());
                psUpdate.setString(2, reason.trim());
                psUpdate.setInt(3, orderId);
                psUpdate.executeUpdate();
            }

            String insertHistSql = "INSERT INTO tblOrderStatusHistory (orderId, fromStatus, toStatus, changedBy, createdAt, tblUserId) "
                    + "VALUES (?, ?, 'CANCELLED', ?, NOW(), ?)";
            try (PreparedStatement psHist = con.prepareStatement(insertHistSql)) {
                psHist.setInt(1, orderId);
                psHist.setString(2, currentStatus);
                psHist.setInt(3, u.getUserId());
                psHist.setInt(4, u.getUserId());
                psHist.executeUpdate();
            }

            con.commit();
            return true;
        } catch (Exception e) {
            try {
                con.rollback();
            } catch (SQLException rollbackEx) {
                e.addSuppressed(rollbackEx);
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Lỗi hủy đơn hàng: " + e.getMessage(), e);
        } finally {
            try {
                con.setAutoCommit(prevAutoCommit);
            } catch (SQLException ignored) {
            }
        }
    }


    public ArrayList<Order> listSellingOrders(User u, String status) {
        requireUser(u);
        ArrayList<Order> list = new ArrayList<>();
        boolean filterByStatus = status != null
                && !status.trim().isEmpty()
                && !"ALL".equalsIgnoreCase(status.trim());

        StringBuilder sql = new StringBuilder(
                "SELECT orderId, buyerId, sellerId, status, fulfillmentMethod, "
                        + "paymentMethod, paymentStatus, meetupPlace, cancelledBy, contactName, contactPhone, note, cancelReason "
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
            throw new RuntimeException("Lỗi tải danh sách đơn bán: " + e.getMessage(), e);
        }
        return list;
    }

    public ArrayList<Order> listBuyingOrders(User u, String status) {
        requireUser(u);
        ArrayList<Order> list = new ArrayList<>();
        boolean filterByStatus = status != null
                && !status.trim().isEmpty()
                && !"ALL".equalsIgnoreCase(status.trim());

        StringBuilder sql = new StringBuilder(
                "SELECT orderId, buyerId, sellerId, status, fulfillmentMethod, "
                        + "paymentMethod, paymentStatus, meetupPlace, cancelledBy, contactName, contactPhone, note, cancelReason "
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
            throw new RuntimeException("Lỗi tải danh sách đơn mua: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean confirmOrder(User u, Order o, String note) {
        requireOrder(u, o);
        Connection db = con;
        beginTransaction(db);
        try {
            Order current = lockOrder(db, o.getOrderId());
            if (current.getSellerId() != u.getUserId()) {
                throw new SecurityException("Chỉ người bán của đơn mới được chấp nhận đơn.");
            }
            if (!"PENDING".equalsIgnoreCase(current.getStatus())) {
                throw new IllegalStateException("Chỉ có thể chấp nhận đơn đang chờ xác nhận.");
            }
            String sql = "UPDATE tblOrder SET status = 'CONFIRMED' WHERE orderId = ? AND status = 'PENDING'";
            try (PreparedStatement ps = db.prepareStatement(sql)) {
                ps.setInt(1, current.getOrderId());
                requireOneRow(ps.executeUpdate());
            }
            Timestamp now = new Timestamp(System.currentTimeMillis());
            insertStatusHistory(db, current.getOrderId(), current.getStatus(), "CONFIRMED", u.getUserId(), now);
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
                throw new SecurityException("Chỉ người bán của đơn mới được đánh dấu đang giao.");
            }
            if (!"CONFIRMED".equalsIgnoreCase(current.getStatus())) {
                throw new IllegalStateException("Đơn hàng phải ở trạng thái đã xác nhận.");
            }
            if (!"SHIPPING".equalsIgnoreCase(current.getFulfillmentMethod())) {
                throw new IllegalStateException("Chỉ áp dụng cho đơn sử dụng hình thức giao hàng.");
            }
            String sql = "UPDATE tblOrder SET status = 'DELIVERING' WHERE orderId = ? AND status = 'CONFIRMED'";
            try (PreparedStatement ps = db.prepareStatement(sql)) {
                ps.setInt(1, current.getOrderId());
                requireOneRow(ps.executeUpdate());
            }
            Timestamp now = new Timestamp(System.currentTimeMillis());
            insertStatusHistory(db, current.getOrderId(), current.getStatus(), "DELIVERING", u.getUserId(), now);
            db.commit();
            return true;
        } catch (SQLException | RuntimeException e) {
            throw rollbackAndWrap(db, e, "Lỗi đánh dấu đơn đang giao");
        } finally {
            finishTransaction(db);
        }
    }

    public boolean completeOrder(User u, Order o) {
        requireOrder(u, o);
        Connection db = con;
        beginTransaction(db);
        try {
            Order current = lockOrder(db, o.getOrderId());
            if (current.getBuyerId() != u.getUserId() && current.getSellerId() != u.getUserId()) {
                throw new SecurityException("Chỉ người mua hoặc người bán của đơn được hoàn tất.");
            }
            if (!canComplete(current)) {
                throw new IllegalStateException(
                        "Chỉ hoàn tất được đơn gặp trực tiếp đã xác nhận hoặc đơn giao hàng đang giao.");
            }
            List<OrderItem> items = fetchOrderItems(current.getOrderId());
            if (items.isEmpty()) {
                throw new IllegalStateException("Đơn hàng không có dòng sản phẩm.");
            }
            Map<Integer, Integer> quantities = new TreeMap<>();
            for (OrderItem item : items) {
                if (item.getProductId() <= 0 || item.getQuantity() <= 0) {
                    throw new IllegalStateException("Dữ liệu dòng sản phẩm không hợp lệ.");
                }
                int previousQuantity = quantities.getOrDefault(item.getProductId(), 0);
                quantities.put(item.getProductId(), Math.addExact(previousQuantity, item.getQuantity()));
            }
            for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
                int productId = entry.getKey();
                int quantity = entry.getValue();
                Product product;
                String lockProductSql = "SELECT productId, stock, reservedStock FROM tblProduct WHERE productId = ? FOR UPDATE";

                try (PreparedStatement ps = db.prepareStatement(lockProductSql)) {
                    ps.setInt(1, productId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException("Không tìm thấy sản phẩm #" + productId);
                        }
                        product = new Product();
                        product.setProductId(rs.getInt("productId"));
                        product.setStock(rs.getInt("stock"));
                        product.setReservedStock(rs.getInt("reservedStock"));
                    }
                }
                if (product.getStock() < quantity || product.getReservedStock() < quantity) {
                    throw new IllegalStateException("Tồn kho hoặc số lượng giữ chỗ không đủ cho sản phẩm #" + productId);
                }
                String updateProductSql = "UPDATE tblProduct "
                        + "SET stock = stock - ?, reservedStock = reservedStock - ? "
                        + "WHERE productId = ? AND stock >= ? AND reservedStock >= ?";

                try (PreparedStatement ps = db.prepareStatement(updateProductSql)) {
                    ps.setInt(1, quantity);
                    ps.setInt(2, quantity);
                    ps.setInt(3, productId);
                    ps.setInt(4, quantity);
                    ps.setInt(5, quantity);
                    requireOneRow(ps.executeUpdate());
                }
            }
            String updateOrderSql = "UPDATE tblOrder SET status = 'COMPLETED', paymentStatus = 'PAID', "
                    + "completedAt = NOW() WHERE orderId = ? AND status = ?";
            try (PreparedStatement ps = db.prepareStatement(updateOrderSql)) {
                ps.setInt(1, current.getOrderId());
                ps.setString(2, current.getStatus());
                requireOneRow(ps.executeUpdate());
            }
            Timestamp now = new Timestamp(System.currentTimeMillis());
            insertStatusHistory(db, current.getOrderId(), current.getStatus(), "COMPLETED", u.getUserId(), now);
            db.commit();
            return true;
        } catch (SQLException | RuntimeException e) {
            throw rollbackAndWrap(db, e, "Lỗi hoàn tất đơn hàng");
        } finally {
            finishTransaction(db);
        }
    }

    public static boolean canComplete(Order o) {
        if (o == null) {
            return false;
        }
        return ("MEETUP".equalsIgnoreCase(o.getFulfillmentMethod()) && "CONFIRMED".equalsIgnoreCase(o.getStatus()))
                || ("SHIPPING".equalsIgnoreCase(o.getFulfillmentMethod()) && "DELIVERING".equalsIgnoreCase(o.getStatus()));
    }


    private List<OrderItem> fetchOrderItems(int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.orderItemId, oi.orderId, oi.productId, oi.productTitle, oi.quantity, oi.priceAtPurchase, "
                + "       p.sellerId, p.categoryId, p.title, p.price, p.stock, p.reservedStock, "
                + "       p.status, p.approvalStatus, p.moderationStatus, p.isDeleted "
                + "FROM tblOrderItem oi "
                + "LEFT JOIN tblProduct p ON oi.productId = p.productId "
                + "WHERE oi.orderId = ? ORDER BY oi.orderItemId ASC";
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

                    if (rs.getObject("title") != null) {
                        Product p = new Product();
                        p.setProductId(rs.getInt("productId"));
                        p.setSellerId(rs.getInt("sellerId"));
                        p.setCategoryId(rs.getInt("categoryId"));
                        p.setTitle(rs.getString("title"));
                        p.setPrice(rs.getFloat("price"));
                        p.setStock(rs.getInt("stock"));
                        p.setReservedStock(rs.getInt("reservedStock"));
                        p.setStatus(rs.getString("status"));
                        p.setApprovalStatus(rs.getString("approvalStatus"));
                        p.setModerationStatus(rs.getString("moderationStatus"));
                        p.setDeleted(rs.getBoolean("isDeleted"));
                        item.setProduct(p);
                    }
                    items.add(item);
                }
            }
        }
        return items;
    }

    private List<OrderStatusHistory> fetchStatusHistory(int orderId) throws SQLException {
        List<OrderStatusHistory> list = new ArrayList<>();
        String sql = "SELECT historyId, orderId, fromStatus, toStatus, changedBy, createdAt "
                + "FROM tblOrderStatusHistory WHERE orderId = ? ORDER BY createdAt ASC, historyId ASC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderStatusHistory h = new OrderStatusHistory();
                    h.setHistoryId(rs.getInt("historyId"));
                    h.setOrderId(rs.getInt("orderId"));
                    h.setFromStatus(rs.getString("fromStatus"));
                    h.setToStatus(rs.getString("toStatus"));
                    int changedBy = rs.getInt("changedBy");
                    h.setChangedBy(rs.wasNull() ? 0 : changedBy);
                    h.setCreatedAt(rs.getTimestamp("createdAt"));
                    list.add(h);
                }
            }
        }
        return list;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("orderId"));
        o.setBuyerId(rs.getInt("buyerId"));
        o.setSellerId(rs.getInt("sellerId"));
        o.setStatus(rs.getString("status"));
        o.setFulfillmentMethod(rs.getString("fulfillmentMethod"));
        o.setPaymentMethod(rs.getString("paymentMethod"));
        o.setPaymentStatus(rs.getString("paymentStatus"));
        o.setMeetupPlace(rs.getString("meetupPlace"));
        int cancelledBy = rs.getInt("cancelledBy");
        o.setCancelledBy(rs.wasNull() ? 0 : cancelledBy);
        o.setContactName(rs.getString("contactName"));
        o.setContactPhone(rs.getString("contactPhone"));
        o.setNote(rs.getString("note"));
        o.setCancelReason(rs.getString("cancelReason"));
        return o;
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
        String sql = "SELECT orderId, buyerId, sellerId, status, fulfillmentMethod, "
                + "paymentMethod, paymentStatus, meetupPlace, cancelledBy, contactName, contactPhone, note, cancelReason "
                + "FROM tblOrder WHERE orderId = ? FOR UPDATE";
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Không tìm thấy đơn hàng #" + orderId);
                }
                return mapOrder(rs);
            }
        }
    }

    private OrderStatusHistory insertStatusHistory(
            Connection db, int orderId, String fromStatus, String toStatus, int userId, Timestamp now
    ) throws SQLException {
        String sql = "INSERT INTO tblOrderStatusHistory "
                + "(orderId, fromStatus, toStatus, changedBy, createdAt, tblOrderId, tblUserId) "
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
            throw new SQLException("Số bản ghi cập nhật không hợp lệ: " + rows);
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
                throw new IllegalStateException("Kết nối đang có giao dịch khác chưa kết thúc.");
            }
            db.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("Không thể bắt đầu giao dịch.", e);
        }
    }

    private RuntimeException rollbackAndWrap(Connection db, Exception cause, String message) {
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
        return new RuntimeException(message + ": " + cause.getMessage(), cause);
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
            throw new RuntimeException("Không thể khôi phục chế độ kết nối; cần mở lại kết nối.", e);
        }
    }
}