package it.jnrpe.utils;

import it.jnrpe.plugins.PluginConfigurationException;
import it.jnrpe.plugins.PluginDefinition;
import it.jnrpe.plugins.PluginOption;
import it.jnrpe.plugins.PluginRepository;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public final class PluginRepositoryUtil
{
    public static void loadFromXmlPluginPackageDefinitions(PluginRepository repo, ClassLoader cl, InputStream in) throws PluginConfigurationException
    {
        SAXReader reader = new SAXReader();
        Document document;
        try
        {
            document = reader.read(in);
        }
        catch (DocumentException e)
        {
            throw new PluginConfigurationException(e);
        }
        
        Element plugins = document.getRootElement();

        // TODO : validate against schema
        
        // iterate through child elements of root
        for ( Iterator<Element> i = plugins.elementIterator(); i.hasNext(); ) {
            Element plugin = i.next();

            PluginDefinition pd = parsePluginDefinition(cl, plugin);
            repo.addPluginDefinition(pd);
        }
    }

    public static PluginDefinition parseXmlPluginDefinition(ClassLoader cl, InputStream in) throws PluginConfigurationException
    {
        SAXReader reader = new SAXReader();
        Document document;
        try
        {
            document = reader.read(in);
        }
        catch (DocumentException e)
        {
            throw new PluginConfigurationException(e);
        }
        
        Element plugin = document.getRootElement();

        // TODO : validate against schema
        
        return parsePluginDefinition(cl, plugin);
    }

    private static PluginDefinition parsePluginDefinition(ClassLoader cl, Element plugin) throws PluginConfigurationException
    {
        if (plugin.attributeValue("definedIn") != null)
        {
            StreamManager sm = new StreamManager();
            
            String sFileName = plugin.attributeValue("definedIn");

            try
            {
                InputStream in = sm.handle(cl.getResourceAsStream(sFileName));
                
                return parseXmlPluginDefinition(cl, in);
            }
            finally
            {
                sm.closeAll();
            }
        }
        else
        {
            Class c;
            try
            {
                c = cl.loadClass(plugin.attributeValue("class"));
            }
            catch (ClassNotFoundException e)
            {
                throw new PluginConfigurationException(e);
            }
            String sDescription = plugin.elementText("description");
            
            PluginDefinition pluginDef = new PluginDefinition(plugin.attributeValue("name"), sDescription, c);

            Element commandLine = plugin.element("command-line");
            Element options = commandLine.element("options");
            
            for ( Iterator i = options.elementIterator(); i.hasNext(); ) {
                Element option = (Element) i.next();

                PluginOption po = parsePluginOption(option);

                pluginDef.addOption(po);
            }
            return pluginDef;
        }
    }
    
    private static PluginOption parsePluginOption(Element option)
    {
        PluginOption po = new PluginOption();
        po.setArgName(option.attributeValue("argName"));
        po.setArgsCount(Integer.parseInt(option.attributeValue("argsCount", "1")));
        po.setArgsOptional(Boolean.valueOf(option.attributeValue("optionalArgs", "false")));
        po.setDescription(option.attributeValue("description"));
        po.setHasArgs(Boolean.valueOf(option.attributeValue("hasArgs", "false")));
        po.setLongOpt(option.attributeValue("longName"));
        po.setOption(option.attributeValue("shortName"));
        po.setRequired(Boolean.valueOf(option.attributeValue("description", "false")));
        po.setType(option.attributeValue("type"));
        po.setValueSeparator(option.attributeValue("separator"));
        
        return po;
    }

    public static void main(String[] args) throws Exception
    {
        loadFromXmlPluginPackageDefinitions(null, PluginRepositoryUtil.class.getClassLoader(), new FileInputStream("/home/ziccardi/git/jnrpe-lib/jnrpe-parent/jnrpe-plugins/src/main/resources/plugin.xml"));
    }
}
