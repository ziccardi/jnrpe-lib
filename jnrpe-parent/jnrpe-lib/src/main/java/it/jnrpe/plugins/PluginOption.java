/*
 * Copyright (c) 2008 Massimiliano Ziccardi Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package it.jnrpe.plugins;

import org.apache.commons.cli.Option;

/**
 * This class describes a plugin option.
 *
 * @author Massimiliano Ziccardi
 */
public final class PluginOption
{
    /**
     * The option.
     */
    private String m_sOption = null;

    /**
     * Indicate if the option ha arguments.
     */
    private boolean m_bHasArgs = false;

    /**
     * The number of arguments.
     */
    private Integer m_iArgsCount = null;

    /**
     * If the option is mandatory.
     */
    private boolean m_bRequired = false;

    /**
     * If the argument is optional.
     */
    private Boolean m_bArgsOptional = null;

    /**
     * The name of the argument.
     */
    private String m_sArgName = null;

    /**
     * Long version of the option.
     */
    private String m_sLongOpt = null;

    /**
     * The type.
     */
    private String m_sType = null;

    /**
     * The separator of the values.
     */
    private String m_sValueSeparator = null;

    /**
     * The description.
     */
    private String m_sDescription = null;

    /**
     * Default constructor.
     */
    public PluginOption()
    {

    }

    /**
     * Returns the option string.
     *
     * @return The option as string
     */
    public String getOption()
    {
        return m_sOption;
    }

    /**
     * Sets the option string. For example, if the plugin must receive the.
     * '--file' option, sOption will be 'file'.
     *
     * @param sOption The option as string
     * @return this
     */
    public PluginOption setOption(final String sOption)
    {
        m_sOption = sOption;
        return this;
    }

    /**
     * Returns true if the option has an argument.
     *
     * @return true if the option has an argument.
     */
    public boolean hasArgs()
    {
        return m_bHasArgs;
    }

    /**
     * Tells the option that it must accept an argument.
     *
     * @param bHasArgs true if the option has an argument.
     * @return this
     */
    public PluginOption setHasArgs(final boolean bHasArgs)
    {
        m_bHasArgs = bHasArgs;
        return this;
    }

    /**
     * Returns the number of arguments.
     *
     * @return the number of arguments.
     */
    public Integer getArgsCount()
    {
        return m_iArgsCount;
    }

    /**
     * Sets the number of arguments.
     *
     * @param iArgCount the number of arguments.
     * @return this
     */
    public PluginOption setArgsCount(final Integer iArgCount)
    {
        m_iArgsCount = iArgCount;
        return this;
    }

    /**
     * Returns the string 'true' if required.
     *
     * @return the string 'true' if required.
     */
    public String getRequired()
    {
        return "" + m_bRequired;
    }

    /**
     * Set if the option is required.
     *
     * @param bRequired <code>true</code> if the option is required.
     * @return this
     */
    public PluginOption setRequired(final boolean bRequired)
    {
        m_bRequired = bRequired;
        return this;
    }

    /**
     * Used to know if the option has optional arguments.
     *
     * @return <code>true</code> if the option has optional arguments.
     */
    public Boolean getArgsOptional()
    {
        return m_bArgsOptional;
    }

    /**
     * Sets if the arguments are mandatory.
     *
     * @param bArgsOptional <code>true</code> if the option
     * has optional arguments.
     * @return this
     */
    public PluginOption setArgsOptional(final Boolean bArgsOptional)
    {
        m_bArgsOptional = bArgsOptional;
        return this;
    }

    /**
     * Returns the name of the argument of this option.
     *
     * @return the name of the argument of this option.
     */
    public String getArgName()
    {
        return m_sArgName;
    }

    /**
     * Sets the name of the argument of this option.
     *
     * @param sArgName The argument name
     * @return this
     */
    public PluginOption setArgName(final String sArgName)
    {
        m_sArgName = sArgName;
        return this;
    }

    /**
     * Returns the long name of this option.
     *
     * @return the long name of this option.
     */
    public String getLongOpt()
    {
        return m_sLongOpt;
    }

    /**
     * Sets the long name of this option.
     *
     * @param sLongOpt the long name of this option.
     * @return this
     */
    public PluginOption setLongOpt(final String sLongOpt)
    {
        m_sLongOpt = sLongOpt;
        return this;
    }

    /**
     * Returns the type of this option.
     *
     * @return the type of this option.
     */
    public String getType()
    {
        return m_sType;
    }

    /**
     * Sets the type of this option.
     *
     * @param sType the type of this option.
     * @return this
     */
    public PluginOption setType(final String sType)
    {
        m_sType = sType;
        return this;
    }

    /**
     * Returns the value separator.
     *
     * @return the value separator.
     */
    public String getValueSeparator()
    {
        return m_sValueSeparator;
    }

    /**
     * Sets the value separator.
     *
     * @param sValueSeparator the value separator.
     * @return this
     */
    public PluginOption setValueSeparator(final String sValueSeparator)
    {
        m_sValueSeparator = sValueSeparator;
        return this;
    }

    /**
     * Returns the description of this option.
     *
     * @return the description of this option.
     */
    public String getDescription()
    {
        return m_sDescription;
    }

    /**
     * Sets the description of this option.
     *
     * @param sDescription the description of this option.
     * @return this
     */
    public PluginOption setDescription(final String sDescription)
    {
        m_sDescription = sDescription;
        return this;
    }

    /**
     * Convert this {@link PluginOption} to the Option required by Apache.
     * Commons Cli.
     *
     * @return The option object required by commons cli
     */
    Option toOption()
    {
        Option ret = new Option(m_sOption, m_sDescription);

        if (m_bArgsOptional != null)
        {
            ret.setOptionalArg(m_bArgsOptional.booleanValue());
        }

        if (m_bHasArgs)
        {
            if (m_iArgsCount == null)
            {
                ret.setArgs(Option.UNLIMITED_VALUES);
            }
        }

        ret.setRequired(m_bRequired);
        if (m_iArgsCount != null)
        {
            ret.setArgs(m_iArgsCount.intValue());
        }

        if (m_sArgName != null)
        {
            if (m_iArgsCount == null)
            {
                ret.setArgs(Option.UNLIMITED_VALUES);
            }
            ret.setArgName(m_sArgName);
        }

        if (m_sLongOpt != null)
        {
            ret.setLongOpt(m_sLongOpt);
        }

        if (m_sValueSeparator != null && m_sValueSeparator.length() != 0)
        {
            ret.setValueSeparator(m_sValueSeparator.charAt(0));
        }

        return ret;
    }
}
