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

/**
 * Tests valid annotations on interface methods
 *
 * @since 1.0.600
 */
public class ValidInterfaceMethodAnnotationTests extends InvalidInterfaceMethodAnnotationTests {

    public  ValidInterfaceMethodAnnotationTests(String name) {
        super(name);
    }

    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$
        return super.getTestSourcePath().append("valid");
    }

    /**
	 * @return the test for this class
	 */
    public static Test suite() {
        return buildTestSuite(ValidInterfaceMethodAnnotationTests.class);
    }

    /**
	 * Tests the valid @NoReferrence annotation on interface methods
	 *
	 * @throws Exception
	 */
    public void testNoReferenceF() throws Exception {
        x1(false);
    }

    /**
	 * Tests the valid @NoReferrence annotation on interface methods
	 *
	 * @throws Exception
	 */
    public void testNoReferenceI() throws Exception {
        x1(true);
    }

    @Override
    void x1(boolean inc) {
        //$NON-NLS-1$
        deployAnnotationTest("test2.java", inc, false);
    }
}
