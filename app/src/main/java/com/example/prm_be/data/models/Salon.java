package com.example.prm_be.data.models;

/**
 * Data Model cho Salon
 * Đây là "hợp đồng" chung cho toàn bộ dự án
 */
public class Salon {
    private String id;
    private String name;
    private String address;
    private String imageUrl;

    // Default constructor (required for Firestore)
    public Salon() {
    }

    // Constructor đầy đủ
    public Salon(String id, String name, String address, String imageUrl) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.imageUrl = imageUrl;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

