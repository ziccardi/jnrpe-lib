/*
 * Copyright (c) 2008 Massimiliano Ziccardi Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package it.jnrpe.plugins;

import it.jnrpe.ICommandLine;

import org.apache.commons.cli.CommandLine;

/**
 * Incapsulate the commons cli CommandLine object, so that the plugins have no
 * dependencies against the command line parsing library
 *
 * @author Massimiliano Ziccardi
 *
 */
class PluginCommandLine implements ICommandLine
{
    /**
     * The Apache Commons Cli {@link CommandLine} object.
     */
    private CommandLine m_CommandLine = null;

    /**
     * Incapsulate the given command line.
     *
     * @param cl
     *            The command line to be incapsulated
     */
    public PluginCommandLine(final CommandLine cl)
    {
        m_CommandLine = cl;
    }

    /**
     * @see it.jnrpe.core.ICommandLine#getOptionValue(java.lang.String)
     */
    public String getOptionValue(final String sOptionName)
    {
        return m_CommandLine.getOptionValue(sOptionName);
    }

    /**
     * @see it.jnrpe.core.ICommandLine#getOptionValue(java.lang.String,
     *      java.lang.String)
     */
    public String getOptionValue(final String sOptionName,
            final String sDefaultValue)
    {
        return m_CommandLine.getOptionValue(sOptionName, sDefaultValue);
    }

    /**
     * @see it.jnrpe.core.ICommandLine#getOptionValue(char)
     */
    public String getOptionValue(final char cOption)
    {
        return m_CommandLine.getOptionValue(cOption);
    }

    /**
     * @see it.jnrpe.core.ICommandLine#getOptionValue(char, java.lang.String)
     */
    public String getOptionValue(final char cOption, final String sDefaultValue)
    {
        return m_CommandLine.getOptionValue(cOption, sDefaultValue);
    }

    /**
     * @see it.jnrpe.core.ICommandLine#hasOption(java.lang.String)
     */
    public boolean hasOption(final String sOptionName)
    {
        return m_CommandLine.hasOption(sOptionName);
    }

    /**
     * @see it.jnrpe.core.ICommandLine#hasOption(char)
     */
    public boolean hasOption(final char cOption)
    {
        return m_CommandLine.hasOption(cOption);
    }
}
