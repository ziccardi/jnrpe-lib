/*
 * Copyright (c) 2012 Massimiliano Ziccardi
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
package it.jnrpe;

import java.util.HashMap;
import java.util.Map;

import it.jnrpe.events.IJNRPEEvent;

/**
 * A basic, very simple, general purpose event.
 * 
 * @author Massimiliano Ziccardi
 */
class SimpleEvent implements IJNRPEEvent
{
    private String m_sEventName = null;
    private Map<String, Object> m_mParams = new HashMap<String, Object>();
    
    public SimpleEvent(String sEventName, Object[] vParams)
    {
        m_sEventName = sEventName;
        for (int i = 0; vParams != null && i < vParams.length; i+=2)
        {
            m_mParams.put((String)vParams[i], vParams[i + 1]);
        }
    }
    
    public String getEventName()
    {
        return m_sEventName;
    }

    public Map<String, Object> getEventParams()
    {
        return m_mParams;
    }

}
