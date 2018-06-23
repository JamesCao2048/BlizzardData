/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.tests;

import org.eclipse.core.runtime.Plugin;

/**
 * Test Plug-in Class
 */
public class ApiTestsPlugin extends Plugin {

    //$NON-NLS-1$
    public static final String PLUGIN_ID = "org.eclipse.pde.api.tools.tests";

    private static ApiTestsPlugin fgDefault = null;

    public  ApiTestsPlugin() {
        fgDefault = this;
    }

    /**
	 * Returns the test plug-in.
	 *
	 * @return the test plug-in
	 */
    public static ApiTestsPlugin getDefault() {
        return fgDefault;
    }
}
