/*
 * Copyright (c) 2011 Massimiliano Ziccardi Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package it.jnrpe;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.jnrpe.commands.CommandInvoker;
import it.jnrpe.commands.CommandRepository;
import it.jnrpe.events.IJNRPEEventListener;
import it.jnrpe.plugins.PluginRepository;

/**
 * This class is the real JNRPE worker. It must be used to start listening for
 * NRPE requests
 *
 * @author Massimiliano Ziccardi
 */
public final class JNRPE
{
    /**
     * The plugin repository to be used to find the requested plugin.
     */
    private final PluginRepository m_pluginRepository;
    /**
     * The command repository to be used to find the requested command.
     */
    private final CommandRepository m_commandRepository;

    /**
     * The list of accepted clients.
     */
    private List<String> m_vAcceptedHosts = new ArrayList<String>();

    /**
     * All the listeners.
     */
    private Map<String, IJNRPEListener> m_mInstantiatedListeners
                            = new HashMap<String, IJNRPEListener>();

    /**
     * All the listeners.
     */
    private Set<IJNRPEEventListener> m_vEventListeners
                            = new HashSet<IJNRPEEventListener>();

    /**
     * Initializes the JNRPE worker.
     *
     * @param pluginRepository
     *            The repository containing all the installed plugins
     * @param commandRepository
     *            The repository containing all the configured commands.
     */
    public JNRPE(final PluginRepository pluginRepository,
            final CommandRepository commandRepository)
    {
        m_pluginRepository = pluginRepository;
        m_commandRepository = commandRepository;
    }

    /**
     * Instructs the server to listen to the given IP/port.
     *
     * @param sAddress
     *            The address to bind to
     * @param iPort
     *            The port to bind to
     * @throws UnknownHostException
     */
    public void listen(final String sAddress, final int iPort) throws UnknownHostException
    {
        listen(sAddress, iPort, true);
    }

    /**
     * Adds a new event listener.
     *
     * @param listener The event listener to be added
     */
    public void addEventListener(final IJNRPEEventListener listener)
    {
        m_vEventListeners.add(listener);
    }

    /**
     * Starts a new thread that listen for requests. The method is <b>not
     * blocking</b>
     *
     * @param sAddress
     *            The address to bind to
     * @param iPort
     *            The listening port
     * @param bSSL
     *            <code>true</code> if an SSL socket must be created.
     * @throws UnknownHostException
     */
    public void listen(final String sAddress, final int iPort,
            final boolean bSSL) throws UnknownHostException
    {
        JNRPEListenerThread bt = new JNRPEListenerThread(m_vEventListeners,
                sAddress, iPort, new CommandInvoker(m_pluginRepository,
                        m_commandRepository, m_vEventListeners));

        for (String sAddr : m_vAcceptedHosts)
        {
            bt.addAcceptedHosts(sAddr);
        }
        if (bSSL)
        {
            bt.enableSSL();
        }
        bt.start();

        m_mInstantiatedListeners.put(sAddress + iPort, bt);
    }

    /**
     * Adds an address to the list of accepted hosts.
     *
     * @param sAddress
     *            The address to accept
     */
    public void addAcceptedHost(final String sAddress)
    {
        m_vAcceptedHosts.add(sAddress);
    }

    /**
     * Shuts down all the listener handled by this instance.
     */
    public void shutdown()
    {
        if (m_mInstantiatedListeners.isEmpty())
            return;

        for (IJNRPEListener listener : m_mInstantiatedListeners.values())
        {
            listener.shutdown();
        }
    }
}
