/*
 * Copyright (c) 2011 Massimiliano Ziccardi Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package it.jnrpe.plugins;

import java.util.ArrayList;
import java.util.List;

/**
 * This object represent a plugin definition. It is used to describe to JNRPE
 * which parameters a plugin supports
 *
 * @author Massimiliano Ziccardi
 */
public final class PluginDefinition
{
    /**
     * The name of the plugin (as parsed from the XML file)
     */
    private final String m_sName;
    /**
     *The Class of the plugin.
     */
    private final Class<? extends IPluginInterface> m_pluginClass;

    /**
     * The plugin instance.
     */
    private final IPluginInterface m_pluginInterface;

    /**
     * The plugin description (as parsed from the XML file)
     */
    private final String m_sDescription;

    /**
     * All the options this plugin supports (as parsed from the XML file)
     */
    private List<PluginOption> m_vPluginOptions = new ArrayList<PluginOption>();

    /**
     *Initializes the plugin definition specifying the Class object that.
     *represent the plugin. This constructor is used, for example, by JNRPE.
     *server where all the plugins are described in an XML file and are loaded.
     * with potentially different class loaders.
     *
     * @param sName
     *            The plugin name
     * @param sDescription
     *            The plugin description
     * @param pluginClass
     *            The plugin Class object
     */
    public PluginDefinition(final String sName, final String sDescription,
            final Class<? extends IPluginInterface> pluginClass)
    {
        m_sName = sName;
        m_pluginClass = pluginClass;
        m_sDescription = sDescription;
        m_pluginInterface = null;
    }

    /**
     *Initializes the plugin definition specifying a plugin instance. This is.
     *useful when you embed JNRPE: with this constructor you can pass a <i>pre.
     * inizialized/configured</i> instance.
     *
     * @param sName
     *            The plugin name
     * @param sDescription
     *            The plugin description
     * @param pluginInterface
     *            The plugin instance
     */
    public PluginDefinition(final String sName, final String sDescription,
            final IPluginInterface pluginInterface)
    {
        m_sName = sName;
        m_pluginClass = null;
        m_sDescription = sDescription;
        m_pluginInterface = pluginInterface;
    }

    /**
     *Adds a new option to the plugin.
     *
     * @param option
     *            The option
     * @return this
     */
    public PluginDefinition addOption(final PluginOption option)
    {
        m_vPluginOptions.add(option);
        return this;
    }

    /**
     * Returns the plugin name.
     *
     * @return the plugin name.
     */
    public String getName()
    {
        return m_sName;
    }

    /**
     * Returns all the plugin options.
     *
     * @return a List of all the plugin option
     */
    public List<PluginOption> getOptions()
    {
        return m_vPluginOptions;
    }

    /**
     * Returns the plugin description.
     *
     * @return The plugin description
     */
    public String getDescription()
    {
        return m_sDescription;
    }

    /**
     * Returns the plugin class, if specified.
     *
     * @return the plugin class. <code>null</code> if not specified.
     */
    Class<? extends IPluginInterface> getPluginClass()
    {
        return m_pluginClass;
    }

    /**
     * Returns the plugin instance, if present.
     *
     * @return the plugin interface.<code>null</code> if not specified.
     */
    IPluginInterface getPluginInterface()
    {
        return m_pluginInterface;
    }
}
