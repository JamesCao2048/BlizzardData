/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.anttasks.tests;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Properties;
import junit.framework.TestCase;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.pde.api.tools.model.tests.TestSuiteHelper;

public abstract class AntRunnerTestCase extends TestCase {

    //$NON-NLS-1$
    public static final String PROJECT_NAME = "pde.apitools";

    //$NON-NLS-1$
    private static final String BUILD_EXCEPTION_CLASS_NAME = "org.apache.tools.ant.BuildException";

    private IFolder buildFolder = null;

    public abstract String getTestResourcesFolder();

    protected IProject newTest() throws Exception {
        IProject builderProject = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
        if (!builderProject.exists()) {
            builderProject.create(null);
        }
        if (!builderProject.isOpen()) {
            builderProject.open(null);
        }
        return builderProject;
    }

    protected IFolder newTest(String resources) throws Exception {
        IProject builderProject = newTest();
        // create build folder for this test
        IPath resourcePath = new Path(resources);
        for (int i = 0, max = resourcePath.segmentCount(); i < max; i++) {
            String segment = resourcePath.segment(i);
            if (i == 0) {
                this.buildFolder = builderProject.getFolder(segment);
            } else {
                this.buildFolder = this.buildFolder.getFolder(segment);
            }
            if (this.buildFolder.exists()) {
                try {
                    this.buildFolder.delete(true, null);
                    this.buildFolder.create(true, true, null);
                } catch (CoreException e) {
                }
            } else {
                this.buildFolder.create(true, true, null);
            }
        }
        IPath pluginDirectoryPath = TestSuiteHelper.getPluginDirectoryPath();
        //$NON-NLS-1$
        String path = pluginDirectoryPath.append(new Path("/test-anttasks/" + resources)).toOSString();
        File sourceFile = new File(path);
        if (!sourceFile.exists()) {
            //$NON-NLS-1$ //$NON-NLS-2$
            System.err.println("Source folder " + path + " is missing");
            return buildFolder;
        } else if (!sourceFile.isDirectory()) {
            //$NON-NLS-1$ //$NON-NLS-2$
            System.err.println("Source folder " + path + " is not a folder");
            return buildFolder;
        }
        path = buildFolder.getLocation().toOSString();
        File destinationFile = new File(path);
        if (!destinationFile.exists()) {
            //$NON-NLS-1$ //$NON-NLS-2$
            System.err.println("Destination folder " + path + " is missing");
            return buildFolder;
        } else if (!destinationFile.isDirectory()) {
            //$NON-NLS-1$ //$NON-NLS-2$
            System.err.println("Destination folder " + path + " is not a folder");
            return buildFolder;
        }
        TestSuiteHelper.copy(sourceFile, destinationFile);
        buildFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
        return buildFolder;
    }

    protected IFolder newTest(String parentFolder, String[] resources) throws Exception {
        buildFolder = newTest(parentFolder + resources[0]);
        if (resources.length > 1) {
            for (int index = 1; index < resources.length; ++index) {
                IPath pluginDirectoryPath = TestSuiteHelper.getPluginDirectoryPath();
                String path = //$NON-NLS-1$
                pluginDirectoryPath.append(new Path("/test-anttasks/" + parentFolder + resources[index])).toOSString();
                File sourceDataFile = new File(path);
                path = buildFolder.getLocation().toOSString();
                File destinationDataFile = new File(path);
                TestSuiteHelper.copy(sourceDataFile, destinationDataFile);
            }
        }
        buildFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
        return buildFolder;
    }

    protected void runAntScript(String script, String[] targets, String antHome, Properties additionalProperties) throws Exception {
        runAntScript(script, targets, antHome, additionalProperties, null, null);
    }

    protected void runAntScript(String script, String[] targets, String antHome, Properties additionalProperties, String listener, String logger) throws Exception {
        String[] args = createAntRunnerArgs(script, targets, antHome, additionalProperties, listener, logger);
        try {
            AntRunner runner = new AntRunner();
            runner.run(args);
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof Exception) {
                throw (Exception) target;
            }
            throw e;
        } finally {
            this.buildFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
        }
    }

    protected String[] createAntRunnerArgs(String script, String[] targets, String antHome, Properties additionalProperties, String listener, String logger) {
        int numArgs = 5 + targets.length + (additionalProperties != null ? additionalProperties.size() : 0);
        if (listener != null) {
            numArgs += 2;
        }
        if (logger != null) {
            numArgs += 2;
        }
        String[] args = new String[numArgs];
        int idx = 0;
        //$NON-NLS-1$
        args[idx++] = "-buildfile";
        args[idx++] = script;
        //$NON-NLS-1$
        args[idx++] = "-logfile";
        //$NON-NLS-1$
        args[idx++] = antHome + "/log.log";
        //$NON-NLS-1$
        args[idx++] = "-Dbuilder=" + antHome;
        if (listener != null) {
            //$NON-NLS-1$
            args[idx++] = "-listener";
            args[idx++] = listener;
        }
        if (logger != null) {
            //$NON-NLS-1$
            args[idx++] = "-logger";
            args[idx++] = logger;
        }
        if (additionalProperties != null && additionalProperties.size() > 0) {
            Enumeration<Object> e = additionalProperties.keys();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                String value = additionalProperties.getProperty(key);
                if (value.length() > 0) {
                    //$NON-NLS-1$ //$NON-NLS-2$
                    args[idx++] = "-D" + key + "=" + additionalProperties.getProperty(key);
                } else {
                    //$NON-NLS-1$
                    args[idx++] = "";
                }
            }
        }
        for (int i = 0; i < targets.length; i++) {
            args[idx++] = targets[i];
        }
        return args;
    }

    public void checkBuildException(Exception e) {
        //$NON-NLS-1$
        assertEquals("Not BuildException", BUILD_EXCEPTION_CLASS_NAME, e.getClass().getCanonicalName());
    }
}
