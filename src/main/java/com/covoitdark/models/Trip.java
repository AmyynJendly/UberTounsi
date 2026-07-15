package com.covoitdark.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Trip {

    public enum Status { ACTIVE, COMPLETE, CANCELLED }
    public enum LuggagePolicy { NONE, SMALL, LARGE }
    public enum MusicPreference { NONE, SOFT, ANY }

    private int id;
    private int driverId;
    private int carId;
    private String departure;
    private String arrival;
    private LocalTime departureTime;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> repeatDays;
    private int availableSeats;
    private double price;
    private LuggagePolicy luggagePolicy;
    private MusicPreference musicPreference;
    private boolean smokingAllowed;
    private boolean acAvailable;
    private boolean flexiblePickup;
    private boolean womenOnly;
    private boolean petsAllowed;
    private Status status;
    private LocalDateTime createdAt;

    private int totalSeats;

    // Joined display fields
    private String driverName;
    private double driverRating;
    private String carInfo;
    private String driverAvatar;
    private String carImage;
    private String carColor;
    private String carPlate;

    public Trip() {
        this.repeatDays = new ArrayList<>();
        this.status = Status.ACTIVE;
        this.luggagePolicy = LuggagePolicy.SMALL;
        this.musicPreference = MusicPreference.ANY;
        this.acAvailable = true;
    }

    public Trip(int id, int driverId, int carId, String departure, String arrival,
                LocalTime departureTime, LocalDate startDate, LocalDate endDate,
                List<String> repeatDays, int availableSeats, double price,
                LuggagePolicy luggagePolicy, MusicPreference musicPreference,
                boolean smokingAllowed, boolean acAvailable, boolean flexiblePickup,
                Status status) {
        this.id = id;
        this.driverId = driverId;
        this.carId = carId;
        this.departure = departure;
        this.arrival = arrival;
        this.departureTime = departureTime;
        this.startDate = startDate;
        this.endDate = endDate;
        this.repeatDays = repeatDays != null ? new ArrayList<>(repeatDays) : new ArrayList<>();
        this.availableSeats = availableSeats;
        this.price = price;
        this.luggagePolicy = luggagePolicy != null ? luggagePolicy : LuggagePolicy.SMALL;
        this.musicPreference = musicPreference != null ? musicPreference : MusicPreference.ANY;
        this.smokingAllowed = smokingAllowed;
        this.acAvailable = acAvailable;
        this.flexiblePickup = flexiblePickup;
        this.status = status != null ? status : Status.ACTIVE;
    }

    // Getters
    public int getId() { return id; }
    public int getDriverId() { return driverId; }
    public int getCarId() { return carId; }
    public String getDeparture() { return departure; }
    public String getArrival() { return arrival; }
    public LocalTime getDepartureTime() { return departureTime; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public List<String> getRepeatDays() { return new ArrayList<>(repeatDays); }
    public int getAvailableSeats() { return availableSeats; }
    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    public double getPrice() { return price; }
    public LuggagePolicy getLuggagePolicy() { return luggagePolicy; }
    public MusicPreference getMusicPreference() { return musicPreference; }
    public boolean isSmokingAllowed() { return smokingAllowed; }
    public boolean isAcAvailable() { return acAvailable; }
    public boolean isFlexiblePickup() { return flexiblePickup; }
    public boolean isWomenOnly() { return womenOnly; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getDriverName() { return driverName; }
    public double getDriverRating() { return driverRating; }
    public String getCarInfo() { return carInfo; }
    public String getDriverAvatar() { return driverAvatar; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setDriverId(int driverId) { this.driverId = driverId; }
    public void setCarId(int carId) { this.carId = carId; }
    public void setDeparture(String departure) { this.departure = departure; }
    public void setArrival(String arrival) { this.arrival = arrival; }
    public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setRepeatDays(List<String> repeatDays) {
        this.repeatDays = repeatDays != null ? new ArrayList<>(repeatDays) : new ArrayList<>();
    }
    public void setAvailableSeats(int seats) { this.availableSeats = Math.max(0, seats); }
    public void setPrice(double price) { this.price = Math.max(0, price); }
    public void setLuggagePolicy(LuggagePolicy p) { this.luggagePolicy = p; }
    public void setMusicPreference(MusicPreference m) { this.musicPreference = m; }
    public void setSmokingAllowed(boolean b) { this.smokingAllowed = b; }
    public void setAcAvailable(boolean b) { this.acAvailable = b; }
    public void setFlexiblePickup(boolean b) { this.flexiblePickup = b; }
    public void setWomenOnly(boolean b) { this.womenOnly = b; }
    public boolean isPetsAllowed() { return petsAllowed; }
    public void setPetsAllowed(boolean b) { this.petsAllowed = b; }
    public void setStatus(Status status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public void setDriverRating(double driverRating) { this.driverRating = driverRating; }
    public void setCarInfo(String carInfo) { this.carInfo = carInfo; }
    public void setDriverAvatar(String avatar) { this.driverAvatar = avatar; }
    public String getCarImage() { return carImage; }
    public void setCarImage(String carImage) { this.carImage = carImage; }
    public String getCarColor() { return carColor; }
    public void setCarColor(String color) { this.carColor = color; }
    public String getCarPlate() { return carPlate; }
    public void setCarPlate(String plate) { this.carPlate = plate; }

    public boolean isActive() { return status == Status.ACTIVE; }
    public boolean isFull() { return availableSeats == 0; }
    public String getRepeatDaysString() { return String.join(",", repeatDays); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trip t)) return false;
        return id == t.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Trip{id=" + id + ", " + departure + "→" + arrival
                + ", " + startDate + " " + departureTime
                + ", seats=" + availableSeats + ", status=" + status + "}";
    }
}
