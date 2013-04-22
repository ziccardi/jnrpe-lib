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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Helper class to connect to a mysql database
 * 
 * @author Frederico Campos
 *
 */
public class Mysql {
	/*
	 * Helper class to connect to a Msysql database 
	 * 
	 * Author: Frederico Campos
	 */
	
	/**
	 * Db hostname
	 */
	private String hostname;
	
	/**
	 * Db port number
	 */
	private String port;
	
	/**
	 * Db username
	 */
	private String username;
	
	/*
	 * Db password
	 */
	private String password;
	
	/**
	 * Db name
	 */
	private String database;
	
	public Mysql(ICommandLine cl){
		this.database = "mysql";
		if (cl.hasOption("database")){
			this.database = cl.getOptionValue("database");
		}
		this.hostname = "localhost";
		if (cl.hasOption("hostname") && !"".equals(cl.getOptionValue("hostname"))) {
			this.hostname = cl.getOptionValue("hostname");
			System.out.println("hostname = " + hostname);
		}
		this.port = "3306";
		if (cl.hasOption("port")  && !"".equals(cl.getOptionValue("port"))) {
			this.port = cl.getOptionValue("port");
			System.out.println("port = " + port);
		}
		this.password = "";
		if (cl.hasOption("password")) {
			this.password = cl.getOptionValue("password");
		}else{
			// find password from my.cfg or my.ini
		}
		this.username = "";
		if (cl.hasOption("user")){
			this.username = cl.getOptionValue("user"); 
		}
	}
	
	public Mysql(String hostname, String port, String username, String password, String database) {
		this.hostname = hostname;
		this.port = port;
		this.username = username;
		this.password = password;
		this.database = database;
	}

	/**
	 * Get database connection
	 * @param cl
	 * @return
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * Connection
	 */
	public Connection getConnection() throws SQLException, 
	InstantiationException, 
	IllegalAccessException,
	ClassNotFoundException	{
		String url = "jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database + 
				"?user="  + this.username  +"&password=" + this.password + 
				"&autoReconnect=true&failOverReadOnly=false&maxReconnects=3";  
		DriverManager.registerDriver((Driver) Class.forName("com.mysql.jdbc.Driver").newInstance());		
		Connection conn = DriverManager.getConnection(url);
		return conn;
	}
	
	/**
	 * 
	 * @param conn
	 * void
	 */
	public void closeConnection(Connection conn){
		try {
			if (conn != null){
				conn.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			
		}
	}
	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * @param hostname the hostname to set
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * @return the port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the database
	 */
	public String getDatabase() {
		return database;
	}

	/**
	 * @param database the database to set
	 */
	public void setDatabase(String database) {
		this.database = database;
	}
	
	
}
