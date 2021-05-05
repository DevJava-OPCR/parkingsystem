package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A class responsible for parking availability
 */
public class ParkingSpotDAO {

    private static final Logger logger = LogManager.getLogger(ParkingSpotDAO.class);
    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    /**
     * A method responsible for getting the next available spot of parking
     * @param parkingType the type of parking used by the vehicle
     * @return the number of the next available spot of this type of parking
     */
    public int getNextAvailableSpot(ParkingType parkingType){
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int result=-1;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.GET_NEXT_PARKING_SPOT);
            ps.setString(1, parkingType.toString());
            rs = ps.executeQuery();
            if(rs.next()){
                result = rs.getInt(1);;
            }
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
            logger.info("Next available spot get.");
        } catch (SQLException | ClassNotFoundException ex){
            logger.error("Error fetching next available spot",ex);
        }finally {
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }
        return result;
    }

    /**
     * A function responsible for updating parking availability
     * @param parkingSpot the number of the parking spot
     * @return true if the parkingSpot is updated
     */
    public boolean updateParking(ParkingSpot parkingSpot){
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.UPDATE_PARKING_SPOT);
            ps.setBoolean(1, parkingSpot.isAvailable());
            ps.setInt(2, parkingSpot.getId());
            int updateRowCount = ps.executeUpdate();
            dataBaseConfig.closePreparedStatement(ps);
            logger.info("Parking availability updated.");
            return (updateRowCount == 1);
        } catch (SQLException | ClassNotFoundException ex) {
            logger.error("Error updating parking availability", ex);
            return false;
        } finally {
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }
    }

}
