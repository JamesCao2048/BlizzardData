/*******************************************************************************
 * Copyright (c) Apr 2, 2014 IBM Corporation and others.
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
import org.eclipse.pde.api.tools.internal.builder.BuilderMessages;

/**
 * Tests invalid annotations on interface methods
 *
 * @since 1.0.600
 */
public class InvalidInterfaceMethodAnnotationTests extends MethodAnnotationTest {

    /**
	 * @param name
	 */
    public  InvalidInterfaceMethodAnnotationTests(String name) {
        super(name);
    }

    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$
        return super.getTestSourcePath().append("interface");
    }

    /**
	 * @return the test for this class
	 */
    public static Test suite() {
        return buildTestSuite(InvalidInterfaceMethodAnnotationTests.class);
    }

    public void testNoInstantiateF() throws Exception {
        x1(false);
    }

    public void testNoInstantiateI() throws Exception {
        x1(true);
    }

    void x1(boolean inc) {
        setExpectedProblemIds(getDefaultProblemIdSet(1));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method } });
        //$NON-NLS-1$
        deployAnnotationTestWithErrors("test2.java", inc, false);
    }

    public void testNoImplementF() throws Exception {
        x2(false);
    }

    public void testNoImplementI() throws Exception {
        x2(true);
    }

    void x2(boolean inc) {
        setExpectedProblemIds(getDefaultProblemIdSet(1));
        setExpectedMessageArgs(new String[][] { { "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method } });
        //$NON-NLS-1$
        deployAnnotationTestWithErrors("test3.java", inc, false);
    }

    public void testNoExtendF() throws Exception {
        x3(false);
    }

    public void testNoExtendI() throws Exception {
        x3(true);
    }

    void x3(boolean inc) {
        setExpectedProblemIds(getDefaultProblemIdSet(1));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method } });
        //$NON-NLS-1$
        deployAnnotationTestWithErrors("test4.java", inc, false);
    }

    public void testNoOverrideF() throws Exception {
        x4(false);
    }

    public void testNoOverrideI() throws Exception {
        x4(true);
    }

    void x4(boolean inc) {
        setExpectedProblemIds(getDefaultProblemIdSet(1));
        setExpectedMessageArgs(new String[][] { { "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_nondefault_interface_method } });
        //$NON-NLS-1$
        deployAnnotationTest("test5.java", inc, false);
    }
}
