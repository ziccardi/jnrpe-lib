package it.jnrpe.plugins.mocks.sql;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

public abstract class MockDriver implements Driver {

    public static int QUERY_TIME = 0;

    private static int CONNECTION_TIME = 0;

    public Connection connect(String url, Properties info) throws SQLException {
        if (CONNECTION_TIME > 0) {
            try {
                Thread.sleep(CONNECTION_TIME);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void setConnectionTime(int millis) {
        CONNECTION_TIME = millis;
    }

    public boolean acceptsURL(String url) throws SQLException {
        return false;
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
            throws SQLException {
        return null;
    }

    public int getMajorVersion() {
        return 0;
    }

    public int getMinorVersion() {
        return 0;
    }

    public boolean jdbcCompliant() {
        return true;
    }
}
