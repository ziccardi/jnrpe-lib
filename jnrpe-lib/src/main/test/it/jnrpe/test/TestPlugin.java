package it.jnrpe.test;

import it.jnrpe.ICommandLine;
import it.jnrpe.ReturnValue;
import it.jnrpe.plugins.PluginBase;

public class TestPlugin extends PluginBase{

	
	public TestPlugin()
	{
//		m_testType = testType;
	}
	
	public ReturnValue execute(ICommandLine cl) {
		
		System.out.println ("*********************************" + cl.getOptionValue("type"));
		
		if (cl.getOptionValue("t").equals("NullPointerException"))
			throw new NullPointerException("Thrown NullPointerException as requested");
		
		if (cl.getOptionValue("type").equals("ReturnNull"))
			return null;
		
		if (cl.getOptionValue("type").equals("ThrowRuntimeException"))
			throw new RuntimeException("Thrown RuntimeException as requested");
//<>		switch (m_testType)
//		{
//		case THROW_NULL:
//			throw new NullPointerException("Thrown NullPointerException as requested");
//		}
		
		return null;
	}

}
