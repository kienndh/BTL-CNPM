package model;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private int orderId;
    private int buyerId;
    private int sellerId;
    private String status;
    private String fulfillmentMethod;
    private String paymentMethod;
    private String paymentStatus;
    private String meetupPlace;
    private float totalAmount;
    private int cancelledBy;
    private List<OrderItem> items = new ArrayList<>();
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    public Order() {
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFulfillmentMethod() {
        return fulfillmentMethod;
    }

    public void setFulfillmentMethod(String fulfillmentMethod) {
        this.fulfillmentMethod = fulfillmentMethod;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getMeetupPlace() {
        return meetupPlace;
    }

    public void setMeetupPlace(String meetupPlace) {
        this.meetupPlace = meetupPlace;
    }

    public float getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(float totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(int cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public List<OrderStatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<OrderStatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
    }
}