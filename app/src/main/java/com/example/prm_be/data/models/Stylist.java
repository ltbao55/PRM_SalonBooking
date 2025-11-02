package com.example.prm_be.data.models;

/**
 * Data Model cho Stylist
 * Đây là "hợp đồng" chung cho toàn bộ dự án
 */
public class Stylist {
    private String id;
    private String name;
    private String salonId;
    private String imageUrl;
    private String specialization; // Ví dụ: "Haircut", "Coloring", etc.

    // Default constructor (required for Firestore)
    public Stylist() {
    }

    // Constructor đầy đủ
    public Stylist(String id, String name, String salonId, String imageUrl, String specialization) {
        this.id = id;
        this.name = name;
        this.salonId = salonId;
        this.imageUrl = imageUrl;
        this.specialization = specialization;
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

    public String getSalonId() {
        return salonId;
    }

    public void setSalonId(String salonId) {
        this.salonId = salonId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}

