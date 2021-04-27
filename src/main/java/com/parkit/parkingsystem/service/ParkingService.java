package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Calendar;
import java.util.Date;

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

    public void processIncomingVehicle() {
        try{
            ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
            if(parkingSpot !=null && parkingSpot.getId() > 0){
                String vehicleRegNumber = getVehicleRegNumber();
                Calendar calendar = Calendar.getInstance();
                Date inTime =   calendar.getTime();
                Ticket ticket = new Ticket();
                ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(0);
                ticket.setInTime(inTime);
                ticket.setOutTime(null);
                Ticket savedTicket = ticketDAO.getTicket(vehicleRegNumber);
                if (ticketDAO.getLastTicket(vehicleRegNumber)!=null && ticketDAO.getLastTicket(vehicleRegNumber).getOutTime()==null){
                    logger.error("Vehicle already parked.");
                    System.out.println("Vehicle already parked.");
                } else if (savedTicket!=null && savedTicket.getOutTime()!=null && savedTicket.getRecurrent()) {
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

    public ParkingSpot getNextParkingNumberIfAvailable(){
        int parkingNumber=0;
        ParkingSpot parkingSpot = null;
        try{
            ParkingType parkingType = getVehicleType();
            parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
            if(parkingNumber > 0){
                parkingSpot = new ParkingSpot(parkingNumber,parkingType, true);
            }else{
                System.out.println("Error fetching parking number from DB. Parking slots might be full");
                throw new Exception("Error fetching parking number from DB. Parking slots might be full");
            }
        }catch(IllegalArgumentException ie){
            logger.error("Error parsing user input for type of vehicle", ie);
        }catch(Exception e){
            logger.error("Error fetching next available parking slot", e);
        }
        return parkingSpot;
    }

    public void processExitingVehicle() {
        try{
            String vehicleRegNumber = getVehicleRegNumber();
            Ticket ticket = ticketDAO.getLastTicket(vehicleRegNumber);
            Calendar calendar = Calendar.getInstance();
            Date outTime = calendar.getTime();
            ticket.setOutTime(outTime);
            fareCalculatorService.calculateFare(ticket);
            fareCalculatorService.applyReduction(ticket);
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

    private void saveIncomingVehicule(Ticket ticket, ParkingSpot parkingSpot, boolean isRecurrent) {
        ticket.setRecurrent(isRecurrent);
        ticketDAO.saveTicket(ticket);
        parkingSpot.setAvailable(false);
        parkingSpotDAO.updateParking(parkingSpot);
        logger.info("Process incoming vehicle");
        System.out.println("Generated Ticket and saved in DB");
        System.out.println("Please park your vehicle in spot number:"+parkingSpot.getId());
        System.out.println("Recorded in-time for vehicle number:"+ticket.getVehicleRegNumber()+" is:"+ticket.getInTime());
    }

    private String getVehicleRegNumber() throws Exception {
        System.out.println("Please type the vehicle registration number and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }

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