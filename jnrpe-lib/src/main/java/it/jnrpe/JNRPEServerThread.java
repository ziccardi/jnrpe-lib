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
import it.jnrpe.commands.CommandInvoker;
import it.jnrpe.events.IJNRPEEventListener;
import it.jnrpe.net.BadCRCException;
import it.jnrpe.net.IJNRPEConstants;
import it.jnrpe.net.JNRPERequest;
import it.jnrpe.net.JNRPEResponse;
import it.jnrpe.utils.StreamManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Thread used to server client request 
 * 
 * @author Massimiliano Ziccardi
 */
class JNRPEServerThread extends Thread
{
	private Socket socket = null;
	private Boolean m_bStopped = Boolean.FALSE;
	private final CommandInvoker m_commandInvoker;
	private JNRPEListenerThread m_parent = null;
	private List<IJNRPEEventListener> m_vListeners = null;
	
	public JNRPEServerThread(Socket socket, CommandInvoker commandInvoker)
	{
		super("JNRPEServerThread");
		this.socket = socket;
		m_commandInvoker = commandInvoker;
	}

	void configure(JNRPEListenerThread listenerThread, List<IJNRPEEventListener> vListeners)
	{
	    m_parent = listenerThread;
	    m_vListeners = vListeners;
	}
	
	public JNRPEResponse handleRequest (JNRPERequest req)
	{
		// extracting command name and params
		String[] vParts = req.getStringMessage().split("!");
		
		String sCommandName = vParts[0];
		String[] vArgs = new String[vParts.length - 1];
		
		System.arraycopy(vParts, 1, vArgs, 0, vArgs.length);
		
		ReturnValue ret = m_commandInvoker.invoke(sCommandName, vArgs);
		
		JNRPEResponse res = new JNRPEResponse();
		res.setPacketVersion(IJNRPEConstants.NRPE_PACKET_VERSION_2);
		
		res.setResultCode(ret.getReturnCode());
		res.setMessage(ret.getMessage());
		res.updateCRC();
		
		EventsUtil.sendEvent(m_vListeners, m_parent, "COMMAND_INVOKED", 
		        new Object[]{"COMMAND_NAME", sCommandName, 
		                    "ARGS", argsToString(vArgs), 
		                    "RETURN_CODE", ret.getReturnCode(), 
		                    "RETURN_MESSAGE", ret.getMessage()});
		
		return res;
	}
	
	private String argsToString(String[] vArgs)
    {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		sb.append(StringUtils.join(vArgs, ","));
		sb.append("]");
		return sb.toString();
    }

    public void run()
	{
		StreamManager streamMgr = new StreamManager();
		
		try
		{
			InputStream in = streamMgr.handle(socket.getInputStream());
			JNRPEResponse res = null;
			JNRPERequest req = null;
			
			try
			{
				req = new JNRPERequest(in);

				switch (req.getPacketType())
				{
				case IJNRPEConstants.QUERY_PACKET:
						res = handleRequest(req);
					break;
				default:
					res = new JNRPEResponse();
					res.setPacketVersion(req.getPacketVersion());
					res.setResultCode(IJNRPEConstants.STATE_UNKNOWN);
					res.setMessage("Invalid Packet Type");
					res.updateCRC();
					
				}
				
			}
			catch (BadCRCException e)
			{
				res = new JNRPEResponse();
				res.setPacketVersion(IJNRPEConstants.NRPE_PACKET_VERSION_2);
				res.setResultCode(IJNRPEConstants.STATE_UNKNOWN);
				res.setMessage("BAD REQUEST CRC");
				res.updateCRC();
				
			}
			
			synchronized(m_bStopped)
			{
			    if (!m_bStopped.booleanValue())
			    {
        			OutputStream out = streamMgr.handle(socket.getOutputStream());
        			out.write(res.toByteArray());
			    }
			}
		}
		catch (IOException e)
		{
//			if (!m_bStopped.booleanValue())
//			    m_Logger.error("ERROR DURING SOCKET OPERATION.", e);
		    EventsUtil.sendEvent(m_vListeners, m_parent, "ERROR", 
	                new Object[]{"MESSAGE", "Error during socket operation",
		                         "EXCEPTION", e
		                        });
		}
		finally
		{
		    try
			{
		        if (socket != null && !socket.isClosed())
		            socket.close();
			}
			catch (IOException e)
			{
//				m_Logger.error("ERROR CLOSING SOCKET", e);
			}
			
			streamMgr.closeAll();
		}
		
	}
	
	public void stopNow()
    {
	    StreamManager streamMgr = new StreamManager();
	    try
	    {
    	    synchronized (m_bStopped)
    	    {
    	        // If the socket is closed, the thread has finished...
    	        if (!socket.isClosed())
    	        {
        	        m_bStopped = Boolean.TRUE;
        	        
        	        try
        	        {
            	        JNRPEResponse res = new JNRPEResponse();
                        res.setPacketVersion(IJNRPEConstants.NRPE_PACKET_VERSION_2);
                        res.setResultCode(IJNRPEConstants.STATE_UNKNOWN);
                        res.setMessage("Command execution timeout");
                        res.updateCRC();
            	        
                        OutputStream out = streamMgr.handle(socket.getOutputStream());
                        out.write(res.toByteArray());
                    
            	        // This is just to stop any socket operations...
                        socket.close();
                    }
                    catch (IOException e)
                    {
                    }
                    
                    // Let's try to interrupt all other operations...
                    if (this.isAlive())
                        this.interrupt();
                    
                    // We can exit now..
    	        }
    	    }
	    }
	    catch (Exception e)
	    {
	        
	    }
	    finally
	    {
	        streamMgr.closeAll();
	    }
    }
}
