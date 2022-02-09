package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private final String vehiculeRegistrationNumber = "ABCDEF";

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){

        //PRE CHECK
        //Try to retrieve ticket from database : no ticket shall be in the database before processing Incoming
        Ticket ticket = ticketDAO.getTicket(vehiculeRegistrationNumber);
        assertEquals(null, ticket);
        //Next available slot shall be 1
        int parkingSpotNumber = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        assertEquals(1, parkingSpotNumber);

        //ACT
        ParkingService parkingService = new ParkingService(parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle(ParkingType.CAR, vehiculeRegistrationNumber);

        //CHECK
        //retrieve ticket from database, Check that ticket has been found in DB with right data
        ticket = ticketDAO.getTicket(vehiculeRegistrationNumber);
        assertEquals(vehiculeRegistrationNumber, ticket.getVehicleRegNumber());
        assertEquals(1, ticket.getParkingSpot().getId());
        assertEquals(ParkingType.CAR, ticket.getParkingSpot().getParkingType());
        assertEquals(false, ticket.getParkingSpot().isAvailable());
        assertNotEquals(null, ticket.getInTime());
        assertEquals(null, ticket.getOutTime());

        //Check that next available slot is 2
        parkingSpotNumber = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        assertEquals(2, parkingSpotNumber);
    }

    @Test
    public void testParkingLotExit() throws InterruptedException {
        //INIT TEST -> execute incoming vehicle
        testParkingACar();

        //wait a while for price computation
        Thread.sleep(1000);

        //ACT
        ParkingService parkingService = new ParkingService( parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle(vehiculeRegistrationNumber);

        //CHECK
        //retrieve ticket from database, Check that ticket has been found in DB with right data
        Ticket ticket = ticketDAO.getTicket(vehiculeRegistrationNumber);
        assertEquals(vehiculeRegistrationNumber, ticket.getVehicleRegNumber());
        assertEquals(1, ticket.getParkingSpot().getId());
        assertEquals(ParkingType.CAR, ticket.getParkingSpot().getParkingType());
        assertEquals(false, ticket.getParkingSpot().isAvailable());

        assertNotEquals(null, ticket.getInTime());
        assertNotEquals(null, ticket.getOutTime());
        assertNotEquals(0.0f, ticket.getPrice()  );

        //Check that slot 1 is available now
        int parkingSpotNumber = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        assertEquals(1, parkingSpotNumber);
    }

}
