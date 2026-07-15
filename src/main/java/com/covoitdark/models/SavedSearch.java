package com.covoitdark.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class SavedSearch {
    private int id;
    private int passengerId;
    private String name;
    private String departure;
    private String arrival;
    private LocalTime preferredTime;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;

    public SavedSearch() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPassengerId() { return passengerId; }
    public void setPassengerId(int passengerId) { this.passengerId = passengerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDeparture() { return departure; }
    public void setDeparture(String departure) { this.departure = departure; }

    public String getArrival() { return arrival; }
    public void setArrival(String arrival) { this.arrival = arrival; }

    public LocalTime getPreferredTime() { return preferredTime; }
    public void setPreferredTime(LocalTime preferredTime) { this.preferredTime = preferredTime; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
