package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    //Feature STORY#1 : Free 30-min parking
    private static final double freeFareInHour = 0.5f;
    private static final double MILLISECONDS_IN_ONE_HOUR = 3600000f;

    public void calculateFare(Ticket ticket){
        if((ticket.getOutTime() == null) || ticket.getInTime() == null ){
            throw new NullPointerException("Null pointer detected In time:" +
                    ticket.getInTime() + " ; Out time" + ticket.getOutTime());
        }
        if (ticket.getOutTime().before(ticket.getInTime())){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inTimeInMs = ticket.getInTime().getTime();
        long outTimeInMs = ticket.getOutTime().getTime();

        //Feature STORY#1 : Free 30-min parking
        double durationInHour = Math.max (0, ((outTimeInMs - inTimeInMs) / MILLISECONDS_IN_ONE_HOUR) - freeFareInHour);
        //Feature STORY#2 : x%-discount for recurring
        double discountRate = ticket.getDiscountInPercent()/100f;

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice((1 - discountRate) * durationInHour * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice((1 - discountRate) * durationInHour * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default: throw new IllegalArgumentException("Unknown Parking Type");
        }
    }
}