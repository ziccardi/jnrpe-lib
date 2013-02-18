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

import it.jnrpe.JNRPE;
import it.jnrpe.plugins.PluginDefinition;
import it.jnrpe.plugins.PluginProxy;
import it.jnrpe.plugins.PluginRepository;
import it.jnrpe.server.plugins.DynaPluginRepository;
import it.jnrpe.server.xml.XMLOptions;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class JNRPEServer
{
    private static final String VERSION = JNRPEServer.class.getPackage().getImplementationVersion();
    private static Options m_Options = null;
    
    private JNRPEServer()
    {
	}
    
    private static CommandLine parseCommandLine(String[] vsArgs)
    {
        Digester d = DigesterLoader.createDigester(new InputSource(JNRPEServer.class.getResourceAsStream("command-line-digester.xml")));
        
        try
        {
            XMLOptions opts= (XMLOptions) d.parse(JNRPEServer.class.getResourceAsStream("jnrpe-command-line.xml"));
            m_Options = opts.toOptions();
            CommandLineParser clp = new PosixParser();
            return clp.parse(m_Options, vsArgs);
        }
        catch (IOException e)
        {
            // Should never happen...           
        }
        catch (SAXException e)
        {
            // Should never happen...           
            e.printStackTrace();
        }
        catch (ParseException e)
        {
            printUsage(e);
        }
        return null;
    }
    
    private static void printHelp(PluginRepository pr, String sPluginName)
    {
        try
        {
            PluginProxy pp = (PluginProxy) pr.getPlugin(sPluginName);
            
//            CPluginProxy pp = CPluginFactory.getInstance().getPlugin(sPluginName);
            if (pp == null)
                System.out.println ("Plugin " + sPluginName + " does not exists.");
            else
            {
                pp.printHelp();
            }
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.exit(0);
    }
    
    private static void printVersion()
    {
        // TODO: this should be handled by ant...
        System.out.println ("JNRPE version " + VERSION);
        System.out.println ("Copyright (c) 2011 Massimiliano Ziccardi");
        System.out.println ("Licensed under the Apache License, Version 2.0");
        System.out.println ();
    }
    
    private static void printUsage(Exception e)
    {
        printVersion();
        if (e != null)
            System.out.println (e.getMessage() + "\n");
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp("JNRPE.jar", m_Options);
        System.exit(0);
    }
    
    private static JNRPEConfiguration loadConfiguration(String sConfigurationFilePath) throws ConfigurationException
    {
        File confFile = new File(sConfigurationFilePath);
        
        if (!confFile.exists() || !confFile.canRead())
        {
            throw new ConfigurationException("Cannot access config file : " + sConfigurationFilePath);
        }
        
        return JNRPEConfigurationFactory.createConfiguration(sConfigurationFilePath);
        
    }

    private static PluginRepository loadPluginDefinitions(String sPluginDirPath)
    {
        File fDir = new File(sPluginDirPath);
        DynaPluginRepository repo =  new DynaPluginRepository();
        repo.load(fDir);
        
        return repo;
    }

    private static void printPluginList(PluginRepository pr)
    {
        System.out.println ("List of installed plugins : ");

        for (PluginDefinition pd : pr.getAllPlugins())
        {
            System.out.println ("  * " + pd.getName());
        }
        
        System.exit(0);
    }
    
    
    public static void main(String[] args) throws Exception
    {
        CommandLine cl = parseCommandLine(args);
        if (cl.hasOption("help") && cl.getOptionValue("help") == null)
            printUsage(null);

        if (cl.hasOption("version"))
            printVersion();
        
        JNRPEConfiguration conf = loadConfiguration(cl.getOptionValue("conf"));
        
        String sPluginPath = conf.getServerSection().getPluginPath();
        if (sPluginPath == null)
        {
            System.out.println ("Plugin path has not been specified");
            System.exit(-1);
        }
        File fPluginPath = new File(sPluginPath);
        
        if (fPluginPath.exists())
        {
            if (!fPluginPath.isDirectory())
            {
                System.out.println ("Specified plugin path ('" + sPluginPath + "') must be a directory");
                System.exit(-1);
            }
        }
        else
        {
            System.out.println ("Specified plugin path ('" + sPluginPath + "') do not exist");
            System.exit(-1);
        }
        
        PluginRepository pr = loadPluginDefinitions(conf.getServerSection().getPluginPath());
        //CJNRPEConfiguration.init(cl.getOptionValue("conf"));

        if (cl.hasOption("help") && cl.getOptionValue("help") != null)
            printHelp(pr, cl.getOptionValue("help"));

        if (cl.hasOption("list"))
            printPluginList(pr);

        JNRPE jnrpe = new JNRPE(pr, conf.createCommandRepository());
        jnrpe.addEventListener(new EventLoggerListener());
        
        for (String sAcceptedAddress : conf.getServerSection().getAllowedAddresses())
            jnrpe.addAcceptedHost(sAcceptedAddress);
                
        for (BindAddress bindAddress : conf.getServerSection().getBindAddresses())
        {
            int iPort = 5666;
            String[] vsParts = bindAddress.getBindingAddress().split(":");
            String sIp = vsParts[0];
            if (vsParts.length > 1)
                iPort = Integer.parseInt(vsParts[1]);
            
            jnrpe.listen(sIp, iPort, bindAddress.isSSL());
        }
    }
}
