package com.parkit.parkingsystem.unit.model;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ParkingSpot UT")
class ParkingSpotTest {

    private ParkingSpot parkingSpot;

    @BeforeEach
    public void initTest(){
        parkingSpot = new ParkingSpot(0, ParkingType.CAR,false);
    }

    @Nested
    @DisplayName("Equals")
    class Equals {
        @Test
        @DisplayName("Check that equals return false when parameter is null")
        void testEqualsParameterNull() {
            //GIVEN

            //ACTION and check
            boolean isEqual = parkingSpot.equals(null);

            //CHECK
            assertFalse(isEqual);
        }

        @Test
        @DisplayName("Check that equals return false when parameter has not the same number")
        void testEqualsParameterIsDifferent() {
            //GIVEN
            ParkingSpot parkingSpotToCompare = new ParkingSpot(1,ParkingType.CAR, true);
            //ACTION
            boolean isEqual = parkingSpot.equals(parkingSpotToCompare);

            //CHECK
            assertFalse(isEqual);
        }

        @Test
        @DisplayName("Check that equals return false when parameter has different class")
        void testEqualsParameterOfDifferentClass() {
            //GIVEN
            Object parkingSpotToCompare = new Object();
            //ACTION
            boolean isEqual = parkingSpot.equals(parkingSpotToCompare);

            //CHECK
            assertFalse(isEqual);
        }

        @Test
        @DisplayName("Check that equals return true when parameter has the same reference")
        void testEqualsParameterOfSameReference() {
            //GIVEN
            ParkingSpot parkingSpotToCompare = parkingSpot;
            //ACTION
            boolean isEqual = parkingSpot.equals(parkingSpotToCompare);

            //CHECK
            assertTrue(isEqual);
        }

        @ParameterizedTest(name="number:''{0}''")
        @ValueSource(ints = {Integer.MIN_VALUE, Integer.MIN_VALUE/2, 0, Integer.MAX_VALUE/2, Integer.MAX_VALUE})
        @DisplayName("Check that equals return true when parameter has not the same reference but has the same number")
        void testEqualsParameterOfSameNumber(int number) {
            //GIVEN
            ParkingSpot parkingSpotToCompare = new ParkingSpot(number, ParkingType.CAR,true);
            parkingSpot = new ParkingSpot(number, ParkingType.BIKE,false);
            //ACTION
            boolean isEqual = parkingSpot.equals(parkingSpotToCompare);

            //CHECK
            assertTrue(isEqual);
        }
    }
}