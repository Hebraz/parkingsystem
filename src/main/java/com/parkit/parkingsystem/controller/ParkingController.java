package com.parkit.parkingsystem.controller;

import com.parkit.parkingsystem.constants.ParkingAppAction;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.view.InteractiveShell;

public class ParkingController{

    private final  ParkingService parkingService;
    private final InteractiveShell interactiveShell;
    private boolean continueApp;

    public ParkingController(InteractiveShell interactiveShell, ParkingService parkingService) {
        this.interactiveShell = interactiveShell;
        this.parkingService = parkingService;
        this.continueApp = true;
    }

    public void start(){
        ParkingAppAction appAction;

        interactiveShell.startInterface();

        while(isContinueApp()) {
            appAction = getParkingAppAction();
            switch (appAction) {
                case INCOMING_VEHICLE:
                    processIncomingVehicle();
                    break;
                case EXITING_VEHICLE:
                    processExitingVehicle();
                    break;
                case EXIT_APPLICATION:
                    setContinueApp(false);
                    break;
                default:
                    interactiveShell.printError("Unsupported option. Please enter a number corresponding to the provided menu");
                    break;
            }
        }
        interactiveShell.stopInterface();
    }


    public void processExitingVehicle() {
        String vehicleRegNumber;
        try {
            vehicleRegNumber = interactiveShell.getVehicleRegNumber();
            Ticket ticket = parkingService.processExitingVehicle(vehicleRegNumber);
            interactiveShell.printExitingVehicleInfo(ticket);
        }
        catch(Exception e){
            interactiveShell.printError(e.getMessage());
        }
    }

    public void processIncomingVehicle() {
        String vehicleRegNumber;
        try {
            vehicleRegNumber = interactiveShell.getVehicleRegNumber();
            ParkingType parkingType = getParkingType();
            Ticket ticket = parkingService.processIncomingVehicle(parkingType, vehicleRegNumber);
            interactiveShell.printIncomingVehicleInfo(ticket);
        }
        catch(Exception e){
            interactiveShell.printError(e.getMessage());
        }
    }

    public ParkingType getParkingType(){
       ParkingType parkingTypeToReturn;
       int parkingTypeFromView;

        parkingTypeFromView = interactiveShell.getVehicleType();
       switch(parkingTypeFromView) {
           case 1:
                parkingTypeToReturn = ParkingType.CAR;
                break;
           case 2:
                parkingTypeToReturn = ParkingType.BIKE;
                break;
           default:
               throw new IllegalArgumentException("Entered input is invalid");
       }
       return parkingTypeToReturn;
    }

    public ParkingAppAction getParkingAppAction(){
        ParkingAppAction parkingAppActionToReturn;
        int parkingAppActionFromView;

        parkingAppActionFromView = interactiveShell.getParkingAction();
        switch (parkingAppActionFromView) {
            case 1:
                parkingAppActionToReturn = ParkingAppAction.INCOMING_VEHICLE;
                break;
            case 2:
                parkingAppActionToReturn = ParkingAppAction.EXITING_VEHICLE;
                break;
            case 3:
                parkingAppActionToReturn = ParkingAppAction.EXIT_APPLICATION;
                break;
            default:
                throw new IllegalArgumentException("Entered input is invalid");
        }
        return parkingAppActionToReturn;
    }

    public boolean isContinueApp() {
        return continueApp;
    }

    public void setContinueApp(boolean continueApp) {
        this.continueApp = continueApp;
    }
}
