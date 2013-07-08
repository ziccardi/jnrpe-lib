package it.jnrpe.server.console;

import jline.console.ConsoleReader;
import it.jnrpe.JNRPE;

public abstract class ConsoleCommand implements IConsoleCommand {
    private final JNRPE jnrpeInstance;
    private final ConsoleReader console;
    
    public ConsoleCommand(ConsoleReader consoleReader, JNRPE jnrpe) {
        jnrpeInstance = jnrpe;
        console = consoleReader;
    }
    
    protected JNRPE getJNRPE() {
        return jnrpeInstance;
    }
    
    protected ConsoleReader getConsole() {
        return console;
    }
}
