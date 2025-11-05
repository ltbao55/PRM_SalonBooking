package com.example.prm_be.data.models;

/**
 * Data Model cho Salon
 * Đây là "hợp đồng" chung cho toàn bộ dự án
 */
public class Salon {
    private String id;
    private String name;
    private String address;
    private String phone;
    private String imageUrl;
    private String description;
    private double rating;

    // Default constructor (required for Firestore)
    public Salon() {
    }

    // Constructor đầy đủ
    public Salon(String id, String name, String address, String imageUrl) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.imageUrl = imageUrl;
        this.rating = 0.0;
    }

    public Salon(String id, String name, String address, String phone, String imageUrl, String description, double rating) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.imageUrl = imageUrl;
        this.description = description;
        this.rating = rating;
    }

    // Getters và Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}

