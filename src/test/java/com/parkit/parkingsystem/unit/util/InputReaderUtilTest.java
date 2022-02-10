package com.parkit.parkingsystem.unit.util;

import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("InputReaderUtil UT")
class InputReaderUtilTest {

    private InputReaderUtil inputReaderUtil;

    private void initTest(String data){
        inputReaderUtil = new InputReaderUtil(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
    }

    static Stream<String> blankStrings() {
        return Stream.of("", "   ");
    }

    @Nested
    @DisplayName("Read selection")
    class ReadSelectionTest {

        @DisplayName("Check that readSelection returns user entry when it is an integer")
        @ParameterizedTest(name = "user entry: ''{0}''")
        @ValueSource(ints = { Integer.MIN_VALUE, Integer.MIN_VALUE/2, 0, Integer.MAX_VALUE/2, Integer.MAX_VALUE })
        void readSelectionOk(int expectedSelection) {
            //GIVEN
            initTest(expectedSelection+"\r\n");
            //WHEN
            int selection = inputReaderUtil.readSelection();
            //THEN
            assertEquals(expectedSelection, selection);
        }

        @DisplayName("Check that it returns -1 when user entry is not an integer")
        @ParameterizedTest(name = "user entry: ''{0}''")
        @ValueSource(strings = { "1.0", "", "A", "0x11", "1e23" })
        void readSelectionNok(String typedInput) {
            //GIVEN
            initTest(typedInput+"\r\n");
            //WHEN
            int selection = inputReaderUtil.readSelection();
            //THEN
            assertEquals(-1, selection);
        }
    }

    @Nested
    @DisplayName("Read vehicle registration number")
    class ReadVehicleRegistrationNumberTest {

        @DisplayName("Check that readVehicleRegistrationNumber returns user entry when it is a non null empty string")
        @ParameterizedTest(name = "user entry: ''{0}''")
        @ValueSource(strings = { "0", "A", "AZaz09", "?,;.:/!§&~é\"'{([-|è`_\\ç^@)]=}^¨$£%ù*µ"})
        void readVehicleRegistrationNumberOk(String expectedVehicleRegNumber) throws Exception {
            //GIVEN
            initTest(expectedVehicleRegNumber+"\r\n");
            //WHEN
            String vehicleRegNumber = inputReaderUtil.readVehicleRegistrationNumber();
            //THEN
            assertEquals(expectedVehicleRegNumber, vehicleRegNumber);
        }

        @DisplayName("Check that readVehicleRegistrationNumber throws IllegalArgumentException when user entry is a blank string")
        @ParameterizedTest(name = "user entry: ''{0}''")
        @MethodSource("com.parkit.parkingsystem.unit.util.InputReaderUtilTest#blankStrings")
        void readVehicleRegistrationNumberBlankString(String expectedVehicleRegNumber) {
            //GIVEN
            initTest(expectedVehicleRegNumber+"\r\n");
            //WHEN
            assertThrows(IllegalArgumentException.class, () -> inputReaderUtil.readVehicleRegistrationNumber());
            //THEN

        }
    }
}