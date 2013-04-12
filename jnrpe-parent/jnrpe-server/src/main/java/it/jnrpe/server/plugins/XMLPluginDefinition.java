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
package it.jnrpe.server.plugins;

import it.jnrpe.server.plugins.xml.CommandLineType;
import it.jnrpe.server.plugins.xml.OptionType;
import it.jnrpe.server.plugins.xml.PluginType;
import it.jnrpe.server.plugins.xml.XMLPluginOption;
import it.jnrpe.server.plugins.xml.XMLPluginOptions;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;

/**
 * This class contains all the data JNRPE will need to instantiate the plugin
 * 
 * @author Massimiliano Ziccardi
 */
public class XMLPluginDefinition
{
	private String m_sPluginName = null;
	private String m_sPluginClass = null;
	private XMLPluginOptions m_Options = null;
	private String m_sDecription = null;
	
	XMLPluginDefinition(PluginType plugin)
	{
		init(plugin);
	}
	
	private void init(PluginType plugin)
	{
	    m_sPluginName = plugin.getName();
	    m_sPluginClass = plugin.getClazz();
	    m_sDecription = plugin.getDescription();
	    
	    m_Options = new XMLPluginOptions();
	    CommandLineType clt = plugin.getCommandLine();
	    for (OptionType ot : clt.getOptions().getOption())
	    {
	        m_Options.addOption( new XMLPluginOption(ot));
	    }
	}
	
	void setName(String sName)
	{
		m_sPluginName = sName;
	}

	/**
	 * Returns the name of the plugin
	 */
	public String getName()
	{
		return m_sPluginName;
	}

	void setPluginClass(String sClassName)
	{
		m_sPluginClass = sClassName;
	}

	/**
	 * Returns the class name implementing the plugin
	 */
	public String getPluginClass()
	{
		return m_sPluginClass;
	}
	
	void setOptions(XMLPluginOptions opts)
	{
		m_Options = opts;
	}
	
	public XMLPluginOptions getOptions()
	{
		return m_Options;
	}
	
    private String cleanDesc(String sDesc)
    {
        if (sDesc == null)
            return "";
        
        
        BufferedReader r = new BufferedReader(new StringReader(sDesc));
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintWriter w = new PrintWriter(new OutputStreamWriter(bout));
        
        String sLine = null;
        try
        {
            while ((sLine = r.readLine()) != null)
            {
                w.println(sLine.trim());
            }
            w.flush();
        }
        catch (IOException ioe)
        {
            // Ignore it. Should never happen
        }
        
        return new String (bout.toByteArray());
    }
	
	void setDescription(String sDesc)
    {
        m_sDecription = cleanDesc(sDesc);
    }

    public String getDescription()
    {
        return m_sDecription;
    }
}
