package com.parkit.parkingsystem.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class InputReaderUtil {

    private final Scanner scan;
    private static final Logger logger = LogManager.getLogger("InputReaderUtil");

    public InputReaderUtil(InputStream inputStream){
        scan =  new Scanner(inputStream, StandardCharsets.UTF_8);
    }
    public int readSelection() {
        try {
            return Integer.parseInt(scan.nextLine());
        }catch(Exception e){
            logger.error("Error while reading user input from Shell", e);
            return -1;
        }
    }

    public String readVehicleRegistrationNumber() {
        try {
            String vehicleRegNumber= scan.nextLine();

            //scanner will throw an NullPointerException if no line is in input Stream => vehicleRegNumber cannot be null

            if(vehicleRegNumber.trim().length()==0) {
                throw new IllegalArgumentException("Invalid input provided");
            }
            return vehicleRegNumber;
        }catch(Exception e){
            logger.error("Error while reading user input from Shell", e);
            throw new IllegalArgumentException("Error reading input. Please enter a valid string for vehicle registration number", e);
        }
    }
}
