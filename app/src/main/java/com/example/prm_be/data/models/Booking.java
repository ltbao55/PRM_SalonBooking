package com.example.prm_be.data.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Model cho Booking (Lịch hẹn)
 * Đây là "hợp đồng" chung cho toàn bộ dự án
 */
public class Booking {
    private String id;
    private String userId;
    private String salonId;
    private String serviceId; // Deprecated - giữ lại để tương thích, sử dụng serviceIds thay thế
    private List<String> serviceIds; // Danh sách ID các dịch vụ được đặt
    private String stylistId; // Optional - có thể null
    private long timestamp; // Timestamp bắt đầu của lịch hẹn
    private long endTime; // Timestamp kết thúc của lịch hẹn (timestamp + duration)
    private String status; // "pending", "confirmed", "completed", "cancelled"
    private long createdAt; // Thời gian tạo booking

    // Default constructor (required for Firestore)
    public Booking() {
        this.serviceIds = new ArrayList<>();
    }

    // Constructor đầy đủ
    public Booking(String id, String userId, String salonId, String serviceId, 
                   String stylistId, long timestamp, String status, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.salonId = salonId;
        this.serviceId = serviceId;
        this.stylistId = stylistId;
        this.timestamp = timestamp;
        this.status = status;
        this.createdAt = createdAt;
        this.serviceIds = new ArrayList<>();
        if (serviceId != null) {
            this.serviceIds.add(serviceId);
        }
        this.endTime = timestamp; // Sẽ được tính sau
    }

    // Constructor với serviceIds (cho nhiều dịch vụ)
    public Booking(String id, String userId, String salonId, List<String> serviceIds, 
                   String stylistId, long timestamp, long endTime, String status, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.salonId = salonId;
        this.serviceIds = serviceIds != null ? new ArrayList<>(serviceIds) : new ArrayList<>();
        this.serviceId = serviceIds != null && !serviceIds.isEmpty() ? serviceIds.get(0) : null; // Giữ lại cho tương thích
        this.stylistId = stylistId;
        this.timestamp = timestamp;
        this.endTime = endTime;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Constructor không có stylist (optional)
    public Booking(String id, String userId, String salonId, String serviceId, 
                   long timestamp, String status, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.salonId = salonId;
        this.serviceId = serviceId;
        this.stylistId = null;
        this.timestamp = timestamp;
        this.status = status;
        this.createdAt = createdAt;
        this.serviceIds = new ArrayList<>();
        if (serviceId != null) {
            this.serviceIds.add(serviceId);
        }
        this.endTime = timestamp; // Sẽ được tính sau
    }

    // Getters và Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSalonId() {
        return salonId;
    }

    public void setSalonId(String salonId) {
        this.salonId = salonId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getStylistId() {
        return stylistId;
    }

    public void setStylistId(String stylistId) {
        this.stylistId = stylistId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getServiceIds() {
        return serviceIds != null ? serviceIds : new ArrayList<>();
    }

    public void setServiceIds(List<String> serviceIds) {
        this.serviceIds = serviceIds != null ? new ArrayList<>(serviceIds) : new ArrayList<>();
        // Update serviceId để tương thích
        if (serviceIds != null && !serviceIds.isEmpty()) {
            this.serviceId = serviceIds.get(0);
        }
    }

    public long getEndTime() {
        return endTime > 0 ? endTime : timestamp; // Fallback nếu chưa set
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}

