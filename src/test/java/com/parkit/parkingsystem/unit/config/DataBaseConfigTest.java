package com.parkit.parkingsystem.unit.config;

import com.parkit.parkingsystem.config.DataBaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
@DisplayName("DataBaseConfigTest UT")
class DataBaseConfigTest {

    @Mock
    Connection connection;
    @Mock
    PreparedStatement  preparedStatement;
    @Mock
    ResultSet resultSet;

    DataBaseConfig dataBaseConfig;

    @BeforeEach
    void initTest(){
        dataBaseConfig = new DataBaseConfig();
    }

    @Nested
    @DisplayName("Close connection")
    class CloseConnection {
        @Test
        @DisplayName("Check that closeConnection call close method of connection instance if this one is not null")
        void closeConnectionNonNullConnectionParam() throws SQLException {
            //ASSUME

            //ACTION
            dataBaseConfig.closeConnection(connection);

            //VERIFY
            verify(connection,times(1)).close();
        }

        @Test
        @DisplayName("Check that closeConnection does not throw an exception if connection instance is null")
        void closeConnectionNullConnectionParam() throws SQLException {
            //ASSUME

            //ACTION
            assertDoesNotThrow(() ->  dataBaseConfig.closeConnection(null));
        }
    }

    @Nested
    @DisplayName("Close prepared statement")
    class ClosePreparedStatement {
        @Test
        @DisplayName("Check that closePreparedStatement call close method of PreparedStatement instance if this one is not null")
        void closePreparedStatementNonNullConnectionParam() throws SQLException {
            //ASSUME

            //ACTION
            dataBaseConfig.closePreparedStatement(preparedStatement);

            //VERIFY
            verify(preparedStatement,times(1)).close();
        }

        @Test
        @DisplayName("Check that closePreparedStatement does not throw an exception if PreparedStatement instance is null")
        void closePreparedStatementNullParam() throws SQLException {
            //ASSUME

            //ACTION
            assertDoesNotThrow(() ->  dataBaseConfig.closePreparedStatement(null));
        }

    }


    @Nested
    @DisplayName("Close result set")
    class CloseResultSet {
        @Test
        @DisplayName("Check that closeResultSe call close method of ResultSet instance if this one is not null")
        void closeResultSetNonNullConnectionParam() throws SQLException {
            //ASSUME

            //ACTION
            dataBaseConfig.closeResultSet(resultSet);

            //VERIFY
            verify(resultSet,times(1)).close();
        }

        @Test
        @DisplayName("Check that closeResultSe does not throw an exception if ResultSet instance is null")
        void closeResultSetNullParam() throws SQLException {
            //ASSUME

            //ACTION
            assertDoesNotThrow(() ->  dataBaseConfig.closeResultSet(null));
        }
    }

}