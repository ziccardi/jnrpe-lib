package it.jnrpe.server.console;

import java.io.IOException;

import jline.console.ConsoleReader;
import it.jnrpe.JNRPE;

public class ExitCommand extends ConsoleCommand {

    public final static String NAME="exit";
    
    public ExitCommand(ConsoleReader consoleReader, JNRPE jnrpe) {
        super(consoleReader, jnrpe);
    }

    public boolean execute(String[] args) throws Exception {
        getJNRPE().shutdown();
        return true;
    }

    public String getName() {
        return NAME;
    }

    public String getCommandLine() {
        return "";
    }

    public void printHelp() throws IOException {
        getConsole().println("Command Line : exit");
        getConsole().println("  Exits from the JNRPE console");
    }

}
