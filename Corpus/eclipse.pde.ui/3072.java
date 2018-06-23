/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.builder.tests.annotations;

import junit.framework.Test;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;

/**
 * Tests valid annotations on Java 8 interface methods
 */
public class ValidJava8InterfaceAnnotationTests extends ValidInterfaceAnnotationTests {

    public  ValidJava8InterfaceAnnotationTests(String name) {
        super(name);
    }

    @Override
    protected String getTestCompliance() {
        return JavaCore.VERSION_1_8;
    }

    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        return new Path("annotations").append("java8").append("interface").append("valid");
    }

    @Override
    protected String getTestingProjectName() {
        //$NON-NLS-1$
        return "java8tags";
    }

    /**
	 * @return the test for this class
	 */
    public static Test suite() {
        return buildTestSuite(ValidJava8InterfaceAnnotationTests.class);
    }

    public void testNoOverrideOnDefaultI() {
        x1(true);
    }

    public void testNoOverrideOnDefaultF() {
        x1(false);
    }

    /**
	 * Tests the NoOverride annotation on a default method
	 */
    private void x1(boolean inc) {
        //$NON-NLS-1$
        deployAnnotationTest("test1.java", inc, false);
    }

    public void testValidTagsOnFunctionalInterfaceI() {
        x2(true);
    }

    public void testValidTagsOnFunctionalInterfaceF() {
        x2(false);
    }

    /**
	 * Tests a variety of annotations on a functional interface
	 */
    private void x2(boolean inc) {
        //$NON-NLS-1$
        deployAnnotationTest("test2.java", inc, false);
    }
}
