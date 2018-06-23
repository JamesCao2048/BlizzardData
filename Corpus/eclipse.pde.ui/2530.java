/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.apiusescan.tests;

import junit.framework.TestCase;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.pde.api.tools.internal.provisional.IApiMarkerConstants;

public class ExternalDependencyProblemMarkerTests extends TestCase {

    private IJavaProject fProject;

    @Override
    protected void setUp() throws Exception {
        IProject setupProject = ExternalDependencyTestUtils.setupProject();
        if (setupProject == null) {
            //$NON-NLS-1$
            fail("Unable to setup the project. Can not run the test cases");
            return;
        }
        fProject = JavaCore.create(setupProject);
        //$NON-NLS-1$
        String location = ExternalDependencyTestUtils.setupReport("reportAll", true);
        if (location == null) {
            //$NON-NLS-1$
            fail("Could not setup the report : reportAll.zip");
        }
    }

    public void testMissingType() {
        try {
            //$NON-NLS-1$
            IType type = fProject.findType("tests.apiusescan.coretestproject.IConstants");
            //$NON-NLS-1$
            type.rename("IConstants1", true, null);
            IProject project = fProject.getProject();
            ExternalDependencyTestUtils.waitForBuild();
            IMarker[] markers = project.findMarkers(IApiMarkerConstants.API_USESCAN_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
            //$NON-NLS-1$
            assertEquals("No API Use Scan problem marker found for missing type IConstants", 1, markers.length);
            String typeName = markers[0].getAttribute(IApiMarkerConstants.API_USESCAN_TYPE, null);
            //$NON-NLS-1$ //$NON-NLS-2$
            assertEquals("Marker for missing type IConstants not found", "tests.apiusescan.coretestproject.IConstants", typeName);
            //$NON-NLS-1$
            type = fProject.findType("tests.apiusescan.coretestproject.IConstants1");
            //$NON-NLS-1$
            type.rename("IConstants", true, null);
            ExternalDependencyTestUtils.waitForBuild();
            markers = project.findMarkers(IApiMarkerConstants.API_USESCAN_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
            //$NON-NLS-1$
            assertEquals("API Use Scan problem marker for missing type IConstants did not clear", 0, markers.length);
        } catch (JavaModelException e) {
            fail(e.getMessage());
        } catch (CoreException e) {
            fail(e.getMessage());
        }
    }

    public void testMissingMethod() {
        try {
            //$NON-NLS-1$
            IType type = fProject.findType("tests.apiusescan.coretestproject.ITestInterface");
            IMethod method = type.getMethods()[0];
            //$NON-NLS-1$
            method.rename("performTask1", true, null);
            ExternalDependencyTestUtils.waitForBuild();
            IMarker[] markers = type.getUnderlyingResource().findMarkers(IApiMarkerConstants.API_USESCAN_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
            //$NON-NLS-1$
            assertEquals("No API Use Scan problem marker found for missing method ITestInterface.performTask()", 1, markers.length);
            String typeName = markers[0].getAttribute(IApiMarkerConstants.API_USESCAN_TYPE, null);
            //$NON-NLS-1$ //$NON-NLS-2$
            assertEquals("Marker for missing method ITestInterface.performTask() not found", "tests.apiusescan.coretestproject.ITestInterface", typeName);
            //$NON-NLS-1$
            type = fProject.findType("tests.apiusescan.coretestproject.ITestInterface");
            method = type.getMethods()[0];
            //$NON-NLS-1$
            method.rename("performTask", true, null);
            ExternalDependencyTestUtils.waitForBuild();
            markers = type.getUnderlyingResource().findMarkers(IApiMarkerConstants.API_USESCAN_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
            //$NON-NLS-1$
            assertEquals("API Use Scan problem marker for missing method ITestInterface.performTask() did not clear.", 0, markers.length);
        } catch (JavaModelException e) {
            fail(e.getMessage());
        } catch (CoreException e) {
            fail(e.getMessage());
        }
    }

    public void testMissingField() {
        try {
            //$NON-NLS-1$
            IType type = fProject.findType("tests.apiusescan.coretestproject.TestInterfaceImpl");
            //$NON-NLS-1$
            IField field = type.getField("fField");
            //$NON-NLS-1$
            field.rename("fField1", true, null);
            ExternalDependencyTestUtils.waitForBuild();
            IMarker[] markers = type.getUnderlyingResource().findMarkers(IApiMarkerConstants.API_USESCAN_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
            //$NON-NLS-1$
            assertEquals("No API Use Scan problem marker found for missing field TestInterfaceImpl.fField", 1, markers.length);
            String typeName = markers[0].getAttribute(IApiMarkerConstants.API_USESCAN_TYPE, null);
            //$NON-NLS-1$ //$NON-NLS-2$
            assertEquals("Marker for missing field TestInterfaceImpl.fField not found", "tests.apiusescan.coretestproject.TestInterfaceImpl", typeName);
            //$NON-NLS-1$
            type = fProject.findType("tests.apiusescan.coretestproject.TestInterfaceImpl");
            //$NON-NLS-1$
            field = type.getField("fField1");
            //$NON-NLS-1$
            field.rename("fField", true, null);
            ExternalDependencyTestUtils.waitForBuild();
            markers = type.getUnderlyingResource().findMarkers(IApiMarkerConstants.API_USESCAN_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
            //$NON-NLS-1$
            assertEquals("API Use Scan problem marker for missing field TestInterfaceImpl.fField did not clear.", 0, markers.length);
        } catch (JavaModelException e) {
            fail(e.getMessage());
        } catch (CoreException e) {
            fail(e.getMessage());
        }
    }

    public void testMissingInnerType() {
        try {
            //$NON-NLS-1$
            IType type = fProject.findType("tests.apiusescan.coretestproject.ClassWithInnerType.InnerType");
            //$NON-NLS-1$
            type.rename("InnerType1", true, null);
            IProject project = fProject.getProject();
            ExternalDependencyTestUtils.waitForBuild();
            IMarker[] markers = project.findMarkers(IApiMarkerConstants.API_USESCAN_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
            //$NON-NLS-1$
            assertEquals("No API Use Scan problem marker found for missing type IConstants", 1, markers.length);
            String typeName = markers[0].getAttribute(IApiMarkerConstants.API_USESCAN_TYPE, null);
            //$NON-NLS-1$ //$NON-NLS-2$
            assertEquals("Marker for missing type InnerType not found", "tests.apiusescan.coretestproject.ClassWithInnerType.InnerType", typeName);
            //$NON-NLS-1$
            type = fProject.findType("tests.apiusescan.coretestproject.ClassWithInnerType.InnerType1");
            //$NON-NLS-1$
            type.rename("InnerType", true, null);
            ExternalDependencyTestUtils.waitForBuild();
            markers = project.findMarkers(IApiMarkerConstants.API_USESCAN_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
            //$NON-NLS-1$
            assertEquals("API Use Scan problem marker for missing type InnerType did not clear", 0, markers.length);
        } catch (JavaModelException e) {
            fail(e.getMessage());
        } catch (CoreException e) {
            fail(e.getMessage());
        }
    }
}
