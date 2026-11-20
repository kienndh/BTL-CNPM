package model;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private int productId;
    private int sellerId;
    private int categoryId;
    private String title;
    private String description;
    private float price;
    private int stock;
    private int reservedStock;
    private String status;
    private String approvalStatus;
    private String moderationStatus;
    private boolean isDeleted;
    private List<ProductImage> images = new ArrayList<>();

    public Product() {
    }

    public Product(int productId, int sellerId, int categoryId, String title, float price,
                   int stock, int reservedStock, String status, String approvalStatus,
                   String moderationStatus, boolean isDeleted, String description) {
        this.productId = productId;
        this.sellerId = sellerId;
        this.categoryId = categoryId;
        this.title = title;
        this.price = price;
        this.stock = stock;
        this.reservedStock = reservedStock;
        this.status = status;
        this.approvalStatus = approvalStatus;
        this.moderationStatus = moderationStatus;
        this.isDeleted = isDeleted;
        this.description = description;
    }

    public int getAvailableStock() {
        return Math.max(0, stock - reservedStock);
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public float getPrice() { return price; }
    public void setPrice(float price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getReservedStock() { return reservedStock; }
    public void setReservedStock(int reservedStock) { this.reservedStock = reservedStock; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getModerationStatus() { return moderationStatus; }
    public void setModerationStatus(String moderationStatus) { this.moderationStatus = moderationStatus; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public List<ProductImage> getImages() { return images; }
    public void setImages(List<ProductImage> images) { this.images = images; }
}