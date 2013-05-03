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
 * @author Frederico Campos
 *
 */
public class CheckPgsql extends PluginBase {
	
	/**
	 * default settings
	 */
	private final String DEFAULT_HOSTNAME = "localhost";
	private final String DEFAULT_PORT = "5432";
	private final String DEFAULT_TABLE = "template1";
	private final String DEFAULT_TIMEOUT = "10";
	
	
	/* Run the plugin
	 * 
	 * @see it.jnrpe.plugins.IPluginInterface#execute(it.jnrpe.ICommandLine)
	 */
	public ReturnValue execute(ICommandLine cl) throws BadThresholdException {
		
		String warning = cl.getOptionValue("warning");
		String critical = cl.getOptionValue("critical");
		Connection conn = null;
		Long start = System.currentTimeMillis();
		String error = "";
		try {
			conn = getConnection(cl);
		} catch (InstantiationException e) {
			error = e.getMessage();
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			error = e.getMessage();
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			error = e.getMessage();
			sendEvent(LogEvent.ERROR, "Pgsql driver library not found into the classpath: download and put it in the same directory of this plugin");
			e.printStackTrace();
		}catch(SQLException e){
			sendEvent(LogEvent.ERROR, "Error communicating with database.", e);
			error = e.getMessage();
			e.printStackTrace();
		}

		if (conn == null){
			return new ReturnValue(Status.CRITICAL, "CHECK_PGSQL - CRITICAL: No database connection - " + error);
		}
		Long end = System.currentTimeMillis();
		Long ellapsed = new Long((end  - start) / 1000);
		Status status = null;

		if (warning != null && (ThresholdUtil.isValueInRange(warning, ellapsed))){
			status = Status.WARNING;
		}
		if (critical != null && ThresholdUtil.isValueInRange(critical, ellapsed)){
			status = Status.CRITICAL;
		}		
		closeConnection(conn);
		if (status == null){
			status = Status.OK;
		}
		String database = DEFAULT_TABLE;
		if (cl.hasOption("database")){
			database = cl.getOptionValue("database");
		}
		return new ReturnValue(status, "Database " + database + " " + ellapsed + " secs.").
				withPerformanceData("secs", ellapsed, ReturnValue.UnitOfMeasure.seconds, warning, critical, null, null);
	}
	
	private Connection getConnection(ICommandLine cl) throws SQLException, 
	InstantiationException, 
	IllegalAccessException,
	ClassNotFoundException	{
		String database = DEFAULT_TABLE;
		if (cl.hasOption("database")){
			database = cl.getOptionValue("database");
		}
		String hostname = DEFAULT_HOSTNAME;
		if (cl.hasOption("hostname") && !"".equals(cl.getOptionValue("hostname"))) {
			hostname = cl.getOptionValue("hostname");
			System.out.println("hostname = " + hostname);
		}
		String port = DEFAULT_PORT;
		if (cl.hasOption("port")  && !"".equals(cl.getOptionValue("port"))) {
			port = cl.getOptionValue("port");			
		}
		String password = "";
		if (cl.hasOption("password")) {
			password = cl.getOptionValue("password");
		}
		String username = "";
		if (cl.hasOption("logname")){
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
		//props.setProperty("loglevel","2");
		String url = "jdbc:postgresql://" + hostname + ":" + port + "/" + database;
		System.out.println(url);
		DriverManager.registerDriver((Driver) Class.forName("org.postgresql.Driver").newInstance());
		Connection conn = DriverManager.getConnection(url, props);
		return conn;
		
		/*
		 printf (_(" %s - database %s (%d sec.)|%s\n"),
	        state_text(status), dbName, elapsed_time,
	        fperfdata("time", elapsed_time, "s",
	                 (int)twarn, twarn, (int)tcrit, tcrit, TRUE, 0, FALSE,0));
	return status;
		 */
	}
	
	private void closeConnection(Connection conn){
		try {
			if (conn != null){
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

}
