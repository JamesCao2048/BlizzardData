/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.ui.tests.runtime;

import java.util.Collections;
import org.eclipse.core.runtime.*;
import org.eclipse.pde.ui.tests.PDETestsPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * Utility methods for JUnit tests.
 */
@SuppressWarnings("deprecation")
public class TestUtils {

    // We use package admin to access bundles during the tests
    private static PackageAdmin packageAdmin;

    public static Bundle getBundle(String symbolicName) {
        if (packageAdmin == null) {
            packageAdmin = (PackageAdmin) PDETestsPlugin.getBundleContext().getService(PDETestsPlugin.getBundleContext().getServiceReference(PackageAdmin.class.getName()));
        }
        Bundle[] bundles = packageAdmin.getBundles(symbolicName, null);
        if (bundles != null) {
            return bundles[0];
        }
        return null;
    }

    public static IExtensionPoint getExtensionPoint(String extensionPointId) {
        return Platform.getExtensionRegistry().getExtensionPoint(extensionPointId);
    }

    public static IExtension getExtension(String extensionId) {
        return Platform.getExtensionRegistry().getExtension(extensionId);
    }

    public static ServiceReference getServiceReference(String clazzName) {
        return PDETestsPlugin.getBundleContext().getServiceReference(clazzName);
    }

    public static String findPath(String path) {
        return FileLocator.find(PDETestsPlugin.getBundleContext().getBundle(), new Path(path), Collections.EMPTY_MAP).toString();
    }
}
