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
package it.jnrpe.server;

import java.util.ArrayList;
import java.util.List;

public class ServerSection
{
    private boolean m_bAcceptParams;
    private String m_sPluginPath;

    private List<BindAddress> m_vBindings = new ArrayList<BindAddress>();
    private List<String> m_vAllowedAddresses = new ArrayList<String>();
    
    public List<BindAddress> getBindAddresses()
    {
        return m_vBindings;
    }
    
    void addBindAddress(final String sBindAddress)
    {

        boolean ssl = sBindAddress.toUpperCase().startsWith("SSL/");

        String sAddress;
        
        if (ssl)
        {
            sAddress = sBindAddress.substring("SSL/".length());
        }
        else
        {
            sAddress = sBindAddress;
        }
        
//        if (sBindAddress.indexOf(',') != -1)
//        {
//            String[] vParts = sBindAddress.split(",");
//            sAddress = vParts[0];
//            sSSL = vParts[1].toLowerCase();
//        }
        addBindAddress(sAddress, ssl);
    }

    void addBindAddress(String sBindAddress, boolean bSSL)
    {
        m_vBindings.add(new BindAddress(sBindAddress, bSSL));
    }
    
    public List<String> getAllowedAddresses()
    {
        return m_vAllowedAddresses;
    }
    
    void addAllowedAddress(String sAddress)
    {
        m_vAllowedAddresses.add(sAddress);
    }
    
    public boolean acceptParams()
    {
        return m_bAcceptParams;
    }
    
    void setAcceptParams(boolean bAcceptParams)
    {
        this.m_bAcceptParams = bAcceptParams;
    }
    
    public String getPluginPath()
    {
        return m_sPluginPath;
    }
    
    void setPluginPath(String sPluginPath)
    {
        this.m_sPluginPath = sPluginPath;
    }
}
