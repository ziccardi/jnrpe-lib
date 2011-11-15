/*
 * Copyright (c) 2011 Massimiliano Ziccardi
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
package it.jnrpe;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import it.jnrpe.commands.CommandInvoker;
import it.jnrpe.commands.CommandRepository;
import it.jnrpe.plugins.PluginRepository;

/**
 * This class is the real JNRPE worker.
 * It must be used to start listening for NRPE requests
 * 
 * @author Massimiliano Ziccardi
 */
public class JNRPE
{
    private final PluginRepository m_pluginRepository;
    private final CommandRepository m_commandRepository;
    
    private List<String> m_vAcceptedHosts = new ArrayList<String>();
    
    /**
     * Initializes the JNRPE worker
     * 
     * @param pluginRepository The repository containing all the installed plugins
     * @param commandRepository The repository containing all the configured commands.
     */
    public JNRPE(final PluginRepository pluginRepository, final CommandRepository commandRepository)
    {
        m_pluginRepository = pluginRepository;
        m_commandRepository = commandRepository;
        
        //m_threadFactory = new ThreadFactory(20000, new CommandInvoker(pluginRepository, commandRepository));
    }
    
    public JNRPEListenerThread listen(final String sAddress, final int iPort)
    {
        return listen(sAddress, iPort, true);
    }
    
    /**
     * Starts a new thread that listen for requests.
     * The method is <b>not blocking</b>
     * @param sAddress The address to bind to
     * @param iPort The listening port
     * @return Returns the newly created thread.
     */
    public JNRPEListenerThread listen(final String sAddress, final int iPort, final boolean bSSL)
    {
        JNRPEListenerThread bt = new JNRPEListenerThread(sAddress, iPort, new CommandInvoker(m_pluginRepository, m_commandRepository));
        try
        {
            for (String sAddr : m_vAcceptedHosts)
                bt.addAcceptedHosts(sAddr);
        }
        catch (UnknownHostException e)
        {
            // FIXME : must be handled!!
            e.printStackTrace();
        }
        if (bSSL)
            bt.enableSSL();
        bt.start();
        return bt;
    }
    
    /**
     * Adds an address to the list of accepted hosts
     * @param sAddress The address to accept
     */
    public void addAcceptedHost(String sAddress)
    {
        m_vAcceptedHosts.add(sAddress);
    }
}
