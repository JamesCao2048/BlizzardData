/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Carsten Reckord <eclipse@reckord.de> - bug 288343
 *******************************************************************************/
package org.eclipse.pde.internal.junit.runtime;

import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.ui.PlatformUI;

/**
 * A Workbench that runs a test suite specified in the
 * command line arguments.
 */
public class UITestApplication extends NonUIThreadTestApplication {

    //$NON-NLS-1$
    private static final String DEFAULT_APP_3_0 = "org.eclipse.ui.ide.workbench";

    @Override
    protected String getDefaultApplicationId() {
        // In 3.0, the default is the "org.eclipse.ui.ide.worbench" application.
        return DEFAULT_APP_3_0;
    }

    @Override
    protected Object runApp(Object app, IApplicationContext context, String[] args) throws Exception {
        // Get the testable object from the service
        Object testableObject = PDEJUnitRuntimePlugin.getDefault().getTestableObject();
        // Unlike in NonUIThreadTestApplication if the platform dependency is not available we will fail here
        if (testableObject == null) {
            testableObject = PlatformUI.getTestableObject();
        }
        fTestHarness = new PlatformUITestHarness(testableObject, false);
        // continue application launch
        return super.runApp(app, context, args);
    }
}
