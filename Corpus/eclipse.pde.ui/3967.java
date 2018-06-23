/*******************************************************************************
 * Copyright (c) 2011, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Christian Georgi, SAP SE - Bug 453894
 *******************************************************************************/
package org.eclipse.pde.internal.core.util;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.*;
import org.eclipse.pde.internal.core.PDECore;

/**
 * Tests whether an object is a package fragment root with
 * a parent of the PDE classpath containers "Plug-in Dependencies or "External Plug-in Libraries".
 * Intended for use with the command/menu framework.  This tester is registered for the type
 * {@link IPackageFragmentRoot}.
 *
 * <p>This class must always have a default constructor to function as a property tester</p>
 */
public class PackageFragmentRootPropertyTester extends PropertyTester {

    /**
	 * A property indicating a whether a package fragment root belongs to the PDE
	 * classpath container.  (value <code>"inPluginContainer"</code>). No expected
	 * value is required.
	 */
    //$NON-NLS-1$
    public static final String PROP_IN_PLUGIN_CONTAINER = "inPluginContainer";

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (property.equals(PROP_IN_PLUGIN_CONTAINER)) {
            if (receiver instanceof IPackageFragmentRoot) {
                IPackageFragmentRoot element = (IPackageFragmentRoot) receiver;
                try {
                    IClasspathEntry entry = element.getRawClasspathEntry();
                    IPath path = entry.getPath();
                    if (path.equals(PDECore.REQUIRED_PLUGINS_CONTAINER_PATH)) {
                        return true;
                    }
                    if (path.equals(PDECore.JAVA_SEARCH_CONTAINER_PATH)) {
                        return true;
                    }
                } catch (JavaModelException e) {
                    return false;
                }
            }
        }
        return false;
    }
}
