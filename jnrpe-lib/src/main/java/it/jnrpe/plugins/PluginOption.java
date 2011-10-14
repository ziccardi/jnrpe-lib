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

import org.apache.commons.cli.Option;

/**
 * This class describes a plugin option
 *  
 * @author Massimiliano Ziccardi
 */
public class PluginOption
{
	private String m_sOption = null;
	private boolean m_bHasArgs = false;
	private Integer m_iArgsCount = null;
	private boolean m_bRequired = false;
	private Boolean m_bArgsOptional = null;
	private String m_sArgName = null;
	private String m_sLongOpt = null;
	private String m_sType = null;
	private String m_sValueSeparator = null;
	private String m_sDescription = null;
	
	public PluginOption()
	{
		
	}
	
	/**
	 * Returns the option string
	 * 
	 * @return
	 */
	public String getOption()
	{
		return m_sOption;
	}
	
	/**
	 * Sets the option string. For example, if the plugin must receive 
	 * the '--file' option, sOption will be 'file'.
	 * @param sOption
	 * @return
	 */
	public PluginOption setOption(String sOption)
	{
		m_sOption = sOption;
		return this;
	}

	/**
	 * Returns true if the option has an argument
	 * @return
	 */
	public boolean hasArgs()
	{
		return m_bHasArgs;
	}
	
	public PluginOption setHasArgs(boolean bHasArgs)
	{
		m_bHasArgs = bHasArgs;
		return this;
	}
	
	public Integer getArgsCount()
	{
		return m_iArgsCount;
	}
	
	public PluginOption setArgsCount(Integer iArgCount)
	{
		m_iArgsCount = iArgCount;
		return this;
	}
	
	public String getRequired()
	{
		return "" + m_bRequired;
	}
	
	public PluginOption setRequired(boolean bRequired)
	{
		m_bRequired = bRequired;
		return this;
	}
	
	public Boolean getArgsOptional()
	{
		return m_bArgsOptional;
	}
	
	public PluginOption setArgsOptional(Boolean bArgsOptional)
	{
		m_bArgsOptional = bArgsOptional;
		return this;
	}
	
	public String getArgName()
	{
		return m_sArgName;
	}
	
	public PluginOption setArgName(String sArgName)
	{
		m_sArgName = sArgName;
		return this;
	}
	
	public String getLongOpt()
	{
		return m_sLongOpt;
	}
	
	public PluginOption setLongOpt(String sLongOpt)
	{
		m_sLongOpt = sLongOpt;
		return this;
	}
	
	public String getType()
	{
		return m_sType;
	}
	
	public PluginOption setType(String sType)
	{
		m_sType = sType;
		return this;
	}
	
	public String getValueSeparator()
	{
		return m_sValueSeparator;
	}
	
	public PluginOption setValueSeparator(String sValueSeparator)
	{
		m_sValueSeparator = sValueSeparator;
		return this;
	}
	
	public String getDescription()
	{
		return m_sDescription;
	}

	public PluginOption setDescription(String sDescription)
	{
		m_sDescription = sDescription;
		return this;
	}
	
	Option toOption()
	{
		Option ret = new Option(m_sOption, m_sDescription);
		
		if (m_bArgsOptional != null)
			ret.setOptionalArg(m_bArgsOptional.booleanValue());
		
		if (m_bHasArgs)
		{
			if (m_iArgsCount == null)
				ret.setArgs(Option.UNLIMITED_VALUES);
		}
		
		ret.setRequired(m_bRequired);
		if (m_iArgsCount != null)
			ret.setArgs(m_iArgsCount.intValue());
		
		if (m_sArgName != null)
		{
			if (m_iArgsCount == null)
				ret.setArgs(Option.UNLIMITED_VALUES);
			ret.setArgName(m_sArgName);
		}
		
		if (m_sLongOpt != null)
			ret.setLongOpt(m_sLongOpt);
		
		if (m_sValueSeparator != null && m_sValueSeparator.length() != 0)
			ret.setValueSeparator(m_sValueSeparator.charAt(0));
		
		return ret;
	}
}
