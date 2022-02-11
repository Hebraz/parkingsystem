package com.parkit.parkingsystem.unit.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
@DisplayName("FareCalculatorService UT")
public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;
    private final double fareAcceptedDeltaForEquality = 0.001; //equality on double shall be test with a delta : take a centime / 10
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
    class CalculateFareTest {
        @Test
        @DisplayName("Check fare rate for a car")
        public void calculateFareCar(){
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  90 * 60 * 1000)); //first 30 minutes are free: stay 90 minutes to pay 60
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);
            fareCalculatorService.calculateFare(ticket);
            assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR, fareAcceptedDeltaForEquality);
        }

        @Test
        @DisplayName("Check fare rate for a bike")
        public void calculateFareBike(){
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  90 * 60 * 1000) ); //first 30 minutes are free: stay 90 minutes to pay 60
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);
            fareCalculatorService.calculateFare(ticket);
            assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR, fareAcceptedDeltaForEquality);
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
            inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 15 minutes parking fare (First 30minutes are free)
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);
            fareCalculatorService.calculateFare(ticket);
            assertEquals((0.25 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice(), fareAcceptedDeltaForEquality );
        }

        @Test
        @DisplayName("Check calculateFare calculates expected fare for less than an hour for car")
        public void calculateFareCarWithLessThanOneHourParkingTime(){
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 15 minutes parking fare (First 30minutes are free)
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);
            fareCalculatorService.calculateFare(ticket);
            assertEquals( (0.25 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice(), fareAcceptedDeltaForEquality);
        }

        @Test
        @DisplayName("Check calculateFare calculates expected fare for more than one day")
        public void calculateFareCarWithMoreThanADayParkingTime(){
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  24 * 60 * 60 * 1000) );//24 hours parking time should give 23,5 * parking fare per hour(First 30minutes are free)
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);
            fareCalculatorService.calculateFare(ticket);
            assertEquals( (23.5 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice(), fareAcceptedDeltaForEquality);
        }

        @Test
        @DisplayName("Check calculateFare throws exception when in time is null")
        public void calculateFareCarWithInTimeNull(){
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

            ticket.setInTime(null);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);
            assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
        }

        @Test
        @DisplayName("Check calculateFare throws exception when out time is null")
        public void calculateFareCarWithOutTimeNull(){
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  24 * 60 * 60 * 1000) );//24 hours parking time should give 24 * parking fare per hour
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

            ticket.setInTime(inTime);
            ticket.setOutTime(null);
            ticket.setParkingSpot(parkingSpot);
            assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
        }

        @ParameterizedTest(name ="Vehicle type : ''{0}'' , Parking time : ''{1}'' minutes , expected minutes to be paid : ''{2}''")
        @DisplayName("Check calculateFare computes fare with 30 first minutes free")
        @CsvSource({"CAR, 1, 0", "CAR, 15, 0", "CAR, 30, 0", "CAR, 31, 1", "CAR, 60, 30", "CAR, 21600, 21570"/*15 days*/,
                "BIKE, 1, 0", "BIKE, 15, 0", "BIKE, 30, 0", "BIKE, 31, 1", "BIKE, 60, 30", "BIKE, 21600, 21570"/*15 days*/ })
        public void calculateFareFirst30MinutesFree(String parkingTypeStr, int parkingTimeInMinutes, int expectedTimeToPayInMinutes){

            //PREPARE TICKET
            double fare_rate;
            ParkingType parkingType = ParkingType.valueOf(parkingTypeStr);
            switch (parkingType){
                case CAR: fare_rate = 1.5; break;
                case BIKE: fare_rate = 1.0; break;
                default:fare_rate=0;
            }

            double expectedFare = ((double)expectedTimeToPayInMinutes / 60.0f) * fare_rate;

            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  parkingTimeInMinutes * 60 * 1000) );
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, parkingType,false);

            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);

            //ACT
            fareCalculatorService.calculateFare(ticket);

            //CHECK
            assertEquals( expectedFare , ticket.getPrice(), fareAcceptedDeltaForEquality);
        }

        @ParameterizedTest(name ="Vehicle type : ''{0}'' , Parking time : ''{1}'' minutes , discount : ''{2}'' %")
        @DisplayName("Check calculateFare takes into account ticket discount")
        @CsvSource({"CAR, 15, 5", "CAR, 60, 100", "CAR, 600, 0","CAR, 21600, 1"/*15 days*/,
                "BIKE, 30, 10", "BIKE, 100, 5", "BIKE, 6000, 99", "BIKE, 21600, 100"/*15 days*/ })
        public void calculateFareDiscountToApply(String parkingTypeStr, int parkingTimeInMinutes, double discount){

            //PREPARE TICKET
            ParkingType parkingType = ParkingType.valueOf(parkingTypeStr);

            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  parkingTimeInMinutes * 60 * 1000) );
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, parkingType,false);

            ticket.setInTime(inTime);
            ticket.setDiscountInPercent(discount);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);

            //ACT
            fareCalculatorService.calculateFare(ticket);

            //CHECK
            double fare_rate;

            switch (parkingType){
                case CAR: fare_rate = 1.5; break;
                case BIKE: fare_rate = 1.0; break;
                default:fare_rate=0;
            }

            int parkingTimeToPayInMinutes = Math.max(0, parkingTimeInMinutes - 30);
            double expectedFare = ((double)parkingTimeToPayInMinutes / 60.0f) * fare_rate * (1-discount/100f);

            assertEquals( expectedFare , ticket.getPrice(), fareAcceptedDeltaForEquality);
        }
    }
}
