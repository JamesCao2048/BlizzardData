/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver;
import org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver2;
import org.eclipse.jdt.launching.IVMInstall;

/**
 * Proxy to a runtime classpath entry resolver extension.
 */
public class RuntimeClasspathEntryResolver implements IRuntimeClasspathEntryResolver2 {

    private IConfigurationElement fConfigurationElement;

    private IRuntimeClasspathEntryResolver fDelegate;

    /**
	 * Constructs a new resolver on the given configuration element
	 * @param element the element
	 */
    public  RuntimeClasspathEntryResolver(IConfigurationElement element) {
        fConfigurationElement = element;
    }

    /**
	 * @see IRuntimeClasspathEntryResolver#resolveRuntimeClasspathEntry(IRuntimeClasspathEntry, ILaunchConfiguration)
	 */
    @Override
    public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, ILaunchConfiguration configuration) throws CoreException {
        return getResolver().resolveRuntimeClasspathEntry(entry, configuration);
    }

    /**
	 * Returns the resolver delegate (and creates if required) 
	 * @return the resolver
	 * @throws CoreException if an error occurs 
	 */
    protected IRuntimeClasspathEntryResolver getResolver() throws CoreException {
        if (fDelegate == null) {
            //$NON-NLS-1$
            fDelegate = (IRuntimeClasspathEntryResolver) fConfigurationElement.createExecutableExtension("class");
        }
        return fDelegate;
    }

    /**
	 * Returns the variable name this resolver is registered for, or <code>null</code>
	 * @return the variable name or <code>null</code>
	 */
    public String getVariableName() {
        //$NON-NLS-1$
        return fConfigurationElement.getAttribute("variable");
    }

    /**
	 * Returns the container id this resolver is registered for, or <code>null</code>
	 * @return the id or <code>null</code>
	 */
    public String getContainerId() {
        //$NON-NLS-1$
        return fConfigurationElement.getAttribute("container");
    }

    /**
	 * Returns the runtime classpath entry id this resolver is registered
	 * for,or <code>null</code> if none.
	 * @return the entry id or <code>null</code>
	 */
    public String getRuntimeClasspathEntryId() {
        //$NON-NLS-1$
        return fConfigurationElement.getAttribute("runtimeClasspathEntryId");
    }

    /**
	 * @see IRuntimeClasspathEntryResolver#resolveVMInstall(IClasspathEntry)
	 */
    @Override
    public IVMInstall resolveVMInstall(IClasspathEntry entry) throws CoreException {
        return getResolver().resolveVMInstall(entry);
    }

    /**
	 * @see IRuntimeClasspathEntryResolver#resolveRuntimeClasspathEntry(IRuntimeClasspathEntry, IJavaProject)
	 */
    @Override
    public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, IJavaProject project) throws CoreException {
        return getResolver().resolveRuntimeClasspathEntry(entry, project);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver2#isVMInstallReference(org.eclipse.jdt.core.IClasspathEntry)
	 */
    @Override
    public boolean isVMInstallReference(IClasspathEntry entry) {
        try {
            IRuntimeClasspathEntryResolver resolver = getResolver();
            if (resolver instanceof IRuntimeClasspathEntryResolver2) {
                IRuntimeClasspathEntryResolver2 resolver2 = (IRuntimeClasspathEntryResolver2) resolver;
                return resolver2.isVMInstallReference(entry);
            }
            return resolver.resolveVMInstall(entry) != null;
        } catch (CoreException e) {
            return false;
        }
    }
}
