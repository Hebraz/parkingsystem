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

        TicketDAO ticketDAO = new TicketDAO();
        ParkingSpotDAO parkingSpotDAO = new ParkingSpotDAO();
        ParkingService parkingService = new ParkingService(parkingSpotDAO, ticketDAO);

        InputReaderUtil inputReaderUtil = new InputReaderUtil(System.in);
        InteractiveShell interactiveShell = new InteractiveShell(inputReaderUtil, System.out);
        ParkingController parkingController = new ParkingController(interactiveShell, parkingService);

        parkingController.start();
    }
}
