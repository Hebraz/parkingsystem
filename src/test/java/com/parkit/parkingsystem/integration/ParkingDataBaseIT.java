package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

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
        Ticket firstTicket = new Ticket();
        firstTicket.setVehicleRegNumber(vehiculeRegistrationNumber);
        firstTicket.setParkingSpot(new ParkingSpot(1,ParkingType.CAR, true));
        firstTicket.setInTime(new Date(System.currentTimeMillis() - (48*3600*1000))); //first entrance 48h ago
        firstTicket.setOutTime(new Date(System.currentTimeMillis() - (47*3600*1000))); //first entrance 47h ago
        firstTicket.setPrice(0.75);
        ticketDAO.saveTicket(firstTicket);

        //A ticket into database that corresponds to the second entrance
        long dataInSecondTicketInMs = System.currentTimeMillis() - (3*3600*1000);//second entrance 3h ago
        Ticket secondTicketEntrance = new Ticket();
        secondTicketEntrance.setVehicleRegNumber(vehiculeRegistrationNumber);
        secondTicketEntrance.setParkingSpot(new ParkingSpot(1,ParkingType.CAR, true));
        secondTicketEntrance.setInTime(new Date(dataInSecondTicketInMs)); //second entrance 3h ago
        System.out.println("########" +secondTicketEntrance.getInTime().getTime());
        secondTicketEntrance.setOutTime(null);
        secondTicketEntrance.setPrice(0.0);
        ticketDAO.saveTicket(secondTicketEntrance);

        //AC
        ParkingService parkingService = new ParkingService( parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle(vehiculeRegistrationNumber);
        long dataOutSecondTicketInMs = System.currentTimeMillis();

        //CHECK
        //retrieve ticket from database, Check that ticket has been found in DB with right data
        Ticket ticket = ticketDAO.getTicket(vehiculeRegistrationNumber);
        assertEquals((double)dataInSecondTicketInMs, (double)ticket.getInTime().getTime(), 500);//500 ms tolerance//500 ms tolerance as miliseconds are lost in db
        assertEquals((double)dataOutSecondTicketInMs, (double)ticket.getOutTime().getTime(), 500); //500 ms tolerance as miliseconds are lost in db

        //expected price with discount :
        double timeInHourToPay = ((dataOutSecondTicketInMs - dataInSecondTicketInMs) / (3600000)) - 0.5; //30 minutes free
        double expectedTicketPrice = (timeInHourToPay * 1.5 /*fare rate for car*/) * 0.95; /*5% discount*/

        assertEquals(expectedTicketPrice, ticket.getPrice(), 0.001 );
    }

}
