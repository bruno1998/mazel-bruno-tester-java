package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.text.DecimalFormat;
import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    ParkingService parkingService;
    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();

    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown() {

    }

    @Test
    public void testParkingACar() throws Exception {
        assertNotNull(parkingService);

        parkingService.processIncomingVehicle();
        assertNotEquals(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR), 1);
        assertTrue(ticketDAO.isAlreadyInsideTheParking("ABCDEF"));

    }

    @Test
    public void testParkingLotExit() throws Exception {
        testParkingACar();

        parkingService.processExitingVehicle();
        assertNotNull(ticketDAO.getTicket("ABCDEF").getOutTime());
    }

    @Test
    public void testParkingLotExitRecurringUser() throws Exception {

        parkingService.processIncomingVehicle();
        Ticket tmp = ticketDAO.getTicket("ABCDEF");
        Date date0 = new Date(System.currentTimeMillis() - (2 * 60 * 60 * 1000));
        ticketDAO.updateIntimeTicketForTI(tmp, date0);
        parkingService.processExitingVehicle();

        parkingService.processIncomingVehicle();
        Ticket ticketTmp = ticketDAO.getTicket("ABCDEF");
        Date date = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        ticketDAO.updateIntimeTicketForTI(ticketTmp, date);
        parkingService.processExitingVehicle();
        DecimalFormat df = new DecimalFormat("#." + "000");

        assertEquals(df.format(ticketDAO.getTicket("ABCDEF").getPrice()), df.format(Fare.CAR_RATE_PER_HOUR * 0.95));
    }

}
