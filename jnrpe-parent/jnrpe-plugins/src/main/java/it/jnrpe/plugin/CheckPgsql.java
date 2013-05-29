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
 *
 */
package it.jnrpe.plugin;

import it.jnrpe.ICommandLine;
import it.jnrpe.ReturnValue;
import it.jnrpe.Status;
import it.jnrpe.events.LogEvent;
import it.jnrpe.plugins.PluginBase;
import it.jnrpe.utils.BadThresholdException;
import it.jnrpe.utils.ThresholdUtil;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Tests connections to a PostgreSQL Database.
 *
 * @author Frederico Campos
 */
public class CheckPgsql extends PluginBase {

    /*
     * default settings
     */

    /**
     * Default hostname.
     */
    private static final String DEFAULT_HOSTNAME = "localhost";

    /**
     * Default server port.
     */
    private static final String DEFAULT_PORT = "5432";

    /**
     * Default server table.
     */
    private static final String DEFAULT_TABLE = "template1";

    /**
     * Default timeout.
     */
    private static final String DEFAULT_TIMEOUT = "10";

    /**
     * Executes the plugin.
     *
     * @param cl
     *            The command line
     * @return the Return value
     * @throws BadThresholdException
     *             -
     */
    public final ReturnValue execute(final ICommandLine cl)
            throws BadThresholdException {

        String warning = cl.getOptionValue("warning");
        String critical = cl.getOptionValue("critical");
        Connection conn = null;
        Long start = System.currentTimeMillis();

        try {
            conn = getConnection(cl);
            // elapsed = (System.currentTimeMillis() - start) / 1000l;
        } catch (ClassNotFoundException e) {
            log.error("PostgreSQL driver library not found into the classpath: "
                            + "download and put it in the same directory of "
                            + "this plugin");
            return new ReturnValue(
                    Status.CRITICAL,
                    "CHECK_PGSQL - CRITICAL: Error accessing the PostgreSQL "
                            + "server - JDBC driver not installed");
        } catch (Exception e) {
            log.error("Error accessing the PostgreSQL server", e);
            return new ReturnValue(Status.CRITICAL,
                    "CHECK_PGSQL - CRITICAL: Error accessing the PostgreSQL "
                            + "server - " + e.getMessage());
        }

        // if (conn == null){
        // return new ReturnValue(Status.CRITICAL,
        // "CHECK_PGSQL - CRITICAL: No database connection - " + error);
        // }

        Long end = System.currentTimeMillis();
        Long elapsed = new Long((end - start) / 1000);
        Status status = null;

        if (critical != null
                && ThresholdUtil.isValueInRange(critical, elapsed)) {
            status = Status.CRITICAL;
        }

        if (warning != null
                && (ThresholdUtil.isValueInRange(warning, elapsed))) {
            status = Status.WARNING;
        }

        closeConnection(conn);
        if (status == null) {
            status = Status.OK;
        }
        String database = DEFAULT_TABLE;
        if (cl.hasOption("database")) {
            database = cl.getOptionValue("database");
        }
        return new ReturnValue(status, "Database " + database + " " + elapsed
                + " secs.").withPerformanceData("time", elapsed,
                ReturnValue.UnitOfMeasure.seconds, warning, critical, 0L, null);
    }

    /**
     * Connect to the server.
     *
     * @param cl
     *            The command line
     * @return The connection
     * @throws SQLException
     *             -
     * @throws InstantiationException
     *             -
     * @throws IllegalAccessException
     *             -
     * @throws ClassNotFoundException
     *             -
     */
    private Connection getConnection(final ICommandLine cl) throws SQLException,
            InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        String database = DEFAULT_TABLE;
        if (cl.hasOption("database")) {
            database = cl.getOptionValue("database");
        }
        String hostname = DEFAULT_HOSTNAME;
        if (cl.hasOption("hostname")
                && !"".equals(cl.getOptionValue("hostname"))) {
            hostname = cl.getOptionValue("hostname");
        }
        String port = DEFAULT_PORT;
        if (cl.hasOption("port") && !"".equals(cl.getOptionValue("port"))) {
            port = cl.getOptionValue("port");
        }
        String password = "";
        if (cl.hasOption("password")) {
            password = cl.getOptionValue("password");
        }
        String username = "";
        if (cl.hasOption("logname")) {
            username = cl.getOptionValue("logname");
        }
        String timeout = DEFAULT_TIMEOUT;
        if (cl.getOptionValue("timeout") != null) {
            timeout = cl.getOptionValue("timeout");
        }
        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);
        props.setProperty("timeout", timeout);
        // props.setProperty("loglevel","2");
        String url =
                "jdbc:postgresql://" + hostname + ":" + port + "/" + database;
        DriverManager.registerDriver((Driver) Class.forName(
                "org.postgresql.Driver").newInstance());
        Connection conn = DriverManager.getConnection(url, props);
        return conn;

        /*
         * printf (_(" %s - database %s (%d sec.)|%s\n"), state_text(status),
         * dbName, elapsed_time, fperfdata("time", elapsed_time, "s",
         * (int)twarn, twarn, (int)tcrit, tcrit, TRUE, 0, FALSE,0)); return
         * status;
         */
    }

    /**
     * Closes the connection.
     * @param conn The connectiont o be closed
     */
    private void closeConnection(final Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getPluginName() {
        return "CHECK_PGSQL";
    }
}
