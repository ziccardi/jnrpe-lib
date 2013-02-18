/*
 * Copyright (c) 2013 Massimiliano Ziccardi Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package it.jnrpe.client;

import it.jnrpe.ReturnValue;
import it.jnrpe.Status;
import it.jnrpe.net.JNRPERequest;
import it.jnrpe.net.JNRPEResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 *  This class represent a simple JNRPE client that can be used to invoke
 *  commands installed inside JNRPE by code.
 *  It is the JAVA equivalent of check_nrpe.
 *  
 *  WARNING: SSL is not yet supported.
 *  
 *  @author Massimiliano Ziccardi
 */
public class JNRPEClient {
	private final String m_sServerIP;
	private final int m_iServerPort;

	/**
	 * Instantiates a JNRPE client.
	 * @param sJNRPEServerIP The IP where the JNRPE is installed
	 * @param iJNRPEServerPort The port where the JNRPE server listens
	 */
	public JNRPEClient(final String sJNRPEServerIP, final int iJNRPEServerPort) {
		m_sServerIP = sJNRPEServerIP;
		m_iServerPort = iJNRPEServerPort;
	}

	/**
	 * Inovoke a command installed in JNRPE.
	 * 
	 * @param sCommandName The name of the command to be invoked
	 * @param arguments The arguments to pass to the command (will substitute the $ARGSx$ parameters)
	 * @return The value returned by the server
	 * @throws JNRPEClientException Thrown on any communication error.
	 */
	public ReturnValue sendCommand(String sCommandName, String... arguments) throws JNRPEClientException 
	{
		Socket s = null;
		try
		{
			s = new Socket();
			s.connect(new InetSocketAddress(m_sServerIP, m_iServerPort));
			JNRPERequest req = new JNRPERequest(sCommandName, arguments);
	
			s.getOutputStream().write(req.toByteArray());
	
			InputStream in = s.getInputStream();
			JNRPEResponse res = new JNRPEResponse(in);
	
			return new ReturnValue(Status.fromIntValue(res.getResultCode()), res.getStringMessage());
		}
		catch (Exception e)
		{
			throw new JNRPEClientException(e);
		}
		finally
		{
			if (s != null)
			{
				try 
				{
					s.close();
				} 
				catch (IOException e) 
				{
					// Ignore
				}
			}
		}
	}

}
