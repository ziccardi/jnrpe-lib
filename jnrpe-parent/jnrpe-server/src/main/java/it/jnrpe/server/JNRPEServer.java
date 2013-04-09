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

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.option.DefaultOption;
import org.apache.commons.cli2.util.HelpFormatter;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class JNRPEServer
{
    private static final String VERSION = JNRPEServer.class.getPackage()
            .getImplementationVersion();

    // private static Options m_Options = null;

    private JNRPEServer()
    {
    }

    private static Group configureCommandLine()
    {
        DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        ArgumentBuilder aBuilder = new ArgumentBuilder();
        GroupBuilder gBuilder = new GroupBuilder();

        DefaultOption listOption = oBuilder.withLongName("list").withShortName(
                "l").withDescription("Lists all the installed plugins")
                .create();

        DefaultOption versionOption = oBuilder.withLongName("version")
                .withShortName("v").withDescription(
                        "Print the server version number").create();

        DefaultOption helpOption = oBuilder.withLongName("help").withShortName(
                "h").withDescription("Show this help").create();

        DefaultOption pluginNameOption = oBuilder.withLongName("plugin")
                .withShortName("p").withDescription("The plugin name")
                .withArgument(
                        aBuilder.withName("name").withMinimum(1).withMaximum(1)
                                .create()).create();

        DefaultOption pluginHelpOption = oBuilder.withLongName("help")
                .withShortName("h")
                .withDescription("Shows help about a plugin").withArgument(
                        aBuilder.withName("name").withMinimum(1).withMaximum(1)
                                .create()).create();

        Group alternativeOptions = gBuilder.withOption(listOption).withOption(
                pluginHelpOption).create();

        DefaultOption confOption = oBuilder.withLongName("conf").withShortName(
                "c").withDescription("Specifies the JNRPE configuration file")
                .withArgument(
                        aBuilder.withName("path").withMinimum(1).withMaximum(1)
                                .create()).withChildren(alternativeOptions)
                .create();

        Group mainGroup = gBuilder.withOption(versionOption).withOption(
                helpOption).withOption(confOption).withMinimum(1).create();

        return mainGroup;
    }

    private static CommandLine parseCommandLine(String[] vsArgs)
    {
        // Digester d = DigesterLoader.createDigester(new
        // InputSource(JNRPEServer.class.getResourceAsStream("command-line-digester.xml")));

        try
        {
            // XMLOptions opts= (XMLOptions)
            // d.parse(JNRPEServer.class.getResourceAsStream("jnrpe-command-line.xml"));
            // m_Options = opts.toOptions();

            Group opts = configureCommandLine();
            // configure a HelpFormatter
            HelpFormatter hf = new HelpFormatter();

            // configure a parser
            Parser p = new Parser();
            p.setGroup(opts);
            p.setHelpFormatter(hf);
            // p.setHelpTrigger("--help");
            CommandLine cl = p.parseAndHelp(vsArgs);

            return cl;
            // CommandLineParser clp = new PosixParser();
            // return clp.parse(m_Options, vsArgs);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            // Should never happen...
        }
        return null;
    }

    private static void printHelp(PluginRepository pr, String sPluginName)
    {
        try
        {
            PluginProxy pp = (PluginProxy) pr.getPlugin(sPluginName);

            // CPluginProxy pp =
            // CPluginFactory.getInstance().getPlugin(sPluginName);
            if (pp == null)
                System.out.println("Plugin " + sPluginName
                        + " does not exists.");
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
        System.out.println("JNRPE version " + VERSION);
        System.out.println("Copyright (c) 2011 Massimiliano Ziccardi");
        System.out.println("Licensed under the Apache License, Version 2.0");
        System.out.println();
    }

    private static void printUsage(Exception e)
    {
        printVersion();
        if (e != null) System.out.println(e.getMessage() + "\n");

        HelpFormatter hf = new HelpFormatter();

        // DISPLAY SETTING
        hf.getDisplaySettings().clear();
        hf.getDisplaySettings().add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        hf.getDisplaySettings().add(DisplaySetting.DISPLAY_PARENT_CHILDREN);

        // USAGE SETTING

        hf.getFullUsageSettings().clear();
        hf.getFullUsageSettings().add(DisplaySetting.DISPLAY_PARENT_ARGUMENT);
        hf.getFullUsageSettings()
                .add(DisplaySetting.DISPLAY_ARGUMENT_BRACKETED);
        hf.getFullUsageSettings().add(DisplaySetting.DISPLAY_PARENT_CHILDREN);
        hf.getFullUsageSettings().add(DisplaySetting.DISPLAY_GROUP_EXPANDED);

        // hf.getLineUsageSettings().clear();
        // hf.getLineUsageSettings().add(DisplaySetting.DISPLAY_ARGUMENT_NUMBERED);
        // hf.getFullUsageSettings().add(DisplaySetting.DISPLAY_GROUP_NAME);
        // hf.getFullUsageSettings().add(DisplaySetting.DISPLAY_ALIASES);
        // hf.getFullUsageSettings().add(DisplaySetting.DISPLAY_PARENT_ARGUMENT);

        // hf.getFullUsageSettings().add(DisplaySetting.DISPLAY);
        hf.setDivider("=================================================================");

        hf.setGroup(configureCommandLine());
        hf.print();
        // HelpFormatter hf = new HelpFormatter();
        // hf.printHelp("JNRPE.jar", m_Options);
        System.exit(0);
    }

    private static JNRPEConfiguration loadConfiguration(
            String sConfigurationFilePath) throws ConfigurationException
    {
        File confFile = new File(sConfigurationFilePath);

        if (!confFile.exists() || !confFile.canRead())
        {
            throw new ConfigurationException("Cannot access config file : "
                    + sConfigurationFilePath);
        }

        return JNRPEConfigurationFactory
                .createConfiguration(sConfigurationFilePath);

    }

    private static PluginRepository loadPluginDefinitions(String sPluginDirPath)
    {
        File fDir = new File(sPluginDirPath);
        DynaPluginRepository repo = new DynaPluginRepository();
        repo.load(fDir);

        return repo;
    }

    private static void printPluginList(PluginRepository pr)
    {
        System.out.println("List of installed plugins : ");

        for (PluginDefinition pd : pr.getAllPlugins())
        {
            System.out.println("  * " + pd.getName());
        }

        System.exit(0);
    }

    public static void main(String[] args) throws Exception
    {
        CommandLine cl = parseCommandLine(args);
        if (cl.hasOption("--help")) 
        {
            if (!cl.hasOption("--conf"))
                printUsage(null);
        }

        if (cl.hasOption("--version")) printVersion();

        JNRPEConfiguration conf = loadConfiguration((String) cl
                .getValue("--conf"));

        String sPluginPath = conf.getServerSection().getPluginPath();
        if (sPluginPath == null)
        {
            System.out.println("Plugin path has not been specified");
            System.exit(-1);
        }
        File fPluginPath = new File(sPluginPath);

        if (fPluginPath.exists())
        {
            if (!fPluginPath.isDirectory())
            {
                System.out.println("Specified plugin path ('" + sPluginPath
                        + "') must be a directory");
                System.exit(-1);
            }
        }
        else
        {
            System.out.println("Specified plugin path ('" + sPluginPath
                    + "') do not exist");
            System.exit(-1);
        }

        PluginRepository pr = loadPluginDefinitions(conf.getServerSection()
                .getPluginPath());
        // CJNRPEConfiguration.init(cl.getOptionValue("conf"));

        if (cl.hasOption("--help") && cl.getValue("--help") != null)
            printHelp(pr, (String) cl.getValue("--help"));

        if (cl.hasOption("--list")) printPluginList(pr);

        JNRPE jnrpe = new JNRPE(pr, conf.createCommandRepository());
        jnrpe.addEventListener(new EventLoggerListener());

        for (String sAcceptedAddress : conf.getServerSection()
                .getAllowedAddresses())
            jnrpe.addAcceptedHost(sAcceptedAddress);

        for (BindAddress bindAddress : conf.getServerSection()
                .getBindAddresses())
        {
            int iPort = 5666;
            String[] vsParts = bindAddress.getBindingAddress().split(":");
            String sIp = vsParts[0];
            if (vsParts.length > 1) iPort = Integer.parseInt(vsParts[1]);

            jnrpe.listen(sIp, iPort, bindAddress.isSSL());
        }
    }
}
