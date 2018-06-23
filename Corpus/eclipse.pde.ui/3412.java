/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 477527
 *******************************************************************************/
package org.eclipse.pde.internal.core;

import java.io.*;
import java.util.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.core.plugin.*;
import org.eclipse.pde.internal.core.util.CoreUtility;

/**
 * This class manages the ability of external plug-ins in the model manager to
 * take part in the Java search. It manages a proxy Java projects and for each
 * external plug-in added to Java search, it adds its Java libraries as external
 * JARs to the proxy project. This makes the libraries visible to the Java
 * model, and they can take part in various Java searches.
 */
public class SearchablePluginsManager implements IFileAdapterFactory, IPluginModelListener {

    //$NON-NLS-1$
    private static final String PROXY_FILE_NAME = ".searchable";

    //$NON-NLS-1$
    public static final String PROXY_PROJECT_NAME = "External Plug-in Libraries";

    //$NON-NLS-1$
    private static final String KEY = "searchablePlugins";

    private Listener fElementListener;

    private Set<String> fPluginIdSet;

    private ArrayList<IPluginModelListener> fListeners;

    class Listener implements IElementChangedListener {

        @Override
        public void elementChanged(ElementChangedEvent e) {
            if (e.getType() == ElementChangedEvent.POST_CHANGE) {
                handleDelta(e.getDelta());
            }
        }

        private boolean handleDelta(IJavaElementDelta delta) {
            Object element = delta.getElement();
            if (element instanceof IJavaModel) {
                IJavaElementDelta[] projectDeltas = delta.getAffectedChildren();
                for (int i = 0; i < projectDeltas.length; i++) {
                    if (handleDelta(projectDeltas[i]))
                        break;
                }
                return true;
            }
            if (delta.getElement() instanceof IJavaProject) {
                IJavaProject project = (IJavaProject) delta.getElement();
                if (project.getElementName().equals(PROXY_PROJECT_NAME)) {
                    if (delta.getKind() == IJavaElementDelta.REMOVED) {
                        fPluginIdSet.clear();
                    } else if (delta.getKind() == IJavaElementDelta.ADDED) {
                        // We may be getting a queued delta from when the manager was initialized, ignore unless we don't already have data
                        if (fPluginIdSet == null || fPluginIdSet.size() == 0) {
                            // Something other than the manager created the project, check if it has a .searchable file to load from
                            fPluginIdSet = loadStates();
                        }
                    }
                }
                return true;
            }
            return false;
        }
    }

    public  SearchablePluginsManager() {
        fPluginIdSet = loadStates();
        fElementListener = new Listener();
        JavaCore.addElementChangedListener(fElementListener);
        PDECore.getDefault().getModelManager().addPluginModelListener(this);
    }

    private static Set<String> loadStates() {
        Set<String> set = new TreeSet();
        IWorkspaceRoot root = PDECore.getWorkspace().getRoot();
        IProject project = root.getProject(PROXY_PROJECT_NAME);
        try {
            if (project.exists() && project.isOpen()) {
                IFile proxyFile = project.getFile(PROXY_FILE_NAME);
                if (proxyFile.exists()) {
                    Properties properties = new Properties();
                    InputStream stream = proxyFile.getContents(true);
                    properties.load(stream);
                    stream.close();
                    String value = properties.getProperty(KEY);
                    if (value != null) {
                        StringTokenizer stok = new //$NON-NLS-1$
                        StringTokenizer(//$NON-NLS-1$
                        value, //$NON-NLS-1$
                        ",");
                        while (stok.hasMoreTokens()) set.add(stok.nextToken());
                    }
                }
            }
        } catch (IOException e) {
        } catch (CoreException e) {
        }
        return set;
    }

    public IJavaProject getProxyProject() {
        IWorkspaceRoot root = PDECore.getWorkspace().getRoot();
        IProject project = root.getProject(PROXY_PROJECT_NAME);
        try {
            if (project.exists() && project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
                return JavaCore.create(project);
            }
        } catch (CoreException e) {
        }
        return null;
    }

    public void shutdown() {
        // remove listener
        JavaCore.removeElementChangedListener(fElementListener);
        PDECore.getDefault().getModelManager().removePluginModelListener(this);
        if (fListeners != null)
            fListeners.clear();
    }

    public IClasspathEntry[] computeContainerClasspathEntries() throws CoreException {
        ArrayList<IClasspathEntry> result = new ArrayList();
        IPluginModelBase[] wModels = PluginRegistry.getWorkspaceModels();
        for (int i = 0; i < wModels.length; i++) {
            IProject project = wModels[i].getUnderlyingResource().getProject();
            if (project.hasNature(JavaCore.NATURE_ID)) {
                result.add(JavaCore.newProjectEntry(project.getFullPath()));
            }
        }
        Iterator<String> iter = fPluginIdSet.iterator();
        while (iter.hasNext()) {
            ModelEntry entry = PluginRegistry.findEntry(iter.next().toString());
            if (entry != null) {
                boolean addModel = true;
                wModels = entry.getWorkspaceModels();
                for (int i = 0; i < wModels.length; i++) {
                    IProject project = wModels[i].getUnderlyingResource().getProject();
                    if (project.hasNature(JavaCore.NATURE_ID))
                        addModel = false;
                }
                if (!addModel)
                    continue;
                IPluginModelBase[] models = entry.getExternalModels();
                for (int i = 0; i < models.length; i++) {
                    if (models[i].isEnabled())
                        ClasspathUtilCore.addLibraries(models[i], result);
                }
            }
        }
        if (result.size() > 1) {
            // sort
            Map<String, IClasspathEntry> map = new TreeMap();
            for (int i = 0; i < result.size(); i++) {
                IClasspathEntry entry = result.get(i);
                String key = entry.getPath().lastSegment().toString();
                if (map.containsKey(key)) {
                    key += System.currentTimeMillis();
                }
                map.put(key, entry);
            }
            return map.values().toArray(new IClasspathEntry[map.size()]);
        }
        return result.toArray(new IClasspathEntry[result.size()]);
    }

    @Override
    public Object createAdapterChild(FileAdapter parent, File file) {
        if (!file.isDirectory()) {
            if (file.isFile()) {
                IPackageFragmentRoot root = findPackageFragmentRoot(new Path(file.getAbsolutePath()));
                if (root != null)
                    return root;
            }
        }
        return new FileAdapter(parent, file, this);
    }

    private IPackageFragmentRoot findPackageFragmentRoot(IPath jarPath) {
        IJavaProject jProject = getProxyProject();
        if (jProject != null) {
            try {
                IPackageFragmentRoot[] roots = jProject.getAllPackageFragmentRoots();
                for (int i = 0; i < roots.length; i++) {
                    IPackageFragmentRoot root = roots[i];
                    IPath path = root.getPath();
                    if (path.equals(jarPath))
                        return root;
                }
            } catch (JavaModelException e) {
            }
        }
        // Find in other plug-in (and fragments) projects dependencies
        IPluginModelBase[] pluginModels = PluginRegistry.getWorkspaceModels();
        for (int i = 0; i < pluginModels.length; i++) {
            IProject project = pluginModels[i].getUnderlyingResource().getProject();
            IJavaProject javaProject = JavaCore.create(project);
            try {
                IPackageFragmentRoot[] roots = javaProject.getAllPackageFragmentRoots();
                for (int j = 0; j < roots.length; j++) {
                    IPackageFragmentRoot root = roots[j];
                    IPath path = root.getPath();
                    if (path.equals(jarPath))
                        return root;
                }
            } catch (JavaModelException e) {
            }
        }
        return null;
    }

    private void checkForProxyProject() {
        IWorkspaceRoot root = PDECore.getWorkspace().getRoot();
        try {
            IProject project = root.getProject(SearchablePluginsManager.PROXY_PROJECT_NAME);
            if (!project.exists())
                createProxyProject(new NullProgressMonitor());
        } catch (CoreException e) {
        }
    }

    public void addToJavaSearch(IPluginModelBase[] models) {
        checkForProxyProject();
        PluginModelDelta delta = new PluginModelDelta();
        int size = fPluginIdSet.size();
        for (int i = 0; i < models.length; i++) {
            String id = models[i].getPluginBase().getId();
            if (fPluginIdSet.add(id)) {
                ModelEntry entry = PluginRegistry.findEntry(id);
                if (entry != null)
                    delta.addEntry(entry, PluginModelDelta.CHANGED);
            }
        }
        if (fPluginIdSet.size() > size) {
            resetContainer();
            fireDelta(delta);
        }
    }

    public void removeFromJavaSearch(IPluginModelBase[] models) {
        PluginModelDelta delta = new PluginModelDelta();
        int size = fPluginIdSet.size();
        for (int i = 0; i < models.length; i++) {
            String id = models[i].getPluginBase().getId();
            if (fPluginIdSet.remove(id)) {
                ModelEntry entry = PluginRegistry.findEntry(id);
                if (entry != null) {
                    delta.addEntry(entry, PluginModelDelta.CHANGED);
                }
            }
        }
        if (fPluginIdSet.size() < size) {
            resetContainer();
            fireDelta(delta);
        }
    }

    public void removeAllFromJavaSearch() {
        if (fPluginIdSet.size() > 0) {
            PluginModelDelta delta = new PluginModelDelta();
            for (Iterator<String> iterator = fPluginIdSet.iterator(); iterator.hasNext(); ) {
                String id = iterator.next();
                ModelEntry entry = PluginRegistry.findEntry(id);
                if (entry != null) {
                    delta.addEntry(entry, PluginModelDelta.CHANGED);
                }
            }
            fPluginIdSet.clear();
            resetContainer();
            fireDelta(delta);
        }
    }

    public boolean isInJavaSearch(String symbolicName) {
        return fPluginIdSet.contains(symbolicName);
    }

    private void resetContainer() {
        IJavaProject jProject = getProxyProject();
        try {
            if (jProject != null) {
                JavaCore.setClasspathContainer(PDECore.JAVA_SEARCH_CONTAINER_PATH, new IJavaProject[] { jProject }, new IClasspathContainer[] { new ExternalJavaSearchClasspathContainer() }, null);
                saveStates();
            }
        } catch (JavaModelException e) {
        }
    }

    @Override
    public void modelsChanged(PluginModelDelta delta) {
        ModelEntry[] entries = delta.getRemovedEntries();
        for (int i = 0; i < entries.length; i++) {
            if (fPluginIdSet.contains(entries[i].getId())) {
                fPluginIdSet.remove(entries[i].getId());
            }
        }
        resetContainer();
    }

    private void fireDelta(PluginModelDelta delta) {
        if (fListeners != null) {
            for (int i = 0; i < fListeners.size(); i++) {
                fListeners.get(i).modelsChanged(delta);
            }
        }
    }

    public void addPluginModelListener(IPluginModelListener listener) {
        if (fListeners == null)
            fListeners = new ArrayList();
        if (!fListeners.contains(listener))
            fListeners.add(listener);
    }

    public void removePluginModelListener(IPluginModelListener listener) {
        if (fListeners != null)
            fListeners.remove(listener);
    }

    private void saveStates() {
        // persist state
        IWorkspaceRoot root = PDECore.getWorkspace().getRoot();
        IProject project = root.getProject(PROXY_PROJECT_NAME);
        if (project.exists() && project.isOpen()) {
            // modify the .searchable file only if there is any change
            if (loadStates().equals(fPluginIdSet))
                return;
            IFile file = project.getFile(PROXY_FILE_NAME);
            Properties properties = new Properties();
            StringBuffer buffer = new StringBuffer();
            Iterator<String> iter = fPluginIdSet.iterator();
            while (iter.hasNext()) {
                if (buffer.length() > 0)
                    //$NON-NLS-1$
                    buffer.append(//$NON-NLS-1$
                    ",");
                buffer.append(iter.next().toString());
            }
            properties.setProperty(KEY, buffer.toString());
            try {
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                //$NON-NLS-1$
                properties.store(//$NON-NLS-1$
                outStream, //$NON-NLS-1$
                "");
                outStream.flush();
                outStream.close();
                ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
                if (file.exists())
                    file.setContents(inStream, true, false, new NullProgressMonitor());
                else
                    file.create(inStream, true, new NullProgressMonitor());
                inStream.close();
            } catch (IOException e) {
                PDECore.log(e);
            } catch (CoreException e) {
                PDECore.log(e);
            }
        }
    }

    public IProject createProxyProject(IProgressMonitor monitor) throws CoreException {
        IWorkspaceRoot root = PDECore.getWorkspace().getRoot();
        IProject project = root.getProject(SearchablePluginsManager.PROXY_PROJECT_NAME);
        if (project.exists()) {
            if (!project.isOpen()) {
                project.open(monitor);
            }
            return project;
        }
        SubMonitor subMonitor = SubMonitor.convert(monitor, NLS.bind(PDECoreMessages.SearchablePluginsManager_createProjectTaskName, SearchablePluginsManager.PROXY_PROJECT_NAME), 5);
        project.create(subMonitor.split(1));
        project.open(subMonitor.split(1));
        CoreUtility.addNatureToProject(project, JavaCore.NATURE_ID, subMonitor.split(1));
        IJavaProject jProject = JavaCore.create(project);
        jProject.setOutputLocation(project.getFullPath(), subMonitor.split(1));
        computeClasspath(jProject, subMonitor.split(1));
        return project;
    }

    private void computeClasspath(IJavaProject project, IProgressMonitor monitor) {
        IClasspathEntry[] classpath = new IClasspathEntry[2];
        classpath[0] = JavaCore.newContainerEntry(JavaRuntime.newDefaultJREContainerPath());
        classpath[1] = JavaCore.newContainerEntry(PDECore.JAVA_SEARCH_CONTAINER_PATH);
        try {
            project.setRawClasspath(classpath, monitor);
        } catch (JavaModelException e) {
        }
    }
}
