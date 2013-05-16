/*
 * Copyright (c) 2013 Massimiliano Ziccardi
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
package it.jnrpe.utils;

import it.jnrpe.plugins.PluginConfigurationException;
import it.jnrpe.plugins.PluginDefinition;
import it.jnrpe.plugins.PluginOption;
import it.jnrpe.plugins.PluginRepository;

import java.io.InputStream;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * An utility class that allows to define the plugin repository in an XML file
 * instead that using Java Code.
 *
 * @author Massimiliano Ziccardi
 */
public final class PluginRepositoryUtil {

    /**
     *
     */
    private PluginRepositoryUtil() {

    }

    /**
     * Loads a full repository definition from an XML file.
     *
     * @param repo
     *            The repository that must be loaded
     * @param cl
     *            The classloader to be used to instantiate the plugin classes
     * @param in
     *            The stream to the XML file
     * @throws PluginConfigurationException
     *             -
     */
    public static void loadFromXmlPluginPackageDefinitions(
            final PluginRepository repo, final ClassLoader cl,
            final InputStream in)
            throws PluginConfigurationException {
        SAXReader reader = new SAXReader();
        Document document;
        try {
            document = reader.read(in);
        } catch (DocumentException e) {
            throw new PluginConfigurationException(e);
        }

        Element plugins = document.getRootElement();

        // TODO : validate against schema

        // iterate through child elements of root
        for (Iterator<Element> i = plugins.elementIterator(); i.hasNext();) {
            Element plugin = i.next();

            PluginDefinition pd = parsePluginDefinition(cl, plugin);
            repo.addPluginDefinition(pd);
        }
    }

    /**
     * Loads the definition of a single plugin from an XML file.
     *
     * @param cl
     *            The classloader to be used to instantiate the plugin class
     * @param in
     *            The stream to the XML file
     * @return The plugin definition
     * @throws PluginConfigurationException
     *             -
     */
    public static PluginDefinition parseXmlPluginDefinition(
            final ClassLoader cl,
            final InputStream in) throws PluginConfigurationException {
        SAXReader reader = new SAXReader();
        Document document;
        try {
            document = reader.read(in);
        } catch (DocumentException e) {
            throw new PluginConfigurationException(e);
        }

        Element plugin = document.getRootElement();

        // TODO : validate against schema

        return parsePluginDefinition(cl, plugin);
    }

    /**
     * Parse an XML plugin definition.
     *
     * @param cl
     *            The classloader to be used to load classes
     * @param plugin
     *            The plugin XML element
     * @return the parsed plugin definition
     * @throws PluginConfigurationException
     *             -
     */
    private static PluginDefinition parsePluginDefinition(final ClassLoader cl,
            final Element plugin) throws PluginConfigurationException {
        if (plugin.attributeValue("definedIn") != null) {
            StreamManager sm = new StreamManager();

            String sFileName = plugin.attributeValue("definedIn");

            try {
                InputStream in = sm.handle(cl.getResourceAsStream(sFileName));

                return parseXmlPluginDefinition(cl, in);
            } finally {
                sm.closeAll();
            }
        } else {
            Class c;
            try {
                c = cl.loadClass(plugin.attributeValue("class"));
            } catch (ClassNotFoundException e) {
                throw new PluginConfigurationException(e);
            }
            String sDescription = plugin.elementText("description");

            PluginDefinition pluginDef =
                    new PluginDefinition(plugin.attributeValue("name"),
                            sDescription, c);

            Element commandLine = plugin.element("command-line");
            Element options = commandLine.element("options");

            for (Iterator i = options.elementIterator(); i.hasNext();) {
                Element option = (Element) i.next();

                PluginOption po = parsePluginOption(option);

                pluginDef.addOption(po);
            }
            return pluginDef;
        }
    }

    /**
     * Parses a plugin option XML definition.
     *
     * @param option
     *            The plugin option XML definition
     * @return The parsed plugin option
     */
    private static PluginOption parsePluginOption(final Element option) {
        PluginOption po = new PluginOption();
        po.setArgName(option.attributeValue("argName"));
        po.setArgsCount(Integer.parseInt(option
                .attributeValue("argsCount", "1")));
        po.setArgsOptional(Boolean.valueOf(option.attributeValue(
                "optionalArgs", "false")));
        po.setDescription(option.attributeValue("description"));
        po.setHasArgs(
                    Boolean.valueOf(option.attributeValue("hasArgs", "false")));
        po.setLongOpt(option.attributeValue("longName"));
        po.setOption(option.attributeValue("shortName"));
        po.setRequired(Boolean.valueOf(option.attributeValue("description",
                "false")));
        po.setType(option.attributeValue("type"));
        po.setValueSeparator(option.attributeValue("separator"));

        return po;
    }
}
