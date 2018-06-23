/*******************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.search;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.eclipse.pde.core.plugin.*;
import org.eclipse.pde.internal.core.ClasspathUtilCore;
import org.eclipse.pde.internal.core.PDEManager;

public class PluginJavaSearchUtil {

    public static IPluginModelBase[] getPluginImports(IPluginImport dep) {
        HashSet<IPluginModelBase> set = new HashSet();
        VersionRange range = new VersionRange(dep.getVersion());
        collectAllPrerequisites(PluginRegistry.findModel(dep.getId(), range, null), set);
        return set.toArray(new IPluginModelBase[set.size()]);
    }

    public static IPluginModelBase[] getPluginImports(String pluginImportID) {
        HashSet<IPluginModelBase> set = new HashSet();
        collectAllPrerequisites(PluginRegistry.findModel(pluginImportID), set);
        return set.toArray(new IPluginModelBase[set.size()]);
    }

    public static void collectAllPrerequisites(IPluginModelBase model, HashSet<IPluginModelBase> set) {
        if (model == null || !set.add(model))
            return;
        IPluginImport[] imports = model.getPluginBase().getImports();
        for (int i = 0; i < imports.length; i++) {
            if (imports[i].isReexported()) {
                IPluginModelBase child = PluginRegistry.findModel(imports[i].getId());
                if (child != null)
                    collectAllPrerequisites(child, set);
            }
        }
    }

    public static IPackageFragment[] collectPackageFragments(IPluginModelBase[] models, IJavaProject parentProject, boolean filterEmptyPackages) throws JavaModelException {
        ArrayList<IPackageFragment> result = new ArrayList();
        IPackageFragmentRoot[] roots = parentProject.getAllPackageFragmentRoots();
        for (int i = 0; i < models.length; i++) {
            IPluginModelBase model = models[i];
            IResource resource = model.getUnderlyingResource();
            if (resource == null) {
                ArrayList<Path> libraryPaths = new ArrayList();
                addLibraryPaths(model, libraryPaths);
                for (int j = 0; j < roots.length; j++) {
                    if (libraryPaths.contains(roots[j].getPath())) {
                        extractPackageFragments(roots[j], result, filterEmptyPackages);
                    }
                }
            } else {
                IProject project = resource.getProject();
                for (int j = 0; j < roots.length; j++) {
                    IJavaProject jProject = (IJavaProject) roots[j].getParent();
                    if (jProject.getProject().equals(project)) {
                        extractPackageFragments(roots[j], result, filterEmptyPackages);
                    }
                }
            }
        }
        return result.toArray(new IPackageFragment[result.size()]);
    }

    private static void extractPackageFragments(IPackageFragmentRoot root, ArrayList<IPackageFragment> result, boolean filterEmpty) {
        try {
            IJavaElement[] children = root.getChildren();
            for (int i = 0; i < children.length; i++) {
                IPackageFragment fragment = (IPackageFragment) children[i];
                if (!filterEmpty || fragment.hasChildren())
                    result.add(fragment);
            }
        } catch (JavaModelException e) {
        }
    }

    private static void addLibraryPaths(IPluginModelBase model, ArrayList<Path> libraryPaths) {
        IPluginBase plugin = model.getPluginBase();
        IFragmentModel[] fragments = new IFragmentModel[0];
        if (plugin instanceof IPlugin)
            fragments = PDEManager.findFragmentsFor(model);
        File file = new File(model.getInstallLocation());
        if (file.isFile()) {
            libraryPaths.add(new Path(file.getAbsolutePath()));
        } else {
            IPluginLibrary[] libraries = plugin.getLibraries();
            for (int i = 0; i < libraries.length; i++) {
                String libraryName = ClasspathUtilCore.expandLibraryName(libraries[i].getName());
                String path = plugin.getModel().getInstallLocation() + IPath.SEPARATOR + libraryName;
                if (new File(path).exists()) {
                    libraryPaths.add(new Path(path));
                } else {
                    findLibraryInFragments(fragments, libraryName, libraryPaths);
                }
            }
        }
        if (ClasspathUtilCore.hasExtensibleAPI(model)) {
            for (int i = 0; i < fragments.length; i++) {
                addLibraryPaths(fragments[i], libraryPaths);
            }
        }
    }

    private static void findLibraryInFragments(IFragmentModel[] fragments, String libraryName, ArrayList<Path> libraryPaths) {
        for (int i = 0; i < fragments.length; i++) {
            String path = fragments[i].getInstallLocation() + IPath.SEPARATOR + libraryName;
            if (new File(path).exists()) {
                libraryPaths.add(new Path(path));
                break;
            }
        }
    }

    public static IJavaSearchScope createSeachScope(IJavaProject jProject) throws JavaModelException {
        IPackageFragmentRoot[] roots = jProject.getPackageFragmentRoots();
        ArrayList<IPackageFragmentRoot> filteredRoots = new ArrayList();
        for (int i = 0; i < roots.length; i++) {
            if (roots[i].getResource() != null && roots[i].getResource().getProject().equals(jProject.getProject())) {
                filteredRoots.add(roots[i]);
            }
        }
        return SearchEngine.createJavaSearchScope(filteredRoots.toArray(new IJavaElement[filteredRoots.size()]));
    }
}
