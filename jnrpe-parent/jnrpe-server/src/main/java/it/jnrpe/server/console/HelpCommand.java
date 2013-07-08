package it.jnrpe.server.console;

import java.io.IOException;
import java.util.Map;

import jline.console.ConsoleReader;
import it.jnrpe.JNRPE;

public class HelpCommand extends ConsoleCommand {

    public final static String NAME="help";
    private final Map<String, IConsoleCommand> commandMap;
    
    public HelpCommand(ConsoleReader consoleReader, JNRPE jnrpe, Map<String, IConsoleCommand> commands) {
        super(consoleReader, jnrpe);
        commandMap = commands;
    }

    public boolean execute(String[] args) throws Exception {
        if (args == null | args.length == 0) {
            getConsole().println("Available commands are : ");
            for (IConsoleCommand command : commandMap.values()) {
                getConsole().println(command.getName());
            }
            
            return false;
        }
        if (args.length != 1) {
            getConsole().println("Only one parameter can be specified for the help command");
            return false;
        }
        
        IConsoleCommand command = commandMap.get(args[0]);
        if (command == null) {
            getConsole().println("Unknown command : '" + args[0] + "'");
            return false;
        }
        
        command.printHelp();
        
        return false;
    }

    public String getName() {
        return NAME;
    }

    public String getCommandLine() {
        return "[COMMAND NAME]";
    }

    public void printHelp() throws IOException {
        getConsole().println("Command Line: help " + getCommandLine());
        getConsole().println("   Without parameters shows the list of available commands");
        getConsole().println("   otherwise prints some help about the specified command");
    }

}
