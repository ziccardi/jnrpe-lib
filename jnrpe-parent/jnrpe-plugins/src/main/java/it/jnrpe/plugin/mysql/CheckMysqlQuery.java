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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Plugin that checks a mysql query result against threshold levels.
 *
 * @author Frederico Campos
 */
public class CheckMysqlQuery extends PluginBase {

    /**
     * Executes the plugin.
     *
     * @param cl
     *            The parsified command line arguments
     * @return The result of the plugin
     * @throws BadThresholdException
     *             -
     */
    public final ReturnValue execute(final ICommandLine cl)
            throws BadThresholdException {
        Mysql mysql = new Mysql(cl);
        Connection conn = null;
        try {
            conn = mysql.getConnection();
        } catch (ClassNotFoundException e) {
            log.error("Mysql driver library not found into the classpath"
                    + ": download and put it in the same directory "
                    + "of this plugin");
            return new ReturnValue(
                    Status.CRITICAL,
                    "CHECK_MYSQL_QUERY - CRITICAL: Error accessing the "
                    + "MySQL server - JDBC driver not installed");
        } catch (Exception e) {
            log.error("Error accessing the MySQL server", e);
            return new ReturnValue(Status.CRITICAL,
                    "CHECK_MYSQL_QUERY - CRITICAL: Error accessing "
                    + "the MySQL server - " + e.getMessage());
        }

        String query = cl.getOptionValue("query");
        String critical = cl.getOptionValue("critical");

        String warning = cl.getOptionValue("warning");
        Statement st = null;
        ResultSet set = null;
        try {
            st = conn.createStatement();
            st.execute(query);
            set = st.getResultSet();
            BigDecimal value = null;
            if (set.first()) {
                value = set.getBigDecimal(1);

                // if (value.longValue() == 0){
                // mysql.closeConnection(conn);
                // return new ReturnValue(Status.CRITICAL,
                // "MYSQL - WARNING: Query returned no rows.");
                // }
                if (critical != null
                        && ThresholdUtil.isValueInRange(critical, value)) {
                    mysql.closeConnection(conn);
                    return new ReturnValue(Status.CRITICAL,
                            "MYSQL - CRITICAL: Returned value is "
                                    + value.longValue()).withPerformanceData(
                            "rows", value.longValue(), UnitOfMeasure.counter,
                            warning, critical, 0L, null);
                }

                if (warning != null
                        && ThresholdUtil.isValueInRange(warning, value)) {
                    mysql.closeConnection(conn);
                    return new ReturnValue(Status.CRITICAL,
                            "MYSQL - WARNING: Returned value is "
                                    + value.longValue()).withPerformanceData(
                            "rows", value.longValue(), UnitOfMeasure.counter,
                            warning, critical, 0L, null);
                }

                mysql.closeConnection(conn);
                return new ReturnValue(Status.OK,
                        "CHECK_MYSQL_QUERY - OK - Returned value is "
                                + value.longValue()).withPerformanceData(
                        "rows", value.longValue(), UnitOfMeasure.counter,
                        warning, critical, 0L, null);
            } else {
                return new ReturnValue(Status.UNKNOWN, "Query " + query
                        + " returned no rows");
            }
        } catch (SQLException e) {
            log.warn("Error executing plugin CheckMysqlQuery : "
                            + e.getMessage(), e);
            return new ReturnValue(Status.CRITICAL,
                    "CHECK_MYSQL_QUERY - CRITICAL: " + e.getMessage());
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException e) {
                    log.error("Error closing MySQL statement", e);
                }
            }
            if (set != null) {
                try {
                    set.close();
                } catch (SQLException e) {
                    log.error("Error closing MySQL ResultSet", e);
                }
            }
            mysql.closeConnection(conn);
        }
    }

}
