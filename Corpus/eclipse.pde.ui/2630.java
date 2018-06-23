/*******************************************************************************
 * Copyright (c) 2009, 2015 EclipseSource Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     EclipseSource Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.junit.runtime;

import org.eclipse.core.runtime.*;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.junit.Assert;

/**
 * A Workbench that runs a test suite specified in the
 * command line arguments.
 */
public class NonUIThreadTestApplication implements IApplication {

    //$NON-NLS-1$
    private static final String DEFAULT_HEADLESSAPP = "org.eclipse.pde.junit.runtime.coretestapplication";

    protected IApplication fApplication;

    protected Object fTestHarness;

    @Override
    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        String appId = getApplicationToRun(args);
        Object app = getApplication(appId);
        Assert.assertNotNull(app);
        if (!DEFAULT_HEADLESSAPP.equals(appId)) {
            // this means we are running a different application, which potentially can be UI application;
            // non-ui thread test app can also mean we are running UI tests but outside the UI thread;
            // this is a pattern used by SWT bot and worked before; we continue to support this
            // (see bug 340906 for details)
            installPlatformUITestHarness();
        }
        return runApp(app, context, args);
    }

    protected Object runApp(Object app, IApplicationContext context, String[] args) throws Exception {
        if (app instanceof IApplication) {
            fApplication = (IApplication) app;
            return fApplication.start(context);
        }
        return ((IPlatformRunnable) app).run(args);
    }

    /**
	 * the non-UI thread test application also supports launching headless applications;
	 * this may mean that no UI bundle will be available; thus, in order to not
	 * introduce any dependency on UI code we first attempt to get the harness via service.
	 * If that doesn't work, we use reflection to call the workbench code, but don't fail
	 * if Platform UI is not available.
	 *
	 * @throws Exception
	 */
    private void installPlatformUITestHarness() throws Exception {
        Object testableObject = PDEJUnitRuntimePlugin.getDefault().getTestableObject();
        if (testableObject == null) {
            try {
                Class<?> platformUIClass = //$NON-NLS-1$
                Class.forName(//$NON-NLS-1$
                "org.eclipse.ui.PlatformUI", //$NON-NLS-1$
                true, //$NON-NLS-1$
                getClass().getClassLoader());
                testableObject = //$NON-NLS-1$
                platformUIClass.getMethod("getTestableObject").invoke(//$NON-NLS-1$
                null);
            } catch (ClassNotFoundException e) {
            }
        }
        if (testableObject != null) {
            fTestHarness = new PlatformUITestHarness(testableObject, true);
        }
    }

    @Override
    public void stop() {
        if (fApplication != null)
            fApplication.stop();
        if (fTestHarness != null)
            fTestHarness = null;
    }

    /*
	 * return the application to run, or null if not even the default application
	 * is found.
	 */
    private Object getApplication(String appId) throws CoreException {
        // Find the name of the application as specified by the PDE JUnit launcher.
        // If no application is specified, the 3.0 default workbench application
        // is returned.
        IExtension extension = Platform.getExtensionRegistry().getExtension(Platform.PI_RUNTIME, Platform.PT_APPLICATIONS, appId);
        Assert.assertNotNull(extension);
        // If the extension does not have the correct grammar, return null.
        // Otherwise, return the application object.
        IConfigurationElement[] elements = extension.getConfigurationElements();
        if (elements.length > 0) {
            //$NON-NLS-1$
            IConfigurationElement[] runs = elements[0].getChildren("run");
            if (runs.length > 0) {
                Object runnable = //$NON-NLS-1$
                runs[0].createExecutableExtension(//$NON-NLS-1$
                "class");
                if (runnable instanceof IPlatformRunnable || runnable instanceof IApplication)
                    return runnable;
            }
        }
        return null;
    }

    /*
	 * The -testApplication argument specifies the application to be run.
	 * If the PDE JUnit launcher did not set this argument, then return
	 * the name of the default application.
	 * In 3.0, the default is the "org.eclipse.ui.ide.worbench" application.
	 *
	 * see bug 228044
	 *
	 */
    private String getApplicationToRun(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (//$NON-NLS-1$
            args[i].equals("-testApplication") && i < args.length - 1)
                return args[i + 1];
        }
        IProduct product = Platform.getProduct();
        if (product != null)
            return product.getApplication();
        return getDefaultApplicationId();
    }

    protected String getDefaultApplicationId() {
        return DEFAULT_HEADLESSAPP;
    }
}
