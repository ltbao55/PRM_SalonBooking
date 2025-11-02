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
}

