package com.parkit.parkingsystem.model;

import java.util.Date;
import java.util.Objects;

public class Ticket {
    private int id;
    private ParkingSpot parkingSpot;
    private String vehicleRegNumber;
    private double price;
    private Date inTime;
    private Date outTime;
    private double discount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ParkingSpot getParkingSpot() {
        if(Objects.nonNull(parkingSpot)) {
            return new ParkingSpot(parkingSpot.getId(), parkingSpot.getParkingType(), parkingSpot.isAvailable());
        } else {
            return null;
        }
    }

    public void setParkingSpot(ParkingSpot parkingSpot) {
        if(Objects.nonNull(parkingSpot)) {
            this.parkingSpot = new ParkingSpot(parkingSpot.getId(), parkingSpot.getParkingType(), parkingSpot.isAvailable());
        } else {
            this.parkingSpot = null;
        }
    }

    public String getVehicleRegNumber() {
        return vehicleRegNumber;
    }

    public void setVehicleRegNumber(String vehicleRegNumber) {
        this.vehicleRegNumber = vehicleRegNumber;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Date getInTime() {
        if(Objects.nonNull(inTime)) {
            return new Date(inTime.getTime());
        } else {
            return null;
        }
    }

    public void setInTime(Date inTime) {
        if(Objects.nonNull(inTime)) {
            this.inTime = new Date(inTime.getTime());
        } else {
            this.inTime = null;
        }
    }

    public Date getOutTime() {
        if(Objects.nonNull(outTime)) {
            return new Date(outTime.getTime());
        } else {
            return null;
        }
    }

    public void setOutTime(Date outTime) {
        if(Objects.nonNull(outTime)) {
            this.outTime = new Date(outTime.getTime());
        } else {
            this.outTime = null;
        }
    }

    public double getDiscountInPercent() {
        return discount;
    }

    public void setDiscountInPercent(double discount) {
        if(discount >= 0 && discount <= 100){
            this.discount = discount;
        }
    }
}
