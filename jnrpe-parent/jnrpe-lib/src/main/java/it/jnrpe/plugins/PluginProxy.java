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

import java.text.ParseException;
import java.util.Collection;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;

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
    private Group m_MainOptionsGroup = null;

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

        GroupBuilder gBuilder = new GroupBuilder();
        
        for (PluginOption po : pluginDef.getOptions())
        {
            gBuilder = gBuilder.withOption(po.toOption());
        }
        
        m_MainOptionsGroup = gBuilder.create();
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
        //CommandLineParser clp = new PosixParser();
        try
        {
            HelpFormatter hf = new HelpFormatter();

            // configure a parser
            Parser p = new Parser();
            p.setGroup(m_MainOptionsGroup);
            p.setHelpFormatter(hf);
            CommandLine cl = p.parse(args);
            if (getListeners() != null
                    && m_plugin instanceof IPluginInterfaceEx)
            {
                ((IPluginInterfaceEx) m_plugin).addListeners(getListeners());
            }

            Thread.currentThread().setContextClassLoader(
                    m_plugin.getClass().getClassLoader());

            return m_plugin.execute(new PluginCommandLine(cl));
        }
        catch (OptionException e)
        {
            // m_Logger.error("ERROR PARSING PLUGIN ARGUMENTS", e);

            return new ReturnValue(Status.UNKNOWN, e.getMessage());
        }
    }

    /**
     * Prints the help related to the plugin (standard output).
     */
    public void printHelp()
    {
        String sDivider = "================================================================================";
        System.out.println (sDivider);
        System.out.println ("PLUGIN NAME : " + m_pluginDef.getName());
        if (m_sDescription != null && m_sDescription.trim().length() != 0)
        {
            System.out.println (sDivider);
            System.out.println("Description : ");
            System.out.println();
            System.out.println(m_sDescription);
        }
        HelpFormatter hf = new HelpFormatter();
        hf.setGroup(m_MainOptionsGroup);
        //hf.setHeader(m_pluginDef.getName());
        hf.setDivider(sDivider);
        hf.print();
        //hf.printHelp(m_pluginDef.getName(), m_Options);
    }

    /**
     * Not used.
     * 
     * @param cl
     *            Not used
     * @return null.
     */
    public ReturnValue execute(final ICommandLine cl)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
