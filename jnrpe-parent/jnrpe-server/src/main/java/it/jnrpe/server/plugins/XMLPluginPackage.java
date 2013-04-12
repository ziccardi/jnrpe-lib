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
package it.jnrpe.server.plugins;

import it.jnrpe.server.plugins.xml.PluginType;
import it.jnrpe.server.plugins.xml.PluginsType;
import it.jnrpe.server.plugins.xml.XMLPluginParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This is just an utility class to contain plugin configuration data
 * 
 * @author Massimiliano Ziccardi
 *
 */
public class XMLPluginPackage
{
	private List<XMLPluginDefinition> m_vPluginDefinitions = new ArrayList<XMLPluginDefinition>();
	
	private XMLPluginPackage(PluginsType pt)
	{
		init(pt);
	}
	
	private void init(PluginsType pt)
    {
        // TODO Auto-generated method stub
	    for (PluginType plugin : pt.getPlugin())
        {
            m_vPluginDefinitions.add(new XMLPluginDefinition(plugin));
        }
    }

    private void addPluginDefinition(XMLPluginDefinition def)
	{
		m_vPluginDefinitions.add(def);
	}

	public List<XMLPluginDefinition> getPluginDefinitions()
	{
		return m_vPluginDefinitions;
	}
	
	public static XMLPluginPackage getInstance(InputStream in)
	{
	    try
        {
            PluginsType pt = XMLPluginParser.parseDocument(in);
            
            return new XMLPluginPackage(pt);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	    return null;
	}
}
