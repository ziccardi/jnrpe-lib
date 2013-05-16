package com.mysql.jdbc;

import java.util.ArrayList;
import java.util.List;

import it.jnrpe.plugins.mocks.sql.ISQLQueryResolver;
import it.jnrpe.plugins.mocks.sql.ResultSetMockRow;
import it.jnrpe.plugins.mocks.sql.ResultSetMockRowBuilder;

public class MySQLQueryResolver implements ISQLQueryResolver {
    private static boolean _slaveIoRunning = true;
    private static boolean _slaveSQLRunning = true;
    private static int _slaveBehindSeconds = 0;

    public MySQLQueryResolver(boolean slaveIoRunning, boolean slaveSQLRunning,
            int slaveBehindSeconds) {
        _slaveIoRunning = slaveIoRunning;
        _slaveSQLRunning = slaveSQLRunning;
        _slaveBehindSeconds = slaveBehindSeconds;
    }

    public List<ResultSetMockRow> resolveSQL(String sSQL) {
        List<ResultSetMockRow> vResult = new ArrayList<ResultSetMockRow>();

        System.out.println("QUERY : " + sSQL);

        if (sSQL.equalsIgnoreCase("show slave status;")) {
            vResult.add(new ResultSetMockRowBuilder()
                    .widthValue("Slave_IO_State",
                            "Waiting for master to send event")
                    .widthValue("Master_Host", "localhost")
                    .widthValue("Master_User", "dbadmin")
                    .widthValue("Master_Port", 3306)
                    .widthValue("Connect_Retry", 3)
                    .widthValue("Master_Log_File", "gbichot-bin.005")
                    .widthValue("Read_Master_Log_Pos", 79)
                    .widthValue("Relay_Log_File", "gbichot-relay-bin.005")
                    .widthValue("Relay_Log_Pos", 548)
                    .widthValue("Relay_Master_Log_File", "gbichot-bin.005")
                    .widthValue("Slave_IO_Running", _slaveIoRunning ? 1 : 0)
                    .widthValue("Slave_SQL_Running", _slaveSQLRunning ? 1 : 0)
                    .widthValue("Last_Errno", 0).widthValue("Skip_Counter", 0)
                    .widthValue("Exec_Master_Log_Pos", 79)
                    .widthValue("Relay_Log_Space", 552)
                    .widthValue("Until_Condition", "None")
                    .widthValue("Until_Log_Pos", 0)
                    .widthValue("Master_SSL_Allowed", "No")
                    .widthValue("Until_Log_Pos", 0)
                    .widthValue("Seconds_Behind_Master", _slaveBehindSeconds)
                    .create());
        }

        return vResult;
    }

}
