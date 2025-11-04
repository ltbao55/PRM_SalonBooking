package com.example.prm_be.data.models;

/**
 * Data Model cho Service (Dịch vụ)
 * Đây là "hợp đồng" chung cho toàn bộ dự án
 */
public class Service {
    private String id;
    private String name;
    private long price;
    private int durationInMinutes; // optional; default 60

    // Default constructor (required for Firestore)
    public Service() {
        this.durationInMinutes = 60;
    }

    // Constructor đầy đủ
    public Service(String id, String name, long price) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.durationInMinutes = 60;
    }

    public Service(String id, String name, long price, int durationInMinutes) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.durationInMinutes = durationInMinutes;
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

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public int getDurationInMinutes() {
        return durationInMinutes <= 0 ? 60 : durationInMinutes;
    }

    public void setDurationInMinutes(int durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }
}

