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
package org.eclipse.pde.api.tools.builder.tests.tags;

import junit.framework.Test;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;

/**
 * Tests that the builder accepts valid tags on annotations
 */
public class ValidAnnotationTagTests extends InvalidAnnotationTagTests {

    /**
	 * Constructor
	 * @param name
	 */
    public  ValidAnnotationTagTests(String name) {
        super(name);
    }

    /**
	 * @return the tests for this class
	 */
    public static Test suite() {
        return buildTestSuite(ValidAnnotationTagTests.class);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.ApiBuilderTests#getTestSourcePath()
	 */
    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$
        return super.getTestSourcePath().append("valid");
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.ApiBuilderTest#getTestCompliance()
	 */
    @Override
    protected String getTestCompliance() {
        return JavaCore.VERSION_1_5;
    }

    public void testInvalidAnnotationTag1I() {
        x1(true);
    }

    public void testInvalidAnnotationTag1F() {
        x1(false);
    }

    /**
	 * Tests having an @noreference tag on a variety of annotations in package a.b.c
	 */
    private void x1(boolean inc) {
        //$NON-NLS-1$
        String typename = "test1.java";
        deployTagTest(typename, inc, false);
    }

    public void testInvalidAnnotationTag2I() {
        x2(true);
    }

    public void testInvalidAnnotationTag2F() {
        x2(false);
    }

    /**
	 * Tests having an @noreference tag on an annotation in the default package
	 */
    private void x2(boolean inc) {
        //$NON-NLS-1$
        String typename = "test2.java";
        deployTagTest(typename, inc, true);
    }

    @Override
    public void testInvalidAnnotationTag3I() {
        x3(true);
    }

    @Override
    public void testInvalidAnnotationTag3F() {
        x3(false);
    }

    /**
	 * Tests having a bunch tags on member annotation elements
	 */
    private void x3(boolean inc) {
        //$NON-NLS-1$
        String typename = "test2.java";
        deployTagTest(typename, inc, true);
    }
}
