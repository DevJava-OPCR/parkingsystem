package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

/**
 * A class responsible for ticket data
 */
public class TicketDAO {

    private static final Logger logger = LogManager.getLogger(TicketDAO.class);
    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    /**
     * A function responsible for recording a new ticket for incoming vehicle
     * @param ticket the ticket containing inTime, outTime, vehicle, recurrence and price data
     * @return true if the ticket is saved with success
     */
    public boolean saveTicket(Ticket ticket){
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.SAVE_TICKET);
            ps.setInt(1,ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
            ps.setBoolean(5, (ticket.getIsRecurrent()));
            logger.info("Ticket saved.");
            return ps.execute();
        } catch (SQLException ex) {
            logger.error("Error saving ticket.",ex);
        } finally {
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
            return false;
        }
    }

    /**
     * A function responsible for getting a ticket for a recurrent vehicle
     * @param vehicleRegNumber the vehicle registration number
     * @return a ticket associated with this vehicle registration number
     */
    public Ticket getTicket(String vehicleRegNumber) {
        Connection con = null;
        Ticket ticket = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.GET_TICKET);
            ps.setString(1,vehicleRegNumber);
            rs = ps.executeQuery();
            if(rs.next()){
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(7)),false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
                ticket.setIsRecurrent(rs.getBoolean(6));
            }
            logger.info("Ticket get.");
        } catch (SQLException ex) {
            logger.error("Error getting ticket.",ex);
        } finally {
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
            return ticket;
        }
    }

    /**
     * A function responsible for getting the last ticket data for a recurrent vehicle
     * @param vehicleRegNumber the vehicle registration number
     * @return the last ticket recording for this recurrent vehicle
     */
    public Ticket getLastTicket(String vehicleRegNumber) {
        Connection con = null;
        Ticket ticket = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.GET_LAST_TICKET);
            ps.setString(1,vehicleRegNumber);
            rs = ps.executeQuery();
            if(rs.next()){
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(8)),false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
                ticket.setIsRecurrent(rs.getBoolean(6));
                ticket.setLastUpdated(rs.getBoolean(7));
            }
            logger.info("Ticket get.");
        } catch (SQLException ex) {
            logger.error("Error getting ticket.",ex);
        } finally {
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
            return ticket;
        }
    }

    /**
     * A function responsible for updating ticket data for a recurrent vehicle
     * @param ticket the ticket of the vehicle
     * @return true if the ticket data is updated
     */
    public boolean updateTicket(Ticket ticket) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
            ps.setBoolean(3, true);
            ps.setBoolean(4, false);
            ps.setInt(5,ticket.getId());
            ps.execute();
            logger.info("Ticket info updated");
            return true;
        } catch (SQLException | ClassNotFoundException ex) {
            logger.error("Error updating ticket info.",ex);
        } finally {
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }
        return false;
    }

}
