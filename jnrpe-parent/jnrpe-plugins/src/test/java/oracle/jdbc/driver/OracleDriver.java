package oracle.jdbc.driver;

import it.jnrpe.plugins.mocks.sql.DbConnectionMock;
import it.jnrpe.plugins.mocks.sql.MockDriver;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class OracleDriver extends MockDriver {

    public Connection connect(String url, Properties info) throws SQLException {
        if (url.equals("jdbc:oracle:thin:@127.0.0.1:1521:mockdb")) {
            return new DbConnectionMock(new OracleSQLQueryResolver());
        }

        throw new SQLException(
                "Listener refused the connection with the following error: ORA-12505, TNS:listener does not currently know of SID given in connect descriptor");
    }

}
