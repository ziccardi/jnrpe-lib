/*
 * Copyright (c) 2013 Massimiliano Ziccardi
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
package it.jnrpe.server.console;

import it.jnrpe.JNRPE;
import it.jnrpe.ReturnValue;
import it.jnrpe.plugins.PluginOption;
import it.jnrpe.plugins.PluginProxy;
import it.jnrpe.plugins.PluginRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import jline.console.ConsoleReader;

import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.util.HelpFormatter;
public class PluginCommand extends ConsoleCommand {

    public final static String NAME="plugin:";
    
    private final String pluginName;
    private final PluginRepository pluginRepository;
    
    public PluginCommand(ConsoleReader consoleReader, JNRPE jnrpe, String pluginName, PluginRepository pr) {
        super(consoleReader, jnrpe);
        this.pluginName = pluginName;
        this.pluginRepository = pr;
    }

    public boolean execute(String[] args) throws Exception {
        PluginProxy plugin = (PluginProxy) pluginRepository.getPlugin(pluginName);

        ReturnValue retVal = plugin.execute(args);
        
        getConsole().println(retVal.getMessage());
        return false;
    }

    public String getName() {
        return NAME + pluginName;
    }

    private Group getGroup() {
        PluginProxy pp = (PluginProxy) pluginRepository.getPlugin(pluginName);
        GroupBuilder gBuilder = new GroupBuilder();
        
        for (PluginOption po : pp.getOptions()) {
            gBuilder = gBuilder.withOption(toOption(po));
        }
        
       return gBuilder.create();
    }
    
    private Option toOption(PluginOption po) {
        DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();

        oBuilder.withShortName(po.getOption()).withDescription(po.getDescription())
                .withRequired(po.getRequired().equalsIgnoreCase("true"));

        if (po.getLongOpt() != null) {
            oBuilder.withLongName(po.getLongOpt());
        }

        if (po.hasArgs()) {
            ArgumentBuilder aBuilder = new ArgumentBuilder();

            if (po.getArgName() != null) {
                aBuilder.withName(po.getArgName());
            }

            if (po.getArgsOptional()) {
                aBuilder.withMinimum(0);
            }

            if (po.getArgsCount() != null) {
                aBuilder.withMaximum(po.getArgsCount());
            } else {
                aBuilder.withMaximum(1);
            }

            if (po.getValueSeparator() != null
                    && po.getValueSeparator().length() != 0) {
                aBuilder.withInitialSeparator(po.getValueSeparator().charAt(0));
                aBuilder.withSubsequentSeparator(po.getValueSeparator().charAt(0));
            }
            oBuilder.withArgument(aBuilder.create());
        }

        return oBuilder.create();
    }
    
    public String getCommandLine() {
        PluginProxy pp = (PluginProxy) pluginRepository.getPlugin(pluginName);
        GroupBuilder gBuilder = new GroupBuilder();
        
        for (PluginOption po : pp.getOptions()) {
            gBuilder = gBuilder.withOption(toOption(po));
        }
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        Group g = gBuilder.create();
        HelpFormatter hf = new HelpFormatter(null, null, null, getConsole().getTerminal().getWidth());
        hf.setGroup(g);
        hf.setPrintWriter(new PrintWriter(bout));
        hf.printUsage();
        
        String usage = new String(bout.toByteArray());
        
        String[] lines = usage.split("\\n");
        
        StringBuffer res = new StringBuffer();
        
        for (int i = 1; i < lines.length; i++) {
            res.append(lines[i]);
        }
        
        return res.toString();
    }

    public void printHelp() throws IOException {
        PluginProxy pp = (PluginProxy) pluginRepository.getPlugin(pluginName);
        GroupBuilder gBuilder = new GroupBuilder();

        for (PluginOption po : pp.getOptions()) {
            gBuilder = gBuilder.withOption(toOption(po));
        }
        Group g = gBuilder.create();
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        HelpFormatter hf = new HelpFormatter(null, null, null, getConsole().getTerminal().getWidth());
        hf.setGroup(g);
        
        PrintWriter pw = new PrintWriter(bout);
        hf.setPrintWriter(pw);
        hf.printHelp();

        pw.flush();
        
        getConsole().print("Command Line: " + getName() + " " + getCommandLine());
        getConsole().println();
        getConsole().println(new String(bout.toByteArray()));
        
    }

}
