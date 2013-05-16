/*
 * Copyright (c) 2013 Massimiliano Ziccardi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.jnrpe.plugin.mysql;

import it.jnrpe.ICommandLine;
import it.jnrpe.ReturnValue;
import it.jnrpe.Status;
import it.jnrpe.ReturnValue.UnitOfMeasure;
import it.jnrpe.events.LogEvent;
import it.jnrpe.plugins.PluginBase;
import it.jnrpe.utils.BadThresholdException;
import it.jnrpe.utils.ThresholdUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * This plugin tests connections to a MySql server.
 *
 * @author Frederico Campos
 */
public class CheckMysql extends PluginBase {

    /**
     * Executes the check.
     *
     * @param cl
     *            the command line
     * @return the check return code
     * @throws BadThresholdException
     *             -
     */
    public final ReturnValue execute(final ICommandLine cl)
            throws BadThresholdException {
        Mysql mysql = new Mysql(cl);
        long start = System.currentTimeMillis();
        long elapsed = 0L;
        Connection conn = null;
        try {
            conn = mysql.getConnection();
            elapsed = (System.currentTimeMillis() - start) / 1000L;
        } catch (ClassNotFoundException e) {
            sendEvent(
                    LogEvent.ERROR,
                    "Mysql driver library not found into the classpath: "
                            + "download and put it in the same directory "
                            + "of this plugin");
            return new ReturnValue(
                    Status.CRITICAL,
                    "CHECK_MYSQL_QUERY - CRITICAL: Error accessing "
                            + "the MySQL server - JDBC driver not installed");
        } catch (Exception e) {
            sendEvent(LogEvent.ERROR, "Error accessing the MySQL server", e);
            return new ReturnValue(Status.CRITICAL,
                    "CHECK_MYSQL_QUERY - CRITICAL: Error accessing "
                            + "the MySQL server - " + e.getMessage());
        }

        if (cl.hasOption("check-slave")) {
            return checkSlave(cl, mysql, conn).withPerformanceData("time",
                    elapsed, UnitOfMeasure.seconds,
                    cl.getOptionValue("warning"),
                    cl.getOptionValue("critical"), 0L, null);
        }

        if (cl.hasOption("critical")) {
            if (ThresholdUtil.isValueInRange(cl.getOptionValue("critical"),
                    elapsed)) {
                return new ReturnValue(Status.CRITICAL,
                        "CHECK_MYSQL - CRITICAL").withPerformanceData("time",
                        elapsed, UnitOfMeasure.seconds,
                        cl.getOptionValue("warning"),
                        cl.getOptionValue("critical"), 0L, null);
            }
        }

        if (cl.hasOption("warning")) {
            if (ThresholdUtil.isValueInRange(cl.getOptionValue("warning"),
                    elapsed)) {
                return new ReturnValue(Status.WARNING, "CHECK_MYSQL - WARNING")
                        .withPerformanceData("time", elapsed,
                                UnitOfMeasure.seconds,
                                cl.getOptionValue("warning"),
                                cl.getOptionValue("critical"), 0L, null);
            }
        }

        mysql.closeConnection(conn);
        return new ReturnValue(Status.OK, "CHECK_MYSQL - OK")
                .withPerformanceData("time", elapsed, UnitOfMeasure.seconds,
                        cl.getOptionValue("warning"),
                        cl.getOptionValue("critical"), 0L, null);
    }

    /**
     * Check the status of mysql slave thread.
     *
     * @param cl
     *            The command line
     * @param mysql
     *            MySQL connection mgr object
     * @param conn
     *            The SQL connection
     * @return ReturnValue -
     * @throws BadThresholdException
     *             -
     */
    private ReturnValue
            checkSlave(final ICommandLine cl, final Mysql mysql,
                    final Connection conn)
                    throws BadThresholdException {
        try {
            Map<String, Integer> status = getSlaveStatus(conn);
            if (status.isEmpty()) {
                mysql.closeConnection(conn);
                return new ReturnValue(Status.CRITICAL,
                        "CHECK_MYSQL - WARNING: No slaves defined. ");
            }

            // check if slave is running
            int slaveIoRunning = status.get("Slave_IO_Running");
            int slaveSqlRunning = status.get("Slave_SQL_Running");
            int secondsBehindMaster = status.get("Seconds_Behind_Master");

            if (slaveIoRunning == 0 || slaveSqlRunning == 0) {
                mysql.closeConnection(conn);
                return new ReturnValue(Status.CRITICAL,
                        "CHECK_MYSQL - CRITICAL: Slave status unavailable. ");
            }

            String slaveResult =
                    "Slave IO: " + slaveIoRunning + " Slave SQL: "
                            + slaveSqlRunning + " Seconds Behind Master: "
                            + secondsBehindMaster;

            if (cl.hasOption("critical")) {
                String critical = cl.getOptionValue("critical");
                if (ThresholdUtil.isValueInRange(
                        critical, secondsBehindMaster)) {
                    return new ReturnValue(Status.CRITICAL,
                            "CHECK_MYSQL - CRITICAL: Slow slave - "
                                    + slaveResult).withPerformanceData(
                            "secondsBehindMaster", (long) secondsBehindMaster,
                            UnitOfMeasure.seconds,
                            cl.getOptionValue("warning"),
                            cl.getOptionValue("critical"), 0L, null);
                }
            }

            if (cl.hasOption("warning")) {
                String warning = cl.getOptionValue("warning");
                if (ThresholdUtil.isValueInRange(
                        warning, secondsBehindMaster)) {
                    mysql.closeConnection(conn);
                    return new ReturnValue(Status.WARNING,
                            "CHECK_MYSQL - WARNING: Slow slave - "
                                    + slaveResult).withPerformanceData(
                            "secondsBehindMaster", (long) secondsBehindMaster,
                            UnitOfMeasure.seconds,
                            cl.getOptionValue("warning"),
                            cl.getOptionValue("critical"), 0L, null);
                }
            }

            return new ReturnValue(Status.OK, "CHECK_MYSQL - OK: "
                    + slaveResult).withPerformanceData("secondsBehindMaster",
                    (long) secondsBehindMaster, UnitOfMeasure.seconds,
                    cl.getOptionValue("warning"),
                    cl.getOptionValue("critical"), 0L, null);
        } catch (SQLException e) {
            sendEvent(LogEvent.WARNING,
                    "Error executing the CheckMysql plugin: " + e.getMessage(),
                    e);
            return new ReturnValue(Status.CRITICAL,
                    "CHECK_MYSQL - CRITICAL: Unable to check slave status:  - "
                            + e.getMessage());
        }

    }

    /**
     * Get slave statuses.
     *
     * @param conn
     *            The database connection
     * @return The slave status info
     * @throws SQLException
     *             -
     */
    private Map<String, Integer> getSlaveStatus(final Connection conn)
            throws SQLException {
        Map<String, Integer> map = new HashMap<String, Integer>();
        String query = "show slave status;";
        Statement statement;
        ResultSet rs = null;
        statement = conn.createStatement();
        rs = statement.executeQuery(query);
        while (rs.next()) {
            map.put("Slave_IO_Running", rs.getInt("Slave_IO_Running"));
            map.put("Slave_SQL_Running", rs.getInt("Slave_SQL_Running"));
            map.put("Seconds_Behind_Master",
                        rs.getInt("Seconds_Behind_Master"));
        }
        rs.close();

        return map;
    }

}
