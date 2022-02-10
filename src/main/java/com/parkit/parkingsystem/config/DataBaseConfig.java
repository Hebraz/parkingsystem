package com.parkit.parkingsystem.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.TimeZone;

public class DataBaseConfig {

    private static final Logger logger = LogManager.getLogger("DataBaseConfig");

    public Connection getConnection() throws ClassNotFoundException, SQLException, IOException {

        Connection connection = null;
        logger.info("Create DB connection");
        Class.forName("com.mysql.cj.jdbc.Driver");

        //load connection string from properties file
        Properties prop = new Properties();
        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties")){
            prop.load(inputStream);
            String connectionString = prop.getProperty("prodConnectionString");
            String login = prop.getProperty("login");
            String password = prop.getProperty("password");

            connection = DriverManager.getConnection(
                    connectionString + TimeZone.getDefault().getID(),login,password);
        }
        return connection;
    }

    public void closeConnection(Connection con){
        if(con!=null){
            try {
                con.close();
                logger.info("Closing DB connection");
            } catch (SQLException e) {
                logger.error("Error while closing connection",e);
            }
        }
    }

    public void closePreparedStatement(PreparedStatement ps) {
        if(ps!=null){
            try {
                ps.close();
                logger.info("Closing Prepared Statement");
            } catch (SQLException e) {
                logger.error("Error while closing prepared statement",e);
            }
        }
    }

    public void closeResultSet(ResultSet rs) {
        if(rs!=null){
            try {
                rs.close();
                logger.info("Closing Result Set");
            } catch (SQLException e) {
                logger.error("Error while closing result set",e);
            }
        }
    }
}
