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
 * Plugin that checks a mysql query result against threshold levels
 * 
 * @author Frederico Campos
 *
 */
public class CheckMysqlQuery extends PluginBase {

	/* (non-Javadoc)
	 * @see it.jnrpe.plugins.IPluginInterface#execute(it.jnrpe.ICommandLine)
	 */

	public ReturnValue execute(ICommandLine cl) {
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
			return new ReturnValue(Status.CRITICAL, "CHECK_MYSQL_QUERY - CRITICAL: No database connection - " + error);
		}

		String query = cl.getOptionValue("query");
		if (query.startsWith("'") || query.startsWith("\"")){
			query = query.substring(1);
		}
		if (query.endsWith("'") || query.endsWith("\"")){
			query = query.substring(0, query.length() -1);
		}
		System.out.println(query);
		String critical = cl.getOptionValue("critical");
		
		String warning = cl.getOptionValue("warning");
		Statement st = null;
		ResultSet set = null;
		try {
			st = conn.createStatement();
			st.execute(query);
			set = st.getResultSet();
			BigDecimal value = null;
			if (set.first()){
				value = set.getBigDecimal(1);
				System.out.println(value);
				if (value.longValue() == 0){
					mysql.closeConnection(conn);
					return new ReturnValue(Status.CRITICAL, "MYSQL - WARNING: Query returned no rows.");
				}
				try {
					if (critical != null && ThresholdUtil.isValueInRange(critical, value)){
						mysql.closeConnection(conn);
						return new ReturnValue(Status.CRITICAL, "MYSQL - CRITICAL: Query result is " + value.longValue() + " rows.")
						.withPerformanceData("rows", value.longValue(), UnitOfMeasure.counter, warning, critical, 0l, null);
					}

					if (warning != null && ThresholdUtil.isValueInRange(warning, value)){
						mysql.closeConnection(conn);
						return new ReturnValue(Status.CRITICAL, "MYSQL - WARNING: Query result is " + value.longValue() + " rows.")
						.withPerformanceData("rows", value.longValue(), UnitOfMeasure.counter, warning, critical, 0l, null);
					}
					
					mysql.closeConnection(conn);
					return new ReturnValue(Status.OK, "CHECK_MYSQL_QUERY - OK - Result " + value.longValue());
					
				} catch (BadThresholdException e) {
					return new ReturnValue(Status.CRITICAL, "CHECK_MYSQL_QUERY - CRITICAL: " + e.getMessage());
				}
				

			}
		} catch (SQLException e) {
			e.printStackTrace();
			return new ReturnValue(Status.CRITICAL, "CHECK_MYSQL_QUERY - CRITICAL: " + e.getMessage());
		}finally{
			if (st != null){
				try {
					st.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (set != null){
				try {
					set.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}

		mysql.closeConnection(conn);
		return new ReturnValue(Status.OK, "CHECK_MYSQL_QUERY - OK");
	}

}

