package com.ptit.choptiters.model;

public class Category {
    private int categoryId;
    private String slug;
    private String name;
    private boolean isDeleted;

    public Category() {
    }

    public Category(int categoryId, String slug, String name, boolean isDeleted) {
        this.categoryId = categoryId;
        this.slug = slug;
        this.name = name;
        this.isDeleted = isDeleted;
    }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    @Override
    public String toString() {
        return name;
    }
}