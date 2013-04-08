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
package it.jnrpe.plugin.jmx;

import it.jnrpe.ICommandLine;
import it.jnrpe.ReturnValue;
import it.jnrpe.Status;
import it.jnrpe.events.LogEvent;
import it.jnrpe.plugins.IPluginInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class CCheckJMX
    extends JMXQuery
    implements IPluginInterface
{
    
//    for(int i=0;i<args.length;i++){
//        String option = args[i];
//        if(option.equals("-help"))
//        {
//            printHelp(System.out);
//            System.exit(RETURN_UNKNOWN);
//        }else if(option.equals("-U")){
//            this.url = args[++i];
//        }else if(option.equals("-O")){
//            this.object = args[++i];
//        }else if(option.equals("-A")){
//            this.attribute = args[++i];
//        }else if(option.equals("-I")){
//            this.info_attribute = args[++i];
//        }else if(option.equals("-J")){
//            this.info_key = args[++i];
//        }else if(option.equals("-K")){
//            this.attribute_key = args[++i];
//        }else if(option.startsWith("-v")){
//            this.verbatim = option.length()-1;
//        }else if(option.equals("-w")){
//            this.warning = args[++i];
//        }else if(option.equals("-c")){
//            this.critical = args[++i];
//        }else if(option.equals("-username")) {
//            this.username = args[++i];
//        }else if(option.equals("-password")) {
//            this.password = args[++i];
//        }    
    
    
    public ReturnValue execute(ICommandLine cl)
    {
        if (cl.hasOption('U'))
            setUrl(cl.getOptionValue('U'));
        if (cl.hasOption('O'))
            setObject(cl.getOptionValue('O'));
        if (cl.hasOption('A'))
            setAttribute(cl.getOptionValue('A'));
        if (cl.hasOption('I'))
            setInfo_attribute(cl.getOptionValue('I'));
        if (cl.hasOption('J'))
            setInfo_key(cl.getOptionValue('J'));
        if (cl.hasOption('K'))
            setAttribute_key(cl.getOptionValue('K'));
        if (cl.hasOption('w'))
            setWarning(cl.getOptionValue('w'));
        if (cl.hasOption('c'))
            setCritical(cl.getOptionValue('c'));
        if (cl.hasOption("username"))
            setUsername(cl.getOptionValue("username"));
        if (cl.hasOption("password"))
            setPassword(cl.getOptionValue("password"));
        
        setVerbatim(2);
        //setVerbatim(4);
        
        try
        {
            connect();
            execute();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(bout);
            Status status = report(ps);
            ps.flush();
            ps.close();
            return new ReturnValue(status, new String(bout.toByteArray()));
        }
        catch(Exception ex)
        {
            sendEvent(LogEvent.WARNING, "An error has occurred during execution of the CHECK_JMX plugin : " + ex.getMessage(), ex);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(bout);
            Status status = report(ex, ps);
            ps.flush();
            ps.close();
            return new ReturnValue(status, new String(bout.toByteArray()));
        }

        finally
        {
            try {
                disconnect();
            } catch (IOException e) {
                sendEvent(LogEvent.WARNING, "An error has occurred during execution of the CHECK_JMX plugin : " + e.getMessage(), e);
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(bout);
                
                Status status = report(e, ps);
                ps.flush();
                ps.close();
                return new ReturnValue(status, new String(bout.toByteArray()));
            }
        }
        
        // Build command line for JMXQuery
    }
}
