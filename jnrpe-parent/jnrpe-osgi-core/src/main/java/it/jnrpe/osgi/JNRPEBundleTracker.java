package it.jnrpe.osgi;

import it.jnrpe.commands.CommandDefinition;
import it.jnrpe.commands.CommandRepository;
import it.jnrpe.plugins.IPluginInterface;
import it.jnrpe.plugins.IPluginRepository;
import it.jnrpe.plugins.PluginDefinition;
import it.jnrpe.utils.PluginRepositoryUtil;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;

public class JNRPEBundleTracker extends BundleTracker {

    private final String JNRPE_PLUGIN_CLASS = "JNRPE-Plugin-Class";

    private final IPluginRepository pluginRepository;
    private final CommandRepository commandRepository;

    public JNRPEBundleTracker(final BundleContext context,
            final IPluginRepository pluginRepo,
            final CommandRepository commandRepo) {
        super(context, Bundle.ACTIVE | Bundle.STOPPING, null);
        pluginRepository = pluginRepo;
        commandRepository = commandRepo;
    }

    @Override
    public Object addingBundle(final Bundle bundle, final BundleEvent event) {
        String pluginClassName =
                (String) bundle.getHeaders().get(JNRPE_PLUGIN_CLASS);

        if (pluginClassName != null) {
            Class<? extends IPluginInterface> clazz;
            try {
                clazz = bundle.loadClass(pluginClassName);
                PluginDefinition pd =
                        PluginRepositoryUtil.loadFromPluginAnnotation(clazz);
                pluginRepository.addPluginDefinition(pd);
            } catch (Exception e) {

            }
        }

        return bundle;
    }

    @Override
    public void remove(final Bundle bundle) {
        String pluginClassName =
                (String) bundle.getHeaders().get(JNRPE_PLUGIN_CLASS);

        if (pluginClassName != null) {
            Class<? extends IPluginInterface> clazz;
            try {
                clazz = bundle.loadClass(pluginClassName);

                // TODO : this method is not very efficient. It should be not a
                // problem, but
                // a better way should be followed
                PluginDefinition pd =
                        PluginRepositoryUtil.loadFromPluginAnnotation(clazz);

                // First remove all the commands using the plugin...
                for (CommandDefinition cd : commandRepository.getAllCommandDefinition(pd.getName())) {
                    commandRepository.removeCommandDefinition(cd);
                }

                // Now we can remove the plugin...
                pluginRepository.removePluginDefinition(pd);
            } catch (Exception e) {

            }
        }
    }
}
