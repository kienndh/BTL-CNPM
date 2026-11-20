package model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int userId;
    private String publicId;
    private String email;
    private String fullName;
    private String phone;
    private String status;
    private String role;
    private List<Product> sellingProducts = new ArrayList<>();

    public User() {
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<Product> getSellingProducts() {
        return sellingProducts;
    }

    public void setSellingProducts(List<Product> sellingProducts) {
        this.sellingProducts = sellingProducts;
    }
}