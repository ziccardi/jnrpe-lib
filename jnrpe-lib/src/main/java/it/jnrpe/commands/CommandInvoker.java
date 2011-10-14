/*
 * Copyright (c) 2008 Massimiliano Ziccardi
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
package it.jnrpe.commands;

import it.jnrpe.ReturnValue;
import it.jnrpe.net.IJNRPEConstants;
import it.jnrpe.plugins.PluginProxy;
import it.jnrpe.plugins.PluginRepository;

/**
 * This class is used to invoke a command.
 * 
 * @author Massimiliano Ziccardi
 *
 */
public class CommandInvoker
{
    private final boolean m_bAcceptParams;
    private final PluginRepository m_pluginRepository;
    private final CommandRepository m_commandRepository;
    
    
	public CommandInvoker(PluginRepository pluginRepository, CommandRepository commandRepository)
	{
		m_bAcceptParams = true;
		m_pluginRepository = pluginRepository;
		m_commandRepository = commandRepository;
	}

	/**
	 * This method executes built in commands or builds a CommandDefinition 
	 * to execute external commands (plugins).
	 * The methods also expands the $ARG?$ macros
	 * 
	 * @param sCommandName The name of the command, as configured in the 
	 * server configuration XML
	 * @param args The arguments to pass to the command as configured in the
	 * server configuration XML (with the $ARG?$ macros) 
	 * @return The result of the command
	 */
	public ReturnValue invoke(String sCommandName, String[] args)
	{
		if (sCommandName.equals("_NRPE_CHECK"))
		{
			return new ReturnValue(IJNRPEConstants.STATE_OK, "JNRPE v" + IJNRPEConstants.VERSION);
		}
		
		//CommandDefinition cd = (CommandDefinition) CJNRPEConfiguration.getInstance().getCommandDefinitions().get(sCommandName);
		CommandDefinition cd = m_commandRepository.getCommand(sCommandName);
		
		if (cd == null)
		{
			return new ReturnValue(IJNRPEConstants.STATE_UNKNOWN, "Bad command");
		}
		
		return invoke(cd, args);
	}
	
	/**
	 * This method executes external commands (plugins)
	 * The methods also expands the $ARG?$ macros 
	 * 
	 * @param cd The command definition
	 * @param args The arguments to pass to the command as configured in the
	 * server configuration XML (with the $ARG?$ macros) 
	 * @return The result of the command
	 */
	public ReturnValue invoke(CommandDefinition cd, String[] args)
	{
		String sPluginName = cd.getPluginName();
		
		String[] sCommandLine = cd.getCommandLine();
		
		if (m_bAcceptParams)
            for (int j = 0; sCommandLine != null && j < sCommandLine.length; j++)
    			for (int i = 0; i < args.length; i++)
    			{
                    //sCommandLine[j] = CStringUtil.replaceAll(sCommandLine[j], "$ARG" + (i + 1) + "$", args[i]);
    			    sCommandLine[j] =  sCommandLine[j].replaceAll("\\$[Aa][Rr][Gg]" + (i + 1) + "\\$", args[i]);
    			}
		
		PluginProxy plugin;
//		try
//		{
			plugin = (PluginProxy) m_pluginRepository.getPlugin(sPluginName);
//		    plugin = CPluginFactory.getInstance().getPlugin(sPluginName);
//		}
//		catch (PluginInstantiationException e)
//		{
//			m_Logger.error(e.getMessage(), e);
//			
//			return new CReturnValue(IJNRPEConstants.STATE_UNKNOWN, "Configuration error");
//		}
		
		if (plugin == null)
		{
			return new ReturnValue(IJNRPEConstants.STATE_UNKNOWN, "Configuration error");
		}
		
		try
		{
			if (sCommandLine != null)
			{
			    return plugin.execute(sCommandLine);
			}
			else
				return plugin.execute(new String[0]);
		}
		catch (RuntimeException re)
		{
		    return new ReturnValue(IJNRPEConstants.STATE_UNKNOWN, "Plugin execution error: " + re.getMessage());
		}
		catch (Throwable thr)
		{
			return new ReturnValue(IJNRPEConstants.STATE_UNKNOWN, "Plugin execution error: " + thr.getMessage());
		}
	}
	
}
