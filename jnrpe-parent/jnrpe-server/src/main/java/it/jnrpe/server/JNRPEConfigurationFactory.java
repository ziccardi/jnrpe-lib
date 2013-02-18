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

import java.io.File;

class JNRPEConfigurationFactory
{
    private JNRPEConfigurationFactory()
    {
        
    }
    
    public static JNRPEConfiguration createConfiguration(String sConfigurationFilePath) throws ConfigurationException
    {
        JNRPEConfiguration conf = null;
        
        if (sConfigurationFilePath.toLowerCase().endsWith(".conf")  || sConfigurationFilePath.toLowerCase().endsWith(".ini")  )
            conf = new IniJNRPEConfiguration();
        else if (sConfigurationFilePath.toLowerCase().endsWith(".xml")   )
            conf = new XmlJNRPEConfiguration();
        
        if (conf == null)
        {
            throw new ConfigurationException("Config file name must end with either '.ini' (ini file) or '.xml' (xml file)");
        }
        
        conf.load(new File(sConfigurationFilePath));
        
        return conf;
    }
}
