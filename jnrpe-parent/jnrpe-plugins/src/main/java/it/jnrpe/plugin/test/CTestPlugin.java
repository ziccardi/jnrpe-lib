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
package it.jnrpe.plugin.test;

import it.jnrpe.ICommandLine;
import it.jnrpe.ReturnValue;
import it.jnrpe.Status;
import it.jnrpe.plugins.IPluginInterface;

/**
 * A simple test plugin that returns the status as specified by the
 * 'status' parameter and as text the value of the 'text' parameter
 * 
 * @author Massimiliano Ziccardi
 *
 */
public class CTestPlugin implements IPluginInterface
{
	public CTestPlugin()
	{
		
	}

	/**
	 * Executes the plugin.
	 * The accepter params are:
	 * <UL>
	 * <LI>--text : the text to be returned
	 * <LI>--status : the status to be returned (ok, warning, critical. Any other status is interpreted as UNKNOWN). 
	 *    The default value is 'ok'.
	 * </UL>
	 */
	public ReturnValue execute(ICommandLine cl)
	{
		Status returnStatus;
		
		String statusParam = cl.getOptionValue("status", "ok");
		
		if (statusParam.equalsIgnoreCase("ok"))
			returnStatus = Status.OK;
		else if (statusParam.equalsIgnoreCase("critical"))
			returnStatus = Status.CRITICAL;
		else if (statusParam.equalsIgnoreCase("warning"))
			returnStatus = Status.WARNING;
		else
			returnStatus = Status.UNKNOWN;
		
		return new ReturnValue(returnStatus, "TEST : " + cl.getOptionValue("text"));
	}
}
