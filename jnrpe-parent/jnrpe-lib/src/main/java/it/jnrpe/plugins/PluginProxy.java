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
import it.jnrpe.ReturnValue;
import it.jnrpe.Status;

import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * This class was intended to abstract the kind of plugin to execute. Hides
 * command line parsing from command invoker.
 *
 * @author Massimiliano Ziccardi
 *
 */
public final class PluginProxy extends PluginBase
{
    /**
     * The plugin instance proxied by this object.
     */
    private IPluginInterface m_plugin = null;

    /**
     * The plugin definition of the plugin proxied by this object.
     */
    private PluginDefinition m_pluginDef;

    /**
     * The command line definition as requested by the Apache commons cli.
     * library.
     */
    private Options m_Options = new Options();

    /**
     * The proxied plugin description.
     */
    private final String m_sDescription;

    /**
     * Instantiate a new plugin proxy.
     *
     * @param plugin
     *            The plugin to be proxied
     * @param pluginDef
     *            The plugin definition of the plugin to be proxied
     */
    public PluginProxy(final IPluginInterface plugin,
            final PluginDefinition pluginDef)
    {
        m_plugin = plugin;
        m_pluginDef = pluginDef;
        m_sDescription = m_pluginDef.getDescription();

        for (PluginOption po : pluginDef.getOptions())
        {
            m_Options.addOption(po.toOption());
        }
    }

    /**
     * Returns a collection of all the options accepted by this plugin.
     *
     * @return a collection of plugin options.
     */
    public Collection<PluginOption> getOptions()
    {
        return m_pluginDef.getOptions();
    }

    /**
     * Executes the proxied plugin passing the received arguments as parameters.
     *
     * @param args
     *            The parameters to be passed to the plugin
     * @return The return value of the plugin.
     */
    public ReturnValue execute(final String[] args)
    {
        CommandLineParser clp = new PosixParser();
        try
        {
            CommandLine cl = clp.parse(m_Options, args);
            if (getListeners() != null
                    && m_plugin instanceof IPluginInterfaceEx)
            {
                ((IPluginInterfaceEx) m_plugin).addListeners(getListeners());
            }
            return m_plugin.execute(new PluginCommandLine(cl));
        }
        catch (ParseException e)
        {
            // m_Logger.error("ERROR PARSING PLUGIN ARGUMENTS", e);

            return new ReturnValue(Status.UNKNOWN,
                    e.getMessage());
        }
    }

    /**
     * Prints the help related to the plugin (standard output).
     */
    public void printHelp()
    {
        if (m_sDescription != null && m_sDescription.trim().length() != 0)
        {
            System.out.println("Description : ");
            System.out.println(m_sDescription);
        }
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp(m_pluginDef.getName(), m_Options);
    }

    /**
     * Not used.
     * @param cl Not used
     * @return null.
     */
    public ReturnValue execute(final ICommandLine cl)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
