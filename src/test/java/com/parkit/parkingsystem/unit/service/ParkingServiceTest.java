package com.parkit.parkingsystem.unit.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParkingService UT")
public class ParkingServiceTest {

    private static ParkingService parkingService;
    private static final String VEHICULE_REG_NUMBER ="ABCDEF";
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
            parkingService = spy(new ParkingService(parkingSpotDAO, ticketDAO));
    }

    @Nested
    @DisplayName("Process exiting vehicle")
    class ProcessExitingVehicleTest {

        @Test
        @DisplayName("Check that processExitingVehicle fails and return null" +
                " ticket when no ticket in database correspond to vehicle regular number")
        public void processExitingVehicleTesNullTicketFromDao(){
            //ASSUME
            when(ticketDAO.getTicket(any(String.class))).thenReturn(null);

            //ACTION
            Ticket returnedTicket = parkingService.processExitingVehicle(VEHICULE_REG_NUMBER);

            //CHECK
            verify(ticketDAO, never()).updateTicket(any(Ticket.class));
            verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
            assertNull(returnedTicket);
        }

        @Test
        @DisplayName("Check that processExitingVehicle does not update parking spot" +
                " when ticket update fails")
        public void processExitingVehicleTesUpdateTicketFails(){
            Ticket ticketFromDao = new Ticket();
            ticketFromDao.setInTime(new Date());
            ticketFromDao.setParkingSpot(new ParkingSpot(1,ParkingType.CAR, false));
            //ASSUME
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
            when(ticketDAO.getTicket(any(String.class))).thenReturn(ticketFromDao);

            //ACTION
            Ticket returnedTicket = parkingService.processExitingVehicle(VEHICULE_REG_NUMBER);

            //CHECK
            verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
            assertNull(returnedTicket);
        }

        @Test
        @DisplayName("Check that processExitingVehicle fails and return null" +
                " ticket when an error occurs during fare calculation")
        public void processExitingVehicleTesNullTicketAfterFareCalculationError(){

            Date inTime = new Date();
            inTime.setTime(System.currentTimeMillis() + (  60 * 60 * 1000) ); //n the future to raise a fare calculation exception

            Ticket ticketFromDao = new Ticket();
            ticketFromDao.setInTime(inTime);
            ticketFromDao.setParkingSpot(new ParkingSpot(1,ParkingType.CAR, false));

            //ASSUME
            when(ticketDAO.getTicket(any(String.class))).thenReturn(ticketFromDao);

            //ACTION
            Ticket returnedTicket = parkingService.processExitingVehicle(VEHICULE_REG_NUMBER);

            //CHECK
            verify(ticketDAO,never()).updateTicket(any(Ticket.class));
            verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
            assertNull(returnedTicket);
        }

        @Test
        @DisplayName("Check that processExitingVehicle returns expected ticket" +
                " when ticket exists in database for the vehicle regular number and error occurs during fare calculation")
        public void processExitingVehicleTesSucceed(){
            //delcare argument captors
            final ArgumentCaptor<ParkingSpot> parkingSpotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
            final ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

            //construct DAO ticket
            Date inTime = new Date();
            inTime.setTime(System.currentTimeMillis() - (  60 * 60 * 1000) ); //in the past to avoid fare calculation erro

            Ticket ticketFromDao = new Ticket();
            ticketFromDao.setInTime(inTime);
            ticketFromDao.setVehicleRegNumber(VEHICULE_REG_NUMBER);
            ticketFromDao.setParkingSpot(new ParkingSpot(1,ParkingType.CAR, false));

            //ASSUME
            when(ticketDAO.getTicket(any(String.class))).thenReturn(ticketFromDao);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            //ACTION
            Ticket returnedTicket = parkingService.processExitingVehicle(VEHICULE_REG_NUMBER);
            Date actionDate = new Date();

            //CHECK
            verify(ticketDAO, times(1)).getTicket(VEHICULE_REG_NUMBER);
            verify(ticketDAO, times(1)).updateTicket(ticketCaptor.capture());
            verify(parkingSpotDAO, times(1)).updateParking(parkingSpotCaptor.capture());

            assertThat(parkingSpotCaptor.getValue())
                    .extracting(ParkingSpot::getParkingType,
                            ParkingSpot::getId,
                            ParkingSpot::isAvailable)
                    .containsExactly(
                            ParkingType.CAR,
                            1,
                            true
                    );

            assertThat(ticketCaptor.getValue())
                    .extracting(Ticket::getParkingSpot,
                            Ticket::getVehicleRegNumber,
                            Ticket::getOutTime)
                    .containsExactly(
                            parkingSpotCaptor.getValue(),
                            VEHICULE_REG_NUMBER,
                            actionDate
                    );
            assertEquals(ticketCaptor.getValue(), returnedTicket);
        }

        @DisplayName("Check that processExitingVehicle returns expected ticket with a discount of 5 percent " +
                " if user has entered parking previously.")
        @ParameterizedTest(name="number of previous entries: ''{0}'' ; expected discount : ''{1}''")
        @CsvSource({"-2147483648, 0", "0,0","1,5", "2147483647,5"})
        public void processExitingVehicleForRecurringUser(int nbPreviousEntries, double expectedDiscount){

            final ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

            //construct DAO ticket
            Date inTime = new Date();
            inTime.setTime(System.currentTimeMillis() - (5 * 60 * 60 * 1000) ); //only 4,5h are to be paid

            Ticket ticketFromDao = new Ticket();
            ticketFromDao.setInTime(inTime);
            ticketFromDao.setVehicleRegNumber(VEHICULE_REG_NUMBER);
            ticketFromDao.setParkingSpot(new ParkingSpot(1,ParkingType.CAR, false));

            //ASSUME
            when(ticketDAO.getTicket(any(String.class))).thenReturn(ticketFromDao);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            when(ticketDAO.getNbPaidTickets(any(String.class))).thenReturn(nbPreviousEntries);

            //ACTION
            parkingService.processExitingVehicle(VEHICULE_REG_NUMBER);

            //CHECK
            verify(ticketDAO, times(1)).getTicket(VEHICULE_REG_NUMBER);
            verify(ticketDAO, times(1)).updateTicket(ticketCaptor.capture());
            verify(ticketDAO, times(1)).getNbPaidTickets(any(String.class));
            verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));

            assertEquals(expectedDiscount, ticketCaptor.getValue().getDiscountInPercent());
            assertEquals((1-expectedDiscount/100)* 4.5 * 1.5, ticketCaptor.getValue().getPrice(),0.001);
        }
    }

    @Nested
    @DisplayName("Process incoming vehicule")
    class ProcessIncomingVehicleTest {
        @Test
        @DisplayName("Check that processIncomingVehicle does not update parking spot" +
                " and ticket when next available parking spot is null")
        public void processIncomingVehicleTestNextAvailableParkingSpotNull(){
            //ASSUME
            doReturn(null).when(parkingService).getNextParkingNumberIfAvailable(any(ParkingType.class));

            //ACTION
            Ticket returnedTicket = parkingService.processIncomingVehicle(ParkingType.CAR, "1231");

            //CHECK
            verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
            verify(ticketDAO, never()).saveTicket(any(Ticket.class));
            assertNull(returnedTicket);
        }

        @DisplayName("Check that processIncomingVehicle does not update parking spot" +
                " and ticket when ID of next available parking spot is lower or equal to 0")
        @ParameterizedTest(name= "spotId:''{0}''")
        @ValueSource(ints = {Integer.MIN_VALUE, Integer.MIN_VALUE/2, -1, 0})
        public void processIncomingVehicleTestNextAvailableParkingSpotIdOutOfRange(int nextAvailableSpotId){
            //ASSUME
            ParkingSpot nextAvailableParkingSpot = new ParkingSpot(nextAvailableSpotId, ParkingType.CAR, true);
            doReturn(nextAvailableParkingSpot).when(parkingService).getNextParkingNumberIfAvailable(ParkingType.CAR);

            //ACTION
            Ticket returnedTicket = parkingService.processIncomingVehicle(ParkingType.CAR, "1231");

            //CHECK
            verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
            verify(ticketDAO, never()).saveTicket(any(Ticket.class));
            assertNull(returnedTicket);
        }

        @DisplayName("Check that processIncomingVehicle updates parking spot" +
                " and ticket as expected for a car and bike")
        @ParameterizedTest(name= "process incoming of ''{0}'' with regNumber ''{1}'' in parking spot n° ''{2}''")
        @CsvSource({"CAR, ?;.:/!§&~é\"'{([-|è`_\\ç^@)]=}^¨$£%ù*µ, 1",
                    "BIKE, With blank, 1564"})
        public void processIncomingVehicle(String parkingTypeStr, String regNumber, int parkingSpotId){

            final ArgumentCaptor<ParkingSpot> parkingSpotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
            final ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

            ParkingType parkingType = ParkingType.valueOf(parkingTypeStr);

            //ASSUME
            ParkingSpot nextAvailableParkingSpot = new ParkingSpot(parkingSpotId, parkingType, true);
            doReturn(nextAvailableParkingSpot).when(parkingService).getNextParkingNumberIfAvailable(parkingType);

            //ACTION
            Ticket returnedTicket = parkingService.processIncomingVehicle(parkingType, regNumber);
            Date actionDate = new Date();

            //CHECK
            verify(parkingService, times(1)).getNextParkingNumberIfAvailable(parkingType);
            verify(parkingSpotDAO, times(1)).updateParking(parkingSpotCaptor.capture());
            verify(ticketDAO, times(1)).saveTicket(ticketCaptor.capture());

            assertThat(parkingSpotCaptor.getValue())
                    .extracting(ParkingSpot::getParkingType,
                                ParkingSpot::getId,
                                ParkingSpot::isAvailable)
                    .containsExactly(
                            parkingType,
                            parkingSpotId,
                            false
                    );

            assertThat(ticketCaptor.getValue())
                    .extracting(Ticket::getParkingSpot,
                            Ticket::getVehicleRegNumber,
                            Ticket::getPrice,
                            Ticket::getOutTime)
                    .containsExactly(
                            parkingSpotCaptor.getValue(),
                            regNumber,
                            0.0,
                            null
                    );
            assertEquals((double)actionDate.getTime(), (double)ticketCaptor.getValue().getInTime().getTime(),(double)5);

            assertEquals(ticketCaptor.getValue(), returnedTicket);
        }

        @DisplayName("Check that processIncomingVehicle returns ticket with a discount of 5 percent " +
                " if user has entered parking previously.")
        @ParameterizedTest(name="number of previous entries: ''{0}'' ; expected discount : ''{1}''")
        @CsvSource({"1,5", "2147483647,5"})
        public void processIncomingVehicleForRecurringUser(int nbPreviousEntries, double expectedDiscount){

            //ASSUME
            ParkingSpot nextAvailableParkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
            doReturn(nextAvailableParkingSpot).when(parkingService).getNextParkingNumberIfAvailable(ParkingType.CAR);
            when(ticketDAO.getNbPaidTickets(any(String.class))).thenReturn(nbPreviousEntries);

            //ACTION
            Ticket returnedTicket = parkingService.processIncomingVehicle(ParkingType.CAR, VEHICULE_REG_NUMBER);

            //CHECK
            verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
            verify(ticketDAO, times(1)).getNbPaidTickets(VEHICULE_REG_NUMBER);

            assertEquals(expectedDiscount, returnedTicket.getDiscountInPercent());
        }
    }

    @Nested
    @DisplayName("Get next parking number if available")
    class GetNextParkingNumberIfAvailableTest {

        @ParameterizedTest(name= "return value of getNextAvailableSlot:''{0}''")
        @ValueSource(ints = {Integer.MIN_VALUE, Integer.MIN_VALUE/2, -1, 0})
        @DisplayName("Check that getNextParkingNumberIfAvailable returns null when there is no available parking slot")
        public void getNextParkingNumberIfAvailableNoSlotAvailable(int getNextAvailableSlotRetValue) {
            //ASSUME
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(getNextAvailableSlotRetValue);

            //ACTION
            ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable(ParkingType.BIKE);

            //CHECK
            assertNull(parkingSpot);
        }

        @ParameterizedTest(name= "available slot: type:''{0}'' ; id:''{1}''")
        @CsvSource({"CAR, 1", "BIKE, 2147483647"})
        @DisplayName("Check that getNextParkingNumberIfAvailable returns ParkingSlot when there is an available one in database")
        public void getNextParkingNumberIfAvailableSlotAvailable(String parkingTypeStr, int availableSlotId) {

            ParkingType parkingType = ParkingType.valueOf(parkingTypeStr);

            //ASSUME
            when(parkingSpotDAO.getNextAvailableSlot(parkingType)).thenReturn(availableSlotId);

            //ACTION
            ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable(parkingType);

            //CHECK
            assertEquals(availableSlotId, parkingSpot.getId());
            assertEquals(parkingType, parkingSpot.getParkingType());
            assertTrue(parkingSpot.isAvailable());

        }
    }

}
