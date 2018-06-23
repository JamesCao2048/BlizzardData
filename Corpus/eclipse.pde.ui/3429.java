/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     EclipseSource Corporation - ongoing enhancements
 *******************************************************************************/
package org.eclipse.pde.launching;

import java.io.File;
import java.net.URL;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.pde.core.plugin.*;
import org.eclipse.pde.internal.build.IPDEBuildConstants;
import org.eclipse.pde.internal.core.ClasspathHelper;
import org.eclipse.pde.internal.core.P2Utils;
import org.eclipse.pde.internal.core.util.CoreUtility;
import org.eclipse.pde.internal.launching.IPDEConstants;
import org.eclipse.pde.internal.launching.PDEMessages;
import org.eclipse.pde.internal.launching.launcher.*;

/**
 * A launch delegate for launching the Equinox framework
 * <p>
 * Clients may subclass and instantiate this class.
 * </p>
 * <p>
 * This class originally existed in 3.2 as
 * <code>org.eclipse.pde.ui.launcher.EquinoxLaunchConfiguration</code>.
 * </p>
 * @since 3.6
 */
public class EquinoxLaunchConfiguration extends AbstractPDELaunchConfiguration {

    // used to generate the dev classpath entries
    // key is bundle ID, value is a model
    protected Map<String, IPluginModelBase> fAllBundles;

    // key is a model, value is startLevel:autoStart
    private Map<IPluginModelBase, String> fModels;

    @Override
    public String[] getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
        ArrayList<String> programArgs = new ArrayList();
        //$NON-NLS-1$
        programArgs.add("-dev");
        //$NON-NLS-1$
        programArgs.add(ClasspathHelper.getDevEntriesProperties(getConfigDir(configuration).toString() + "/dev.properties", fAllBundles));
        saveConfigurationFile(configuration);
        //$NON-NLS-1$
        programArgs.add("-configuration");
        //$NON-NLS-1$
        programArgs.add("file:" + new Path(getConfigDir(configuration).getPath()).addTrailingSeparator().toString());
        String[] args = super.getProgramArguments(configuration);
        for (String arg : args) {
            programArgs.add(arg);
        }
        return programArgs.toArray(new String[programArgs.size()]);
    }

    private void saveConfigurationFile(ILaunchConfiguration configuration) throws CoreException {
        Properties properties = new Properties();
        //$NON-NLS-1$ //$NON-NLS-2$
        properties.setProperty("osgi.install.area", "file:" + TargetPlatform.getLocation());
        //$NON-NLS-1$ //$NON-NLS-2$
        properties.setProperty("osgi.configuration.cascaded", "false");
        //$NON-NLS-1$
        properties.put("osgi.framework", LaunchConfigurationHelper.getBundleURL(IPDEBuildConstants.BUNDLE_OSGI, fAllBundles, false));
        int start = configuration.getAttribute(IPDELauncherConstants.DEFAULT_START_LEVEL, 4);
        //$NON-NLS-1$
        properties.put("osgi.bundles.defaultStartLevel", Integer.toString(start));
        boolean autostart = configuration.getAttribute(IPDELauncherConstants.DEFAULT_AUTO_START, true);
        String bundles = null;
        if (fAllBundles.containsKey(IPDEBuildConstants.BUNDLE_SIMPLE_CONFIGURATOR)) {
            // If update configurator is set to its default start level, override it as simple/update configurators should not be autostarted together
            IPluginModelBase updateConfiguratorBundle = fAllBundles.get(IPDEBuildConstants.BUNDLE_UPDATE_CONFIGURATOR);
            if (updateConfiguratorBundle != null) {
                String startLevel = fModels.get(updateConfiguratorBundle);
                if (startLevel != null && startLevel.equals(BundleLauncherHelper.DEFAULT_UPDATE_CONFIGURATOR_START_LEVEL)) {
                    //$NON-NLS-1$
                    fModels.put(//$NON-NLS-1$
                    updateConfiguratorBundle, //$NON-NLS-1$
                    "4:false");
                }
            }
            // If simple configurator is being used, we need to write out the bundles.txt instead of writing out the list in the config.ini
            URL bundlesTxt = P2Utils.writeBundlesTxt(fModels, start, autostart, getConfigDir(configuration), null);
            if (bundlesTxt != null) {
                //$NON-NLS-1$
                properties.setProperty(//$NON-NLS-1$
                "org.eclipse.equinox.simpleconfigurator.configUrl", //$NON-NLS-1$
                bundlesTxt.toString());
                if (fAllBundles.get(IPDEBuildConstants.BUNDLE_UPDATE_CONFIGURATOR) != null) {
                    //$NON-NLS-1$ //$NON-NLS-2$
                    properties.setProperty("org.eclipse.update.reconcile", "false");
                }
            }
            StringBuffer buffer = new StringBuffer();
            IPluginModelBase model = fAllBundles.get(IPDEBuildConstants.BUNDLE_SIMPLE_CONFIGURATOR);
            buffer.append(LaunchConfigurationHelper.getBundleURL(model, true));
            appendStartData(buffer, fModels.get(model), autostart);
            bundles = buffer.toString();
        } else {
            bundles = getBundles(autostart);
        }
        if (bundles.length() > 0)
            //$NON-NLS-1$
            properties.put("osgi.bundles", bundles);
        if (//$NON-NLS-1$ //$NON-NLS-2$
        !"3.3".equals(configuration.getAttribute(IPDEConstants.LAUNCHER_PDE_VERSION, ""))) {
            //$NON-NLS-1$ //$NON-NLS-2$
            properties.put("eclipse.ignoreApp", "true");
            //$NON-NLS-1$ //$NON-NLS-2$
            properties.put("osgi.noShutdown", "true");
        }
        //$NON-NLS-1$
        LaunchConfigurationHelper.save(new File(getConfigDir(configuration), "config.ini"), properties);
    }

    private String getBundles(boolean defaultAuto) {
        StringBuffer buffer = new StringBuffer();
        Iterator<IPluginModelBase> iter = fModels.keySet().iterator();
        while (iter.hasNext()) {
            IPluginModelBase model = iter.next();
            String id = model.getPluginBase().getId();
            if (!IPDEBuildConstants.BUNDLE_OSGI.equals(id)) {
                if (buffer.length() > 0)
                    //$NON-NLS-1$
                    buffer.append(//$NON-NLS-1$
                    ",");
                buffer.append(LaunchConfigurationHelper.getBundleURL(model, true));
                // fragments must not be started or have a start level
                if (model instanceof IFragmentModel)
                    continue;
                String data = fModels.get(model).toString();
                appendStartData(buffer, data, defaultAuto);
            }
        }
        return buffer.toString();
    }

    /**
	 * Convenience method to parses the startData ("startLevel:autoStart"), convert it to the
	 * format expected by the OSGi bundles property, and append to a StringBuffer.
	 * @param buffer buffer to append the data to
	 * @param startData data to parse ("startLevel:autoStart")
	 * @param defaultAuto default auto start setting
	 */
    private void appendStartData(StringBuffer buffer, String startData, boolean defaultAuto) {
        int index = startData.indexOf(':');
        //$NON-NLS-1$
        String level = index > 0 ? startData.substring(0, index) : "default";
        //$NON-NLS-1$
        String auto = index > 0 && index < startData.length() - 1 ? startData.substring(index + 1) : "default";
        if (//$NON-NLS-1$
        "default".equals(auto))
            auto = Boolean.toString(defaultAuto);
        if (//$NON-NLS-1$ //$NON-NLS-2$
        !level.equals("default") || "true".equals(auto))
            //$NON-NLS-1$
            buffer.append("@");
        if (//$NON-NLS-1$
        !level.equals("default")) {
            buffer.append(level);
            if (//$NON-NLS-1$
            "true".equals(auto))
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                ":");
        }
        if (//$NON-NLS-1$
        "true".equals(auto)) {
            //$NON-NLS-1$
            buffer.append("start");
        }
    }

    @Override
    protected void preLaunchCheck(ILaunchConfiguration configuration, ILaunch launch, IProgressMonitor monitor) throws CoreException {
        fModels = BundleLauncherHelper.getMergedBundleMap(configuration, true);
        fAllBundles = new HashMap(fModels.size());
        Iterator<IPluginModelBase> iter = fModels.keySet().iterator();
        while (iter.hasNext()) {
            IPluginModelBase model = iter.next();
            fAllBundles.put(model.getPluginBase().getId(), model);
        }
        if (!fAllBundles.containsKey(IPDEBuildConstants.BUNDLE_OSGI)) {
            // implicitly add it
            IPluginModelBase model = PluginRegistry.findModel(IPDEBuildConstants.BUNDLE_OSGI);
            if (model != null) {
                //$NON-NLS-1$
                fModels.put(//$NON-NLS-1$
                model, //$NON-NLS-1$
                "default:default");
                fAllBundles.put(IPDEBuildConstants.BUNDLE_OSGI, model);
            } else {
                String message = PDEMessages.EquinoxLaunchConfiguration_oldTarget;
                throw new CoreException(LauncherUtils.createErrorStatus(message));
            }
        }
        super.preLaunchCheck(configuration, launch, monitor);
    }

    @Override
    protected void validatePluginDependencies(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
        OSGiValidationOperation op = new OSGiValidationOperation(configuration);
        LaunchPluginValidator.runValidationOperation(op, monitor);
    }

    /**
	 * Clears the configuration area if the area exists and that option is selected.
	 *
	 * @param configuration
	 * 			the launch configuration
	 * @param monitor
	 * 			the progress monitor
	 * @throws CoreException
	 * 			if unable to retrieve launch attribute values
	 * @since 3.3
	 */
    @Override
    protected void clear(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
        // clear config area, if necessary
        if (configuration.getAttribute(IPDELauncherConstants.CONFIG_CLEAR_AREA, false))
            CoreUtility.deleteContent(getConfigDir(configuration), monitor);
    }
}
