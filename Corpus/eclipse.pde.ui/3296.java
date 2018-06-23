/*******************************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 477527
 *******************************************************************************/
package org.eclipse.pde.internal.core.exports;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.TargetPlatform;
import org.eclipse.pde.internal.core.*;
import org.eclipse.pde.internal.core.ifeature.IFeature;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;

public abstract class FeatureBasedExportOperation extends FeatureExportOperation {

    protected String fFeatureLocation;

    public  FeatureBasedExportOperation(FeatureExportInfo info, String name) {
        super(info, name);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        //$NON-NLS-1$
        SubMonitor subMonitor = SubMonitor.convert(monitor, "Exporting...", 33);
        try {
            createDestination();
            // create a feature to contain all plug-ins
            //$NON-NLS-1$
            String featureID = "org.eclipse.pde.container.feature";
            fFeatureLocation = fBuildTempLocation + File.separator + featureID;
            String[][] config = new String[][] { { TargetPlatform.getOS(), TargetPlatform.getWS(), TargetPlatform.getOSArch(), TargetPlatform.getNL() } };
            createFeature(featureID, fFeatureLocation, config, false);
            createBuildPropertiesFile(fFeatureLocation);
            if (fInfo.useJarFormat) {
                createPostProcessingFiles();
            }
            IStatus status = testBuildWorkspaceBeforeExport(subMonitor.split(10));
            doExport(featureID, null, fFeatureLocation, config, subMonitor.split(20));
            if (subMonitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            return status;
        } catch (IOException e) {
            return new Status(IStatus.ERROR, PDECore.PLUGIN_ID, PDECoreMessages.FeatureBasedExportOperation_ProblemDuringExport, e);
        } catch (CoreException e) {
            return e.getStatus();
        } catch (InvocationTargetException e) {
            return new Status(IStatus.ERROR, PDECore.PLUGIN_ID, PDECoreMessages.FeatureBasedExportOperation_ProblemDuringExport, e.getTargetException());
        } finally {
            for (int i = 0; i < fInfo.items.length; i++) {
                if (fInfo.items[i] instanceof IModel)
                    try {
                        deleteBuildFiles(fInfo.items[i]);
                    } catch (CoreException e) {
                        PDECore.log(e);
                    }
            }
            cleanup(subMonitor.split(3));
        }
    }

    protected abstract void createPostProcessingFiles();

    @Override
    protected String[] getPaths() {
        String[] paths = super.getPaths();
        String[] all = new String[paths.length + 1];
        all[0] = fFeatureLocation + File.separator + ICoreConstants.FEATURE_FILENAME_DESCRIPTOR;
        System.arraycopy(paths, 0, all, 1, paths.length);
        return all;
    }

    private void createBuildPropertiesFile(String featureLocation) {
        File file = new File(featureLocation);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        Properties prop = new Properties();
        //$NON-NLS-1$ //$NON-NLS-2$
        prop.put("pde", "marker");
        if (fInfo.exportSource && fInfo.exportSourceBundle) {
            //$NON-NLS-1$ //$NON-NLS-2$
            prop.put("individualSourceBundles", "true");
            Dictionary<String, String> environment = new Hashtable(4);
            //$NON-NLS-1$
            environment.put("osgi.os", TargetPlatform.getOS());
            //$NON-NLS-1$
            environment.put("osgi.ws", TargetPlatform.getWS());
            //$NON-NLS-1$
            environment.put("osgi.arch", TargetPlatform.getOSArch());
            //$NON-NLS-1$
            environment.put("osgi.nl", TargetPlatform.getNL());
            for (int i = 0; i < fInfo.items.length; i++) {
                if (fInfo.items[i] instanceof IFeatureModel) {
                    IFeature feature = ((IFeatureModel) fInfo.items[i]).getFeature();
                    //$NON-NLS-1$ //$NON-NLS-2$
                    prop.put("generate.feature@" + feature.getId() + ".source", feature.getId());
                } else {
                    BundleDescription bundle = null;
                    if (fInfo.items[i] instanceof IPluginModelBase) {
                        bundle = ((IPluginModelBase) fInfo.items[i]).getBundleDescription();
                    }
                    if (bundle == null) {
                        if (fInfo.items[i] instanceof BundleDescription)
                            bundle = (BundleDescription) fInfo.items[i];
                    }
                    if (bundle == null)
                        continue;
                    if (shouldAddPlugin(bundle, environment)) {
                        //$NON-NLS-1$ //$NON-NLS-2$
                        prop.put("generate.plugin@" + bundle.getSymbolicName() + ".source", bundle.getSymbolicName());
                    }
                }
            }
        }
        //$NON-NLS-1$
        save(new File(file, ICoreConstants.BUILD_FILENAME_DESCRIPTOR), prop, "Marker File");
    }
}
