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
 * This plugin tests connections to a MySql server
 * @author Frederico Campos
 *
 */
public class CheckMysql extends PluginBase {

	/**
	 * Executes the check
	 * 
	 * @param cl the command line
	 * @return the check return code
	 */
	public ReturnValue execute(ICommandLine cl) throws BadThresholdException
	{
		System.out.println("check_mysql");
		Mysql mysql = new Mysql(cl);
		Connection conn = null;
		String error = "";
		try {
			conn = mysql.getConnection();
		} catch (InstantiationException e) {
			error = e.getMessage();
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			error = e.getMessage();
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			error = e.getMessage();
			sendEvent(LogEvent.ERROR, "Mysql driver library not found into the classpath: download and put it in the same directory of this plugin");
			e.printStackTrace();
		}catch(SQLException e){
			sendEvent(LogEvent.ERROR, "Error communicating with database.", e);
			error = e.getMessage();
			e.printStackTrace();
		}

		
		if (conn == null){
			mysql.closeConnection(conn);
			return new ReturnValue(Status.CRITICAL, "CHECK_MYSQL - CRITICAL: No database connection - " + error);
		}
		
		//if (cl.getOptionValue("checkslave") != null || cl.getOptionValue("-S") != null){
		if (cl.hasOption("checkslave")) {
			return checkSlave(cl, mysql, conn);			
		}
		
		mysql.closeConnection(conn);
		return new ReturnValue(Status.OK, "CHECK_MYSQL - OK");
	}
	
	/**
	 * Check the status of mysql slave thread
	 * 
	 * @param cl
	 * @param mysql
	 * @param conn
	 * @return
	 * ReturnValue
	 * @throws BadThresholdException 
	 */
	private ReturnValue checkSlave(ICommandLine cl, Mysql mysql, Connection conn) throws BadThresholdException{
		System.out.println("checking slave");
		try {
			Map<String, Integer> status = getSlaveStatus(conn);
			if (status.isEmpty()){
				mysql.closeConnection(conn);
				return new ReturnValue(Status.CRITICAL, "CHECK_MYSQL - WARNING: No slaves defined. ");
			}
			
			// check if slave is running	
			int slaveIoRunning = status.get("Slave_IO_Running");
			int slaveSqlRunning = status.get("Slave_SQL_Running");
			int secondsBehindMaster = status.get("Seconds_Behind_Master");
			
			if (slaveIoRunning == 0 || slaveSqlRunning == 0){
				mysql.closeConnection(conn);
				return new ReturnValue(Status.CRITICAL, "CHECK_MYSQL - CRITICAL: Slave status unavailable. ");
			}
			
			String slaveResult = "Slave IO: " + slaveIoRunning + " Slave SQL: " + slaveSqlRunning + " Seconds Behind Master: " + secondsBehindMaster;
			
			if (cl.hasOption("critical")){
				String critical = cl.getOptionValue("critical");
				if (ThresholdUtil.isValueInRange(critical, secondsBehindMaster)){
					return new ReturnValue(Status.CRITICAL, "CHECK_MYSQL - CRITICAL: Slow slave - " + slaveResult);
				}
			}
			
			if (cl.hasOption("warning")){
				String warning = cl.getOptionValue("warning");
				if (ThresholdUtil.isValueInRange(warning, secondsBehindMaster)){
					mysql.closeConnection(conn);
					return new ReturnValue(Status.WARNING, "CHECK_MYSQL - WARNING: Slow slave - " + slaveResult);
				}
			}
			
			return new ReturnValue(Status.OK, "CHECK_MYSQL - OK: " + slaveResult);
		} catch (SQLException e) {
			e.printStackTrace();
			return new ReturnValue(Status.CRITICAL, "CHECK_MYSQL - CRITICAL: Unable to check slave status:  - " + e.getMessage());
		}
		
		
	}

	
	/*
	 * Get slave statuses
	 */
	private Map<String, Integer> getSlaveStatus(Connection conn) throws SQLException {
		Map<String, Integer> map = new HashMap<String, Integer>();
		String query = "show slave status;";
		Statement statement;
		ResultSet rs = null;
		statement = conn.createStatement();
		rs = statement.executeQuery(query);
		while (rs.next()) {
			map.put("Slave_IO_Running", rs.getInt("Slave_IO_Running"));
			map.put("Slave_SQL_Running", rs.getInt("Slave_SQL_Running"));
			map.put("Seconds_Behind_Master", rs.getInt("Seconds_Behind_Master"));			
		}
		rs.close();
		
		return map;
	}

}
