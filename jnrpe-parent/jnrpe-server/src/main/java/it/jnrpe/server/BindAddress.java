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

class BindAddress
{
    private final boolean m_bSSL;
    private final String m_sBindingAddress;
    
    public BindAddress (String sAddress)
    {
        m_sBindingAddress = sAddress;
        m_bSSL = true;
    }

    public BindAddress (String sAddress, boolean bSSL)
    {
        m_sBindingAddress = sAddress;
        m_bSSL = bSSL;
    }
    
    public String getBindingAddress()
    {
        return m_sBindingAddress;
    }
    
    public boolean isSSL()
    {
        return m_bSSL;
    }
}
