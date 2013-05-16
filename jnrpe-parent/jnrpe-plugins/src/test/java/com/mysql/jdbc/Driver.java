package com.mysql.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import it.jnrpe.plugins.mocks.sql.DbConnectionMock;
import it.jnrpe.plugins.mocks.sql.MockDriver;

public class Driver extends MockDriver {
    private final String RIGHTDBURL =
            "jdbc:mysql://localhost:3306/mockdb?user=dbadmin&password=dbadminpwd&autoReconnect=true&failOverReadOnly=false&maxReconnects=3";

    private static boolean _slaveIoRunning = true;
    private static boolean _slaveSQLRunning = true;
    private static int _slaveBehindSeconds = 0;

    public Connection connect(String url, Properties info) throws SQLException {
        super.connect(url, info);

        if (url.equals(RIGHTDBURL)) {
            return new DbConnectionMock(new MySQLQueryResolver(_slaveIoRunning,
                    _slaveSQLRunning, _slaveBehindSeconds));
        }

        throw new SQLException(
                "Unable to connect to any hosts due to exception: java.net.ConnectException: Connection refused");
    }

    public static void setSlaveStatus(boolean slaveIoRunning,
            boolean slaveSQLRunning, int slaveBehindSeconds) {
        _slaveIoRunning = slaveIoRunning;
        _slaveSQLRunning = slaveSQLRunning;
        _slaveBehindSeconds = slaveBehindSeconds;
    }

}
