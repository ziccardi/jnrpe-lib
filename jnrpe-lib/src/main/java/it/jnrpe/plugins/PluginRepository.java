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
package it.jnrpe.plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represent the repository of all the installed plugins
 * 
 * @author Massimiliano Ziccardi
 *
 */
public class PluginRepository
{
    private Map<String, PluginDefinition> m_mPluginsDefs = new HashMap<String, PluginDefinition>();
    
    public void addPluginDefinition(PluginDefinition pluginDef)
    {
        m_mPluginsDefs.put(pluginDef.getName(), pluginDef);
    }
    
    public IPluginInterface getPlugin(String sName)
    {
        PluginDefinition pluginDef = m_mPluginsDefs.get(sName);
        if (pluginDef == null)
            return null;
        
        try
        {
            IPluginInterface pluginInterface = pluginDef.getPluginInterface();
            
            if (pluginInterface == null)
                pluginInterface = (IPluginInterface) pluginDef.getPluginClass().newInstance();
            return new PluginProxy(pluginInterface, pluginDef);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
    
    public Collection<PluginDefinition> getAllPlugins()
    {
        return m_mPluginsDefs.values();
    }
}
