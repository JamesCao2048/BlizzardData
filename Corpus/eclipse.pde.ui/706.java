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
 * Tests valid annotations on interfaces
 *
 * @since 1.0.600
 */
public class ValidInterfaceAnnotationTests extends InvalidInterfaceAnnotationTests {

    /**
	 * @param name
	 */
    public  ValidInterfaceAnnotationTests(String name) {
        super(name);
    }

    /**
	 * @return the tests for this class
	 */
    public static Test suite() {
        return buildTestSuite(ValidInterfaceAnnotationTests.class);
    }

    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$
        return super.getTestSourcePath().append("valid");
    }

    /**
	 * Tests @NoImplement annotation on interfaces
	 *
	 * @throws Exception
	 */
    public void testNoImplementF() throws Exception {
        x1(false);
    }

    /**
	 * Tests @NoImplement annotation on interfaces
	 *
	 * @throws Exception
	 */
    public void testNoImplementI() throws Exception {
        x1(true);
    }

    private void x1(boolean inc) {
        //$NON-NLS-1$
        deployAnnotationTest("test1.java", inc, false);
    }

    /**
	 * Tests @NoExtend annotation on interfaces
	 *
	 * @throws Exception
	 */
    public void testNoExtendF() throws Exception {
        x2(false);
    }

    /**
	 * Tests @NoExtend annotation on interfaces
	 *
	 * @throws Exception
	 */
    public void testNoExtendI() throws Exception {
        x2(true);
    }

    private void x2(boolean inc) {
        //$NON-NLS-1$
        deployAnnotationTest("test2.java", inc, false);
    }

    /**
	 * Tests @NoReference annotation on interfaces
	 *
	 * @throws Exception
	 */
    public void testNoReferenceF() throws Exception {
        x3(false);
    }

    /**
	 * Tests @NoReference annotation on interfaces
	 *
	 * @throws Exception
	 */
    public void testNoReferenceI() throws Exception {
        x3(true);
    }

    private void x3(boolean inc) {
        //$NON-NLS-1$
        deployAnnotationTest("test3.java", inc, false);
    }
}
