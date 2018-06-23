/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.project;

import org.eclipse.core.runtime.IPath;
import org.eclipse.pde.core.project.IBundleClasspathEntry;

/**
 * Defines relationship between a source folder and/or classfile folder and bundle
 * classpath entry.
 */
public class BundleClasspathSpecification implements IBundleClasspathEntry {

    private IPath fSource;

    private IPath fBinary;

    private IPath fEntry;

    /**
	 * Constructs a relationship. Must specify one of <code>sourceFolder</code> or
	 * <code>binaryFolder</code>.
	 *
	 * @param sourceFolder source folder or <code>null</code>
	 * @param binaryFolder binary folder or <code>null</code>
	 * @param entry entry on the Bundle-Classpath header
	 */
    public  BundleClasspathSpecification(IPath sourceFolder, IPath binaryFolder, IPath entry) {
        fSource = sourceFolder;
        fBinary = binaryFolder;
        fEntry = entry;
    }

    @Override
    public IPath getSourcePath() {
        return fSource;
    }

    @Override
    public IPath getBinaryPath() {
        return fBinary;
    }

    @Override
    public IPath getLibrary() {
        return fEntry;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IBundleClasspathEntry) {
            IBundleClasspathEntry spec = (IBundleClasspathEntry) obj;
            return equalOrNull(getSourcePath(), spec.getSourcePath()) && equalOrNull(getBinaryPath(), spec.getBinaryPath()) && equalOrNull(getLibrary(), spec.getLibrary());
        }
        return false;
    }

    private boolean equalOrNull(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        }
        return o1.equals(o2);
    }

    @Override
    public int hashCode() {
        int code = getClass().hashCode();
        if (fSource != null) {
            code += fSource.hashCode();
        }
        if (fBinary != null) {
            code += fBinary.hashCode();
        }
        if (fEntry != null) {
            code += fEntry.hashCode();
        }
        return code;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        //$NON-NLS-1$
        buf.append("Bundle-Claspath: [");
        //$NON-NLS-1$
        buf.append("src=");
        buf.append(fSource);
        //$NON-NLS-1$
        buf.append(" bin=");
        buf.append(fBinary);
        //$NON-NLS-1$
        buf.append(" jar=");
        buf.append(fEntry);
        //$NON-NLS-1$
        buf.append("]");
        return buf.toString();
    }
}
