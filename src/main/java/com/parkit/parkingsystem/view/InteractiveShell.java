package com.parkit.parkingsystem.view;

import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class InteractiveShell {

    private static final Logger logger = LogManager.getLogger("InteractiveShell");

    private InputReaderUtil inputReaderUtil;
    private PrintStream outStream;
    private SimpleDateFormat dateFormater;

    public InteractiveShell(InputReaderUtil inputReaderUtil, PrintStream outStream) {
        this.inputReaderUtil = inputReaderUtil;
        this.outStream = outStream;
        this.dateFormater = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    }

    public void startInterface() {
        logger.info("App initialized!!!");
        outStream.println("Welcome to Parking System!");
    }

    public int getParkingAction() {
        outStream.println("Please select an option. Simply enter the number to choose an action");
        outStream.println("1 New Vehicle Entering - Allocate Parking Space");
        outStream.println("2 Vehicle Exiting - Generate Ticket Price");
        outStream.println("3 Shutdown System");

        return inputReaderUtil.readSelection();
    }

    public String getVehichleRegNumber() throws Exception {
        outStream.println("Please type the vehicle registration number and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }

    public int getVehichleType(){
        outStream.println("Please select vehicle type from menu");
        outStream.println("1 CAR");
        outStream.println("2 BIKE");
        return inputReaderUtil.readSelection();
    }

    public void printError(String error){
        outStream.print("ERROR : ");
        outStream.println(error);
    }

    public void printIncomingVehicleInfo(Ticket ticket){
        if(Objects.nonNull(ticket) && Objects.nonNull(ticket.getParkingSpot())){
            outStream.println("Generated Ticket and saved in DB");
            outStream.println("Please park your vehicle in spot number:"+ ticket.getParkingSpot().getId());
            outStream.println("Recorded in-time for vehicle number:"+ ticket.getVehicleRegNumber() +" is:"+ dateFormater.format(ticket.getInTime()));
        } else {
            outStream.println("Unable to process incoming vehicle.");
        }
    }

    public void printExitingVehicleInfo(Ticket ticket){
        if(Objects.nonNull(ticket)) {
            outStream.println("Please pay the parking fare:" + String.format("%.2f", ticket.getPrice()));
            outStream.println("Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + dateFormater.format(ticket.getOutTime()));
        } else {
            outStream.println("Unable to update ticket information. Error occurred");
        }
    }

    public void stopInterface(){
        outStream.println("Exiting from the system!");
    }
}
