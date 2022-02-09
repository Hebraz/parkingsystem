package com.parkit.parkingsystem.unit.view;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import com.parkit.parkingsystem.view.InteractiveShell;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import org.junit.jupiter.params.ParameterizedTest;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InteractiveShell UT")
class InteractiveShellTest {

    @Mock
    private InputReaderUtil inputReaderUtil;
    @Mock
    private PrintStream outStream;

    private InteractiveShell interactiveShell;

    static Stream<String> blankStrings() {
        return Stream.of("", "   ");
    }

    @BeforeEach
    private void initTest(){
        interactiveShell = new InteractiveShell(inputReaderUtil, outStream);
    }

    @Nested
    @DisplayName("Start interface")
    class StartInterface {

        @Test
        @DisplayName("Check that startInterface outputs expected welcome message")
        void startInterface() {
            // WHEN
            interactiveShell.startInterface();
            //THEN
            verify(outStream).println("Welcome to Parking System!");
        }
    }

    @Nested
    @DisplayName("Get parking action")
    class GetParkingAction {

        @DisplayName("Check that getParkingAction outputs expected parking actions and returns user entry")
        @ParameterizedTest(name = "user entry: ''{0}''")
        @ValueSource(ints = { Integer.MIN_VALUE, Integer.MIN_VALUE/2, 0, Integer.MAX_VALUE/2, Integer.MAX_VALUE })
        void getParkingAction(int expectedActionNumber) {
            //GIVEN
            when(inputReaderUtil.readSelection()).thenReturn(expectedActionNumber);
            //WHEN
            int actionNumber = interactiveShell.getParkingAction();
            //THEN
            verify(outStream).println("Please select an option. Simply enter the number to choose an action");
            verify(outStream).println("1 New Vehicle Entering - Allocate Parking Space");
            verify(outStream).println("2 Vehicle Exiting - Generate Ticket Price");
            verify(outStream).println("3 Shutdown System");
            assertEquals(expectedActionNumber, actionNumber);
        }
    }


    @Nested
    @DisplayName("Get vehicle regular number")
    class GetVehicleRegNumber {

        @DisplayName("Check that getVehicleRegNumber outputs expected message and returns user entry")
        @ParameterizedTest(name = "user entry : ''{0}''")
        @ValueSource(strings = { "A", "AZaz09", "0","?,;.:/!§&~é\"'{([-|è`_\\ç^@)]=}^¨$£%ù*µ"})
        @MethodSource("com.parkit.parkingsystem.unit.view.InteractiveShellTest#blankStrings")
        void getVehicleRegNumber(String expectedVehicleRegNumber) throws Exception {
            //GIVEN
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(expectedVehicleRegNumber);
            //WHEN
            String vehicleRegNumber = interactiveShell.getVehichleRegNumber();

            //THEN
            verify(outStream).println("Please type the vehicle registration number and press enter key");
            assertEquals(expectedVehicleRegNumber, vehicleRegNumber);
        }
    }

    @Nested
    @DisplayName("Get vehicle type")
    class GetVehicleType {
        @DisplayName("Check that getVehicleType outputs expected vehicle type message and returns user entry")
        @ParameterizedTest(name = "user entry: ''{0}''")
        @ValueSource(ints = { Integer.MIN_VALUE, Integer.MIN_VALUE/2, 0, Integer.MAX_VALUE/2, Integer.MAX_VALUE })
        void getVehicleType(int expectedVehicleType) {
            //GIVEN
            when(inputReaderUtil.readSelection()).thenReturn(expectedVehicleType);
            //WHEN
            int actionNumber = interactiveShell.getVehichleType();
            //THEN
            verify(outStream).println("Please select vehicle type from menu");
            verify(outStream).println("1 CAR");
            verify(outStream).println("2 BIKE");
            assertEquals(expectedVehicleType, actionNumber);
        }
    }

    @Nested
    @DisplayName("Print error")
    class PrintError {

        @DisplayName("Check that printError outputs expected error message")
        @ParameterizedTest(name = "error message: ''{0}''")
        @ValueSource(strings = { "A", "AZaz09", "0","?,;.:/!§&~é\"'{([-|è`_\\ç^@)]=}^¨$£%ù*µ"})
        @MethodSource("com.parkit.parkingsystem.unit.view.InteractiveShellTest#blankStrings")
        void printError(String expectedError) {
            //WHEN
            interactiveShell.printError(expectedError);
            //THEN
            verify(outStream).println(expectedError);
        }
    }

    @Nested
    @DisplayName("Print incoming vehicle info")
    class PrintIncomingVehicleInfo {

        @Test
        @DisplayName("Check that printIncomingVehicleInfo outputs error message when ticket is null")
        void printIncomingVehicleInfoWithNullTicket() {
            //WHEN
            interactiveShell.printIncomingVehicleInfo(null);
            //THEN
            verify(outStream).println("Unable to process incoming vehicle.");
        }

        @Test
        @DisplayName("Check that printIncomingVehicleInfo outputs error message when ParkingSpot of ticket is null")
        void printIncomingVehicleInfoWithNullParkingSpot() {
            Ticket ticket = new Ticket();
            ticket.setParkingSpot(null);
            //WHEN
            interactiveShell.printIncomingVehicleInfo(ticket);
            //THEN
            verify(outStream).println("Unable to process incoming vehicle.");
        }

        @Test
        @DisplayName("Check that printIncomingVehicleInfo outputs right incoming message when ticket is well filled")
        void printIncomingVehicleInfoWithGoodTicket() throws ParseException {
            //GIVEN
            SimpleDateFormat dateFormater = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date expectedDateHour = dateFormater.parse("01/01/1990 00:01:02"); ;
            Ticket ticket = new Ticket();
            ticket.setParkingSpot(new ParkingSpot(8, ParkingType.CAR, true));
            ticket.setInTime(expectedDateHour);
            ticket.setVehicleRegNumber("ABCDEF");

            //WHEN
            interactiveShell.printIncomingVehicleInfo(ticket);
            //THEN
            verify(outStream).println("Generated Ticket and saved in DB");
            verify(outStream).println("Please park your vehicle in spot number:8");
            verify(outStream).println("Recorded in-time for vehicle number:ABCDEF is:01/01/1990 00:01:02");
        }
    }


    @Nested
    @DisplayName("Print exiting vehicle info")
    class PrintExitingVehicleInfo {

        @Test
        @DisplayName("Check that printExitingVehicleInfo outputs error message when ticket is null")
        void printExitingVehicleInfoWithNullTicket() {
            //WHEN
            interactiveShell.printExitingVehicleInfo(null);
            //THEN
            verify(outStream).println("Unable to update ticket information. Error occurred");
        }

        @Test
        @DisplayName("Check that printExitingVehicleInfo outputs right exiting message when ticket is well filled")
        void printExitingVehicleInfoWithGoodTicket() throws ParseException {
            //GIVEN
            SimpleDateFormat dateFormater = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date expectedDateHour = dateFormater.parse("31/12/5000 18:45:59"); ;
            Ticket ticket = new Ticket();
            ticket.setOutTime(expectedDateHour);
            ticket.setPrice(3.5946);
            ticket.setVehicleRegNumber("ABCDEF");

            //WHEN
            interactiveShell.printExitingVehicleInfo(ticket);
            //THEN
            verify(outStream).println("Please pay the parking fare:3,59");
            verify(outStream).println("Recorded out-time for vehicle number:ABCDEF is:31/12/5000 18:45:59");
        }
    }

    @Nested
    @DisplayName("Stop interface")
    class StopInterface {
        @Test
        @DisplayName("Check that stopInterface outputs exiting application message")
        void stopInterface() {
            // WHEN
            interactiveShell.stopInterface();
            //THEN
            verify(outStream).println("Exiting from the system!");
        }
    }
}