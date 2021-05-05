package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * A class responsible for parking vehicle
 */
public class ParkingService {

    private static final Logger logger = LogManager.getLogger(ParkingService.class);

    private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

    private InputReaderUtil inputReaderUtil;
    private ParkingSpotDAO parkingSpotDAO;
    private  TicketDAO ticketDAO;

    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO){
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
    }

    /**
     * A method responsible for processing an incoming vehicle
     */
    public void processIncomingVehicle() {
        try{
            ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
            if(parkingSpot !=null && parkingSpot.getId() > 0){
                String vehicleRegNumber = getVehicleRegNumber();
                Timestamp inTime = Timestamp.from(Instant.now());
                Ticket ticket = new Ticket();
                ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(0);
                ticket.setInTime(inTime);
                Ticket savedTicket = ticketDAO.getTicket(vehicleRegNumber);
                if (ticketDAO.getLastTicket(vehicleRegNumber)!=null && ticketDAO.getLastTicket(vehicleRegNumber).getOutTime()==null){
                    logger.error("Vehicle already parked.");
                    System.out.println("Vehicle already parked.");
                } else if (savedTicket!=null && savedTicket.getOutTime()!=null && savedTicket.getIsRecurrent()) {
                    System.out.println("Recurrent User New Entry");
                    saveIncomingVehicule(ticket, parkingSpot, true);
                } else if (savedTicket==null) {
                    saveIncomingVehicule(ticket, parkingSpot, false);
                }
            }
        }catch(Exception e){
            logger.error("Unable to process incoming vehicle",e);
        }
    }

    /**
     * A method responsible for getting the next parking slot available
     * @return the number of the next parking slot
     */
    public ParkingSpot getNextParkingNumberIfAvailable(){
        int parkingNumber=0;
        ParkingSpot parkingSpot = null;
        try{
            ParkingType parkingType = getVehicleType();
            parkingNumber = parkingSpotDAO.getNextAvailableSpot(parkingType);
            if(parkingNumber > 0){
                parkingSpot = new ParkingSpot(parkingNumber,parkingType, true);
            }else{
                System.out.println("Error fetching parking number from DB. Parking spots might be full");
                throw new Exception("Error fetching parking number from DB. Parking spots might be full");
            }
        }catch(IllegalArgumentException ie){
            logger.error("Error parsing user input for type of vehicle", ie);
        }catch(Exception e){
            logger.error("Error fetching next available parking spot", e);
        }
        return parkingSpot;
    }

    /**
     * A method responsible for processing vehicle exiting
     */
    public void processExitingVehicle() {
        try{
            String vehicleRegNumber = getVehicleRegNumber();
            Ticket ticket = ticketDAO.getLastTicket(vehicleRegNumber);
            Timestamp outTime = Timestamp.from(Instant.now());
            ticket.setOutTime(outTime);
            fareCalculatorService.calculateFare(ticket);
            fareCalculatorService.applyReduction(ticket);
            ticketDAO.updateTicket(ticket);
            if(ticketDAO.updateTicket(ticket)) {
                ParkingSpot parkingSpot = ticket.getParkingSpot();
                parkingSpot.setAvailable(true);
                parkingSpotDAO.updateParking(parkingSpot);
                System.out.println("Please pay the parking fare:" + ticket.getPrice());
                System.out.println("Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + outTime);
            }else{
                System.out.println("Unable to update ticket information. Error occurred");
            }
        }catch(Exception e){
            logger.error("Unable to process exiting vehicle",e);
        }
    }

    /**
     * A method responsible for processing vehicle incoming
     * @param ticket the ticket containing inTime, outTime, vehicle number, recurrence and price data
     * @param parkingSpot the number of the parkingSpot used by the new vehicle
     * @param isRecurrent which is true for a recurrent user
     */
    private void saveIncomingVehicule(Ticket ticket, ParkingSpot parkingSpot, boolean isRecurrent) {
        ticket.setIsRecurrent(isRecurrent);
        ticketDAO.saveTicket(ticket);
        parkingSpot.setAvailable(false);
        parkingSpotDAO.updateParking(parkingSpot);
        logger.info("Process incoming vehicle");
        System.out.println("Generated Ticket and saved in DB");
        System.out.println("Please park your vehicle in spot number:"+parkingSpot.getId());
        System.out.println("Recorded in-time for vehicle number:"+ticket.getVehicleRegNumber()+" is:"+ticket.getInTime());
    }

    /**
     * A method responsible for getting vehicle registration number
     * @return the number of the vehicle registration number written by the user
     * @throws Exception
     */
    private String getVehicleRegNumber() throws Exception {
        System.out.println("Please type the vehicle registration number and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }

    /**
     * A method responsible for getting vehicle type
     * @return the number of the vehicle type written by the user
     */
    private ParkingType getVehicleType(){
        System.out.println("Please select vehicle type from menu");
        System.out.println("1 CAR");
        System.out.println("2 BIKE");
        int input = inputReaderUtil.readSelection();
        switch(input){
            case 1: {
                return ParkingType.CAR;
            }
            case 2: {
                return ParkingType.BIKE;
            }
            default: {
                System.out.println("Incorrect input provided");
                throw new IllegalArgumentException("Entered input is invalid");
            }
        }
    }
}
