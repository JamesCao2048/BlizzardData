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
package org.eclipse.pde.internal.core;

import java.io.File;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.service.resolver.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.build.IPDEBuildConstants;
import org.eclipse.pde.internal.core.util.ManifestUtils;
import org.eclipse.pde.internal.core.util.UtilMessages;
import org.osgi.framework.BundleException;

public class MinimalState {

    protected State fState;

    protected long fId;

    // indicates that the EE has changed
    private boolean fEEListChanged = false;

    // this could be due to the system bundle changing location
    // or initially when the ee list is first created.
    // an ordered list of known/supported execution environments
    private String[] fExecutionEnvironments;

    private boolean fNoProfile;

    protected static StateObjectFactory stateObjectFactory;

    protected static String DIR;

    protected String fSystemBundle = IPDEBuildConstants.BUNDLE_OSGI;

    static {
        DIR = PDECore.getDefault().getStateLocation().toOSString();
        stateObjectFactory = Platform.getPlatformAdmin().getFactory();
    }

    protected  MinimalState(MinimalState state) {
        this.fState = stateObjectFactory.createState(state.fState);
        this.fState.setPlatformProperties(state.fState.getPlatformProperties());
        this.fState.setResolver(Platform.getPlatformAdmin().createResolver());
        this.fId = state.fId;
        this.fEEListChanged = state.fEEListChanged;
        this.fExecutionEnvironments = state.fExecutionEnvironments;
        this.fNoProfile = state.fNoProfile;
        this.fSystemBundle = state.fSystemBundle;
    }

    protected  MinimalState() {
    }

    public void addBundle(IPluginModelBase model, boolean update) {
        if (model == null)
            return;
        BundleDescription desc = model.getBundleDescription();
        long bundleId = desc == null || !update ? -1 : desc.getBundleId();
        try {
            BundleDescription newDesc = addBundle(new File(model.getInstallLocation()), bundleId);
            model.setBundleDescription(newDesc);
            if (newDesc == null && update)
                fState.removeBundle(desc);
        } catch (CoreException e) {
            PDECore.log(e);
            model.setBundleDescription(null);
        }
    }

    public BundleDescription addBundle(Map<String, String> manifest, File bundleLocation, long bundleId) throws CoreException {
        try {
            // OSGi requires a dictionary over any map
            Hashtable<String, String> dictionaryManifest = new Hashtable(manifest);
            BundleDescription descriptor = stateObjectFactory.createBundleDescription(fState, dictionaryManifest, bundleLocation.getAbsolutePath(), bundleId == -1 ? getNextId() : bundleId);
            // new bundle
            if (bundleId == -1) {
                fState.addBundle(descriptor);
            } else if (!fState.updateBundle(descriptor)) {
                fState.addBundle(descriptor);
            }
            return descriptor;
        } catch (BundleException e) {
            MultiStatus status = new MultiStatus(PDECore.PLUGIN_ID, 0, NLS.bind(UtilMessages.ErrorReadingManifest, bundleLocation.toString()), null);
            status.add(new Status(IStatus.ERROR, PDECore.PLUGIN_ID, e.getMessage()));
            throw new CoreException(status);
        } catch (NumberFormatException e) {
        } catch (IllegalArgumentException e) {
        }
        return null;
    }

    public BundleDescription addBundle(File bundleLocation, long bundleId) throws CoreException {
        Map<String, String> manifest = ManifestUtils.loadManifest(bundleLocation);
        // update for development mode
        TargetWeaver.weaveManifest(manifest);
        BundleDescription desc = addBundle(manifest, bundleLocation, bundleId);
        if (//$NON-NLS-1$
        desc != null && manifest != null && "true".equals(manifest.get(ICoreConstants.ECLIPSE_SYSTEM_BUNDLE))) {
            // if this is the system bundle then
            // indicate that the javaProfile has changed since the new system
            // bundle may not contain profiles for all EE's in the list
            fEEListChanged = true;
            fSystemBundle = desc.getSymbolicName();
        }
        if (desc != null) {
            addAuxiliaryData(desc, manifest, true);
        }
        return desc;
    }

    protected void addAuxiliaryData(BundleDescription desc, Map<String, String> manifest, boolean hasBundleStructure) {
    }

    public StateDelta resolveState(boolean incremental) {
        return internalResolveState(incremental);
    }

    /**
	 * Resolves the state incrementally based on the given bundle names.
	 *
	 * @param symbolicNames
	 * @return state delta
	 */
    public StateDelta resolveState(String[] symbolicNames) {
        if (initializePlatformProperties()) {
            return fState.resolve(false);
        }
        List<BundleDescription> bundles = new ArrayList();
        for (int i = 0; i < symbolicNames.length; i++) {
            BundleDescription[] descriptions = fState.getBundles(symbolicNames[i]);
            for (int j = 0; j < descriptions.length; j++) {
                bundles.add(descriptions[j]);
            }
        }
        return fState.resolve(bundles.toArray(new BundleDescription[bundles.size()]));
    }

    private synchronized StateDelta internalResolveState(boolean incremental) {
        boolean fullBuildRequired = initializePlatformProperties();
        return fState.resolve(incremental && !fullBuildRequired);
    }

    protected boolean initializePlatformProperties() {
        if (fExecutionEnvironments == null && !fNoProfile)
            setExecutionEnvironments();
        if (fEEListChanged) {
            fEEListChanged = false;
            return fState.setPlatformProperties(getProfilePlatformProperties());
        }
        return false;
    }

    private Dictionary<String, String>[] getProfilePlatformProperties() {
        return TargetPlatformHelper.getPlatformProperties(fExecutionEnvironments, this);
    }

    public void removeBundleDescription(BundleDescription description) {
        if (description != null)
            fState.removeBundle(description);
    }

    public void updateBundleDescription(BundleDescription description) {
        if (description != null)
            fState.updateBundle(description);
    }

    public State getState() {
        return fState;
    }

    private void setExecutionEnvironments() {
        String[] knownExecutionEnviroments = TargetPlatformHelper.getKnownExecutionEnvironments();
        if (knownExecutionEnviroments.length == 0) {
            //$NON-NLS-1$
            String jreProfile = System.getProperty("pde.jreProfile");
            if (jreProfile != null && jreProfile.length() > 0)
                if (//$NON-NLS-1$
                "none".equals(//$NON-NLS-1$
                jreProfile))
                    fNoProfile = true;
        }
        if (!fNoProfile) {
            fExecutionEnvironments = knownExecutionEnviroments;
        }
        // alway indicate the list has changed
        fEEListChanged = true;
    }

    public void addBundleDescription(BundleDescription toAdd) {
        if (toAdd != null)
            fState.addBundle(toAdd);
    }

    public long getNextId() {
        return ++fId;
    }

    public String getSystemBundle() {
        return fSystemBundle;
    }
}
