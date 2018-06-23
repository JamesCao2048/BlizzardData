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
 * Tests valid uses of @noreference on interface fields
 *
 * @since 1.0
 */
public class ValidInterfaceFieldTagTests extends ValidFieldTagTests {

    /**
	 * Constructor
	 * @param name
	 */
    public  ValidInterfaceFieldTagTests(String name) {
        super(name);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.tags.ValidJavadocTagFieldTests#getTestSourcePath()
	 */
    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$
        return super.getTestSourcePath().append("interface");
    }

    /**
	 * @return the test for this class
	 */
    public static Test suite() {
        return buildTestSuite(ValidInterfaceFieldTagTests.class);
    }

    /**
	 * Tests the valid use of an @noreference tag on a field in an interface
	 * using an incremental build
	 */
    public void testValidInterfaceFieldTag1I() {
        x1(true);
    }

    /**
	 * Tests the valid use of an @noreference tag on a field in an interface
	 * using a full build
	 */
    public void testValidInterfaceFieldTag1F() {
        x1(false);
    }

    private void x1(boolean inc) {
        //$NON-NLS-1$
        deployTagTest("test1.java", inc, false);
    }

    /**
	 * Tests the valid use of an @noreference tag on a field in an inner interface
	 * using an incremental build
	 */
    public void testValidInterfaceFieldTag3I() {
        x3(true);
    }

    /**
	 * Tests the valid use of an @noreference tag on a field in an inner interface
	 * using a full build
	 */
    public void testValidInterfaceFieldTag3F() {
        x3(false);
    }

    private void x3(boolean inc) {
        //$NON-NLS-1$
        deployTagTest("test3.java", inc, false);
    }

    /**
	 * Tests the valid use of an @noreference tag on fields in inner / outer interfaces
	 * using an incremental build
	 */
    public void testValidInterfaceFieldTag4I() {
        x4(true);
    }

    /**
	 * Tests the valid use of an @noreference tag on fields in inner / outer interfaces
	 * using a full build
	 */
    public void testValidInterfaceFieldTag4F() {
        x4(false);
    }

    private void x4(boolean inc) {
        //$NON-NLS-1$
        deployTagTest("test4.java", inc, false);
    }

    /**
	 * Tests the valid use of an @noreference tag on fields in interfaces
	 * using an incremental build
	 */
    public void testValidInterfaceFieldTag5I() {
        x5(true);
    }

    /**
	 * Tests the valid use of an @noreference tag on fields in interfaces
	 * using a full build
	 */
    public void testValidInterfaceFieldTag5F() {
        x5(false);
    }

    private void x5(boolean inc) {
        //$NON-NLS-1$
        deployTagTest("test5.java", inc, false);
    }
}
