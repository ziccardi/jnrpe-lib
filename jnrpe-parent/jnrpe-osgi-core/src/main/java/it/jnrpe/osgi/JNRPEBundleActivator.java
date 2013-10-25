package it.jnrpe.osgi;

import it.jnrpe.commands.CommandRepository;
import it.jnrpe.plugins.IPluginRepository;
import it.jnrpe.plugins.PluginRepository;

import java.util.Dictionary;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.util.tracker.BundleTracker;

public class JNRPEBundleActivator implements BundleActivator, ManagedService {

    private BundleTracker bundleTracker;

    private IPluginRepository pluginRepository;
    private CommandRepository commandRepository;

    /**
     * Automatically called by the OSGI layer when a new configuration is ready.
     */
    public void updated(final Dictionary properties) throws ConfigurationException {
        // TODO Auto-generated method stub
    }

    /**
     * Initializes the bundle.
     */
    public void start(final BundleContext context) throws Exception {
        pluginRepository = new PluginRepository();
        commandRepository = new CommandRepository();

        bundleTracker = new JNRPEBundleTracker(context, pluginRepository, commandRepository);
    }

    /**
     * Stops the JNRPE bundle.
     */
    public void stop(final BundleContext context) throws Exception {
        // TODO Auto-generated method stub

    }

}
