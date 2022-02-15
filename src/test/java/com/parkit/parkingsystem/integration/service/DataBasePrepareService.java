package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

import java.sql.Connection;
import java.util.Date;

public class DataBasePrepareService {

    DataBaseTestConfig dataBaseTestConfig;
    TicketDAO  ticketDAO;
    ParkingSpotDAO parkingSpotDAO;
    public DataBasePrepareService() {
        dataBaseTestConfig = new DataBaseTestConfig();
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
    }

    public void clearDataBaseEntries(){
        try(Connection connection = dataBaseTestConfig.getConnection()){
            //set parking entries to available
            connection.prepareStatement("update parking set available = true").execute();
            //clear ticket entries;
            connection.prepareStatement("truncate table ticket").execute();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void addTicketToDatabase(String vehicleRegNumber, int parkingSpotId, ParkingType parkingType,
                                        boolean isParkingAvailable, long inTime, long outTime, double price){
        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber(vehicleRegNumber);
        ParkingSpot parkingSpot = new ParkingSpot(parkingSpotId, parkingType, isParkingAvailable);
        ticket.setParkingSpot(parkingSpot);
        parkingSpotDAO.updateParking(parkingSpot);
        if(inTime>0){
            ticket.setInTime(new Date(inTime));
        } else {
            ticket.setInTime(null);
        }
        if(outTime>0){
            ticket.setOutTime(new Date(outTime));
        } else {
            ticket.setOutTime(null);
        }
        ticket.setPrice(price);
        ticketDAO.saveTicket(ticket);
    }
}
