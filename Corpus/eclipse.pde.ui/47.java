/*******************************************************************************
 *  Copyright (c) 2007, 2013 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import org.eclipse.core.runtime.*;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.ifeature.IFeature;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.update.configurator.ConfiguratorUtils;
import org.eclipse.update.configurator.IPlatformConfiguration;

@SuppressWarnings("deprecation")
public class UpdateManagerHelper {

    private static class LocalSite {

        private ArrayList<IPluginModelBase> fPlugins;

        private IPath fPath;

        public  LocalSite(IPath path) {
            if (path.getDevice() != null)
                fPath = path.setDevice(path.getDevice().toUpperCase(Locale.ENGLISH));
            else
                fPath = path;
            fPlugins = new ArrayList();
        }

        public IPath getPath() {
            return fPath;
        }

        public URL getURL() throws MalformedURLException {
            //$NON-NLS-1$
            return new URL("file:" + fPath.removeTrailingSeparator());
        }

        public void add(IPluginModelBase model) {
            fPlugins.add(model);
        }

        public String[] getRelativePluginList() {
            String[] list = new String[fPlugins.size()];
            for (int i = 0; i < fPlugins.size(); i++) {
                IPluginModelBase model = fPlugins.get(i);
                IPath location = new Path(model.getInstallLocation());
                // defect 37319
                if (location.segmentCount() > 2)
                    location = location.removeFirstSegments(location.segmentCount() - 2);
                //31489 - entry must be relative
                list[i] = location.setDevice(null).makeRelative().toString();
            }
            return list;
        }
    }

    public static void createPlatformConfiguration(File configLocation, IPluginModelBase[] models, IPluginModelBase brandingPlugin) throws CoreException {
        try {
            IPlatformConfiguration platformConfiguration = ConfiguratorUtils.getPlatformConfiguration(null);
            // Compute local sites
            ArrayList<LocalSite> sites = new ArrayList();
            for (int i = 0; i < models.length; i++) {
                IPath path = new Path(models[i].getInstallLocation()).removeLastSegments(2);
                addToSite(path, models[i], sites);
            }
            createConfigurationEntries(platformConfiguration, sites);
            if (brandingPlugin != null)
                createFeatureEntries(platformConfiguration, brandingPlugin);
            platformConfiguration.refresh();
            //$NON-NLS-1$
            platformConfiguration.save(new URL("file:" + configLocation.getPath()));
        } catch (Exception e) {
            String message = e.getMessage();
            if (message == null || message.length() == 0)
                message = PDECoreMessages.TargetPlatform_exceptionThrown;
            throw new CoreException(new Status(IStatus.ERROR, PDECore.PLUGIN_ID, IStatus.ERROR, message, e));
        }
    }

    private static void addToSite(IPath path, IPluginModelBase model, ArrayList<LocalSite> sites) {
        if (path.getDevice() != null)
            path = path.setDevice(path.getDevice().toUpperCase(Locale.ENGLISH));
        for (int i = 0; i < sites.size(); i++) {
            LocalSite localSite = sites.get(i);
            if (localSite.getPath().equals(path)) {
                localSite.add(model);
                return;
            }
        }
        // First time - add site
        LocalSite localSite = new LocalSite(path);
        localSite.add(model);
        sites.add(localSite);
    }

    private static void createConfigurationEntries(IPlatformConfiguration config, ArrayList<LocalSite> sites) throws MalformedURLException {
        for (int i = 0; i < sites.size(); i++) {
            LocalSite localSite = sites.get(i);
            String[] plugins = localSite.getRelativePluginList();
            int policy = IPlatformConfiguration.ISitePolicy.USER_INCLUDE;
            IPlatformConfiguration.ISitePolicy sitePolicy = config.createSitePolicy(policy, plugins);
            IPlatformConfiguration.ISiteEntry siteEntry = config.createSiteEntry(localSite.getURL(), sitePolicy);
            config.configureSite(siteEntry);
        }
        config.isTransient(true);
    }

    private static void createFeatureEntries(IPlatformConfiguration config, IPluginModelBase plugin) throws MalformedURLException {
        String id = plugin.getPluginBase().getId();
        IFeatureModel featureModel = PDECore.getDefault().getFeatureModelManager().findFeatureModel(id);
        if (featureModel != null) {
            IFeature feature = featureModel.getFeature();
            //$NON-NLS-1$
            IPlatformConfiguration.IFeatureEntry featureEntry = config.createFeatureEntry(id, feature.getVersion(), id, plugin.getPluginBase().getVersion(), true, null, new URL[] { new URL("file:" + plugin.getInstallLocation()) });
            config.configureFeatureEntry(featureEntry);
        }
    }
}
