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

import org.apache.commons.cli.CommandLine;


/**
 * Incapsulate the command line object, so that the plugins have
 * no dependencies against the command line library
 * 
 * @author Massimiliano Ziccardi
 *
 */
class PluginCommandLine implements ICommandLine
{
    private CommandLine m_CommandLine = null;
    
    public PluginCommandLine(CommandLine cl)
    {
        m_CommandLine = cl;
    }
    
    /* (non-Javadoc)
     * @see it.jnrpe.core.ICommandLine#getOptionValue(java.lang.String)
     */
    @Override
    public String getOptionValue(String sOptionName)
    {
        return m_CommandLine.getOptionValue(sOptionName);
    }

    /* (non-Javadoc)
     * @see it.jnrpe.core.ICommandLine#getOptionValue(java.lang.String, java.lang.String)
     */
    @Override
    public String getOptionValue(String sOptionName, String sDefaultValue)
    {
        return m_CommandLine.getOptionValue(sOptionName, sDefaultValue);
    }

    /* (non-Javadoc)
     * @see it.jnrpe.core.ICommandLine#getOptionValue(char)
     */
    @Override
    public String getOptionValue(char cOption)
    {
        return m_CommandLine.getOptionValue(cOption);
    }

    /* (non-Javadoc)
     * @see it.jnrpe.core.ICommandLine#getOptionValue(char, java.lang.String)
     */
    @Override
    public String getOptionValue(char cOption, String sDefaultValue)
    {
        return m_CommandLine.getOptionValue(cOption, sDefaultValue);
    }
    
    /* (non-Javadoc)
     * @see it.jnrpe.core.ICommandLine#hasOption(java.lang.String)
     */
    @Override
    public boolean hasOption(String sOptionName)
    {
        return m_CommandLine.hasOption(sOptionName);
    }

    /* (non-Javadoc)
     * @see it.jnrpe.core.ICommandLine#hasOption(char)
     */
    @Override
    public boolean hasOption(char cOption)
    {
        return m_CommandLine.hasOption(cOption);
    }
}
