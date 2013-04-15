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
package it.jnrpe.server.plugins;

import it.jnrpe.plugins.PluginConfigurationException;
import it.jnrpe.plugins.PluginRepository;
import it.jnrpe.utils.PluginRepositoryUtil;
import it.jnrpe.utils.StreamManager;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynaPluginRepository extends PluginRepository
{
    private final static Logger LOG = LoggerFactory.getLogger(DynaPluginRepository.class);
    
    private void configurePlugins(File fDir) throws PluginConfigurationException
    {
        LOG.trace("READING PLUGIN CONFIGURATION FROM DIRECTORY " + fDir.getName());
        StreamManager streamMgr = new StreamManager();
        File[] vfJars = fDir.listFiles(new FileFilter()
        {

            public boolean accept(File f)
            {
                return f.getName().endsWith(".jar");
            }

        });

        if (vfJars == null || vfJars.length == 0)
            return;
        
        // Initializing classloader
        List<URL> vUrls = new ArrayList<URL>(vfJars.length);
        
        ClassLoader ul = null;
        
        for (int j = 0; j < vfJars.length; j++)
        {
            try
            {
                //urls[j] = vfJars[j].toURI().toURL();
                vUrls.add(vfJars[j].toURI().toURL());
            }
            catch (MalformedURLException e)
            {
                // should never happen
            }
        }

        ul = new JNRPEClassLoader(vUrls);
        
        try
        {
            InputStream in = streamMgr.handle(ul.getResourceAsStream("plugin.xml"));
            if (in == null)
            {
                // Error : No plugin.xml
                // TODO : must throw an exception
                return;
            }
            
            PluginRepositoryUtil.loadFromXmlPluginPackageDefinitions(this, ul, in);
        }
        finally
        {
            streamMgr.closeAll();
        }
    }
    
    public void load(File fDirectory) throws PluginConfigurationException
    {
        File[] vFiles = fDirectory.listFiles();
        if (vFiles != null)
        {
            for (File f : vFiles)
            {
                if (f.isDirectory())
                {
                    configurePlugins(f);
                }
            }
        }
    }
}
