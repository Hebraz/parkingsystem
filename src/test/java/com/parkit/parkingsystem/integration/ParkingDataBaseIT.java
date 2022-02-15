package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("ParkingDataBase IT")
public class ParkingDataBaseIT {

    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private static final  String vehiculeRegistrationNumber = "ABCDEF";

    @BeforeAll
    private static void setUp() {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest()  {
        dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    @DisplayName("Check that when a car enters into parking, ticket ant parking spot are well updated into database")
    public void testParkingACar(){

        //PRE CHECK
        //Try to retrieve ticket from database : no ticket shall be in the database before processing Incoming
        Ticket ticket = ticketDAO.getTicket(vehiculeRegistrationNumber);
        assertNull(ticket);
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
        assertFalse(ticket.getParkingSpot().isAvailable());
        assertNotEquals(null, ticket.getInTime());
        assertNull(ticket.getOutTime());

        //Check that next available slot is 2
        parkingSpotNumber = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        assertEquals(2, parkingSpotNumber);
    }

    @Test
    @DisplayName("Check that when a car exits from parking, ticket ant parking spot are well updated into database")
    public void testParkingLotExit() throws InterruptedException {
        //INIT TEST -> execute incoming vehicle
        //Add a ticket into database that corresponds car entering one hour ago
        dataBasePrepareService.addTicketToDatabase(  vehiculeRegistrationNumber, 1,ParkingType.CAR, true,
                System.currentTimeMillis() - (3600*1000),
                0,
                0.0);

        //ACT
        ParkingService parkingService = new ParkingService( parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle(vehiculeRegistrationNumber);

        //CHECK
        //retrieve ticket from database, Check that ticket has been found in DB with right data
        Ticket ticket = ticketDAO.getTicket(vehiculeRegistrationNumber);
        assertEquals(vehiculeRegistrationNumber, ticket.getVehicleRegNumber());
        assertEquals(1, ticket.getParkingSpot().getId());
        assertEquals(ParkingType.CAR, ticket.getParkingSpot().getParkingType());
        assertFalse(ticket.getParkingSpot().isAvailable());

        assertNotEquals(null, ticket.getInTime());
        assertNotEquals(null, ticket.getOutTime());
        assertNotEquals(0.0f, ticket.getPrice()  );

        //Check that slot 1 is available now
        int parkingSpotNumber = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        assertEquals(1, parkingSpotNumber);
    }

    @Test
    @DisplayName("Check that when a car comes again to the parking, a discount is applied ")
    public void testParkingWithDiscount() throws InterruptedException {

        //PREPARE
        //Add a ticket into database that corresponds to the first time the car comes into parking
        dataBasePrepareService.addTicketToDatabase(  vehiculeRegistrationNumber, 1,ParkingType.CAR, true,
                System.currentTimeMillis() - (48*3600*1000), //first entrance 48h ago
                System.currentTimeMillis() - (47*3600*1000),//first exit 47h ago
                0.75);

        //A ticket into database that corresponds to the second entrance
        long dataInSecondTicketInMs = System.currentTimeMillis() - (3*3600*1000);//second entrance 3h ago
        dataBasePrepareService.addTicketToDatabase(  vehiculeRegistrationNumber, 1,ParkingType.CAR, false,
                dataInSecondTicketInMs, //second entrance 3h ago
                0, //no exit
                0.0);
         //ACT
        ParkingService parkingService = new ParkingService( parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle(vehiculeRegistrationNumber);
        long dataOutSecondTicketInMs = System.currentTimeMillis();

        //CHECK
        //retrieve ticket from database, Check that ticket has been found in DB with right data
        Ticket ticket = ticketDAO.getTicket(vehiculeRegistrationNumber);
        assertEquals((double)dataInSecondTicketInMs, (double)ticket.getInTime().getTime(), 500);//500 ms tolerance//500 ms tolerance as milliseconds are lost in db
        assertEquals((double)dataOutSecondTicketInMs, (double)ticket.getOutTime().getTime(), 500); //500 ms tolerance as milliseconds are lost in db

        //expected price with discount :
        double timeInHourToPay = ((dataOutSecondTicketInMs - dataInSecondTicketInMs) / (3600000f)) - 0.5; //30 minutes free
        double expectedTicketPrice = (timeInHourToPay * 1.5 /*fare rate for car*/) * 0.95; /*5% discount*/

        assertEquals(expectedTicketPrice, ticket.getPrice(), 0.001 );
    }

}
