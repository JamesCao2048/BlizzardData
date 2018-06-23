/*******************************************************************************
 *  Copyright (c) 2003, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ralf Ebert - Bug 307076 : JUnit Plug-in test runner exception "No Classloader found for plug-in ..." is confusing
 *******************************************************************************/
package org.eclipse.pde.internal.junit.runtime;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.junit.runner.RemoteTestRunner;
import org.osgi.framework.Bundle;

/**
 * Runs JUnit tests contained inside a plugin.
 */
public class RemotePluginTestRunner extends RemoteTestRunner {

    private String fTestPluginName;

    private ClassLoader fLoaderClassLoader;

    class BundleClassLoader extends ClassLoader {

        private Bundle bundle;

        public  BundleClassLoader(Bundle target) {
            this.bundle = target;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            return bundle.loadClass(name);
        }

        @Override
        protected URL findResource(String name) {
            return bundle.getResource(name);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        protected Enumeration findResources(String name) throws IOException {
            return bundle.getResources(name);
        }
    }

    /**
	 * The main entry point. Supported arguments in addition
	 * to the ones supported by RemoteTestRunner:
	 * <pre>
	 * -testpluginname: the name of the plugin containing the tests.
	  * </pre>
	 * @see RemoteTestRunner
	 */
    public static void main(String[] args) {
        RemotePluginTestRunner testRunner = new RemotePluginTestRunner();
        testRunner.init(args);
        testRunner.run();
    }

    /**
	 * Returns the Plugin class loader of the plugin containing the test.
	 * @see RemoteTestRunner#getTestClassLoader()
	 */
    @Override
    protected ClassLoader getTestClassLoader() {
        final String pluginId = fTestPluginName;
        return getClassLoader(pluginId);
    }

    public ClassLoader getClassLoader(final String bundleId) {
        Bundle bundle = Platform.getBundle(bundleId);
        if (bundle == null) {
            //$NON-NLS-1$ //$NON-NLS-2$
            throw new IllegalArgumentException("Bundle \"" + bundleId + "\" not found. Possible causes include missing dependencies, too restrictive version ranges, or a non-matching required execution environment.");
        }
        return new BundleClassLoader(bundle);
    }

    @Override
    public void init(String[] args) {
        readPluginArgs(args);
        defaultInit(args);
    }

    public void readPluginArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (//$NON-NLS-1$
            isFlag(args, i, "-testpluginname"))
                fTestPluginName = args[i + 1];
            if (//$NON-NLS-1$
            isFlag(args, i, "-loaderpluginname"))
                fLoaderClassLoader = getClassLoader(args[i + 1]);
        }
        if (fTestPluginName == null)
            //$NON-NLS-1$
            throw new IllegalArgumentException("Parameter -testpluginnname not specified.");
        if (fLoaderClassLoader == null)
            fLoaderClassLoader = getClass().getClassLoader();
    }

    @Override
    protected Class<?> loadTestLoaderClass(String className) throws ClassNotFoundException {
        return fLoaderClassLoader.loadClass(className);
    }

    private boolean isFlag(String[] args, int i, final String wantedFlag) {
        String lowerCase = args[i].toLowerCase(Locale.ENGLISH);
        return lowerCase.equals(wantedFlag) && i < args.length - 1;
    }
}
