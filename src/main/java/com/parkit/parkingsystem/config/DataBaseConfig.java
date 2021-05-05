package com.parkit.parkingsystem.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

import static org.mortbay.jetty.security.Credential.MD5.digest;

/**
 * A class responsible for DataBase configuration.
 */
public class DataBaseConfig {

    private static final Logger logger = LogManager.getLogger("DataBaseConfig");
    private static final String password = digest("rootroot").split(":")[1];

    /**
     * A function responsible for getting an access to the prod database for park'it
     * @return a new connection to the prod database
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        logger.info("Create DB connection");
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/prod?serverTimezone=UTC", "root", password);
    }

    /**
     * A function responsible for closing the connection, used to close the access to the prod database for park'it
     * @param con the connection to be closed
     */
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

    /**
     * A function responsible for closing preparedStatement, in order to close the preparedStatement used
     * for transaction with the prod database of park'it
     * @param ps the preparedStatement to be closed
     */
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

    /**
     * A function responsible for closing resultSet, in order to close the resultSet used
     * for transaction with the prod database of park'it
     * @param rs the resultSet to be closed
     */
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
