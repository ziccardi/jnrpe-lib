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

import static java.util.Arrays.asList;
import it.jnrpe.ReturnValue;
import it.jnrpe.Status;
import it.jnrpe.net.JNRPERequest;
import it.jnrpe.net.JNRPEResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 *  This class represent a simple JNRPE client that can be used to invoke
 *  commands installed inside JNRPE by code.
 *  It is the JAVA equivalent of check_nrpe.
 *  
 *  @author Massimiliano Ziccardi
 */
public class JNRPEClient {
	private final String m_sServerIP;
	private final int m_iServerPort;
	private final boolean m_bSSL;
	private int m_iTimeout = 10;
	
	/**
	 * Instantiates a JNRPE client.
	 * @param sJNRPEServerIP The IP where the JNRPE is installed
	 * @param iJNRPEServerPort The port where the JNRPE server listens
	 */
	public JNRPEClient(final String sJNRPEServerIP, final int iJNRPEServerPort, final boolean bSSL) {
		m_sServerIP = sJNRPEServerIP;
		m_iServerPort = iJNRPEServerPort;
		m_bSSL = bSSL;
	}

	/**
	 * Creates a custom TrustManager that trusts any certificate
	 * @return The custom trustmanager
	 */
	private TrustManager getTrustManager()
	{
		// Trust all certificates
		return new X509TrustManager() {
			
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			
			public void checkServerTrusted(X509Certificate[] chain, String authType)
					throws CertificateException {
				
			}
			
			public void checkClientTrusted(X509Certificate[] chain, String authType)
					throws CertificateException {
			}
		};
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
		SocketFactory socketFactory = null;
		
		Socket s = null;
		try
		{
			if (!m_bSSL)
			{
				socketFactory = SocketFactory.getDefault();
			}
			else
			{
				SSLContext sslContext = SSLContext.getInstance("SSL");
		        sslContext.init(null, null,
		                    	new java.security.SecureRandom());

		        sslContext.init(null, new TrustManager[] {getTrustManager()}, new SecureRandom());
				
				socketFactory = sslContext.getSocketFactory();
			}
			
			s = socketFactory.createSocket();
			s.setSoTimeout(m_iTimeout * 1000);
			s.connect(new InetSocketAddress(m_sServerIP, m_iServerPort));
			JNRPERequest req = new JNRPERequest(sCommandName, arguments);
	
			s.getOutputStream().write(req.toByteArray());
	
			InputStream in = s.getInputStream();
			JNRPEResponse res = new JNRPEResponse(in);
	
			return new ReturnValue(Status.fromIntValue(res.getResultCode()), res.getStringMessage());
		}
		catch (Exception e)
		{
			//e.printStackTrace();
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

	/**
	 * Sets the connection timeout in seconds
	 * @param iTimeout The new connection timeout. Default : 10
	 */
	public void setTimeout(int iTimeout)
	{
		m_iTimeout = iTimeout;
	}
	
	/**
	 * Returns the currently configured connection timeout in seconds
	 * @return The connection timeout
	 */
	public int getTimeout()
	{
		return m_iTimeout;
	}
	
//	private static void printSourceMessage(Throwable e)
//	{
//		if (e.getCause() == null)
//			System.out.println (e.getClass().getName() + " : " + e.getMessage());
//		else
//			printSourceMessage(e.getCause());
//	}
	
	public static void main(String[] args)  throws Exception {
		OptionParser parser = new OptionParser();
		parser.acceptsAll(asList("n", "nossl"), "Do no use SSL");
		parser.acceptsAll(asList("u", "unknown"), "Make socket timeouts return an UNKNOWN state instead of CRITICAL");
		parser.acceptsAll(asList("H", "host"), "The address of the host running the JNRPE/NRPE daemon").withRequiredArg().describedAs("host").required();
		parser.acceptsAll(asList("p", "port"), "The port on which the daemon is running (default=5666)").withRequiredArg().ofType(Integer.class).describedAs("port").defaultsTo(5666);
		parser.acceptsAll(asList("t", "timeout"), "Number of seconds before connection times out (default=10)").withRequiredArg().describedAs("timeout").ofType(Integer.class).defaultsTo(10);
		parser.acceptsAll(asList("c", "command"), "The name of the command that the remote daemon should run").withRequiredArg().required().describedAs("command name");
		parser.acceptsAll(asList("a", "arglist"), "Optional arguments that should be passed to the command.  Multiple arguments should be separated by an exlamation mark ('!').  If provided, this must be the last option supplied on the command line.").withRequiredArg().describedAs("arglist");
		parser.acceptsAll(asList("h", "help"), "Shows this help").forHelp();
		
		boolean timeoutAsUnknown = false;
		
		try
		{
			OptionSet os = parser.parse(args);
			
			timeoutAsUnknown = os.has("unknown");
			
			String sHost = (String) os.valueOf("host");
			int port = (Integer) os.valueOf("port");
			String sCommand = (String) os.valueOf("command");
			String sArgs = (String) os.valueOf("arglist");
			
			JNRPEClient client = new JNRPEClient(sHost, port, !os.has("nossl"));
			client.setTimeout((Integer) os.valueOf("timeout"));
			ReturnValue ret = client.sendCommand(sCommand, sArgs);
			
			System.out.println (ret.getMessage());
			System.exit(ret.getStatus().intValue());
		}
		catch (JNRPEClientException exc)
		{
			Status returnStatus = null;
			
			if (timeoutAsUnknown && exc.getCause() != null && exc.getCause() instanceof SocketTimeoutException)
				returnStatus = Status.UNKNOWN;
			else
				returnStatus = Status.CRITICAL;
			
//			printSourceMessage(exc);
			System.out.println(exc.getMessage());
			System.exit(returnStatus.intValue());
		}
		catch (OptionException oe)
		{
			System.out.println ();
			System.out.println ("Error : " + oe.getMessage());
			System.out.println ();
			
			printBanner();
			
			System.out.println ("Usage: jcheck_nrpe -H <host> [-n] [-u] [-p <port>] [-t <timeout>] [-c <command>] [-a <arglist...>]");
			System.out.println ();
			try {
				parser.printHelpOn(System.out);
				System.exit(Status.UNKNOWN.intValue());
			} catch (IOException e) {
				// Should never happen...
				e.printStackTrace();
			}
			
		}

//		parser.printHelpOn(System.out);
	}

	private static void printBanner() {
		System.out.println ("NRPE Plugin for Nagios");
		System.out.println ("Copyright (c) 2013 Massimiliano Ziccardi (massimiliano.ziccardi@gmail.com)");
		System.out.println ("Version: " + JNRPEClient.class.getPackage().getImplementationVersion());
		System.out.println ();
		
	}
	
}
