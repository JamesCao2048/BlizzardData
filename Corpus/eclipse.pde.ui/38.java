/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.builder.tests.usage;

import junit.framework.Test;
import org.eclipse.core.runtime.IPath;
import org.eclipse.pde.api.tools.internal.problems.ApiProblemFactory;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IElementDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblem;

/**
 * Tests a variety of restricted constructor usages, where the callee has noreference restrictions
 *
 * @since 1.0
 */
public class ConstructorUsageTests extends UsageTest {

    //$NON-NLS-1$
    protected static final String CONST_CLASS_NAME = "ConstructorUsageClass";

    private static int pid = -1;

    /**
	 * Constructor
	 * @param name
	 */
    public  ConstructorUsageTests(String name) {
        super(name);
    }

    /**
	 * @see org.eclipse.pde.api.tools.builder.tests.ApiBuilderTest#getDefaultProblemId()
	 */
    @Override
    protected int getDefaultProblemId() {
        if (pid == -1) {
            pid = ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_USAGE, IElementDescriptor.METHOD, IApiProblem.ILLEGAL_REFERENCE, IApiProblem.CONSTRUCTOR_METHOD);
        }
        return pid;
    }

    public static Test suite() {
        return buildTestSuite(ConstructorUsageTests.class);
    }

    /**
	 * @see org.eclipse.pde.api.tools.builder.tests.usage.UsageTest#getTestSourcePath()
	 */
    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$
        return super.getTestSourcePath().append("method");
    }

    /**
	 * Tests that calls the a variety of restricted constructors are properly reported as problems
	 * using a full build
	 */
    public void testConstructorUsageTests1F() {
        x1(false);
    }

    /**
	 * Tests that calls the a variety of restricted constructors are properly reported as problems
	 * using an incremental build
	 */
    public void testConstructorUsageTests1I() {
        x1(true);
    }

    private void x1(boolean inc) {
        //TODO uncomment once https://bugs.eclipse.org/bugs/show_bug.cgi?id=247028 has been fixed
        setExpectedProblemIds(getDefaultProblemIdSet(12));
        //$NON-NLS-1$
        String typename = "testCN1";
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        CONST_CLASS_NAME + "()", //$NON-NLS-1$
        typename }, { //$NON-NLS-1$
        CONST_CLASS_NAME + "(int, Object, char[])", //$NON-NLS-1$
        typename }, { //$NON-NLS-1$
        "inner()", //$NON-NLS-1$
        typename }, { //$NON-NLS-1$
        CONST_CLASS_NAME + "()", //$NON-NLS-1$
        INNER_NAME1 }, { //$NON-NLS-1$
        CONST_CLASS_NAME + "(int, Object, char[])", //$NON-NLS-1$
        INNER_NAME1 }, { //$NON-NLS-1$
        "inner()", //$NON-NLS-1$
        INNER_NAME1 }, { //$NON-NLS-1$
        CONST_CLASS_NAME + "()", //$NON-NLS-1$
        INNER_NAME2 }, { //$NON-NLS-1$
        CONST_CLASS_NAME + "(int, Object, char[])", //$NON-NLS-1$
        INNER_NAME2 }, { //$NON-NLS-1$
        "inner()", //$NON-NLS-1$
        INNER_NAME2 }, { //$NON-NLS-1$
        CONST_CLASS_NAME + "()", //$NON-NLS-1$
        OUTER_NAME }, { //$NON-NLS-1$
        CONST_CLASS_NAME + "(int, Object, char[])", //$NON-NLS-1$
        OUTER_NAME }, { //$NON-NLS-1$
        "inner()", //$NON-NLS-1$
        OUTER_NAME } });
        deployUsageTest(typename, inc);
    }
}
