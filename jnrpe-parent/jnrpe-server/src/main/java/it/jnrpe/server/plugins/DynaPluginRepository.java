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

import it.jnrpe.plugins.PluginDefinition;
import it.jnrpe.plugins.PluginRepository;
import it.jnrpe.server.plugins.xml.XMLPluginOption;
import it.jnrpe.server.xml.XMLOption;
import it.jnrpe.server.xml.XMLOptions;
import it.jnrpe.utils.StreamManager;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DynaPluginRepository extends PluginRepository
{
    
    private void configurePlugins(File fDir)
    {
//        m_Logger.trace("READING PLUGIN CONFIGURATION FROM DIRECTORY " + fDir.getName());
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
        //URL[] urls = new URL[vfJars.length];
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

        //ul = URLClassLoader.newInstance(urls, getClass().getClassLoader());
        //ul = new JNRPEClassLoader(getClass().getClassLoader(), URLClassLoader.newInstance(urls, null), false);
        
        ul = new JNRPEClassLoader(vUrls);
    
        for (int i = 0; i < vfJars.length; i++)
        {
            File file = vfJars[i];
            
            
            try
            {
//                m_Logger.info("READING PLUGINS DATA IN FILE '" + file.getName() + "'");
                
                ZipInputStream jin = (ZipInputStream) streamMgr.handle(new ZipInputStream(new FileInputStream(file)));
                ZipEntry ze = null;

                while ((ze = jin.getNextEntry()) != null)
                {
                    if (ze.getName().equals("plugin.xml"))
                    {
                        XMLPluginPackage xmlPluginPackage = parsePluginXmlFile(jin, ul);
                        
                        for (XMLPluginDefinition pluginDefs : xmlPluginPackage.getPluginDefinitions())
                        {
                            // load the plugin class
                            Class c = ul.loadClass(pluginDefs.getPluginClass());
                            PluginDefinition pd = new PluginDefinition(pluginDefs.getName(), pluginDefs.getDescription(), c);
                            loadOpts(pd, pluginDefs);
                            
                            
                            addPluginDefinition(pd);
                        }
                        

                        
                        break;
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
//                m_Logger.error("UNABLE TO READ DATA FROM FILE '"
//                        + file.getName() + "'. THE FILE WILL BE IGNORED.", e);
            }
            finally
            {
                streamMgr.closeAll();
            }

        }
    }
    
    private void loadOpts(PluginDefinition pd, XMLPluginDefinition pluginDefs)
    {
        XMLOptions opts = pluginDefs.getOptions();
        if (opts != null)
        {
            Collection<XMLOption> vOpts = opts.getOptions();
            if (vOpts != null)
            {
                for (XMLOption xmlOpt : vOpts)
                {
                    pd.addOption(
                            ((XMLPluginOption)xmlOpt).toPluginOption()
                    );
                }
            }
        }

        
    }

    private XMLPluginPackage parsePluginXmlFile(InputStream in, ClassLoader cl) throws IOException, SAXException
    {
        StreamManager streamMgr = new StreamManager();
        
//        m_Logger.trace("PARSING FILE plugin.xml IN JAR FILE.");

        Digester digester = DigesterLoader
                .createDigester(new InputSource(
                 streamMgr.handle(
                 DynaPluginRepository.class.getResourceAsStream("plugin-digester.xml"))));
        XMLPluginPackage oConf = (XMLPluginPackage) digester.parse(in);

        return oConf;
        
//        List vPluginDefs = oConf.getPluginDefinitions();
//
//        //for (CPluginDefinition pluginDef : vPluginDefs)
//        for (Iterator iter = vPluginDefs.iterator(); iter.hasNext(); )
//        {
//            XMLPluginDefinition pluginDef = (XMLPluginDefinition) iter.next();
////            m_Logger.debug("FOUND PLUGIN "
////                    + pluginDef.getName()
////                    + " IMPLEMENTED BY CLASS "
////                    + pluginDef.getPluginClass());
//            m_mPlugins.put(pluginDef.getName(), new CPluginData(pluginDef.getName(), pluginDef.getDescription(), pluginDef.getPluginClass(), cl, pluginDef.getOptions()));
//        }
    }    
    
    public void load(File fDirectory)
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
