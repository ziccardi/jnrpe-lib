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
package it.jnrpe.net;

/**
 * 
 * @author Massimiliano Ziccardi
 */
public class JNRPECommand
{
    private final String m_sCommandName;
    private final String[] m_vArgs;
    
    private JNRPECommand(final String sCommandName, final String[] vArgs)
    {
        m_sCommandName = sCommandName;
        m_vArgs = vArgs;
    }
    
    public static JNRPECommand getInstance(JNRPERequest req)
    {
        // extracting command name and params
        String[] vParts = req.getStringMessage().split("!");
        
        String sCommandName = vParts[0];
        String[] vArgs = new String[vParts.length - 1];
        
        System.arraycopy(vParts, 1, vArgs, 0, vArgs.length);
        
        return new JNRPECommand(sCommandName, vArgs);
    }
    
    public String getCommandName()
    {
        return m_sCommandName;
    }
    
    public String[] getArguments()
    {
        return m_vArgs;
    }
}
