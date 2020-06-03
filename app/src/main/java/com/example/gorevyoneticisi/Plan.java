package com.example.gorevyoneticisi;

public class Plan {

    String startDate, endDate, title, note, startTime, endTime;

    public Plan() {
    }

    public Plan(String startDate, String endDate, String title, String note, String startTime, String endTime) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.title = title;
        this.note = note;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
