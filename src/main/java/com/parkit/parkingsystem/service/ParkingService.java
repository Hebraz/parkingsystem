package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.Objects;

public class ParkingService {

    private static final Logger logger = LogManager.getLogger("ParkingService");

    private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

    private ParkingSpotDAO parkingSpotDAO;
    private  TicketDAO ticketDAO;

    public ParkingService(ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO){
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
    }

    public Ticket processIncomingVehicle(ParkingType parkingType, String vehicleRegNumber ) {
        Ticket ticket = null;

        ParkingSpot parkingSpot = getNextParkingNumberIfAvailable(parkingType);
        if(parkingSpot !=null && parkingSpot.getId() > 0){
            parkingSpot.setAvailable(false);
            parkingSpotDAO.updateParking(parkingSpot);//allot this parking space and mark it's availability as false

            Date inTime = new Date();
            ticket = new Ticket();
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            //ticket.setId(ticketID);
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber(vehicleRegNumber);
            ticket.setPrice(0);
            ticket.setInTime(inTime);
            ticket.setOutTime(null);
            ticketDAO.saveTicket(ticket);
        }

        return ticket;
    }



    public ParkingSpot getNextParkingNumberIfAvailable(ParkingType parkingType){
        int parkingNumber=0;
        ParkingSpot parkingSpot = null;
        try{
            parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
            if(parkingNumber > 0){
                parkingSpot = new ParkingSpot(parkingNumber,parkingType, true);
            }else{
                throw new Exception("Error fetching parking number from DB. Parking slots might be full");
            }
        }catch(IllegalArgumentException ie){
            logger.error("Error parsing user input for type of vehicle", ie);
        }catch(Exception e){
            logger.error("Error fetching next available parking slot", e);
        }
        return parkingSpot;
    }

      public Ticket processExitingVehicle(String vehicleRegNumber) {
        Ticket ticket = null;
        try{
            ticket = ticketDAO.getTicket(vehicleRegNumber);
            if(Objects.nonNull(ticket)) {
                Date outTime = new Date();
                ticket.setOutTime(outTime);
                fareCalculatorService.calculateFare(ticket);
                if (ticketDAO.updateTicket(ticket)) {
                    ParkingSpot parkingSpot = ticket.getParkingSpot();
                    parkingSpot.setAvailable(true);
                    parkingSpotDAO.updateParking(parkingSpot);
                } else {
                    return null;
                }
            }
        }catch(Exception e){
            logger.error("Unable to process exiting vehicle",e);
            ticket = null;
        }
        return ticket;
    }
}
