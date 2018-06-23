/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.ui.tests.target;

import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.core.runtime.CoreException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;

/**
 * Tests for target definitions.  The tested targets will be backed by a workspace file.
 *
 * @see LocalTargetDefinitionTests
 * @since 3.5
 */
public class WorkspaceTargetDefinitionTests extends LocalTargetDefinitionTests {

    private static final String PROJECT_NAME = "WorkspaceTargetDefinitionTests";

    public static Test suite() {
        return new TestSuite(WorkspaceTargetDefinitionTests.class);
    }

    @Override
    protected void setUp() throws Exception {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
        if (!project.exists()) {
            project.create(null);
        }
        assertTrue("Could not create test project", project.exists());
        project.open(null);
        assertTrue("Could not open test project", project.isOpen());
    }

    @Override
    protected void tearDown() throws Exception {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
        if (project.exists()) {
            project.delete(true, null);
        }
        assertFalse("Could not delete test project", project.exists());
    }

    @Override
    protected ITargetDefinition getNewTarget() {
        IFile target = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME).getFile(new Long(System.currentTimeMillis()).toString() + ".target");
        try {
            return getTargetService().getTarget(target).getTargetDefinition();
        } catch (CoreException e) {
            fail(e.getMessage());
        }
        return null;
    }
}
