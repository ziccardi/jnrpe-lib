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
    private final String JNRPE_PLUGIN_PACKAGE_CLASS = "JNRPE-PluginPackage-Class";

    private final IPluginRepository pluginRepository;
    private final CommandRepository commandRepository;

    public JNRPEBundleTracker(final BundleContext context,
            final IPluginRepository pluginRepo,
            final CommandRepository commandRepo) {
        super(context, Bundle.ACTIVE | Bundle.STOPPING, null);
        pluginRepository = pluginRepo;
        commandRepository = commandRepo;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object addingBundle(final Bundle bundle, final BundleEvent event) {
        String pluginClassName =
                (String) bundle.getHeaders().get(JNRPE_PLUGIN_CLASS);

        String pluginPackageClassName =
                (String) bundle.getHeaders().get(JNRPE_PLUGIN_PACKAGE_CLASS);

        if (pluginClassName != null) {
            // The bundle is a plugin...
            Class<? extends IPluginInterface> clazz;
            try {
                clazz = bundle.loadClass(pluginClassName);
                PluginDefinition pd =
                        PluginRepositoryUtil.loadFromPluginAnnotation(clazz);
                pluginRepository.addPluginDefinition(pd);
            } catch (Exception e) {

            }
        } else {
            if (pluginPackageClassName != null) {
                // The bundle is a plugin package...
                try {
                    IJNRPEPluginPackage pp = (IJNRPEPluginPackage) bundle.loadClass(pluginPackageClassName).newInstance();

                    for (Class<? extends IPluginInterface> clazz : pp.getAllPlugins()) {
                        PluginDefinition pd =  PluginRepositoryUtil.loadFromPluginAnnotation(clazz);
                        pluginRepository.addPluginDefinition(pd);
                    }

                } catch (Exception e) {
                    // TODO Error loading package...
                    e.printStackTrace();
                }
            }
        }

        return bundle;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(final Bundle bundle) {
        String pluginClassName =
                (String) bundle.getHeaders().get(JNRPE_PLUGIN_CLASS);

        String pluginPackageClassName =
                (String) bundle.getHeaders().get(JNRPE_PLUGIN_PACKAGE_CLASS);

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
        } else {
            if (pluginPackageClassName != null) {
                // The bundle is a plugin package...
                try {
                    IJNRPEPluginPackage pp = (IJNRPEPluginPackage) bundle.loadClass(pluginPackageClassName).newInstance();

                    for (Class<? extends IPluginInterface> clazz : pp.getAllPlugins()) {
                        PluginDefinition pd =  PluginRepositoryUtil.loadFromPluginAnnotation(clazz);

                        // First remove all the commands using the plugin...
                        for (CommandDefinition cd : commandRepository.getAllCommandDefinition(pd.getName())) {
                            commandRepository.removeCommandDefinition(cd);
                        }

                        pluginRepository.removePluginDefinition(pd);
                    }

                } catch (Exception e) {
                    // TODO Error loading package...
                    e.printStackTrace();
                }
            }
        }
    }
}
