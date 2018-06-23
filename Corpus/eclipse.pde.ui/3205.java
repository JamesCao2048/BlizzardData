/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
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

/**
 * Tests the tags that are valid on a class
 *
 * @since 1.0
 */
public class ValidClassTagTests extends InvalidClassTagTests {

    /**
	 * Constructor
	 * @param name
	 */
    public  ValidClassTagTests(String name) {
        super(name);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.InvalidJavadocTagClassTests#getTestSourcePath()
	 */
    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$
        return super.getTestSourcePath().append("valid");
    }

    /**
	 * @return the tests for this class
	 */
    public static Test suite() {
        return buildTestSuite(ValidClassTagTests.class);
    }

    /**
	 * Tests that @noextend and @noinstantiate are valid tags on a class in the
	 * the testing package a.b.c using an incremental build
	 */
    public void testValidClassTag1I() {
        x1(true);
    }

    /**
	 * Tests that @noextend and @noinstantiate are valid tags on a class in the
	 * the testing package a.b.c using a full build
	 */
    public void testValidClassTag1F() {
        x1(false);
    }

    private void x1(boolean inc) {
        //$NON-NLS-1$
        deployTagTest("test1.java", inc, false);
    }

    /**
	 * Tests that @noextend and @noinstantiate are valid tags on an inner class in the
	 * the testing package a.b.c using an incremental build
	 */
    public void testValidClassTag5I() {
        x5(true);
    }

    /**
	 * Tests that @noextend and @noinstantiate are valid tags on an inner class in the
	 * the testing package a.b.c using a full build
	 */
    public void testValidClassTag5F() {
        x5(false);
    }

    private void x5(boolean inc) {
        //$NON-NLS-1$
        deployTagTest("test5.java", inc, false);
    }
}
