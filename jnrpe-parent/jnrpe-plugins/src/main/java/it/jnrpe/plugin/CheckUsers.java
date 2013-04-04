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
package it.jnrpe.plugin;

import it.jnrpe.ICommandLine;
import it.jnrpe.ReturnValue;
import it.jnrpe.Status;
import it.jnrpe.plugins.IPluginInterface;
import it.jnrpe.utils.ThresholdUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This plugin checks the number of users currently logged in on the local
 * system and generates a critical or an error status according to the
 * passed-in thresholds.
 *
 * @author Frederico Campos
 *
 */
public class CheckUsers implements IPluginInterface {

	/* (non-Javadoc)System.getProperty("os.name")
	 * @see it.jnrpe.plugins.IPluginInterface#execute(it.jnrpe.ICommandLine)
	 */
	public ReturnValue execute(ICommandLine cl) {
		String warning = cl.getOptionValue("warning");
        String critical = cl.getOptionValue("critical");
		
        int totalLoggedIn = -1;
        
        String os = System.getProperty("os.name").toLowerCase();
        try {	
	        if (os.contains("linux")){
	        	totalLoggedIn = getLinuxLoggedInUsers();	        	
	        }else if (os.contains("windows")){
	        	totalLoggedIn = getWindowsLoggedInUsers();
	        }
        }catch(IOException e){
        	e.printStackTrace();
        	// return some error?
        }
        
        if (ThresholdUtil.isValueInRange(critical, totalLoggedIn)){
        	return new ReturnValue(Status.CRITICAL, "CHECK_USER - CRITICAL: " + totalLoggedIn + " users currently logged in");
        }
        
        if (ThresholdUtil.isValueInRange(warning, totalLoggedIn)){
        	return new ReturnValue(Status.WARNING, "CHECK_USER - WARNING: " + totalLoggedIn + " users currently logged in");
        }
        
        return new ReturnValue(Status.OK, "CHECK_USER - OK: " + totalLoggedIn + " users currently logged in");
	}

	/**
	 * Get list of logged in users for linux
	 * @return
	 * @throws IOException
	 */
	private int getLinuxLoggedInUsers() throws IOException {
		String command = "/usr/bin/users";
		List<String> users = new ArrayList<String>();
		ProcessBuilder builder = new ProcessBuilder();
		Process proc = null;
		proc = builder.command(command).start();
		InputStream stdin = proc.getInputStream();
        InputStreamReader isr = new InputStreamReader(stdin);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        while ((line = br.readLine()) != null) {
            users.add(line);
        }
		return users.size();
	}
	
	/**
	 * Get list of logged in users for windows by counting the number of explorer.exe processes
	 * 
	 * @return
	 */
	private int getWindowsLoggedInUsers() throws IOException {
		String command = System.getenv("windir") +"\\system32\\"+"tasklist.exe";
		int userCount = 0;
		ProcessBuilder builder = new ProcessBuilder();
		Process proc = null;
		proc = builder.command(command).start();
		InputStream stdin = proc.getInputStream();
        InputStreamReader isr = new InputStreamReader(stdin);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        while ((line = br.readLine()) != null) {
            if (line.contains("explorer.exe")){
            	userCount++;
            }
        }
		return userCount;
	}

}
