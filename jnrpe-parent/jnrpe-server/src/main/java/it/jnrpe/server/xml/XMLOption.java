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
package it.jnrpe.server.xml;

import it.jnrpe.server.plugins.xml.OptionType;

import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;

/**
 * This object represent the in-memory representation of
 * the XML definition of a plugin's command line.
 * 
 * @author Massimiliano Ziccardi
 */
public class XMLOption
{
//	<option opt="" hasArgs="" required="" optionalArgs="" 
//		argName="" argsCount="" longOpt="" type="" valueSeparator="" 
//		description=""/>
	
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
	
	public XMLOption(OptionType ot)
	{
		init(ot);
	}
	
	private void init(OptionType ot)
    {
        m_sOption = ot.getShortName();
        m_bHasArgs = ot.isHasArgs();
        //m_iArgsCount = ot.  //TODO : manage args count
        m_bRequired = ot.isRequired();
        m_bArgsOptional = ot.isOptionalArgs();
        m_sArgName = ot.getArgName();
        m_sLongOpt = ot.getLongName();
        // m_sType = ot.getT // TODO : manage type
        // m_sValueSeparator = ot.getV // TODO : manage value separator
        m_sDescription = ot.getDescription();
        
    }

    public String getOption()
	{
		return m_sOption;
	}
	
	public void setOption(String sOption)
	{
		m_sOption = sOption;
	}
	
	public boolean hasArgs()
	{
		return m_bHasArgs;
	}
	
	public void setHasArgs(String sHasArgs)
	{
		m_bHasArgs = sHasArgs.equals("true");
	}
	
	public Integer getArgsCount()
	{
		return m_iArgsCount;
	}
	
	public void setArgsCount(String sArgsCount)
	{
		m_iArgsCount = new Integer(sArgsCount);
	}
	
	public String getRequired()
	{
		return "" + m_bRequired;
	}
	
	public void setRequired(String sRequired)
	{
		m_bRequired = sRequired.equals("true");
	}
	
	public Boolean getArgsOptional()
	{
		return m_bArgsOptional;
	}
	
	public void setArgsOptional(String sArgsOptional)
	{
		m_bArgsOptional = new Boolean(sArgsOptional.equals("true"));
	}
	
	public String getArgName()
	{
		return m_sArgName;
	}
	
	public void setArgName(String sArgName)
	{
		m_sArgName = sArgName;
	}
	
	public String getLongOpt()
	{
		return m_sLongOpt;
	}
	
	public void setLongOpt(String sLongOpt)
	{
		m_sLongOpt = sLongOpt;
	}
	
	public String getType()
	{
		return m_sType;
	}
	
	public void setType(String sType)
	{
		m_sType = sType;
	}
	
	public String getValueSeparator()
	{
		return m_sValueSeparator;
	}
	
	public void setValueSeparator(String sValueSeparator)
	{
		m_sValueSeparator = sValueSeparator;
	}
	
	public String getDescription()
	{
		return m_sDescription;
	}

	public void setDescription(String sDescription)
	{
		m_sDescription = sDescription;
	}
	
	Option toOption()
	{
DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        
        oBuilder = oBuilder
          .withShortName(m_sOption)
          .withDescription(m_sDescription)
          .withRequired(m_bRequired)
          ;
        
        if (m_sLongOpt != null)
            oBuilder.withLongName(m_sLongOpt);
        
        //        DefaultOption ret = oBuilder
//                                .withLongName(m_sOption)
//                                .withDescription(m_sDescription);
        
        //Option ret = new Option(m_sOption, m_sDescription);

//        if (m_bArgsOptional != null)
//        {
//            ret.setOptionalArg(m_bArgsOptional.booleanValue());
//        }

        if (m_bHasArgs)
        {
            ArgumentBuilder aBuilder = new ArgumentBuilder();
            
            if (m_sArgName != null)
                aBuilder = aBuilder.withName(m_sArgName);
            
            if (m_bArgsOptional)
                aBuilder = aBuilder.withMinimum(0);
            
            if (m_iArgsCount != null)
            {
                aBuilder.withMaximum(m_iArgsCount);
            }
            
            if (m_sValueSeparator != null && m_sValueSeparator.length() != 0)
            {
                aBuilder.withInitialSeparator(m_sValueSeparator.charAt(0));
                aBuilder.withSubsequentSeparator(m_sValueSeparator.charAt(0));
            }
            oBuilder = oBuilder.withArgument(aBuilder.create());
        }

        return oBuilder.create();
	}
}
