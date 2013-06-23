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
import it.jnrpe.Status;
import it.jnrpe.plugins.Metric;
import it.jnrpe.plugins.MetricGatheringException;
import it.jnrpe.plugins.PluginBase;
import it.jnrpe.utils.BadThresholdException;
import it.jnrpe.utils.thresholds.ThresholdsEvaluatorBuilder;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This plugin tests connections to a MySql server.
 *
 * @author Frederico Campos
 */
public class CheckMysql extends PluginBase {

	@Override
	public void configureThresholdEvaluatorBuilder(
			ThresholdsEvaluatorBuilder thrb, ICommandLine cl)
					throws BadThresholdException {
		if (cl.hasOption("th")) {
			super.configureThresholdEvaluatorBuilder(thrb, cl);
		} else {
			thrb.withLegacyThreshold("time", null, cl.getOptionValue("warning"), cl.getOptionValue("critical")).
					withLegacyThreshold("secondsBehindMaster", null, cl.getOptionValue("warning"), cl.getOptionValue("critical"));
		}
	}

	/**
	 * Execute and gather metrics
	 */
	public Collection<Metric> gatherMetrics(ICommandLine cl) throws MetricGatheringException {
		List<Metric> metrics = new ArrayList<Metric>();
        Mysql mysql = new Mysql(cl);
        long start = System.currentTimeMillis();
        long elapsed = 0L;
        Connection conn = null;
        try {
            conn = mysql.getConnection();
            elapsed = (System.currentTimeMillis() - start) / 1000L;
        } catch (ClassNotFoundException e) {
        	e.printStackTrace();
            log.error("Mysql driver library not found into the classpath: "
                            + "download and put it in the same directory "
                            + "of this plugin");
            throw new MetricGatheringException("CHECK_MYSQL_QUERY - CRITICAL: Error accessing "
                    + "the MySQL server - JDBC driver not installed", Status.CRITICAL, e);
        } catch (Exception e) {
        	e.printStackTrace();
            log.error("Error accessing the MySQL server", e);
            throw new MetricGatheringException("CHECK_MYSQL_QUERY - CRITICAL: Error accessing "
                    + "the MySQL server - " + e.getMessage(),
                    Status.CRITICAL,
                    e);
        }

        if (cl.hasOption("check-slave")) {
            metrics.add(checkSlave(cl, mysql, conn));
        }else{
        	metrics.add(new Metric("time", "Connection took " + elapsed + " secs. ", new BigDecimal(elapsed), new BigDecimal(0), null));
        }
        mysql.closeConnection(conn);
        return metrics;
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
    private Metric checkSlave(final ICommandLine cl, final Mysql mysql, final Connection conn) throws MetricGatheringException {
    	Metric metric = null;
        try {
            Map<String, Integer> status = getSlaveStatus(conn);
            if (status.isEmpty()) {
                mysql.closeConnection(conn);
                throw new MetricGatheringException( "CHECK_MYSQL - WARNING: No slaves defined. ", Status.CRITICAL, null);
            }

            // check if slave is running
            int slaveIoRunning = status.get("Slave_IO_Running");
            int slaveSqlRunning = status.get("Slave_SQL_Running");
            int secondsBehindMaster = status.get("Seconds_Behind_Master");

            if (slaveIoRunning == 0 || slaveSqlRunning == 0) {
                mysql.closeConnection(conn);
                throw new MetricGatheringException( "CHECK_MYSQL - CRITICAL: Slave status unavailable. ", Status.CRITICAL, null);                
            }
            String slaveResult =
                    "Slave IO: " + slaveIoRunning + " Slave SQL: "
                            + slaveSqlRunning + " Seconds Behind Master: "
                            + secondsBehindMaster;

            metric = new Metric("secondsBehindMaster", slaveResult, new BigDecimal(secondsBehindMaster), null, null);
          } catch (SQLException e) {
            log.warn("Error executing the CheckMysql plugin: " + e.getMessage(), e);
            throw new MetricGatheringException("CHECK_MYSQL - CRITICAL: Unable to check slave status:  - " + e.getMessage(), 
            		Status.CRITICAL, e);
        }
        
        return metric;

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

    @Override
    protected String getPluginName() {
        return "CHECK_MYSQL";
    }
}
