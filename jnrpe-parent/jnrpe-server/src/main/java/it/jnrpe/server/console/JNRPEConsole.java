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

import java.io.IOException;

import jline.console.ConsoleReader;
import it.jnrpe.JNRPE;
import it.jnrpe.commands.CommandRepository;
import it.jnrpe.plugins.PluginRepository;

public class JNRPEConsole {
    
    private final JNRPE jnrpeInstance;
    private final PluginRepository pluginRepository;
    private final CommandRepository commandRepository;
    
    public JNRPEConsole(final JNRPE jnrpe, final PluginRepository pr, final CommandRepository cr) {
        jnrpeInstance = jnrpe;
        pluginRepository = pr;
        commandRepository = cr;
    }
    
    public void start() {
        try {
            boolean exit = false;
            ConsoleReader console = new ConsoleReader();
            console.setPrompt("JNRPE > ");
            while (!exit) {
                String commandLine = console.readLine();
                if (commandLine == null || commandLine.trim().length() == 0) {
                    continue;
                }
                try {
                    exit = CommandExecutor.getInstance(console, jnrpeInstance, pluginRepository, commandRepository).executeCommand(commandLine);
                } catch (Exception e) {
                    console.println(""+ e.getMessage());
                }
            }
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
