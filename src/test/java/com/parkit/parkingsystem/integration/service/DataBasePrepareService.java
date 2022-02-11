package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.model.Ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

public class DataBasePrepareService {

    DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

    public void clearDataBaseEntries(){
        try(Connection connection = dataBaseTestConfig.getConnection()){
            //set parking entries to available
            connection.prepareStatement("update parking set available = true").execute();
            //clear ticket entries;
            connection.prepareStatement("truncate table ticket").execute();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
