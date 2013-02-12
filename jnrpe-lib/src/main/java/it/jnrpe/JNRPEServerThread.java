/*
 * Copyright (c) 2008 Massimiliano Ziccardi Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package it.jnrpe;

import it.jnrpe.commands.CommandInvoker;
import it.jnrpe.events.EventsUtil;
import it.jnrpe.events.IJNRPEEventListener;
import it.jnrpe.events.LogEvent;
import it.jnrpe.net.BadCRCException;
import it.jnrpe.net.JNRPERequest;
import it.jnrpe.net.JNRPEResponse;
import it.jnrpe.net.PacketVersion;
import it.jnrpe.utils.StreamManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Thread used to server client request.
 *
 * @author Massimiliano Ziccardi
 */
class JNRPEServerThread extends Thread
{
    /**
     * The socket used by this thread to read the request and write the answer.
     */
    private Socket m_Socket = null;

    /**
     * <code>true</code> if this thread must stop working as soon as possible.
     */
    private Boolean m_bStopped = Boolean.FALSE;

    /**
     * The command invoker to be used to serve the request.
     */
    private final CommandInvoker m_commandInvoker;

    /**
     * The source of the events (I.e. the JNRPE listeners that received the
     * request).
     */
    private JNRPEListenerThread m_parent = null;

    /**
     * The list of event listeners.
     */
    private Set<IJNRPEEventListener> m_vListeners = null;

    /**
     * Builds and initializes a new server thread.
     *
     * @param socket
     *            The socket to be used to read and write
     * @param commandInvoker
     *            The command invoker that will serve the request
     */
    public JNRPEServerThread(final Socket socket,
            final CommandInvoker commandInvoker)
    {
        super("JNRPEServerThread");
        this.m_Socket = socket;
        m_commandInvoker = commandInvoker;
    }

    /**
     * Configures this server thread.
     *
     * @param listenerThread
     *            The listener that received the request
     * @param vListeners
     *            The event listeners
     */
    void configure(final JNRPEListenerThread listenerThread,
            final Set<IJNRPEEventListener> vListeners)
    {
        m_parent = listenerThread;
        m_vListeners = vListeners;
    }

    /**
     * Serve the request.
     *
     * @param req
     *            The request
     * @return The Response
     */
    public JNRPEResponse handleRequest(final JNRPERequest req)
    {
        // extracting command name and params
        String[] vParts = req.getStringMessage().split("!");

        String sCommandName = vParts[0];
        String[] vArgs = new String[vParts.length - 1];

        System.arraycopy(vParts, 1, vArgs, 0, vArgs.length);

        ReturnValue ret = m_commandInvoker.invoke(sCommandName, vArgs);

        if (ret == null)
        {
        	String args = "";
        	if (vArgs != null)
        		args = StringUtils.join(vArgs, ",");
        	
        	ret = new ReturnValue(Status.UNKNOWN, "Command [" + sCommandName + "] with args [" + args + "] returned null");
        }
        
        JNRPEResponse res = new JNRPEResponse();
        res.setPacketVersion(PacketVersion.VERSION_2);

        res.setResultCode(ret.getStatus().intValue());
        res.setMessage(ret.getMessage());
        res.updateCRC();

        String sMessageInvokedLog = MessageFormat
                .format("Invoked command {0} - Status : "
                        + "{1} - Return Message : ''{2}''",
                        sCommandName, ret.getStatus().name(), ret.getMessage());
        String sParamTraceLog = MessageFormat.format("Arguments : ''{0}''",
                argsToString(vArgs));

        EventsUtil.sendEvent(m_vListeners, m_parent, LogEvent.DEBUG,
                sMessageInvokedLog);
        EventsUtil.sendEvent(m_vListeners, m_parent, LogEvent.TRACE,
                sParamTraceLog);

        return res;
    }

    /**
     * Utility to convert the arguments to a printable string.
     *
     * @param vArgs
     *            The arguments array
     * @return The printable string
     */
    private String argsToString(final String[] vArgs)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append(StringUtils.join(vArgs, ","));
        sb.append("]");
        return sb.toString();
    }

    /**
     * Runs the thread.
     */
    public void run()
    {
        StreamManager streamMgr = new StreamManager();

        try
        {
            InputStream in = streamMgr.handle(m_Socket.getInputStream());
            JNRPEResponse res = null;
            JNRPERequest req = null;

            try
            {
                req = new JNRPERequest(in);

                switch (req.getPacketType())
                {
                    case QUERY:
                        res = handleRequest(req);
                        break;
                    default:
                        res = new JNRPEResponse();
                        res.setPacketVersion(req.getPacketVersion());
                        res.setResultCode(Status.UNKNOWN.intValue());
                        res.setMessage("Invalid Packet Type");
                        res.updateCRC();

                }

            }
            catch (BadCRCException e)
            {
                res = new JNRPEResponse();
                res.setPacketVersion(PacketVersion.VERSION_2);
                res.setResultCode(Status.UNKNOWN.intValue());
                res.setMessage("BAD REQUEST CRC");
                res.updateCRC();

            }

            synchronized (m_bStopped)
            {
                if (!m_bStopped.booleanValue())
                {
                    OutputStream out = streamMgr.handle(m_Socket
                            .getOutputStream());
                    out.write(res.toByteArray());
                }
            }
        }
        catch (IOException e)
        {
            // if (!m_bStopped.booleanValue())
            // m_Logger.error("ERROR DURING SOCKET OPERATION.", e);
            EventsUtil.sendEvent(m_vListeners, m_parent, LogEvent.ERROR,
                    "Error during socket operation", e);
        }
        finally
        {
            try
            {
                if (m_Socket != null && !m_Socket.isClosed())
                {
                	m_Socket.shutdownInput();
                	m_Socket.shutdownOutput();
                    m_Socket.close();
                }
            }
            catch (IOException e)
            {
                // m_Logger.error("ERROR CLOSING SOCKET", e);
            }

            streamMgr.closeAll();
        }

    }

    /**
     * Tries to stop the thread.
     */
    public void stopNow()
    {
        StreamManager streamMgr = new StreamManager();
        try
        {
            synchronized (m_bStopped)
            {
                // If the socket is closed, the thread has finished...
                if (!m_Socket.isClosed())
                {
                    m_bStopped = Boolean.TRUE;

                    try
                    {
                        JNRPEResponse res = new JNRPEResponse();
                        res.setPacketVersion(PacketVersion.VERSION_2);
                        res.setResultCode(Status.UNKNOWN.intValue());
                        res.setMessage("Command execution timeout");
                        res.updateCRC();

                        OutputStream out = streamMgr.handle(m_Socket
                                .getOutputStream());
                        out.write(res.toByteArray());

                        // This is just to stop any socket operations...
                        m_Socket.close();
                    }
                    catch (IOException e)
                    {
                    }

                    // Let's try to interrupt all other operations...
                    if (this.isAlive())
                    {
                        this.interrupt();
                    }

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
