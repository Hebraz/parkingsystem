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
@DisplayName("ParkingService IT")
public class ParkingServiceIT {

    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private static final  String vehiculeRegistrationNumber = "ABCDEF";
    private static ParkingService parkingService;
    @BeforeAll
    private static void setUp() {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
        parkingService = new ParkingService(parkingSpotDAO, ticketDAO);
    }

    @BeforeEach
    private void setUpPerTest()  {
        dataBasePrepareService.clearDataBaseEntries();
    }


    @Test
    @DisplayName("Check that when parking if full for cars, entering action fails for a car")
    public void testParkingFullOfCarsEnteringFailForCar() throws InterruptedException {

        //PREPARE
        //Enters 3 cars into parking => shall be accepted <=> returned ticket is not null
        Ticket ticket1 =  parkingService.processIncomingVehicle(ParkingType.CAR, "CAR_1");
        Ticket ticket2 =  parkingService.processIncomingVehicle(ParkingType.CAR, "CAR_2");
        Ticket ticket3 =  parkingService.processIncomingVehicle(ParkingType.CAR, "CAR_3");

         //ACT

        Ticket ticket4 =  parkingService.processIncomingVehicle(ParkingType.CAR, "CAR_4");

        //CHECK
        //Check that ticket 1,2,3 are not null => entering accepted
        assertNotNull(ticket1);
        assertNotNull(ticket2);
        assertNotNull(ticket3);
        //Check that ticket 4 is null => operation fails
        assertNull(ticket4);
    }

    @Test
    @DisplayName("Check that when parking if full for cars, entering action is accepted for a bike")
    public void testParkingFullOfCarsEnteringSucceedForBike() throws InterruptedException {

        //PREPARE
        //Enters 3 cars into parking => shall be accepted <=> returned ticket is not null
        Ticket ticket1 =  parkingService.processIncomingVehicle(ParkingType.CAR, "CAR_1");
        Ticket ticket2 =  parkingService.processIncomingVehicle(ParkingType.CAR, "CAR_2");
        Ticket ticket3 =  parkingService.processIncomingVehicle(ParkingType.CAR, "CAR_3");

        //ACT
        Ticket ticket4 =  parkingService.processIncomingVehicle(ParkingType.BIKE, "BIKE_1");

        //CHECK
        //Check that ticket 1,2,3 and 4 are not null => entering accepted
        assertNotNull(ticket1);
        assertNotNull(ticket2);
        assertNotNull(ticket3);
        assertNotNull(ticket4);
    }

    @Test
    @DisplayName("Check that when parking if full for bikes, entering action fails for a bike")
    public void testParkingFullOfBikesEnteringFailForBike() throws InterruptedException {

        //PREPARE
        //Enters 2 bikes into parking => shall be accepted <=> returned ticket is not null
        Ticket ticket1 =  parkingService.processIncomingVehicle(ParkingType.BIKE, "BIKE_1");
        Ticket ticket2 =  parkingService.processIncomingVehicle(ParkingType.BIKE, "BIKE_2");

        //ACT

        Ticket ticket3 =  parkingService.processIncomingVehicle(ParkingType.BIKE, "BIKE_3");

        //CHECK
        //Check that ticket 1,2,3 are not null => entering accepted
        assertNotNull(ticket1);
        assertNotNull(ticket2);
        //Check that ticket 4 is null => operation fails
        assertNull(ticket3);
    }

    @Test
    @DisplayName("Check that when parking if full for bikes, entering action is accepted for a car")
    public void testParkingFullOfBikesEnteringSucceedForCar() throws InterruptedException {

        //PREPARE
        //Enters 2 bikes into parking => shall be accepted <=> returned ticket is not null
        Ticket ticket1 =  parkingService.processIncomingVehicle(ParkingType.BIKE, "BIKE_1");
        Ticket ticket2 =  parkingService.processIncomingVehicle(ParkingType.BIKE, "BIKE_2");

        //ACT

        Ticket ticket3 =  parkingService.processIncomingVehicle(ParkingType.CAR, "CAR_1");

        //CHECK
        //Check that ticket 1,2,3 are not null => entering accepted
        assertNotNull(ticket1);
        assertNotNull(ticket2);
        assertNotNull(ticket3);
    }

    @Test
    @DisplayName("Check that trying exiting a non registered car fails")
    public void testParkingExitUnknownCarFails() throws InterruptedException {

        //PREPARE
        //Enters 3 cars into parking => shall be accepted <=> returned ticket is not null
        Ticket ticket1 =  parkingService.processIncomingVehicle(ParkingType.CAR, "CAR_1");
        Ticket ticket2 =  parkingService.processIncomingVehicle(ParkingType.CAR, "CAR_2");
        Ticket ticket3 =  parkingService.processIncomingVehicle(ParkingType.CAR, "CAR_3");

        //ACT

        Ticket ticket4 =  parkingService.processExitingVehicle("CAR_4");

        //CHECK
        //Check that ticket 1,2,3 are not null => entering accepted
        assertNotNull(ticket1);
        assertNotNull(ticket2);
        assertNotNull(ticket3);
        //Check that ticket 4 is null => exit fails
        assertNull(ticket4);
    }

}
