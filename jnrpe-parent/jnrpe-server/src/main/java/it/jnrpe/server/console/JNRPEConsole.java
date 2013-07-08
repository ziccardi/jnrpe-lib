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
                try {
                    exit = CommandExecutor.getInstance(console, jnrpeInstance, pluginRepository, commandRepository).executeCommand(commandLine);
                } catch (Exception e) {
                    console.println(e.getMessage());
                }
            }
            console.shutdown();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
