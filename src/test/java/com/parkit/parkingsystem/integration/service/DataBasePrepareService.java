package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A class responsible for preparing test DataBase
 */
public class DataBasePrepareService {

    DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

    /**
     * A function responsible for removing the test DataBase entries in order to launch tests
     */
    public void clearDataBaseEntries(){
        Connection connection = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        try {
            connection = dataBaseTestConfig.getConnection();
            ps1 = connection.prepareStatement("update parking set available = true");
            ps1.execute();
            ps2 = connection.prepareStatement("truncate table ticket");
            ps2.execute();
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            dataBaseTestConfig.closePreparedStatement(ps1);
            dataBaseTestConfig.closePreparedStatement(ps2);
            dataBaseTestConfig.closeConnection(connection);
        }
    }

}
