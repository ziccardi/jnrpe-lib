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
package it.jnrpe.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Container class for command definition configuration.
 *
 * @author Massimiliano Ziccardi
 */
public final class CommandDefinition
{
    /**
     * The command name.
     */
    private final String m_sName;
    /**
     * The plugin name.
     */
    private final String m_sPluginName;

    /**
     * The raw list of arguments.
     */
    private String m_sArgs = null;

    /**
     * The list of options related to this command.
     */
    private List<CommandOption> m_vOptions = new ArrayList<CommandOption>();

    /**
     * Builds and initializes the command definition.
     *
     * @param sName
     *            The command name
     * @param sPluginName
     *            The plugin associated with this command
     */
    public CommandDefinition(final String sName, final String sPluginName)
    {
        m_sName = sName;
        m_sPluginName = sPluginName;
    }

    /**
     * Sets the raw arguments of this command.
     *
     * @param sArgs
     *            The command line
     */
    public void setArgs(final String sArgs)
    {
        m_sArgs = sArgs;
    }

    /**
     * Returns the command name.
     *
     * @return The command name
     */
    public String getName()
    {
        return m_sName;
    }

    /**
     * Returns the name of the plugin associated with this command.
     *
     * @return The name of the plugin associated with this command
     */
    public String getPluginName()
    {
        return m_sPluginName;
    }

    /**
     * The raw command line of this command.
     *
     * @return The raw command line
     */
    public String getArgs()
    {
        return m_sArgs;
    }

    /**
     * Utility function used to quote the characters.
     *
     * @param s
     *            The string to be elaborated
     * @return The string with the quoted characters
     */
    private static String quote(final String s)
    {
        if (s.indexOf(' ') != -1)
        {
            return "\"" + s + "\"";
        }
        return s;
    }

    /**
     * Merges the command line definition read from the server config file with.
     * the values received from check_nrpe and produces a clean command line.
     *
     * @return a parsable command line
     */
    public String[] getCommandLine()
    {
        String[] vsRes = null;
        String[] args = m_sArgs != null ? split(m_sArgs) : new String[0];
        List<String> vArgs = new ArrayList<String>();

        int iStartIndex = 0;

        for (CommandOption opt : m_vOptions)
        {
            String sArgName = opt.getName();
            String sArgVal = opt.getValue();

            vArgs.add((sArgName.length() == 1 ? "-" : "--") + sArgName);

            if (sArgVal != null)
            {
                vArgs.add(quote(sArgVal));
            }
        }

        vsRes = new String[args.length + vArgs.size()];

        for (String sArg : vArgs)
        {
            vsRes[iStartIndex++] = sArg;
        }

        // vsRes = new String[args.length + m_vArguments.size()];
        System.arraycopy(args, 0, vsRes, iStartIndex, args.length);

        return vsRes;
    }

    /**
     * This method splits the command line separating each command and
     * each argument. 
     *
     * @param sCommandLine The raw command line
     * @return the splitted command line.
     */
    private static String[] split(final String sCommandLine)
    {
    	String regex = "[\"|']([^\"']*)[\"|']|([^ ]+)";

    	List<String> res = new ArrayList<String>();
    	
	    Matcher m = Pattern.compile(regex).matcher(sCommandLine);
	    while (m.find()) {
	        if (m.group(1) != null) {
	        	// Quoted
	            res.add(m.group(1));
	        } else {
	        	res.add(m.group(2));
	        }
	    }
    	
	    return res.toArray(new String[0]);
    	
//        char[] vc = sCommandLine.trim().toCharArray();
//        char[] vcTmp = new char[vc.length];
//
//        boolean bOpenQuote = false;
//        List<String> vArgs = new ArrayList<String>();
//        int iLen = 0;
//
//        for (int i = 0; i < vc.length; i++)
//        {
//            if (vc[i] == '\'' || vc[i] == '\"')
//            {
//                bOpenQuote = !bOpenQuote;
//                continue;
//            }
//
//            if (vc[i] == ' ' && !bOpenQuote)
//            {
//                vArgs.add(new String(vcTmp, 0, iLen));
//                iLen = 0;
//                vcTmp = new char[vc.length];
//                continue;
//            }
//
//            vcTmp[iLen++] = vc[i];
//        }
//
//        if (iLen != 0)
//        {
//            vArgs.add(new String(vcTmp, 0, iLen));
//        }
//
//        String[] vsRes = new String[vArgs.size()];
//
//        int i = 0;
//        for (String s : vArgs)
//        {
//            vsRes[i++] = s;
//        }
//        //
//        // for (Iterator iter = vArgs.iterator(); iter.hasNext(); )
//        // vsRes[i++] = (String) iter.next();
//
//        return vsRes;

    }

    /**
     * Adds an option to the command definition.
     *
     * @param arg
     *            The option to be added
     * @return This object.
     */
    public CommandDefinition addArgument(final CommandOption arg)
    {
        m_vOptions.add(arg);
        return this;
    }
}
