package com.parkit.parkingsystem;

import com.parkit.parkingsystem.controller.ParkingController;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import com.parkit.parkingsystem.view.InteractiveShell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {
    private static final Logger logger = LogManager.getLogger("App");
    public static void main(String ... args){
        logger.info("Initializing Parking System");

        //CREATE UTILS INSTANCES
        InputReaderUtil inputReaderUtil = new InputReaderUtil(System.in);
        //CREATE DAO INSTANCES
        TicketDAO ticketDAO = new TicketDAO();
        ParkingSpotDAO parkingSpotDAO = new ParkingSpotDAO();
        //CREATE VIEW CONTROLLER AND SERVICES INSTANCES
        ParkingService parkingService = new ParkingService(parkingSpotDAO, ticketDAO);
        InteractiveShell interactiveShell = new InteractiveShell(inputReaderUtil, System.out);
        ParkingController parkingController = new ParkingController(interactiveShell, parkingService);
        //START APPLICATION
        parkingController.start();
    }
}
