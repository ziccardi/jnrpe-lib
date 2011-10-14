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
package it.jnrpe.plugins;

import it.jnrpe.ICommandLine;
import it.jnrpe.ReturnValue;
import it.jnrpe.net.IJNRPEConstants;

import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * This class was intended to abstract the kind of plugin to execute.
 * Hides command line parsing from command invoker.
 * 
 * @author Massimiliano Ziccardi
 *
 */
public class PluginProxy implements IPluginInterface
{
	private IPluginInterface m_plugin = null;
	private PluginDefinition m_pluginDef;
	
	private Options m_Options = new Options();
	private final String m_sDescription;
	
	public PluginProxy(IPluginInterface plugin, PluginDefinition pluginDef)
	{
		m_plugin = plugin;
		m_pluginDef = pluginDef;
		m_sDescription = m_pluginDef.getDescription();
		
		for(PluginOption po : pluginDef.getOptions())
		    m_Options.addOption(po.toOption());
	}
	
	/**
     * Returns a collection of all the options accepted by this plugin 
     * @return
	 */
    public Collection<PluginOption> getOptions()
    {
        return m_pluginDef.getOptions();
    }
    
    public ReturnValue execute(String[] args)
	{
		CommandLineParser clp = new PosixParser();
		try
		{
			CommandLine cl = clp.parse(m_Options, args);
			return m_plugin.execute(new PluginCommandLine(cl));
		}
		catch (ParseException e)
		{
			//m_Logger.error("ERROR PARSING PLUGIN ARGUMENTS", e);
			
			return new ReturnValue(IJNRPEConstants.STATE_UNKNOWN, e.getMessage());
		}
	}

	public void printHelp()
	{
	    if (m_sDescription != null && m_sDescription.trim().length() != 0)
	    {
	        System.out.println ("Description : ");
	        System.out.println (m_sDescription);
	    }
		HelpFormatter hf = new HelpFormatter();
		hf.printHelp(m_pluginDef.getName(), m_Options);
	}

    @Override
    public ReturnValue execute(ICommandLine cl)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
