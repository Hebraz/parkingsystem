package com.parkit.parkingsystem.unit.controller;

import com.parkit.parkingsystem.constants.ParkingAppAction;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.controller.ParkingController;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.view.InteractiveShell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParkingController UT")
class ParkingControllerTest {

    @Mock
    private InteractiveShell interactiveShell;

    @Mock
    private ParkingService parkingService;

    private ParkingController parkingController;

    @BeforeEach
    public void initTest(){

        ParkingController parkingControllerToSpy = new ParkingController(interactiveShell, parkingService);
        parkingController = spy(parkingControllerToSpy);
    }

    @Nested
    @DisplayName("Start")
    class Start{
        @Test
        @DisplayName("Check that parkingController executes vehicle entering action when user select it")
        void start_incomingVehicle() throws Exception {
            //ASSUMING
            doReturn(ParkingAppAction.INCOMING_VEHICULE).when(parkingController).getParkingAppAction();
            doReturn(true,false).when(parkingController).isContinueApp();
            doNothing().when(parkingController).processIncomingVehicle();

            //ACTION
            parkingController.start();

            //VERIFY
            verify(interactiveShell, times(1)).startInterface();
            verify(parkingController, times(1)).processIncomingVehicle();
            verify(interactiveShell, times(1)).stopInterface();

            verify(interactiveShell, never()).printError(anyString());
            verify(parkingController, never()).processExitingVehicle();
        }

        @Test
        @DisplayName("Check that parkingController executes vehicle exiting action when user select it")
        void start_exitingVehicle() throws Exception {
            //ASSUMING
            doReturn(ParkingAppAction.EXITING_VEHICULE).when(parkingController).getParkingAppAction();
            doReturn(true,false).when(parkingController).isContinueApp();
            doNothing().when(parkingController).processExitingVehicle();

            //ACTION
            parkingController.start();

            //VERIFY
            verify(interactiveShell, times(1)).startInterface();
            verify(parkingController, times(1)).processExitingVehicle();
            verify(interactiveShell, times(1)).stopInterface();

            verify(interactiveShell, never()).printError(anyString());
            verify(parkingController, never()).processIncomingVehicle();
        }

        @Test
        @DisplayName("Check that parkingController executes exiting application action when user select it")
        void start_exitingApplication() throws Exception {
            //ASSUMING
            doReturn(ParkingAppAction.EXIT_APPLICATION).when(parkingController).getParkingAppAction();

            //ACTION
            parkingController.start();

            //VERIFY
            verify(interactiveShell, times(1)).startInterface();
            verify(interactiveShell, times(1)).stopInterface();

            verify(parkingController, never()).processIncomingVehicle();
            verify(parkingController, never()).processExitingVehicle();
            verify(interactiveShell, never()).printError(anyString());
        }

        @Test
        @DisplayName("Check that parkingController returns an error when unknown action is selected ")
        void start_unknownAction() throws Exception {
            //ASSUMING
            doReturn(ParkingAppAction.UNKNOWN).when(parkingController).getParkingAppAction();
            doReturn(true,false).when(parkingController).isContinueApp();

            //ACTION
            parkingController.start();

            //VERIFY
            verify(interactiveShell, times(1)).startInterface();
            verify(interactiveShell, times(1)).printError(anyString());
            verify(interactiveShell, times(1)).stopInterface();

            verify(parkingController, never()).processIncomingVehicle();
            verify(parkingController, never()).processExitingVehicle();
        }
    }

    @Nested
    @DisplayName("Process exiting vehicle")
    class ProcessExitingVehicle{
        @Test
        @DisplayName("Checks processExitingVehicle when no exception is thrown")
        void processExitingVehicle_ok() throws Exception {
            //ASSUMING
            Date now = new Date();
            Ticket ticket = new Ticket();
            ticket.setPrice(2.25);
            ticket.setVehicleRegNumber("01AZE01");
            ticket.setInTime(now);
            ticket.setOutTime(now);
            ticket.setParkingSpot(new ParkingSpot(1,ParkingType.CAR,true));

            when(interactiveShell.getVehichleRegNumber()).thenReturn("AZERTY");
            when(parkingService.processExitingVehicle(anyString())).thenReturn(ticket);

            //ACTION
            parkingController.processExitingVehicle();

            //VERIFY
            verify(parkingService, times(1)).processExitingVehicle("AZERTY");
            verify(interactiveShell, times(1)).printExitingVehicleInfo(ticket);
        }

        @Test
        @DisplayName("Checks processExitingVehicle print an error when an exception is thrown")
        void processExitingVehicle_Error() throws Exception {
            //ASSUMING
            when(interactiveShell.getVehichleRegNumber()).thenThrow(new Exception("Error to display"));

            //ACTION
            parkingController.processExitingVehicle();

            //VERIFY
            verify(interactiveShell, times(1)).printError("Error to display");
        }
    }

    @Nested
    @DisplayName("Process incoming vehicle")
    class ProcessIncomingVehicle {
        @Test
        @DisplayName("Checks that processIncomingVehicle enables to process CAR incoming")
        void processIncomingVehicle_Car() throws Exception {
            //ASSUMING
            Date now = new Date();
            Ticket ticket = new Ticket();
            ticket.setPrice(2.25);
            ticket.setVehicleRegNumber("01AZE01");
            ticket.setInTime(now);
            ticket.setParkingSpot(new ParkingSpot(1,ParkingType.CAR,false));

            when(interactiveShell.getVehichleRegNumber()).thenReturn("AZERTY");
            when(parkingService.processIncomingVehicle(any(ParkingType.class), anyString())).thenReturn(ticket);
            doReturn(ParkingType.CAR).when(parkingController).getParkingType();

            //ACTION
            parkingController.processIncomingVehicle();

            //VERIFY
            verify(parkingService, times(1)).processIncomingVehicle(ParkingType.CAR,"AZERTY");
            verify(interactiveShell, times(1)).printIncomingVehicleInfo(ticket);
        }

        @Test
        @DisplayName("Checks that processIncomingVehicle enables to process BIKE incoming")
        void processIncomingVehicle_Bike() throws Exception {
            //ASSUMING
            Date now = new Date();
            Ticket ticket = new Ticket();
            ticket.setPrice(2.25);
            ticket.setVehicleRegNumber("01AZE01");
            ticket.setInTime(now);
            ticket.setParkingSpot(new ParkingSpot(1,ParkingType.CAR,false));

            when(interactiveShell.getVehichleRegNumber()).thenReturn("XXXX");
            when(parkingService.processIncomingVehicle(any(ParkingType.class), anyString())).thenReturn(ticket);
            doReturn(ParkingType.BIKE).when(parkingController).getParkingType();

            //ACTION
            parkingController.processIncomingVehicle();

            //VERIFY
            verify(parkingService, times(1)).processIncomingVehicle(ParkingType.BIKE,"XXXX");
            verify(interactiveShell, times(1)).printIncomingVehicleInfo(ticket);
        }

        @Test
        @DisplayName("Checks that processIncomingVehicle generates an error when an exception is thrown")
        void processIncomingVehicle_Error() throws Exception {
            //ASSUMING
            when(interactiveShell.getVehichleRegNumber()).thenThrow(new Exception("Error to display"));

            //ACTION
            parkingController.processIncomingVehicle();

            //VERIFY
            verify(interactiveShell, times(1)).printError("Error to display");
        }
    }

    @Nested
    @DisplayName("Get parking type")
    class GetParkingType {
        @DisplayName("Checks that GetParkingType returns correct ParkingType according to a given selection")
        @ParameterizedTest(name = "selection: ''{0}'', expected parkingType ''{1}''")
        @CsvSource({ "1, CAR" , "2, BIKE" } )
        void getParkingType_Ok(int selection, String expectedParkingType) {
            //ASSUMING
            when(interactiveShell.getVehichleType()).thenReturn(selection);

            //ACTION
            ParkingType computedParkingType = parkingController.getParkingType();

            //VERIFY
            assertEquals(ParkingType.valueOf(expectedParkingType), computedParkingType);
        }

        @DisplayName("Checks that GetParkingType throws an exception when selection does not correspond to a valid PakingType")
        @ParameterizedTest(name = "selection: ''{0}''")
        @ValueSource(ints = { 0, -1, 3, Integer.MIN_VALUE, Integer.MAX_VALUE} )
        void getParkingType_Nok(int selection) {
            //ASSUMING
            when(interactiveShell.getVehichleType()).thenReturn(selection);

            //ACTION
            assertThrows(IllegalArgumentException.class, () -> parkingController.getParkingType());
        }
    }

    @Nested
    @DisplayName("Get parking app action")
    class GetParkingAppAction {

        @DisplayName("Checks that GetParkingAppAction returns correct ParkingAppAction according to a given selection")
        @ParameterizedTest(name = "selection: ''{0}'', expected ParkingAppAction ''{1}''")
        @CsvSource({ "1, INCOMING_VEHICULE" , "2, EXITING_VEHICULE", "3, EXIT_APPLICATION" } )
        void getParkingAppAction_Ok(int selection, String expectedParkingAppAction) {
            //ASSUMING
            when(interactiveShell.getParkingAction()).thenReturn(selection);

            //ACTION
            ParkingAppAction parkingAppAction = parkingController.getParkingAppAction();

            //VERIFY
            assertEquals(ParkingAppAction.valueOf(expectedParkingAppAction), parkingAppAction);
        }

        @DisplayName("Checks that getParkingAppAction throws an exception when selection does not correspond to a valid ParkingAppAction")
        @ParameterizedTest(name = "selection: ''{0}''")
        @ValueSource(ints = { 0, -1, 4, Integer.MIN_VALUE, Integer.MAX_VALUE} )
        void getParkingAppAction_Nok(int selection) {
            //ASSUMING
            when(interactiveShell.getParkingAction()).thenReturn(selection);

            //ACTION
            assertThrows(IllegalArgumentException.class, () -> parkingController.getParkingAppAction());
        }
    }

}