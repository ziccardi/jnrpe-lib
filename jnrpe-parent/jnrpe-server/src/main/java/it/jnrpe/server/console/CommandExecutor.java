package it.jnrpe.server.console;

import it.jnrpe.JNRPE;
import it.jnrpe.commands.CommandRepository;
import it.jnrpe.plugins.PluginDefinition;
import it.jnrpe.plugins.PluginRepository;

import java.util.Map;
import java.util.TreeMap;

import jline.console.ConsoleReader;

import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;

public class CommandExecutor {
    
    private Map<String, IConsoleCommand> commandMap = new TreeMap<String, IConsoleCommand>();
    
    private static CommandExecutor instance = null;

    
    public static CommandExecutor getInstance(ConsoleReader consoleReader, JNRPE jnrpe, PluginRepository pluginRepository, CommandRepository commandRepository) {
        if (instance == null) {
            synchronized (CommandExecutor.class) {
                if (instance == null) {
                    instance = new CommandExecutor();
                    instance.commandMap.put(ExitCommand.NAME, new ExitCommand(consoleReader, jnrpe));
                    instance.commandMap.put(HelpCommand.NAME, new HelpCommand(consoleReader, jnrpe, instance.commandMap));
                    
                    for (PluginDefinition pd : pluginRepository.getAllPlugins()) {
                        instance.commandMap.put(PluginCommand.NAME + pd.getName().toLowerCase(), new PluginCommand(consoleReader, jnrpe, pd.getName(), pluginRepository));
                    }
                }
            }
        }
        
        return instance;
    }
    
    private IConsoleCommand getCommand(String commandName) {
        return commandMap.get(commandName.toLowerCase());
    }
    
    public boolean executeCommand(String commandLine) throws Exception {
        StrTokenizer strtok =
                new StrTokenizer(commandLine, StrMatcher.charMatcher(' '),
                        StrMatcher.quoteMatcher());
        String[] tokensAry = strtok.getTokenArray();
        String commandName = tokensAry[0];
        String[] params;
        if (tokensAry.length == 1) {
            params = new String[0];
        } else {
            params = new String[tokensAry.length - 1];
            System.arraycopy(tokensAry, 1, params, 0, params.length);
        }
        
        IConsoleCommand command = getCommand(commandName);
        if (command != null) {
            return command.execute(params);
        } else {
            throw new UnknownCommandException("Unknown command :'" + commandName + "'");
        }
    }
}
