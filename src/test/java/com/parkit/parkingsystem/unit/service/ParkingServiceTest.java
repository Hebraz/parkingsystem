package com.parkit.parkingsystem.unit.service;

import com.parkit.parkingsystem.constants.ParkingAppAction;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParkingService UT")
public class ParkingServiceTest {

    private static ParkingService parkingService;
    private final String VEHICULE_REG_NUMBER ="ABCDEF";
    @Mock
    private static InputReaderUtil inputReaderUtil;
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
    class ProcessExitingVehicle {

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
            assertEquals(null, returnedTicket);
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
            assertEquals(null, returnedTicket);
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
            assertEquals(null, returnedTicket);
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
    }


    @Nested
    @DisplayName("Process incoming vehicule")
    class ProcessIncomingVehicle {
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
            assertEquals(null,returnedTicket);
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
            assertEquals(null,returnedTicket);
        }

        @DisplayName("Check that processIncomingVehicle updates parking spot" +
                " and ticket as expected for a car and bike")
        @ParameterizedTest(name= "process incoming of ''{0}'' with regNumber ''{1}'' in parking spot n° ''{2}''")
        @CsvSource({"CAR, ?;.:/!§&~é\"'{([-|è`_\\ç^@)]=}^¨$£%ù*µ, 1",
                    "CAR, AaZz09, 2147483647",
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
                            Ticket::getInTime,
                            Ticket::getOutTime)
                    .containsExactly(
                            parkingSpotCaptor.getValue(),
                            regNumber,
                            0.0,
                            actionDate,
                            null
                    );
            assertEquals(ticketCaptor.getValue(), returnedTicket);
        }
    }

    @Nested
    @DisplayName("Get next parking number if available")
    class GetNextParkingNumberIfAvailable {

        @ParameterizedTest(name= "return value of getNextAvailableSlot:''{0}''")
        @ValueSource(ints = {Integer.MIN_VALUE, Integer.MIN_VALUE/2, -1, 0})
        @DisplayName("Check that getNextParkingNumberIfAvailable returns null when there is no available parking slot")
        public void getNextParkingNumberIfAvailableNoSlotAvailable(int getNextAvailableSlotRetValue) {
            //ASSUME
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(getNextAvailableSlotRetValue);

            //ACTION
            ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable(ParkingType.BIKE);

            //CHECK
            assertEquals(null, parkingSpot);
        }

        @ParameterizedTest(name= "available slot: type:''{0}'' ; id:''{1}''")
        @CsvSource({"CAR, 1","BIKE, 1073741823", "CAR, 2147483647"})
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
            assertEquals(true, parkingSpot.isAvailable());

        }
    }

}
