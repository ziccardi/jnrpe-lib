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

import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.builder.GroupBuilder;

import it.jnrpe.server.xml.XMLOption;
import it.jnrpe.server.xml.XMLOptions;

public class XMLPluginOptions extends XMLOptions
{
    
    public Group toGroup()
    {
        GroupBuilder gb = new GroupBuilder();
        
        for (XMLOption opt1 : this.getOptions())
        {
            XMLPluginOption opt = (XMLPluginOption) opt1;
            gb.withOption(opt.toOption());
        }
        
        return gb.create();
    }
    
}
