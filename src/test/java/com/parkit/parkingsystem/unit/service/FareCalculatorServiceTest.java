package com.parkit.parkingsystem.unit.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.*;
import org.mockito.internal.matchers.Null;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
@DisplayName("FareCalculatorService UT")
public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    @Nested
    @DisplayName("Calculate Fare")
    class calculateFare {
        @Test
        @DisplayName("Check fare rate for a car")
        public void calculateFareCar(){
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);
            fareCalculatorService.calculateFare(ticket);
            assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR);
        }

        @Test
        @DisplayName("Check fare rate for a bike")
        public void calculateFareBike(){
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);
            fareCalculatorService.calculateFare(ticket);
            assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
        }

        @Test
        @DisplayName("Check calculateFare throws an exception when ParkingType is unknown")
        public void calculateFareUnkownType(){
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, null,false);

            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);
            assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
        }

        @Test
        @DisplayName("Check calculateFare throws an exception when inTime is more recent than outTime")
        public void calculateFareBikeWithFutureInTime(){
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() + (  60 * 60 * 1000) );
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);
            assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
        }

        @Test
        @DisplayName("Check calculateFare calculates expected fare for less than an hour for bike")
        public void calculateFareBikeWithLessThanOneHourParkingTime(){
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);
            fareCalculatorService.calculateFare(ticket);
            assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice() );
        }

        @Test
        @DisplayName("Check calculateFare calculates expected fare for less than an hour for car")
        public void calculateFareCarWithLessThanOneHourParkingTime(){
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);
            fareCalculatorService.calculateFare(ticket);
            assertEquals( (0.75 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
        }

        @Test
        @DisplayName("Check calculateFare calculates expected fare for more than one day")
        public void calculateFareCarWithMoreThanADayParkingTime(){
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  24 * 60 * 60 * 1000) );//24 hours parking time should give 24 * parking fare per hour
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);
            fareCalculatorService.calculateFare(ticket);
            assertEquals( (24 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
        }

        @Test
        @DisplayName("Check calculateFare throws exception when in time is null")
        public void calculateFareCarWithInTimeNull(){
            Date inTime = null;
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);
            assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
        }

        @Test
        @DisplayName("Check calculateFare throws exception when out time is null")
        public void calculateFareCarWithOutTimeNull(){
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  24 * 60 * 60 * 1000) );//24 hours parking time should give 24 * parking fare per hour
            Date outTime = null;
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);
            assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
        }
    }


}
