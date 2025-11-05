package com.example.prm_be.data.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Model cho WorkingHours (Khung giờ làm việc)
 * Lưu thông tin giờ mở cửa/đóng cửa của salon
 */
public class WorkingHours {
    private String salonId;
    private String openTime; // Format: "HH:mm" (ví dụ: "09:00")
    private String closeTime; // Format: "HH:mm" (ví dụ: "18:00")
    private List<String> daysOfWeek; // ["MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"]
    private int slotDuration; // Thời lượng mỗi slot (phút), mặc định 30

    // Default constructor (required for Firestore)
    public WorkingHours() {
        this.daysOfWeek = new ArrayList<>();
        this.slotDuration = 30;
    }

    public WorkingHours(String salonId, String openTime, String closeTime, List<String> daysOfWeek, int slotDuration) {
        this.salonId = salonId;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.daysOfWeek = daysOfWeek != null ? daysOfWeek : new ArrayList<>();
        this.slotDuration = slotDuration > 0 ? slotDuration : 30;
    }

    // Getters và Setters
    public String getSalonId() {
        return salonId;
    }

    public void setSalonId(String salonId) {
        this.salonId = salonId;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public List<String> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(List<String> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public int getSlotDuration() {
        return slotDuration > 0 ? slotDuration : 30;
    }

    public void setSlotDuration(int slotDuration) {
        this.slotDuration = slotDuration > 0 ? slotDuration : 30;
    }
}

