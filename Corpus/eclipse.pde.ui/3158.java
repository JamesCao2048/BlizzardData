/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.ui.tests.classpathcontributor;

import java.util.*;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.IClasspathContributor;
import org.eclipse.pde.ui.tests.classpathresolver.ClasspathResolverTest;

/**
 * Test classpath contributor that must be added as a extension for
 * {@link ClasspathContributorTest} to pass.
 *
 */
public class TestClasspathContributor implements IClasspathContributor {

    public static List entries;

    public static List entries2;

    static {
        IPath testPath = ResourcesPlugin.getWorkspace().getRoot().getFullPath().append(new Path("TestPath"));
        entries = new ArrayList();
        entries.add(JavaCore.newContainerEntry(testPath));
        entries.add(JavaCore.newLibraryEntry(testPath, null, null));
        entries.add(JavaCore.newProjectEntry(testPath));
        entries.add(JavaCore.newSourceEntry(testPath));
        IPath testPath2 = ResourcesPlugin.getWorkspace().getRoot().getFullPath().append(new Path("TestPath2"));
        entries2 = new ArrayList();
        entries2.add(JavaCore.newContainerEntry(testPath2));
        entries2.add(JavaCore.newLibraryEntry(testPath2, null, null));
        entries2.add(JavaCore.newProjectEntry(testPath2));
        entries2.add(JavaCore.newSourceEntry(testPath2));
    }

    @Override
    public List getInitialEntries(BundleDescription project) {
        if (project.getSymbolicName().equals(ClasspathResolverTest.bundleName)) {
            return entries;
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List getEntriesForDependency(BundleDescription project, BundleDescription addedDependency) {
        if (project.getSymbolicName().equals(ClasspathResolverTest.bundleName) && addedDependency.getSymbolicName().equals("org.eclipse.pde.core")) {
            return entries2;
        }
        return Collections.EMPTY_LIST;
    }
}
