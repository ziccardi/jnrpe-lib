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
package it.jnrpe.server.plugins.xml;

import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;

import it.jnrpe.plugins.PluginOption;
import it.jnrpe.server.xml.XMLOption;

public class XMLPluginOption extends XMLOption
{

    public XMLPluginOption(OptionType ot)
    {
        super(ot);
    }

    public PluginOption toPluginOption()
    {
        return new PluginOption().setArgName(getArgName()).setArgsCount(
                getArgsCount()).setArgsOptional(
                getArgsOptional() == null ? true : getArgsOptional())
                .setDescription(getDescription()).setHasArgs(hasArgs())
                .setLongOpt(getLongOpt()).setOption(getOption()).setRequired(
                        getRequired().equalsIgnoreCase("true")).setType(
                        getType()).setValueSeparator(getValueSeparator());
    }

    Option toOption()
    {
        DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();

        oBuilder.withShortName(getOption()).withDescription(getDescription())
                .withRequired(getRequired().equalsIgnoreCase("true"));

        if (getLongOpt() != null) oBuilder.withLongName(getLongOpt());

        // DefaultOption ret = oBuilder
        // .withLongName(m_sOption)
        // .withDescription(m_sDescription);

        // Option ret = new Option(m_sOption, m_sDescription);

        // if (m_bArgsOptional != null)
        // {
        // ret.setOptionalArg(m_bArgsOptional.booleanValue());
        // }

        if (hasArgs())
        {
            ArgumentBuilder aBuilder = new ArgumentBuilder();

            if (getArgName() != null) aBuilder.withName(getArgName());

            if (getArgsOptional()) aBuilder.withMinimum(0);

            if (getArgsCount() != null)
            {
                aBuilder.withMaximum(getArgsCount());
            }
            else aBuilder.withMaximum(1);

            if (getValueSeparator() != null && getValueSeparator().length() != 0)
            {
                aBuilder.withInitialSeparator(getValueSeparator().charAt(0));
                aBuilder.withSubsequentSeparator(getValueSeparator().charAt(0));
            }
            oBuilder.withArgument(aBuilder.create());
        }

        return oBuilder.create();
    }
}
