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
package it.jnrpe.commands;

import java.util.ArrayList;
import java.util.List;

/**
 * Container class for command definition configuration
 * 
 * @author Massimiliano Ziccardi
 */
public class CommandDefinition
{
	private final String m_sName;
	private final String m_sPluginName;
	private String m_sArgs = null;

    private List<CommandOption> m_vOptions = new ArrayList<CommandOption>();
    
	public CommandDefinition(String sName, String sPluginName)
	{
	    m_sName = sName;
	    m_sPluginName = sPluginName;
	}
	
	public void setArgs(String sArgs)
	{
		m_sArgs = sArgs;
	}

	public String getName()
	{
		return m_sName;
	}

	public String getPluginName()
	{
		return m_sPluginName;
	}
	
	public String getArgs()
    {
	    return m_sArgs;
    }
    
    private static String quote(String s)
    {
        if (s.indexOf(' ') != -1)
            return "\"" + s + "\"";
        return s;
    }
    
    /**
     * Merges the command line definition read from the server config file
     * with the values received from check_nrpe and produces a clean command line.
     * 
     * @return
     */
    public String[] getCommandLine()
	{
        String[] vsRes = null;
        String[] args = m_sArgs != null ? split(m_sArgs) : new String[0];
        List<String> vArgs = new ArrayList<String>();
        
        int iStartIndex = 0;

        for(CommandOption opt : m_vOptions)
        {
            String sArgName = opt.getName();
            String sArgVal = opt.getValue();
          
            vArgs.add((sArgName.length() == 1 ? "-" : "--") + sArgName);
            
            if (sArgVal != null)
                vArgs.add(quote(sArgVal));
        }
        
        vsRes = new String[args.length + vArgs.size()];
        
        for (String sArg : vArgs)
        {
            vsRes[iStartIndex++] = sArg;
        }
        
        //vsRes = new String[args.length + m_vArguments.size()];
        System.arraycopy(args, 0, vsRes, iStartIndex, args.length);
        
        return vsRes;
	}

    /**
     * This method splits the command line.
     * This release does not handle correctly the ' and the " character
     * @param sCommandLine
     * @return
     */
    private static String[] split(String sCommandLine)
    {
        char[] vc = sCommandLine.trim().toCharArray();
        char[] vcTmp = new char[vc.length];
        
        boolean bOpenQuote = false;
        List<String> vArgs = new ArrayList<String>();
        int iLen = 0;
        
        for (int i = 0; i < vc.length; i++)
        {
            if (vc[i] == '\'' || vc[i] =='\"')
            {
                bOpenQuote = !bOpenQuote;
                continue;
            }
            
            if (vc[i] == ' ' && !bOpenQuote)
            {
                vArgs.add(new String(vcTmp, 0, iLen));
                iLen = 0;
                vcTmp = new char[vc.length];
                continue;
            }
            
            vcTmp[iLen++] = vc[i];
        }
        
        if (iLen != 0)
            vArgs.add(new String(vcTmp, 0, iLen));
        
        String[] vsRes = new String[vArgs.size()];
        
        int i = 0;
        for (String s: vArgs)
            vsRes[i++] = s;
//        
//        for (Iterator iter = vArgs.iterator(); iter.hasNext(); )
//            vsRes[i++] = (String) iter.next();
        
        return vsRes;
        
    }
    
    public CommandDefinition addArgument(CommandOption arg)
    {
        m_vOptions.add(arg);
        return this;
    }
}
