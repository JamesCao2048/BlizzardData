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
package org.eclipse.pde.api.tools.builder.tests.tags;

import junit.framework.Test;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;

/**
 * Tests valid tags on Java 8 interface methods
 */
public class ValidJava8InterfaceTagTests extends ValidInterfaceMethodTagTests {

    public  ValidJava8InterfaceTagTests(String name) {
        super(name);
    }

    @Override
    protected String getTestCompliance() {
        return JavaCore.VERSION_1_8;
    }

    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
        return new Path("tags").append("java8").append("interface").append("valid");
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
        return buildTestSuite(ValidJava8InterfaceTagTests.class);
    }

    public void testNoOverrideOnDefaultI() {
        x1(true);
    }

    public void testNoOverrideOnDefaultF() {
        x1(false);
    }

    /**
	 * Tests the @nooverride tag on a default method
	 */
    private void x1(boolean inc) {
        //$NON-NLS-1$
        deployTagTest("test1.java", inc, false);
    }

    public void testValidTagsOnFunctionalInterfaceI() {
        x2(true);
    }

    public void testValidTagsOnFunctionalInterfaceF() {
        x2(false);
    }

    /**
	 * Tests a variety of tags on a functional interface
	 */
    private void x2(boolean inc) {
        //$NON-NLS-1$
        deployTagTest("test2.java", inc, false);
    }
}
