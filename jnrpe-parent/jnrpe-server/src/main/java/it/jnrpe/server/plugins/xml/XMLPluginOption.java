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

import it.jnrpe.plugins.PluginOption;
import it.jnrpe.server.xml.XMLOption;

public class XMLPluginOption extends XMLOption
{

    public PluginOption toPluginOption()
    {
        return new PluginOption()
            .setArgName(getArgName())
            .setArgsCount(getArgsCount())
            .setArgsOptional(getArgsOptional())
            .setDescription(getDescription())
            .setHasArgs(hasArgs())
            .setLongOpt(getLongOpt())
            .setOption(getOption())
            .setRequired(getRequired().equalsIgnoreCase("true"))
            .setType(getType())
            .setValueSeparator(getValueSeparator());
    }

}
