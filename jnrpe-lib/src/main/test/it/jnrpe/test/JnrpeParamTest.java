package it.jnrpe.test;

import it.jnrpe.JNRPE;
import it.jnrpe.ReturnValue;
import it.jnrpe.Status;
import it.jnrpe.commands.CommandDefinition;
import it.jnrpe.commands.CommandOption;
import it.jnrpe.commands.CommandRepository;
import it.jnrpe.events.IJNRPEEvent;
import it.jnrpe.events.IJNRPEEventListener;
import it.jnrpe.events.LogEvent;
import it.jnrpe.plugins.PluginDefinition;
import it.jnrpe.plugins.PluginOption;
import it.jnrpe.plugins.PluginRepository;
import it.jnrpe.utils.JNRPEClient;

import java.net.UnknownHostException;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class JnrpeParamTest {

	private String m_sEventType;
	private String m_sEventMessage;
	
	private JNRPE m_jnrpeServer;
	
	public JnrpeParamTest()
	{
		
	}
	
	@BeforeClass
	public void setUp() throws Exception
	{
		PluginRepository pr = new PluginRepository();
		CommandRepository cr = new CommandRepository();
		
		PluginDefinition pd = new PluginDefinition("TESTPLUGIN", "This is a test plugin", new TestPlugin())
			.addOption(new PluginOption()
						.setRequired(true)
						.setOption("t")
						.setLongOpt("type")
						.setHasArgs(true)
					);	
		pr.addPluginDefinition(pd);
		
		CommandDefinition cd = new CommandDefinition("TESTCOMMAND", "TESTPLUGIN")
			.addArgument(new CommandOption("type", "$ARG1$"));
		cr.addCommandDefinition(cd);
		
		m_jnrpeServer = new JNRPE(pr, cr);
		
		m_jnrpeServer.addEventListener(new IJNRPEEventListener() {
			
			public void receive(Object sender, IJNRPEEvent event) {
				System.out.println (">>>> " + event.getEventParams().get("MESSAGE"));
			}
		});
		
		m_jnrpeServer.addAcceptedHost("127.0.0.1");
		
		m_jnrpeServer.listen("127.0.0.1", 5667, false);
	}
	
	@AfterClass
	public void shutDown() 
	{
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (m_jnrpeServer != null)
			m_jnrpeServer.shutdown();
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void testNullProviders() {
		new JNRPE(null, null);
	}
	
	@Test
	public void testPortInUse() throws UnknownHostException
	{
		PluginRepository pr = new PluginRepository();
		CommandRepository cr = new CommandRepository();
		
		JNRPE instance1 = null, instance2 = null;
		try
		{
			instance1 = new JNRPE(pr, cr);
			instance1.listen("127.0.0.1", 5666);
			
			instance2 = new JNRPE(pr, cr);
			
			instance2.addEventListener(new IJNRPEEventListener() {
				
				public void receive(Object sender, IJNRPEEvent event) {
					if (event.getEventName().equals(LogEvent.ERROR.name()))
					{
						m_sEventType = event.getEventName();
						m_sEventMessage = (String) event.getEventParams().get("MESSAGE");
					}
				}
			});
			instance2.listen("127.0.0.1", 5666);

			Assert.assertNotNull(m_sEventMessage, "No event message received");
			Assert.assertNotNull(m_sEventType, "No event type received");
			Assert.assertEquals(m_sEventMessage.contains("Unable to listen on"), true, "'No Unable to listen on error' received");
		}
		finally
		{
			if (instance1 != null)
				instance1.shutdown();
			if (instance2 != null)
				instance2.shutdown();
		}
	}
	
	@Test
	public void testBadCommand() throws Exception
	{
		JNRPEClient client = new JNRPEClient("127.0.0.1", 5667);
		ReturnValue ret = client.sendCommand("BADCOMMAND", "-t", "pippo");

		Assert.assertEquals(ret.getStatus(), Status.UNKNOWN);
		Assert.assertEquals(ret.getMessage().contains("Bad command"), true);
	}
	
	@Test
	public void testCommandNullPointerException() throws Exception
	{
		JNRPEClient client = new JNRPEClient("127.0.0.1", 5667);
		ReturnValue ret = client.sendCommand("TESTCOMMAND", "NullPointerException");
		
		Assert.assertEquals(ret.getStatus(), Status.UNKNOWN);
		Assert.assertEquals(ret.getMessage().contains("Plugin execution error: Thrown NullPointerException as requested"), true);
	}
	
	@Test
	public void testCommandReturnNull() throws Exception
	{
		JNRPEClient client = new JNRPEClient("127.0.0.1", 5667);
		ReturnValue ret = client.sendCommand("TESTCOMMAND", "ReturnNull");
		Assert.assertEquals(ret.getStatus(), Status.UNKNOWN);
		Assert.assertEquals(ret.getMessage().contains("returned null"), true, "Expected 'Command [XXX] with args [YYY] returned null' but got : " + ret.getMessage());
	}
	
	@Test
	public void testThrowRuntimeException() throws Exception
	{
		JNRPEClient client = new JNRPEClient("127.0.0.1", 5667);
		ReturnValue ret = client.sendCommand("TESTCOMMAND", "ThrowRuntimeException");
		Assert.assertEquals(ret.getStatus(), Status.UNKNOWN);
		Assert.assertEquals(ret.getMessage().contains("Plugin execution error: Thrown RuntimeException as requested expected"), true);
	}
}
