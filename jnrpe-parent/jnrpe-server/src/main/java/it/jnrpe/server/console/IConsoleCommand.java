package it.jnrpe.server.console;

import java.io.IOException;

public interface IConsoleCommand {
    public boolean execute(String[] args) throws Exception;
    public String getName();
    public String getCommandLine();
    public void printHelp() throws IOException;
}
