package com.example.prm_be.data.models;

/**
 * Data Model cho User
 * Đây là "hợp đồng" chung cho toàn bộ dự án
 */
public class User {
    private String uid;
    private String name;
    private String email;
    private String avatarUrl;
    private String role; // user | staff | admin
    private String stylistId; // ID của stylist document trong Firestore (nếu user là staff)
    private String status; // active | disabled

    // Default constructor (required for Firestore)
    public User() {
    }

    // Constructor đầy đủ
    public User(String uid, String name, String email, String avatarUrl) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
    }

    public User(String uid, String name, String email, String avatarUrl, String role) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.role = role;
    }

    // Getters và Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStylistId() {
        return stylistId;
    }

    public void setStylistId(String stylistId) {
        this.stylistId = stylistId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

