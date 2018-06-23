/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
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
 * Tests valid use of annotations on enums
 *
 * @since 1.0.400
 */
public class ValidEnumAnnotationsTests extends InvalidEnumAnnotationsTests {

    /**
	 * Constructor
	 *
	 * @param name
	 */
    public  ValidEnumAnnotationsTests(String name) {
        super(name);
    }

    /**
	 * @return the tests for this class
	 */
    public static Test suite() {
        return buildTestSuite(ValidEnumAnnotationsTests.class);
    }

    /*
	 * (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.annotations.
	 * InvalidClassAnnotationsTests#getTestSourcePath()
	 */
    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$
        return super.getTestSourcePath().append("valid");
    }

    public void testValidEnumTag1I() {
        x1(true);
    }

    @Override
    public void testInvalidEnumTag1F() {
        x1(false);
    }

    /**
	 * Tests having an @NoReference tag on an enum in the default package
	 */
    private void x1(boolean inc) {
        //$NON-NLS-1$
        deployAnnotationTest("test1.java", inc, true);
    }
}
