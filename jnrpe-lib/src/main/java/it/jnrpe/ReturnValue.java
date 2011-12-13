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
package it.jnrpe;

import it.jnrpe.net.IJNRPEConstants;

/**
 * This class is just a container for the plugin result
 * 
 * @author Massimiliano Ziccardi
 *
 */
public class ReturnValue
{
	private int m_iReturnCode;
	private String m_sMessage;

    public ReturnValue()
    {
        
    }
	
	public ReturnValue(String sMessage)
    {
        m_iReturnCode = IJNRPEConstants.STATE_OK;
        m_sMessage = sMessage;
    }

	public ReturnValue(int iReturnCode, String sMessage)
	{
		m_iReturnCode = iReturnCode;
		m_sMessage = sMessage;
	}

	/**
	 * Sets the return code and returns 'this' so that
	 * the calls can be cascaded
	 * @param iReturnCode
	 * @return
	 */
	public ReturnValue withReturnCode(int iReturnCode)
	{
	    m_iReturnCode = iReturnCode;
	    return this;
	}
	
	/**
	 * Sets the message and returns 'this' so that
	 * the calls can be cascaded
	 * @param sMessage
	 * @return
	 */
	public ReturnValue withMessage(String sMessage)
	{
	    m_sMessage = sMessage;
	    return this;
	}
	
	public int getReturnCode()
	{
		return m_iReturnCode;
	}
	
	public String getMessage()
	{
		return m_sMessage;
	}
}
