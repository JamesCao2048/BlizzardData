/*******************************************************************************
 * Copyright (c) 2003, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.core.IBundleClasspathResolver;
import org.eclipse.pde.core.build.IBuild;
import org.eclipse.pde.core.build.IBuildEntry;
import org.eclipse.pde.core.plugin.*;
import org.eclipse.pde.internal.core.build.WorkspaceBuildModel;
import org.eclipse.pde.internal.core.project.PDEProject;

public class ClasspathHelper {

    //$NON-NLS-1$
    private static final String DOT = ".";

    //$NON-NLS-1$
    private static final String FRAGMENT_ANNOTATION = "@fragment@";

    public static String getDevEntriesProperties(String fileName, boolean checkExcluded) {
        File file = new File(fileName);
        if (!file.exists()) {
            File directory = file.getParentFile();
            if (directory != null && (!directory.exists() || directory.isFile())) {
                directory.mkdirs();
            }
        }
        Properties properties = new Properties();
        // account for cascading workspaces
        TargetWeaver.weaveDevProperties(properties);
        IPluginModelBase[] models = PluginRegistry.getWorkspaceModels();
        for (int i = 0; i < models.length; i++) {
            String id = models[i].getPluginBase().getId();
            if (id == null)
                continue;
            String entry = writeEntry(getDevPaths(models[i], checkExcluded, null));
            if (entry.length() > 0) {
                String currentValue = (String) properties.get(id);
                if (!entry.equals(currentValue)) {
                    if (currentValue != null)
                        entry = //$NON-NLS-1$
                        currentValue.concat(",").concat(//$NON-NLS-1$
                        entry);
                    properties.put(id, entry);
                }
            }
        }
        //$NON-NLS-1$ //$NON-NLS-2$
        properties.put("@ignoredot@", "true");
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(fileName);
            //$NON-NLS-1$
            properties.store(stream, "");
            stream.flush();
            //$NON-NLS-1$
            return new URL("file:" + fileName).toString();
        } catch (IOException e) {
            PDECore.logException(e);
        } finally {
            try {
                if (stream != null)
                    stream.close();
            } catch (IOException e) {
            }
        }
        return getDevEntries(checkExcluded);
    }

    public static String getDevEntriesProperties(String fileName, Map<?, ?> map) {
        File file = new File(fileName);
        if (!file.exists()) {
            File directory = file.getParentFile();
            if (directory != null && (!directory.exists() || directory.isFile())) {
                directory.mkdirs();
            }
        }
        Properties properties = new Properties();
        // account for cascading workspaces
        TargetWeaver.weaveDevProperties(properties);
        Iterator<?> iter = map.values().iterator();
        while (iter.hasNext()) {
            IPluginModelBase model = (IPluginModelBase) iter.next();
            if (model.getUnderlyingResource() != null) {
                String entry = writeEntry(getDevPaths(model, true, map));
                if (entry.length() > 0) {
                    String id = model.getPluginBase().getId();
                    String currentValue = (String) properties.get(id);
                    if (!entry.equals(currentValue)) {
                        if (currentValue != null)
                            entry = //$NON-NLS-1$
                            currentValue.concat(",").concat(//$NON-NLS-1$
                            entry);
                        properties.put(id, entry);
                    }
                }
            }
        }
        //$NON-NLS-1$ //$NON-NLS-2$
        properties.put("@ignoredot@", "true");
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(fileName);
            //$NON-NLS-1$
            properties.store(stream, "");
            stream.flush();
            //$NON-NLS-1$
            return new URL("file:" + fileName).toString();
        } catch (IOException e) {
            PDECore.logException(e);
        } finally {
            try {
                if (stream != null)
                    stream.close();
            } catch (IOException e) {
            }
        }
        return getDevEntries(true);
    }

    private static String getDevEntries(boolean checkExcluded) {
        IPluginModelBase[] models = PluginRegistry.getWorkspaceModels();
        ArrayList<IPath> list = new ArrayList();
        for (int i = 0; i < models.length; i++) {
            String id = models[i].getPluginBase().getId();
            if (id == null || id.trim().length() == 0)
                continue;
            IPath[] paths = getDevPaths(models[i], checkExcluded, null);
            for (int j = 0; j < paths.length; j++) {
                list.add(paths[j]);
            }
        }
        String entry = writeEntry(list.toArray(new IPath[list.size()]));
        //$NON-NLS-1$
        return entry.length() > 0 ? entry : "bin";
    }

    private static String writeEntry(IPath[] paths) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < paths.length; i++) {
            buffer.append(paths[i].toString());
            if (i < paths.length - 1)
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                ",");
        }
        return buffer.toString();
    }

    // TODO remove - no longer used after bug 217870
    public static Dictionary<String, String> getDevDictionary(IPluginModelBase model) {
        if (model.getUnderlyingResource() == null)
            return null;
        String id = model.getPluginBase().getId();
        if (id == null || id.trim().length() == 0)
            return null;
        IPath[] paths = getDevPaths(model, false, null);
        String entry = writeEntry(paths);
        Hashtable<String, String> map = new Hashtable(2);
        //$NON-NLS-1$ //$NON-NLS-2$
        map.put("@ignoredot@", "true");
        //$NON-NLS-1$
        map.put(id, entry.length() > 0 ? entry : "bin");
        return map;
    }

    // creates a map whose key is a Path to the source directory/jar and the value is a Path output directory or jar.
    private static Map<IPath, ArrayList<IPath>> getClasspathMap(IProject project, boolean checkExcluded, boolean absolutePaths) throws JavaModelException {
        List<Path> excluded = getFoldersToExclude(project, checkExcluded);
        IJavaProject jProject = JavaCore.create(project);
        HashMap<IPath, ArrayList<IPath>> map = new HashMap();
        IClasspathEntry[] entries = jProject.getRawClasspath();
        for (int i = 0; i < entries.length; i++) {
            // most of the paths we get will be project relative, so we need to make the paths relative
            // we will have problems adding an "absolute" path that is workspace relative
            IPath output = null, source = null;
            if (entries[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                source = entries[i].getPath();
                output = entries[i].getOutputLocation();
                if (output == null)
                    output = jProject.getOutputLocation();
            } else if (entries[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
                source = entries[i].getPath();
                output = entries[i].getPath();
                if (source.segmentCount() == 1)
                    source = new Path(DOT);
            }
            if (output != null && !excluded.contains(output)) {
                IResource file = project.findMember(output.removeFirstSegments(1));
                // make the path either relative or absolute
                if (file != null) {
                    boolean isLinked = file.isLinked(IResource.CHECK_ANCESTORS);
                    if (isLinked || absolutePaths) {
                        IPath location = file.getLocation();
                        if (location != null) {
                            output = location.makeAbsolute();
                        } else {
                            PDECore.log(new Status(IStatus.ERROR, PDECore.PLUGIN_ID, NLS.bind(PDECoreMessages.ClasspathHelper_BadFileLocation, file.getFullPath())));
                            continue;
                        }
                    } else {
                        output = output.makeRelative();
                    }
                } else
                    continue;
                ArrayList<IPath> list = map.get(source);
                if (list == null)
                    list = new ArrayList();
                list.add(output);
                map.put(source, list);
            }
        }
        // Add additional entries from contributed bundle classpath resolvers
        IBundleClasspathResolver[] resolvers = PDECore.getDefault().getClasspathContainerResolverManager().getBundleClasspathResolvers(project);
        for (int i = 0; i < resolvers.length; i++) {
            Map<IPath, Collection<IPath>> resolved = resolvers[i].getAdditionalClasspathEntries(jProject);
            Iterator<Entry<IPath, Collection<IPath>>> resolvedIter = resolved.entrySet().iterator();
            while (resolvedIter.hasNext()) {
                Map.Entry<IPath, Collection<IPath>> resolvedEntry = resolvedIter.next();
                IPath ceSource = resolvedEntry.getKey();
                ArrayList<IPath> list = map.get(ceSource);
                if (list == null) {
                    list = new ArrayList();
                    map.put(ceSource, list);
                }
                list.addAll(resolvedEntry.getValue());
            }
        }
        return map;
    }

    // find the corresponding paths for a library name.  Searches for source folders first, but includes any libraries on the buildpath with the same name
    private static IPath[] findLibrary(String libName, IProject project, Map<IPath, ArrayList<IPath>> classpathMap, IBuild build) {
        ArrayList<IPath> paths = new ArrayList();
        IBuildEntry entry = (build != null) ? build.getEntry(IBuildEntry.JAR_PREFIX + libName) : null;
        if (entry != null) {
            String[] resources = entry.getTokens();
            for (int j = 0; j < resources.length; j++) {
                IResource res = project.findMember(resources[j]);
                if (res != null) {
                    ArrayList<IPath> list = classpathMap.get(res.getFullPath());
                    if (list != null) {
                        Iterator<IPath> li = list.iterator();
                        while (li.hasNext()) paths.add(li.next());
                    }
                }
            }
        }
        // search for a library that exists in jar form on the buildpath
        IPath path = null;
        if (libName.equals(DOT))
            path = new Path(DOT);
        else {
            IResource res = project.findMember(libName);
            if (res != null)
                path = res.getFullPath();
            else
                path = new Path(libName);
        }
        List<IPath> list = classpathMap.get(path);
        if (list != null) {
            Iterator<IPath> li = list.iterator();
            while (li.hasNext()) paths.add(li.next());
        }
        return paths.toArray(new IPath[paths.size()]);
    }

    private static IPath[] getDevPaths(IPluginModelBase model, boolean checkExcluded, Map<?, ?> pluginsMap) {
        ArrayList<IPath> result = new ArrayList();
        IProject project = model.getUnderlyingResource().getProject();
        IPluginBase base = model.getPluginBase();
        IPluginLibrary[] libraries = base.getLibraries();
        try {
            if (project.hasNature(JavaCore.NATURE_ID)) {
                Map<IPath, ArrayList<IPath>> classpathMap = getClasspathMap(project, checkExcluded, false);
                IFile file = PDEProject.getBuildProperties(project);
                IPath filePath = file.getLocation();
                boolean searchBuild = filePath != null && filePath.toFile().exists();
                if (searchBuild) {
                    WorkspaceBuildModel bModel = new WorkspaceBuildModel(file);
                    IBuild build = bModel.getBuild();
                    // if it is a custom build, act like there is no build.properties (add everything)
                    IBuildEntry entry = //$NON-NLS-1$
                    build.getEntry(//$NON-NLS-1$
                    "custom");
                    if (entry != null)
                        searchBuild = false;
                    else {
                        if (libraries.length == 0) {
                            IPath[] paths = findLibrary(DOT, project, classpathMap, build);
                            if (paths.length == 0) {
                                // works (see bug 237025)
                                if (!classpathMap.isEmpty()) {
                                    Iterator<ArrayList<IPath>> iterator = classpathMap.values().iterator();
                                    List<IPath> collect = new ArrayList();
                                    while (iterator.hasNext()) {
                                        collect.addAll(iterator.next());
                                    }
                                    paths = collect.toArray(new IPath[collect.size()]);
                                }
                            }
                            for (int j = 0; j < paths.length; j++) addPath(result, project, paths[j]);
                        } else {
                            for (int i = 0; i < libraries.length; i++) {
                                IPath[] paths = findLibrary(libraries[i].getName(), project, classpathMap, build);
                                if (paths.length == 0 && !libraries[i].getName().equals(DOT)) {
                                    paths = findLibraryFromFragments(libraries[i].getName(), model, checkExcluded, pluginsMap);
                                }
                                for (int j = 0; j < paths.length; j++) addPath(result, project, paths[j]);
                            }
                        }
                    }
                }
                if (!searchBuild) {
                    // if no build.properties, add all output folders
                    Iterator<Entry<IPath, ArrayList<IPath>>> it = classpathMap.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<IPath, ArrayList<IPath>> entry = it.next();
                        ArrayList<IPath> list = entry.getValue();
                        ListIterator<IPath> li = list.listIterator();
                        while (li.hasNext()) addPath(result, project, li.next());
                    }
                }
            }
        } catch (JavaModelException e) {
        } catch (CoreException e) {
        }
        return result.toArray(new IPath[result.size()]);
    }

    // looks for fragments for a plug-in.  Then searches the fragments for a specific library.  Will return paths which are absolute (required by runtime)
    private static IPath[] findLibraryFromFragments(String libName, IPluginModelBase model, boolean checkExcluded, Map<?, ?> plugins) {
        IFragmentModel[] frags = PDEManager.findFragmentsFor(model);
        for (int i = 0; i < frags.length; i++) {
            if (plugins != null && !plugins.containsKey(frags[i].getBundleDescription().getSymbolicName()))
                continue;
            // look in project first
            if (frags[i].getUnderlyingResource() != null) {
                try {
                    IProject project = frags[i].getUnderlyingResource().getProject();
                    Map<IPath, ArrayList<IPath>> classpathMap = getClasspathMap(project, checkExcluded, true);
                    IFile file = PDEProject.getBuildProperties(project);
                    IBuild build = null;
                    if (file.exists()) {
                        WorkspaceBuildModel bModel = new WorkspaceBuildModel(file);
                        build = bModel.getBuild();
                    }
                    IPath[] paths = findLibrary(libName, project, classpathMap, build);
                    if (paths.length > 0)
                        return postfixFragmentAnnotation(paths);
                } catch (JavaModelException e) {
                    continue;
                }
            // if external plugin, look in child directories for library
            } else {
                File file = new File(frags[i].getInstallLocation());
                if (file.isDirectory()) {
                    file = new File(file, libName);
                    if (file.exists())
                        // Postfix fragment annotation for fragment path (fix bug 294211)
                        return new IPath[] { new Path(file.getPath() + FRAGMENT_ANNOTATION) };
                }
            }
        }
        return new IPath[0];
    }

    /*
	 * Postfixes the fragment annotation for the paths that we know come
	 * from fragments.  This is needed to fix bug 294211.
	 */
    private static IPath[] postfixFragmentAnnotation(IPath[] paths) {
        for (int i = 0; i < paths.length; i++) paths[i] = new Path(paths[i].toString() + FRAGMENT_ANNOTATION);
        return paths;
    }

    private static void addPath(ArrayList<IPath> result, IProject project, IPath path) {
        IPath resultPath = null;
        if (path.isAbsolute())
            resultPath = path;
        else if (path.segmentCount() > 0 && path.segment(0).equals(project.getName())) {
            IContainer bundleRoot = PDEProject.getBundleRoot(project);
            IPath rootPath = bundleRoot.getFullPath();
            // make path relative to bundle root
            path = path.makeRelativeTo(rootPath);
            if (path.segmentCount() == 0)
                resultPath = new Path(DOT);
            else {
                IResource resource = bundleRoot.findMember(path);
                if (resource != null)
                    resultPath = path;
            }
        }
        if (resultPath != null && !result.contains(resultPath))
            result.add(resultPath);
    }

    private static List<Path> getFoldersToExclude(IProject project, boolean checkExcluded) {
        ArrayList<Path> list = new ArrayList();
        if (checkExcluded) {
            IEclipsePreferences pref = new ProjectScope(project).getNode(PDECore.PLUGIN_ID);
            if (pref != null) {
                String binExcludes = //$NON-NLS-1$
                pref.get(//$NON-NLS-1$
                ICoreConstants.SELFHOSTING_BIN_EXCLUDES, //$NON-NLS-1$
                "");
                StringTokenizer tokenizer = new //$NON-NLS-1$
                StringTokenizer(//$NON-NLS-1$
                binExcludes, //$NON-NLS-1$
                ",");
                while (tokenizer.hasMoreTokens()) {
                    list.add(new Path(tokenizer.nextToken().trim()));
                }
            }
        }
        return list;
    }
}
