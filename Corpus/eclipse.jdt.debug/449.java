/*******************************************************************************
 *  Copyright (c) 2005, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.environments;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallChangedListener;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.PropertyChangeEvent;
import org.eclipse.jdt.launching.environments.IAccessRuleParticipant;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

/**
 * A contributed execution environment.
 * 
 * @since 3.2
 */
class ExecutionEnvironment implements IExecutionEnvironment {

    /**
	 * Add a VM changed listener to clear cached values when a VM changes or is removed
	 */
    private IVMInstallChangedListener fListener = new IVMInstallChangedListener() {

        /* (non-Javadoc)
		 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#defaultVMInstallChanged(org.eclipse.jdt.launching.IVMInstall, org.eclipse.jdt.launching.IVMInstall)
		 */
        @Override
        public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
        }

        /* (non-Javadoc)
		 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmAdded(org.eclipse.jdt.launching.IVMInstall)
		 */
        @Override
        public void vmAdded(IVMInstall newVm) {
        }

        /* (non-Javadoc)
		 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmChanged(org.eclipse.jdt.launching.PropertyChangeEvent)
		 */
        @Override
        public void vmChanged(PropertyChangeEvent event) {
            if (event.getSource() != null) {
                fParticipantMap.remove(event.getSource());
                fRuleCache.remove(event.getSource());
            }
        }

        /* (non-Javadoc)
		 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmRemoved(org.eclipse.jdt.launching.IVMInstall)
		 */
        @Override
        public void vmRemoved(IVMInstall removedVm) {
            fParticipantMap.remove(removedVm);
            fRuleCache.remove(removedVm);
        }
    };

    /**
	 * The backing <code>IConfigurationElement</code>
	 */
    private IConfigurationElement fElement;

    /**
	 * Environment specific rule participant or <code>null</code> if none.
	 */
    private IAccessRuleParticipant fRuleParticipant;

    /**
	 * OSGi profile properties or <code>null</code> if none.
	 */
    private Properties fProfileProperties;

    /**
	 * Whether profile properties have been initialized
	 */
    private boolean fPropertiesInitialized;

    /**
	 * Set of compatible vm's - just the strictly compatible ones
	 */
    private Set<IVMInstall> fStrictlyCompatible = new HashSet<IVMInstall>();

    /** 
	 * All compatible vm's
	 */
    private List<IVMInstall> fCompatibleVMs = new ArrayList<IVMInstall>();

    /**
	 * default VM install or <code>null</code> if none
	 */
    private IVMInstall fDefault = null;

    /**
	 * Cache of access rule participants to consider for this environment.
	 */
    private IAccessRuleParticipant[] fParticipants = null;

    /**
	 * Map of {IVMInstall -> Map of {participant -> IAccessRule[][]}}.
	 * Caches access rules returned by each participant for a given VM.
	 * @since 3.3
	 */
    private Map<IVMInstall, Map<IAccessRuleParticipant, IAccessRule[][]>> fParticipantMap = new HashMap<IVMInstall, Map<IAccessRuleParticipant, IAccessRule[][]>>();

    /**
	 * Cache of VM -> IAccessRule[][] based on the current state of the participant
	 * map. These are the union of the latest rules generated by the participants
	 * for a specific VM. 
	 * @since 3.3
	 */
    private Map<IVMInstall, IAccessRule[][]> fRuleCache = new HashMap<IVMInstall, IAccessRule[][]>();

    /**
	 * Wild card pattern matching all files
	 */
    //$NON-NLS-1$
    private static final IPath ALL_PATTERN = new Path("**/*");

    /**
	 * Prefix of compiler settings in properties file
	 */
    //$NON-NLS-1$
    private static final String COMPILER_SETTING_PREFIX = JavaCore.PLUGIN_ID + ".compiler";

    /**
	 * Constructor
	 * @param element the backing {@link IConfigurationElement}
	 */
     ExecutionEnvironment(IConfigurationElement element) {
        fElement = element;
        fPropertiesInitialized = false;
        String attribute = fElement.getAttribute(EnvironmentsManager.RULE_PARTICIPANT_ELEMENT);
        if (attribute != null) {
            fRuleParticipant = new AccessRuleParticipant(fElement);
        }
        JavaRuntime.addVMInstallChangedListener(fListener);
    }

    /**
	 * Initializes the <code>EnvironmentsManager</code>
	 */
    private void init() {
        EnvironmentsManager.getDefault().initializeCompatibilities();
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironment#getId()
	 */
    @Override
    public String getId() {
        //$NON-NLS-1$
        return fElement.getAttribute("id");
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironment#getDescription()
	 */
    @Override
    public String getDescription() {
        //$NON-NLS-1$
        return fElement.getAttribute("description");
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironment#getCompatibleVMs()
	 */
    @Override
    public IVMInstall[] getCompatibleVMs() {
        init();
        return fCompatibleVMs.toArray(new IVMInstall[fCompatibleVMs.size()]);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironment#isStrictlyCompatible(org.eclipse.jdt.launching.IVMInstall)
	 */
    @Override
    public boolean isStrictlyCompatible(IVMInstall vm) {
        init();
        return fStrictlyCompatible.contains(vm);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironment#getDefaultVM()
	 */
    @Override
    public IVMInstall getDefaultVM() {
        init();
        return fDefault;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironment#setDefaultVM(org.eclipse.jdt.launching.IVMInstall)
	 */
    @Override
    public void setDefaultVM(IVMInstall vm) {
        init();
        if (vm != null && !fCompatibleVMs.contains(vm)) {
            throw new IllegalArgumentException(NLS.bind(EnvironmentMessages.EnvironmentsManager_0, new String[] { getId() }));
        }
        if (vm != null && vm.equals(fDefault)) {
            return;
        }
        fDefault = vm;
        EnvironmentsManager.getDefault().updateDefaultVMs();
        // update classpath containers
        rebindClasspathContainers();
    }

    /** 
	 * Updates Java projects referencing this environment, if any.
	 */
    private void rebindClasspathContainers() {
        IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
        if (model != null) {
            try {
                List<IJavaProject> updates = new ArrayList<IJavaProject>();
                IJavaProject[] javaProjects = model.getJavaProjects();
                IPath path = JavaRuntime.newJREContainerPath(this);
                for (int i = 0; i < javaProjects.length; i++) {
                    IJavaProject project = javaProjects[i];
                    IClasspathEntry[] rawClasspath = project.getRawClasspath();
                    for (int j = 0; j < rawClasspath.length; j++) {
                        IClasspathEntry entry = rawClasspath[j];
                        if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                            if (entry.getPath().equals(path)) {
                                updates.add(project);
                            }
                        }
                    }
                }
                if (!updates.isEmpty()) {
                    JavaCore.setClasspathContainer(path, updates.toArray(new IJavaProject[updates.size()]), new IClasspathContainer[updates.size()], new NullProgressMonitor());
                }
            } catch (JavaModelException e) {
                LaunchingPlugin.log(e);
            }
        }
    }

    /**
	 * Adds the specified VM to the listing of compatible VMs, also
	 * adds the VM to the listing of strictly compatible ones based on
	 * the strictlyCompatible flag
	 * @param vm the VM to add to the environment
	 * @param strictlyCompatible if it is strictly compatible
	 */
    void add(IVMInstall vm, boolean strictlyCompatible) {
        if (fCompatibleVMs.contains(vm)) {
            return;
        }
        fCompatibleVMs.add(vm);
        if (strictlyCompatible) {
            fStrictlyCompatible.add(vm);
        }
    }

    /**
	 * Removes the specified VM from the listings of VMs
	 * @param vm the VM to remove
	 */
    void remove(IVMInstall vm) {
        fCompatibleVMs.remove(vm);
        fStrictlyCompatible.remove(vm);
    }

    /**
	 * Sets the default VM to be the one specified
	 * @param vm the VM to set as the default
	 */
    void initDefaultVM(IVMInstall vm) {
        fDefault = vm;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironment#getAccessRules(org.eclipse.jdt.launching.IVMInstall, org.eclipse.jdt.launching.LibraryLocation[], org.eclipse.jdt.core.IJavaProject)
	 */
    @Override
    public IAccessRule[][] getAccessRules(IVMInstall vm, LibraryLocation[] libraries, IJavaProject project) {
        IAccessRuleParticipant[] participants = getParticipants();
        Map<IAccessRuleParticipant, IAccessRule[][]> rulesByParticipant = collectRulesByParticipant(participants, vm, libraries, project);
        synchronized (this) {
            Map<IAccessRuleParticipant, IAccessRule[][]> cachedRules = fParticipantMap.get(vm);
            if (cachedRules == null || !cachedRules.equals(rulesByParticipant)) {
                // array of lists of access rules
                ArrayList<List<IAccessRule>> libLists = new ArrayList<List<IAccessRule>>();
                for (int i = 0; i < libraries.length; i++) {
                    libLists.add(new ArrayList<IAccessRule>());
                }
                for (int i = 0; i < participants.length; i++) {
                    IAccessRuleParticipant participant = participants[i];
                    addRules(rulesByParticipant.get(participant), libLists);
                }
                IAccessRule[][] allRules = new IAccessRule[libraries.length][];
                for (int i = 0; i < libLists.size(); i++) {
                    List<IAccessRule> l = libLists.get(i);
                    allRules[i] = l.toArray(new IAccessRule[l.size()]);
                }
                fParticipantMap.put(vm, rulesByParticipant);
                fRuleCache.put(vm, allRules);
                return allRules;
            }
            return fRuleCache.get(vm);
        }
    }

    /**
	 * Returns all access rule participants to consider for this environment.
	 * Includes any participant contributed with this environment and all other
	 * stand alone participants.
	 * 
	 * @return access rule participants to consider for this environment
	 */
    private synchronized IAccessRuleParticipant[] getParticipants() {
        if (fParticipants == null) {
            // check participants first
            IAccessRuleParticipant[] participants = EnvironmentsManager.getDefault().getAccessRuleParticipants();
            if (fRuleParticipant != null) {
                // ensure environment specific provider is last and not duplicated
                LinkedHashSet<IAccessRuleParticipant> set = new LinkedHashSet<IAccessRuleParticipant>();
                for (int i = 0; i < participants.length; i++) {
                    set.add(participants[i]);
                }
                // remove, add to make last
                set.remove(fRuleParticipant);
                set.add(fRuleParticipant);
                participants = set.toArray(new IAccessRuleParticipant[set.size()]);
            }
            fParticipants = participants;
        }
        return fParticipants;
    }

    /**
	 * Returns a map of participant to the access rules for that participant for the given
	 * VM, libraries, and project.
	 * 
	 * @param participants access rule participants
	 * @param vm the VM
	 * @param libraries the {@link LibraryLocation}s 
	 * @param project the {@link IJavaProject} context
	 * @return the mapping of {@link IAccessRuleParticipant} to {@link IAccessRule}s
	 */
    private Map<IAccessRuleParticipant, IAccessRule[][]> collectRulesByParticipant(IAccessRuleParticipant[] participants, IVMInstall vm, LibraryLocation[] libraries, IJavaProject project) {
        Map<IAccessRuleParticipant, IAccessRule[][]> map = new HashMap<IAccessRuleParticipant, IAccessRule[][]>();
        for (int i = 0; i < participants.length; i++) {
            // TODO: use safe runnable
            map.put(participants[i], participants[i].getAccessRules(this, vm, libraries, project));
        }
        return map;
    }

    /**
	 * Adds the access rules to each list in the given collection. If the last rule in a 
	 * given collection is the wild card pattern then no more rules are added to that collection.
	 * 
	 * @param accessRules the list of {@link IAccessRule}s
	 * @param collect the array of lists to collect the {@link IAccessRule}s in
	 */
    private void addRules(IAccessRule[][] accessRules, ArrayList<List<IAccessRule>> collect) {
        for (int i = 0; i < accessRules.length; i++) {
            IAccessRule[] libRules = accessRules[i];
            List<IAccessRule> list = collect.get(i);
            // if the last rule is a **/* pattern, don't add any more rules, as they will have no effect
            if (!list.isEmpty()) {
                IAccessRule lastRule = list.get(list.size() - 1);
                if (lastRule.getPattern().equals(ALL_PATTERN)) {
                    continue;
                }
            }
            for (int j = 0; j < libRules.length; j++) {
                list.add(libRules[j]);
            }
        }
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironment#getProfileProperties()
	 */
    @Override
    public Properties getProfileProperties() {
        if (!fPropertiesInitialized) {
            fPropertiesInitialized = true;
            //$NON-NLS-1$
            String path = fElement.getAttribute("profileProperties");
            Bundle bundle = null;
            if (path == null) {
                // attempt default profiles known to OSGi
                bundle = //$NON-NLS-1$
                Platform.getBundle(//$NON-NLS-1$
                "org.eclipse.osgi");
                path = //$NON-NLS-1$
                getId().replace('/', '_') + //$NON-NLS-1$
                ".profile";
            } else {
                // read provided file
                bundle = Platform.getBundle(fElement.getContributor().getName());
            }
            if (bundle != null && path != null) {
                fProfileProperties = getJavaProfileProperties(bundle, path);
            }
        }
        return fProfileProperties;
    }

    /**
	 * Returns properties file contained in the specified bundle at the given
	 * bundle relative path, or <code>null</code> if none.
	 * 
	 * @param bundle bundle to locate file in
	 * @param path bundle relative path to properties file
	 * @return properties or <code>null</code> if none
	 */
    private Properties getJavaProfileProperties(Bundle bundle, String path) {
        URL profileURL = bundle.getEntry(path);
        if (profileURL != null) {
            try (InputStream is = profileURL.openStream()) {
                profileURL = FileLocator.resolve(profileURL);
                if (is != null) {
                    Properties profile = new Properties();
                    profile.load(is);
                    return profile;
                }
            } catch (IOException e) {
            }
        }
        return null;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironment#getSubEnvironments()
	 */
    @Override
    public IExecutionEnvironment[] getSubEnvironments() {
        Properties properties = getProfileProperties();
        Set<IExecutionEnvironment> subenv = new LinkedHashSet<IExecutionEnvironment>();
        if (properties != null) {
            @SuppressWarnings("deprecation") String subsets = properties.getProperty(Constants.FRAMEWORK_EXECUTIONENVIRONMENT);
            if (subsets != null) {
                String[] ids = //$NON-NLS-1$
                subsets.split(//$NON-NLS-1$
                ",");
                for (int i = 0; i < ids.length; i++) {
                    IExecutionEnvironment sub = JavaRuntime.getExecutionEnvironmentsManager().getEnvironment(ids[i].trim());
                    if (sub != null && !sub.getId().equals(getId())) {
                        subenv.add(sub);
                    }
                }
            }
        }
        return subenv.toArray(new IExecutionEnvironment[subenv.size()]);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironment#getComplianceOptions()
	 */
    @Override
    public Map<String, String> getComplianceOptions() {
        Properties properties = getProfileProperties();
        if (properties != null) {
            Map<String, String> map = new HashMap<String, String>();
            Iterator<?> iterator = properties.keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (key.startsWith(COMPILER_SETTING_PREFIX)) {
                    map.put(key, properties.getProperty(key));
                }
            }
            if (!map.isEmpty()) {
                return map;
            }
        }
        return null;
    }
}
