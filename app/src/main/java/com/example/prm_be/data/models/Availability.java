package com.example.prm_be.data.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Model cho Availability (Thời gian trống)
 * Lưu thông tin các slot không khả dụng của staff
 */
public class Availability {
    private String id;
    private String staffId; // ID của staff (stylistId)
    private String salonId;
    private long date; // Timestamp của ngày (00:00:00)
    private List<String> unavailableSlots; // Danh sách các slot không khả dụng (format: "HH:mm")
    private String reason; // Lý do (optional)

    // Default constructor (required for Firestore)
    public Availability() {
        this.unavailableSlots = new ArrayList<>();
    }

    public Availability(String id, String staffId, String salonId, long date, List<String> unavailableSlots, String reason) {
        this.id = id;
        this.staffId = staffId;
        this.salonId = salonId;
        this.date = date;
        this.unavailableSlots = unavailableSlots != null ? unavailableSlots : new ArrayList<>();
        this.reason = reason;
    }

    // Getters và Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getSalonId() {
        return salonId;
    }

    public void setSalonId(String salonId) {
        this.salonId = salonId;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public List<String> getUnavailableSlots() {
        return unavailableSlots != null ? unavailableSlots : new ArrayList<>();
    }

    public void setUnavailableSlots(List<String> unavailableSlots) {
        this.unavailableSlots = unavailableSlots != null ? unavailableSlots : new ArrayList<>();
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Kiểm tra xem một slot có khả dụng không
     */
    public boolean isSlotAvailable(String timeSlot) {
        return unavailableSlots == null || !unavailableSlots.contains(timeSlot);
    }

    /**
     * Đánh dấu slot là không khả dụng
     */
    public void markSlotUnavailable(String timeSlot) {
        if (unavailableSlots == null) {
            unavailableSlots = new ArrayList<>();
        }
        if (!unavailableSlots.contains(timeSlot)) {
            unavailableSlots.add(timeSlot);
        }
    }

    /**
     * Đánh dấu slot là khả dụng
     */
    public void markSlotAvailable(String timeSlot) {
        if (unavailableSlots != null) {
            unavailableSlots.remove(timeSlot);
        }
    }
}

