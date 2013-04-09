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
package it.jnrpe.server.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.GroupBuilder;

/**
 * This object exists for the sole purpose of being used
 * by the Digester when parsing the XML file used to describe
 * the command line options.
 * 
 * @author Massimiliano Ziccardi
 *
 */
public class XMLOptions
{
	/**
	 * The list of the {@link XMLOption} objects contained by this
	 * definition.
	 */
    private List<XMLOption> m_vOptions = new ArrayList<XMLOption>();
	
	/**
	 * Public constructor used by the digester
	 */
    public XMLOptions()
	{
		
	}
	
    /**
     * Used by the digester to add {@link XMLOption} objects
     * @param opt The option to be added.
     */
	public void addOption(XMLOption opt)
	{
		m_vOptions.add(opt);
	}

	/**
	 * Creates a command line definition as required by
	 * the commons cli library.
	 * 
	 * @return The commons cli command line definition.
	 */
	public Option toOptions()
	{
		GroupBuilder gBuilder = new GroupBuilder();
	    //Options opts = new Options();
		
		for (XMLOption opt: m_vOptions)
			gBuilder = gBuilder.withOption(opt.toOption());
		
		return gBuilder.create();
	}
    
    /**
     * Returns a collection of the {@link XMLOption} objects 
     * contained inside this object.
     * 
     * The collection contains the original objects.
     * 
     * @return The collection of {@link XMLOption}
     */
	public Collection<XMLOption> getOptions()
    {
        return m_vOptions;
    }
}
