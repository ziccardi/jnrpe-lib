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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class CommandsSection
{
    private List<Command> m_vCommands = new ArrayList<Command>();
    
    public static class Command
    {
        private final String m_sName;
        private final String m_sPlugin;
        private final String m_sCommandLine;
        
        public Command(String sName, String sPlugin, String sCommandLine)
        {
            m_sName = sName;
            m_sPlugin = sPlugin;
            m_sCommandLine = sCommandLine;
        }
        
        public String getName()
        {
            return m_sName;
        }
        
        public String getPlugin()
        {
            return m_sPlugin;
        }
        
        public String getCommandLine()
        {
            return m_sCommandLine;
        }
    }
    
    public void addCommand(final String sCommandName, final String sPluginName, final String sCommandLine)
    {
        m_vCommands.add(new Command(sCommandName, sPluginName, sCommandLine));
    }
    
    public Collection<Command> getAllCommands()
    {
        return m_vCommands;
    }
}
