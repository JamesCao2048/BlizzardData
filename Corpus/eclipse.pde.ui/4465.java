/*******************************************************************************
 * Copyright (c) 2008, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.target;

import java.io.*;
import java.net.*;
import java.util.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.equinox.frameworkadmin.BundleInfo;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.core.target.*;
import org.eclipse.pde.internal.core.*;

/**
 * Target platform service implementation.
 *
 * @since 3.5
 */
public class TargetPlatformService implements ITargetPlatformService {

    /**
	 * Service instance
	 */
    private static ITargetPlatformService fgDefault;

    /**
	 * External File Targets
	 */
    private static Map<URI, ExternalFileTargetHandle> fExtTargetHandles;

    /**
	 * The target definition currently being used as the target platform for
	 * the workspace.
	 */
    private ITargetDefinition fWorkspaceTarget;

    /**
	 * vm arguments for default target
	 */
    private StringBuffer fVMArguments = null;

    /**
	 * Collects target files in the workspace
	 */
    class ResourceProxyVisitor implements IResourceProxyVisitor {

        private List<IResource> fList;

        protected  ResourceProxyVisitor(List<IResource> list) {
            fList = list;
        }

        /**
		 * @see org.eclipse.core.resources.IResourceProxyVisitor#visit(org.eclipse.core.resources.IResourceProxy)
		 */
        @Override
        public boolean visit(IResourceProxy proxy) {
            if (proxy.getType() == IResource.FILE) {
                if (ICoreConstants.TARGET_FILE_EXTENSION.equalsIgnoreCase(new Path(proxy.getName()).getFileExtension())) {
                    fList.add(proxy.requestResource());
                }
                return false;
            }
            return true;
        }
    }

    private  TargetPlatformService() {
    }

    /**
	 * The target service should be obtained by requesting the {@link ITargetPlatformService} from OSGi. This
	 * method should only be used internally be PDE.
	 *
	 * @return The singleton implementation of this service
	 */
    public static synchronized ITargetPlatformService getDefault() {
        if (fgDefault == null) {
            fgDefault = new TargetPlatformService();
        }
        return fgDefault;
    }

    @Override
    public void deleteTarget(ITargetHandle handle) throws CoreException {
        if (handle instanceof ExternalFileTargetHandle)
            fExtTargetHandles.remove(((ExternalFileTargetHandle) handle).getLocation());
        ((AbstractTargetHandle) handle).delete();
    }

    @Override
    public ITargetHandle getTarget(IFile file) {
        return new WorkspaceFileTargetHandle(file);
    }

    @Override
    public ITargetHandle getTarget(String memento) throws CoreException {
        try {
            URI uri = new URI(memento);
            String scheme = uri.getScheme();
            if (WorkspaceFileTargetHandle.SCHEME.equals(scheme)) {
                return WorkspaceFileTargetHandle.restoreHandle(uri);
            } else if (LocalTargetHandle.SCHEME.equals(scheme)) {
                return LocalTargetHandle.restoreHandle(uri);
            } else if (ExternalFileTargetHandle.SCHEME.equals(scheme)) {
                return ExternalFileTargetHandle.restoreHandle(uri);
            }
        } catch (URISyntaxException e) {
            throw new CoreException(new Status(IStatus.ERROR, PDECore.PLUGIN_ID, Messages.TargetPlatformService_0, e));
        }
        throw new CoreException(new Status(IStatus.ERROR, PDECore.PLUGIN_ID, Messages.TargetPlatformService_1, null));
    }

    @Override
    public ITargetHandle getTarget(URI uri) {
        if (fExtTargetHandles == null)
            fExtTargetHandles = new HashMap(10);
        if (fExtTargetHandles.containsKey(uri)) {
            return fExtTargetHandles.get(uri);
        }
        ExternalFileTargetHandle externalTarget = new ExternalFileTargetHandle(uri);
        fExtTargetHandles.put(uri, externalTarget);
        return externalTarget;
    }

    @Override
    public ITargetHandle[] getTargets(IProgressMonitor monitor) {
        List<ITargetHandle> local = findLocalTargetDefinitions();
        List<WorkspaceFileTargetHandle> ws = findWorkspaceTargetDefinitions();
        local.addAll(ws);
        if (fExtTargetHandles != null) {
            // If an external target is inaccessible then don't show it. But keep the reference in case it becomes accessible later
            Collection<ExternalFileTargetHandle> externalTargets = fExtTargetHandles.values();
            for (Iterator<ExternalFileTargetHandle> iterator = externalTargets.iterator(); iterator.hasNext(); ) {
                ExternalFileTargetHandle target = iterator.next();
                if (target.exists())
                    local.add(target);
            }
        } else {
            PDEPreferencesManager preferences = PDECore.getDefault().getPreferencesManager();
            String memento = preferences.getString(ICoreConstants.WORKSPACE_TARGET_HANDLE);
            if (memento != null && memento.length() != 0 && !memento.equals(ICoreConstants.NO_TARGET)) {
                try {
                    URI uri = new URI(memento);
                    String scheme = uri.getScheme();
                    if (ExternalFileTargetHandle.SCHEME.equals(scheme)) {
                        ITargetHandle target = getTarget(uri);
                        local.add(target);
                    }
                } catch (URISyntaxException e) {
                }
            }
        }
        return local.toArray(new ITargetHandle[local.size()]);
    }

    /**
	 * Finds and returns all local target definition handles
	 *
	 * @return all local target definition handles
	 */
    private List<ITargetHandle> findLocalTargetDefinitions() {
        IPath containerPath = LocalTargetHandle.LOCAL_TARGET_CONTAINER_PATH;
        List<ITargetHandle> handles = new ArrayList(10);
        final File directory = containerPath.toFile();
        if (directory.isDirectory()) {
            FilenameFilter filter = new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return dir.equals(directory) && name.endsWith(ICoreConstants.TARGET_FILE_EXTENSION);
                }
            };
            File[] files = directory.listFiles(filter);
            for (int i = 0; i < files.length; i++) {
                try {
                    handles.add(LocalTargetHandle.restoreHandle(files[i].toURI()));
                } catch (CoreException e) {
                    PDECore.log(e);
                }
            }
        }
        return handles;
    }

    /**
	 * Finds and returns all target definition handles defined by workspace files
	 *
	 * @return all target definition handles in the workspace
	 */
    private List<WorkspaceFileTargetHandle> findWorkspaceTargetDefinitions() {
        List<IResource> files = new ArrayList(10);
        ResourceProxyVisitor visitor = new ResourceProxyVisitor(files);
        try {
            ResourcesPlugin.getWorkspace().getRoot().accept(visitor, IResource.NONE);
        } catch (CoreException e) {
            PDECore.log(e);
            return new ArrayList(0);
        }
        Iterator<IResource> iter = files.iterator();
        List<WorkspaceFileTargetHandle> handles = new ArrayList(files.size());
        while (iter.hasNext()) {
            IFile file = (IFile) iter.next();
            handles.add(new WorkspaceFileTargetHandle(file));
        }
        return handles;
    }

    @Override
    public ITargetLocation newDirectoryLocation(String path) {
        return new DirectoryBundleContainer(path);
    }

    @Override
    public ITargetLocation newProfileLocation(String home, String configurationLocation) {
        return new ProfileBundleContainer(home, configurationLocation);
    }

    @Override
    public ITargetDefinition newTarget() {
        return new TargetDefinition(new LocalTargetHandle());
    }

    @Override
    public void saveTargetDefinition(ITargetDefinition definition) throws CoreException {
        ((AbstractTargetHandle) definition.getHandle()).save(definition);
    }

    @Override
    public ITargetLocation newFeatureLocation(String home, String id, String version) {
        return new FeatureBundleContainer(home, id, version);
    }

    @Override
    public ITargetHandle getWorkspaceTargetHandle() throws CoreException {
        PDEPreferencesManager preferences = PDECore.getDefault().getPreferencesManager();
        String memento = preferences.getString(ICoreConstants.WORKSPACE_TARGET_HANDLE);
        if (memento != null && memento.length() != 0 && !memento.equals(ICoreConstants.NO_TARGET)) {
            return getTarget(memento);
        }
        return null;
    }

    @Override
    public synchronized ITargetDefinition getWorkspaceTargetDefinition() throws CoreException {
        if (fWorkspaceTarget != null && fWorkspaceTarget.getHandle().equals(getWorkspaceTargetHandle())) {
            return fWorkspaceTarget;
        }
        // If no target definition has been chosen before, try using preferences
        initDefaultTargetPlatformDefinition();
        // Load and resolve
        String memento = PDECore.getDefault().getPreferencesManager().getString(ICoreConstants.WORKSPACE_TARGET_HANDLE);
        ITargetDefinition target = null;
        if (//$NON-NLS-1$
        memento == null || memento.equals("") || memento.equals(ICoreConstants.NO_TARGET)) {
            target = newTarget();
        } else {
            ITargetHandle handle = getTarget(memento);
            target = handle.getTargetDefinition();
        }
        fWorkspaceTarget = target;
        return target;
    }

    /**
	 * Updates the current stored target. Provided to allow the LoadTargetDefinitionJob
	 * to pass along a possibly resolved target rather than force it to be resolved again.
	 * This method will not update the stored {@link ICoreConstants#WORKSPACE_TARGET_HANDLE},
	 * as it should only be called from LoadTargetDefinitionJob which does additional
	 * steps to reset the target.
	 *
	 * @param target the new workspace target definition
	 */
    public void setWorkspaceTargetDefinition(ITargetDefinition target) {
        fWorkspaceTarget = target;
    }

    /**
	 * Sets active target definition handle if not yet set. If an existing target
	 * definition corresponds to workspace target settings, it is selected as the
	 * active target. If there are no targets that correspond to workspace settings
	 * a new definition is created.
	 */
    private void initDefaultTargetPlatformDefinition() {
        String memento = PDECore.getDefault().getPreferencesManager().getString(ICoreConstants.WORKSPACE_TARGET_HANDLE);
        if (//$NON-NLS-1$
        memento == null || memento.equals("")) {
            try {
                if (PDECore.DEBUG_MODEL) {
                    System.out.println("No target platform memento, add default target.");
                }
                // Add default target
                ITargetDefinition defaultTarget = newDefaultTarget();
                defaultTarget.setName(Messages.TargetPlatformService_7);
                saveTargetDefinition(defaultTarget);
                // Add target from preferences
                TargetDefinition preferencesTarget = (TargetDefinition) newTargetFromPreferences();
                if (preferencesTarget != null) {
                    if (PDECore.DEBUG_MODEL) {
                        System.out.println("Old target preferences found, loading them into active target.");
                    }
                    preferencesTarget.setName(PDECoreMessages.PluginModelManager_0);
                    saveTargetDefinition(preferencesTarget);
                }
                // Set active platform
                PDEPreferencesManager preferences = PDECore.getDefault().getPreferencesManager();
                ITargetHandle active = preferencesTarget != null ? preferencesTarget.getHandle() : defaultTarget.getHandle();
                preferences.setValue(ICoreConstants.WORKSPACE_TARGET_HANDLE, active.getMemento());
            } catch (CoreException e) {
                PDECore.log(e);
            }
        }
    }

    @Override
    public void copyTargetDefinition(ITargetDefinition from, ITargetDefinition to) throws CoreException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ((TargetDefinition) from).write(outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        ((TargetDefinition) to).setContents(inputStream);
    }

    @Override
    public void loadTargetDefinition(ITargetDefinition definition, String targetExtensionId) throws CoreException {
        IConfigurationElement elem = PDECore.getDefault().getTargetProfileManager().getTarget(targetExtensionId);
        if (elem == null) {
            throw new CoreException(new Status(IStatus.ERROR, PDECore.PLUGIN_ID, NLS.bind(Messages.TargetPlatformService_2, targetExtensionId)));
        }
        //$NON-NLS-1$
        String path = elem.getAttribute("definition");
        String symbolicName = elem.getDeclaringExtension().getNamespaceIdentifier();
        URL url = TargetDefinitionManager.getResourceURL(symbolicName, path);
        if (url != null) {
            try {
                ((TargetDefinition) definition).setContents(new BufferedInputStream(url.openStream()));
            } catch (IOException e) {
                throw new CoreException(new Status(IStatus.ERROR, PDECore.PLUGIN_ID, NLS.bind(Messages.TargetPlatformService_3, path), e));
            }
        } else {
            throw new CoreException(new Status(IStatus.ERROR, PDECore.PLUGIN_ID, NLS.bind(Messages.TargetPlatformService_4, path)));
        }
    }

    /**
	 * Returns a target definition initialized with existing settings from the deprecated
	 * target platform preferences or <code>null</code> if no deprecated preferences are
	 * found.
	 *
	 * @return a target definition initialized with existing settings or <code>null</code>
	 */
    @SuppressWarnings("deprecation")
    public ITargetDefinition newTargetFromPreferences() {
        PDEPreferencesManager preferences = PDECore.getDefault().getPreferencesManager();
        // See if the old preference for the primary target platform location exist
        boolean useThis = preferences.getString(ICoreConstants.TARGET_MODE).equals(ICoreConstants.VALUE_USE_THIS);
        String platformPath = preferences.getString(ICoreConstants.PLATFORM_PATH);
        if (useThis || (platformPath != null && platformPath.length() > 0)) {
            ITargetDefinition target = newTarget();
            initializeArgumentsInfo(preferences, target);
            initializeEnvironmentInfo(preferences, target);
            initializeImplicitInfo(preferences, target);
            initializeLocationInfo(preferences, target);
            initializeAdditionalLocsInfo(preferences, target);
            initializeJREInfo(target);
            initializePluginContent(preferences, target);
            return target;
        }
        return null;
    }

    /**
	 * Returns the given string or <code>null</code> if the empty string.
	 *
	 * @param value
	 * @return value or <code>null</code>
	 */
    private String getValueOrNull(String value) {
        if (value == null) {
            return null;
        }
        if (value.length() == 0) {
            return null;
        }
        return value;
    }

    @SuppressWarnings("deprecation")
    private void initializeArgumentsInfo(PDEPreferencesManager preferences, ITargetDefinition target) {
        target.setProgramArguments(getValueOrNull(preferences.getString(ICoreConstants.PROGRAM_ARGS)));
        StringBuffer result = new StringBuffer();
        String vmArgs = getValueOrNull(preferences.getString(ICoreConstants.VM_ARGS));
        if (vmArgs != null) {
            result.append(vmArgs);
        }
        if (preferences.getBoolean(ICoreConstants.VM_LAUNCHER_INI)) {
            // hack on the arguments from eclipse.ini
            result.append(TargetPlatformHelper.getIniVMArgs());
        }
        if (result.length() == 0) {
            target.setVMArguments(null);
        } else {
            target.setVMArguments(result.toString());
        }
    }

    @SuppressWarnings("deprecation")
    private void initializeEnvironmentInfo(PDEPreferencesManager preferences, ITargetDefinition target) {
        target.setOS(getValueOrNull(preferences.getString(ICoreConstants.OS)));
        target.setWS(getValueOrNull(preferences.getString(ICoreConstants.WS)));
        target.setNL(getValueOrNull(preferences.getString(ICoreConstants.NL)));
        target.setArch(getValueOrNull(preferences.getString(ICoreConstants.ARCH)));
    }

    @SuppressWarnings("deprecation")
    private void initializeImplicitInfo(PDEPreferencesManager preferences, ITargetDefinition target) {
        String value = preferences.getString(ICoreConstants.IMPLICIT_DEPENDENCIES);
        if (value.length() > 0) {
            //$NON-NLS-1$
            StringTokenizer tokenizer = new StringTokenizer(value, ",");
            NameVersionDescriptor[] plugins = new NameVersionDescriptor[tokenizer.countTokens()];
            int i = 0;
            while (tokenizer.hasMoreTokens()) {
                String id = tokenizer.nextToken();
                plugins[i++] = new NameVersionDescriptor(id, null);
            }
            target.setImplicitDependencies(plugins);
        }
    }

    @SuppressWarnings("deprecation")
    private void initializeLocationInfo(PDEPreferencesManager preferences, ITargetDefinition target) {
        boolean useThis = preferences.getString(ICoreConstants.TARGET_MODE).equals(ICoreConstants.VALUE_USE_THIS);
        boolean profile = preferences.getBoolean(ICoreConstants.TARGET_PLATFORM_REALIZATION);
        String home = null;
        // Target weaving
        Location configArea = Platform.getConfigurationLocation();
        String configLocation = null;
        if (configArea != null) {
            configLocation = configArea.getURL().getFile();
        }
        if (configLocation != null) {
            Location location = Platform.getInstallLocation();
            if (location != null) {
                URL url = location.getURL();
                if (url != null) {
                    IPath installPath = new Path(url.getFile());
                    IPath configPath = new Path(configLocation);
                    if (installPath.isPrefixOf(configPath)) {
                        // if it is the default configuration area, do not specify explicitly
                        configPath = configPath.removeFirstSegments(installPath.segmentCount());
                        configPath = configPath.setDevice(null);
                        if (configPath.segmentCount() == 1 && //$NON-NLS-1$
                        configPath.lastSegment().equals(//$NON-NLS-1$
                        "configuration")) {
                            configLocation = null;
                        }
                    }
                }
            }
        }
        if (useThis) {
            //$NON-NLS-1$
            home = "${eclipse_home}";
        } else {
            home = preferences.getString(ICoreConstants.PLATFORM_PATH);
        }
        ITargetLocation primary = null;
        if (profile) {
            primary = newProfileLocation(home, configLocation);
        } else {
            primary = newDirectoryLocation(home);
        }
        target.setName(Messages.TargetPlatformService_5);
        target.setTargetLocations(new ITargetLocation[] { primary });
    }

    @SuppressWarnings("deprecation")
    private void initializeAdditionalLocsInfo(PDEPreferencesManager preferences, ITargetDefinition target) {
        String additional = preferences.getString(ICoreConstants.ADDITIONAL_LOCATIONS);
        //$NON-NLS-1$
        StringTokenizer tokenizer = new StringTokenizer(additional, ",");
        int size = tokenizer.countTokens();
        if (size > 0) {
            ITargetLocation[] locations = new ITargetLocation[size + 1];
            locations[0] = target.getTargetLocations()[0];
            int i = 1;
            while (tokenizer.hasMoreTokens()) {
                locations[i++] = newDirectoryLocation(tokenizer.nextToken().trim());
            }
            target.setTargetLocations(locations);
        }
    }

    private void initializeJREInfo(ITargetDefinition target) {
        target.setJREContainer(null);
    }

    @SuppressWarnings("deprecation")
    private void initializePluginContent(PDEPreferencesManager preferences, ITargetDefinition target) {
        String value = preferences.getString(ICoreConstants.CHECKED_PLUGINS);
        if (value.length() == 0 || value.equals(ICoreConstants.VALUE_SAVED_NONE)) {
            // no bundles
            target.setTargetLocations(null);
            return;
        }
        if (!value.equals(ICoreConstants.VALUE_SAVED_ALL)) {
            // restrictions on container
            IPluginModelBase[] models = PluginRegistry.getExternalModels();
            ArrayList<NameVersionDescriptor> list = new ArrayList(models.length);
            Set<String> disabledIDs = new HashSet();
            for (int i = 0; i < models.length; i++) {
                if (!models[i].isEnabled()) {
                    disabledIDs.add(models[i].getPluginBase().getId());
                }
            }
            for (int i = 0; i < models.length; i++) {
                if (models[i].isEnabled()) {
                    String id = models[i].getPluginBase().getId();
                    if (id != null) {
                        if (disabledIDs.contains(id)) {
                            // include version info since some versions are disabled
                            list.add(new NameVersionDescriptor(id, models[i].getPluginBase().getVersion()));
                        } else {
                            list.add(new NameVersionDescriptor(id, null));
                        }
                    }
                }
            }
            if (list.size() > 0) {
                target.setIncluded(list.toArray(new NameVersionDescriptor[list.size()]));
            }
        }
    }

    @Override
    public ITargetDefinition newDefaultTarget() {
        ITargetDefinition target = newTarget();
        Location configArea = Platform.getConfigurationLocation();
        String configLocation = null;
        if (configArea != null) {
            configLocation = configArea.getURL().getFile();
        }
        if (configLocation != null) {
            Location location = Platform.getInstallLocation();
            if (location != null) {
                URL url = location.getURL();
                if (url != null) {
                    IPath installPath = new Path(url.getFile());
                    IPath configPath = new Path(configLocation);
                    if (installPath.isPrefixOf(configPath)) {
                        // if it is the default configuration area, do not specify explicitly
                        configPath = configPath.removeFirstSegments(installPath.segmentCount());
                        configPath = configPath.setDevice(null);
                        if (configPath.segmentCount() == 1 && //$NON-NLS-1$
                        configPath.lastSegment().equals(//$NON-NLS-1$
                        "configuration")) {
                            configLocation = null;
                        }
                    }
                }
            }
        }
        //$NON-NLS-1$
        ITargetLocation container = newProfileLocation("${eclipse_home}", configLocation);
        target.setTargetLocations(new ITargetLocation[] { container });
        target.setName(Messages.TargetPlatformService_7);
        // initialize environment with default settings
        target.setArch(Platform.getOSArch());
        target.setOS(Platform.getOS());
        target.setWS(Platform.getWS());
        target.setNL(Platform.getNL());
        // initialize vm arguments from the default container
        ITargetLocation[] containers = target.getTargetLocations();
        Job job = new Job(Messages.TargetPlatformService_6) {

            @Override
            public IStatus run(IProgressMonitor monitor) {
                fVMArguments = getVMArguments(containers);
                return Status.OK_STATUS;
            }
        };
        job.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(org.eclipse.core.runtime.jobs.IJobChangeEvent event) {
                if (fVMArguments != null)
                    target.setVMArguments(fVMArguments.toString().trim());
            }
        });
        job.schedule();
        return target;
    }

    private StringBuffer getVMArguments(ITargetLocation[] containers) {
        //$NON-NLS-1$
        StringBuffer arguments = new StringBuffer("");
        for (int i = 0; i < containers.length; i++) {
            String[] vmargs = containers[i].getVMArguments();
            if (vmargs == null)
                continue;
            for (int j = 0; j < vmargs.length; j++) {
                arguments.append(vmargs[j]).append(' ');
            }
        }
        return arguments;
    }

    @Override
    public IStatus compareWithTargetPlatform(ITargetDefinition target) throws CoreException {
        if (!target.isResolved()) {
            return null;
        }
        // Get the current models from the target platform
        IPluginModelBase[] models = PDECore.getDefault().getModelManager().getExternalModels();
        Set<String> allLocations = new HashSet(models.length);
        Map<String, IPluginModelBase> stateLocations = new HashMap(models.length);
        for (int i = 0; i < models.length; i++) {
            IPluginModelBase base = models[i];
            allLocations.add(base.getInstallLocation());
            stateLocations.put(base.getInstallLocation(), base);
        }
        // Compare the platform bundles against the definition ones and collect any missing bundles
        //$NON-NLS-1$
        MultiStatus multi = new MultiStatus(PDECore.PLUGIN_ID, 0, "", null);
        TargetBundle[] bundles = target.getAllBundles();
        Set<NameVersionDescriptor> alreadyConsidered = new HashSet(bundles.length);
        for (int i = 0; i < bundles.length; i++) {
            TargetBundle bundle = bundles[i];
            BundleInfo info = bundle.getBundleInfo();
            File file = URIUtil.toFile(info.getLocation());
            String location = file.getAbsolutePath();
            stateLocations.remove(location);
            NameVersionDescriptor desc = new NameVersionDescriptor(info.getSymbolicName(), info.getVersion());
            if (!alreadyConsidered.contains(desc)) {
                alreadyConsidered.add(desc);
                // ignore duplicates (symbolic name & version)
                if (!allLocations.contains(location)) {
                    // it's not in the state... if it's not really in the target either (missing) this
                    // is not an error
                    IStatus status = bundle.getStatus();
                    if (status.isOK() || (status.getCode() != TargetBundle.STATUS_PLUGIN_DOES_NOT_EXIST && status.getCode() != TargetBundle.STATUS_FEATURE_DOES_NOT_EXIST && status.getCode() != TargetBundle.STATUS_VERSION_DOES_NOT_EXIST)) {
                        // its in the target, missing in the state
                        IStatus s = new Status(IStatus.WARNING, PDECore.PLUGIN_ID, ITargetPlatformService.STATUS_MISSING_FROM_TARGET_PLATFORM, bundle.getBundleInfo().getSymbolicName(), null);
                        multi.add(s);
                    }
                }
            }
        }
        // Anything left over is in the state and not the target (have been removed from the target)
        Iterator<IPluginModelBase> iterator = stateLocations.values().iterator();
        while (iterator.hasNext()) {
            IPluginModelBase model = iterator.next();
            IStatus status = new Status(IStatus.WARNING, PDECore.PLUGIN_ID, ITargetPlatformService.STATUS_MISSING_FROM_TARGET_DEFINITION, model.getPluginBase().getId(), null);
            multi.add(status);
        }
        if (multi.isOK()) {
            return Status.OK_STATUS;
        }
        return multi;
    }

    @Override
    public ITargetLocation newIULocation(IInstallableUnit[] units, URI[] repositories, int resolutionFlags) {
        return new IUBundleContainer(units, repositories, resolutionFlags);
    }

    @Override
    public ITargetLocation newIULocation(String[] unitIds, String[] versions, URI[] repositories, int resolutionFlags) {
        return new IUBundleContainer(unitIds, versions, repositories, resolutionFlags);
    }
}
