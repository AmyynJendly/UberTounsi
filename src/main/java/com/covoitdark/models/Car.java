package com.covoitdark.models;

public class Car {
    private int id;
    private int driverId;
    private String brand;
    private String model;
    private String color;
    private int seats;
    private String licensePlate;
    private String imageBase64;

    public Car() {}

    public Car(int id, int driverId, String brand, String model, String color, int seats, String licensePlate, String imageBase64) {
        this.id = id;
        this.driverId = driverId;
        this.brand = brand;
        this.model = model;
        this.color = color;
        this.seats = seats;
        this.licensePlate = licensePlate;
        this.imageBase64 = imageBase64;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDriverId() { return driverId; }
    public void setDriverId(int driverId) { this.driverId = driverId; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
}
