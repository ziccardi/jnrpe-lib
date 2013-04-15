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
package it.jnrpe.commands;

import it.jnrpe.ReturnValue;
import it.jnrpe.Status;
import it.jnrpe.JNRPELIB;
import it.jnrpe.events.EventsUtil;
import it.jnrpe.events.IJNRPEEventListener;
import it.jnrpe.events.LogEvent;
import it.jnrpe.plugins.PluginProxy;
import it.jnrpe.plugins.PluginRepository;

import java.util.Set;

/**
 * This class is used to invoke a command.
 *
 * @author Massimiliano Ziccardi
 *
 */
public final class CommandInvoker
{
    /**
     * <code>true</code> if the variable parameters ($ARG?$) must be
     * interpolated.
     */
    private final boolean m_bAcceptParams;

    /**
     * The plugin repository to be used to find the plugins.
     */
    private final PluginRepository m_pluginRepository;

    /**
     * The command repository to be used to find the commands.
     */
    private final CommandRepository m_commandRepository;

    /**
     * The listeners.
     */
    private final Set<IJNRPEEventListener> m_vListeners;

    /**
     * Builds and initializes the {@link CommandInvoker} object.
     *
     * @param pluginRepository
     *            The plugin repository containing all the plugins that must be
     *            used by this invoker.
     * @param commandRepository
     *            The command repository containing all the commands that must
     *            be used by this invoker.
     * @param vListeners
     *            All the listeners
     */
    public CommandInvoker(final PluginRepository pluginRepository,
            final CommandRepository commandRepository,
            final Set<IJNRPEEventListener> vListeners)
    {
        m_bAcceptParams = true;
        m_pluginRepository = pluginRepository;
        m_commandRepository = commandRepository;
        m_vListeners = vListeners;
    }

    /**
     * This method executes built in commands or builds a CommandDefinition to.
     * execute external commands (plugins). The methods also expands the $ARG?$
     * macros.
     *
     * @param sCommandName
     *            The name of the command, as configured in the server
     *            configuration XML
     * @param args
     *            The arguments to pass to the command as configured in the
     *            server configuration XML (with the $ARG?$ macros)
     * @return The result of the command
     */
    public ReturnValue invoke(final String sCommandName, final String[] args)
    {
        if (sCommandName.equals("_NRPE_CHECK"))
        {
            return new ReturnValue(Status.OK, JNRPELIB.VERSION); 
        }

        CommandDefinition cd = m_commandRepository.getCommand(sCommandName);

        if (cd == null)
        {
            return new ReturnValue(Status.UNKNOWN, "Bad command");
        }

        return invoke(cd, args);
    }

    /**
     * This method executes external commands (plugins) The methods also expands
     * the $ARG?$ macros.
     *
     * @param cd
     *            The command definition
     * @param args
     *            The arguments to pass to the command as configured in the
     *            server configuration XML (with the $ARG?$ macros)
     * @return The result of the command
     */
    public ReturnValue invoke(final CommandDefinition cd, final String[] args)
    {
        String sPluginName = cd.getPluginName();

        String[] sCommandLine = cd.getCommandLine();

        if (m_bAcceptParams)
        {
            for (int j = 0; sCommandLine != null
                        && j < sCommandLine.length; j++)
            {
                for (int i = 0; i < args.length; i++)
                {
                    // sCommandLine[j] = CStringUtil.replaceAll(sCommandLine[j],
                    // "$ARG" + (i + 1) + "$", args[i]);
                    sCommandLine[j] = sCommandLine[j].replaceAll(
                            "\\$[Aa][Rr][Gg]" + (i + 1) + "\\$", args[i]);
                    if (sCommandLine[j].indexOf(' ') != -1)
                    {
                        if (sCommandLine[j].indexOf('\'') == -1)
                        {
                            sCommandLine[j] = "'" + sCommandLine[j] + "'";
                        }
                        else if (sCommandLine[j].indexOf('"') == -1)
                        {
                            sCommandLine[j] = "\"" + sCommandLine[j] + "\"";
                        }
                    }
                }
            }
        }

        PluginProxy plugin = (PluginProxy) m_pluginRepository
                .getPlugin(sPluginName);

        if (plugin == null)
        {
            EventsUtil.sendEvent(m_vListeners, this, LogEvent.INFO,
                    "Unable to instantiate plugin named " + sPluginName);
            // TODO : it would be better to give some information about the
            // error... (bad plugin name??)
            return new ReturnValue(Status.UNKNOWN,
                    "Error instantiating plugin '" + sPluginName + "' : bad plugin name?");
        }

        plugin.addListeners(m_vListeners);

        try
        {
            if (sCommandLine != null)
            {
                return plugin.execute(sCommandLine);
            }
            else
            {
                return plugin.execute(new String[0]);
            }
        }
        catch (RuntimeException re)
        {
            return new ReturnValue(Status.UNKNOWN,
                    "Plugin execution error: " + re.getMessage());
        }
        catch (Throwable thr)
        {
            return new ReturnValue(Status.UNKNOWN,
                    "Plugin execution error: " + thr.getMessage());
        }
    }
}
