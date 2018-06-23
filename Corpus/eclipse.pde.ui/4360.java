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
import org.eclipse.pde.api.tools.internal.builder.BuilderMessages;

/**
 * Tests invalid annotation in Java 8 interfaces
 */
public class InvalidJava8InterfaceAnnotationTests extends InvalidInterfaceAnnotationTests {

    public  InvalidJava8InterfaceAnnotationTests(String name) {
        super(name);
    }

    @Override
    protected String getTestCompliance() {
        return JavaCore.VERSION_1_8;
    }

    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
        return new Path("annotations").append("java8").append("interface");
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
        return buildTestSuite(InvalidJava8InterfaceAnnotationTests.class);
    }

    public void testInvalidTagOnNonDefaultInterfaceMethodI() {
        x1(true);
    }

    public void testInvalidTagOnNonDefaultInterfaceMethodF() {
        x1(false);
    }

    /**
	 * Tests the unsupported @nooverride tag on non-default interface methods in Java 8 interfaces
	 */
    private void x1(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(2));
        setExpectedMessageArgs(new String[][] { { "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_nondefault_interface_method }, { "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_nondefault_interface_method } });
        //$NON-NLS-1$
        deployAnnotationTest("test1.java", inc, false);
    }

    public void testInvalidTagsOnFunctionalInterfaceI() {
        x2(true);
    }

    public void testInvalidTagsOnFunctionalInterfaceF() {
        x2(false);
    }

    /**
	 * Tests that a variety of tags are unsupported on a functional interface
	 */
    private void x2(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(2));
        setExpectedMessageArgs(new String[][] { { "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_nondefault_interface_method }, { "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_nondefault_interface_method } });
        //$NON-NLS-1$
        deployAnnotationTest("test2.java", inc, false);
    }
}
