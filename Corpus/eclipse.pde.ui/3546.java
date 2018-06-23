/*******************************************************************************
 *  Copyright (c) 2006, 2007 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.templates;

import java.net.URL;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

    // Shared instance
    private static Activator fInstance;

    public URL getInstallURL() {
        //$NON-NLS-1$
        return getDefault().getBundle().getEntry("/");
    }

    public static Activator getDefault() {
        return fInstance;
    }

    public static String getPluginId() {
        return getDefault().getBundle().getSymbolicName();
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        fInstance = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        fInstance = null;
        super.stop(context);
    }
}
