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
package org.eclipse.pde.api.tools.builder.tests.usage;

import junit.framework.Test;
import org.eclipse.core.runtime.IPath;
import org.eclipse.pde.api.tools.internal.problems.ApiProblemFactory;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IElementDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblem;

/**
 * Tests a variety of interface uses, where the callee has API restrictions
 *
 * @since 1.0
 */
public class InterfaceUsageTests extends UsageTest {

    //$NON-NLS-1$
    protected static final String INTERFACE_NAME = "InterfaceUsageInterface";

    /**
	 * Constructor
	 *
	 * @param name
	 */
    public  InterfaceUsageTests(String name) {
        super(name);
    }

    /**
	 * @see org.eclipse.pde.api.tools.builder.tests.ApiBuilderTest#getDefaultProblemId()
	 */
    @Override
    protected int getDefaultProblemId() {
        return -1;
    }

    private int getProblemId(int kind, int flags) {
        return ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_USAGE, IElementDescriptor.TYPE, kind, flags);
    }

    /**
	 * @see org.eclipse.pde.api.tools.builder.tests.usage.UsageTest#getTestSourcePath()
	 */
    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$
        return super.getTestSourcePath().append("interface");
    }

    public static Test suite() {
        return buildTestSuite(InterfaceUsageTests.class);
    }

    /**
	 * Tests that extending an @noimplement interface properly reports no usage
	 * problems using a full build
	 */
    public void testInterfaceUsageTests1F() {
        x1(false);
    }

    /**
	 * Tests that extending an @noimplement interface properly reports no usage
	 * problems using an incremental build
	 */
    public void testInterfaceUsageTests1I() {
        x1(true);
    }

    private void x1(boolean inc) {
        expectingNoProblems();
        //$NON-NLS-1$
        deployUsageTest("testI1", inc);
    }

    /**
	 * Tests that implementing an @noimplement interface properly reports the
	 * usage problems using a full build
	 */
    public void testInterfaceUsageTests2F() {
        x2(false);
    }

    /**
	 * Tests that implementing an @noimplement interface properly reports the
	 * usage problems using an incremental build
	 */
    public void testInterfaceUsageTests2I() {
        x2(true);
    }

    private void x2(boolean inc) {
        setExpectedProblemIds(new int[] { getProblemId(IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS), getProblemId(IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS) });
        //$NON-NLS-1$
        String typename = "testI2";
        setExpectedMessageArgs(new String[][] { { INTERFACE_NAME, OUTER_NAME }, { INTERFACE_NAME, typename } });
        deployUsageTest(typename, inc);
    }

    /**
	 * Tests that extending an @noextend interface properly reports the usage
	 * problems using a full build
	 */
    public void testIllegalExtendInterfaceF() {
        x3(false);
    }

    /**
	 * Tests that extending an @noextend interface properly reports the usage
	 * problems using an incremental build
	 */
    public void testIllegalExtendInterfaceI() {
        x3(true);
    }

    private void x3(boolean inc) {
        setExpectedProblemIds(new int[] { getProblemId(IApiProblem.ILLEGAL_EXTEND, IApiProblem.NO_FLAGS), getProblemId(IApiProblem.ILLEGAL_EXTEND, IApiProblem.NO_FLAGS), getProblemId(IApiProblem.ILLEGAL_EXTEND, IApiProblem.NO_FLAGS) });
        //$NON-NLS-1$
        String typename = "testI3";
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "InterfaceUsageInterface2", //$NON-NLS-1$
        typename }, //$NON-NLS-1$ //$NON-NLS-2$
        { "Iinner", "Iouter" }, //$NON-NLS-1$ //$NON-NLS-2$
        { "Iinner", "inner" } });
        deployUsageTest(typename, inc);
    }

    /**
	 * Tests that an interface tagged with &#64;noreference properly flags usage
	 * of its members as no reference
	 *
	 * @throws Exception
	 * @since 1.0.300
	 */
    public void testNoRefInterface1I() throws Exception {
        x4(true);
    }

    /**
	 * Tests that an interface tagged with &#64;noreference properly flags usage
	 * of its members as no reference
	 *
	 * @throws Exception
	 * @since 1.0.300
	 */
    public void testNoRefInterface1F() throws Exception {
        x4(false);
    }

    private void x4(boolean inc) {
        //$NON-NLS-1$
        String typename = "testI4";
        setExpectedProblemIds(new int[] { getProblemId(IApiProblem.ILLEGAL_REFERENCE, IApiProblem.METHOD) });
        setExpectedMessageArgs(new String[][] { { "NoRefInterface", typename, //$NON-NLS-1$ //$NON-NLS-2$
        "noRefInterfaceMethod()" } });
        deployUsageTest(typename, inc);
    }

    /**
	 * Tests that an interface tagged with &#64;noreference properly flags usage
	 * of its member interfaces as no reference
	 *
	 * @throws Exception
	 * @since 1.0.300
	 */
    public void testNoRefInterface2I() throws Exception {
        x5(true);
    }

    /**
	 * Tests that an interface tagged with &#64;noreference properly flags usage
	 * of its member interfaces as no reference
	 *
	 * @throws Exception
	 * @since 1.0.300
	 */
    public void testNoRefInterface2F() throws Exception {
        x5(false);
    }

    private void x5(boolean inc) {
        //$NON-NLS-1$
        String typename = "testI5";
        setExpectedProblemIds(new int[] { getProblemId(IApiProblem.ILLEGAL_REFERENCE, IApiProblem.METHOD) });
        setExpectedMessageArgs(new String[][] { { "Inner", typename, //$NON-NLS-1$ //$NON-NLS-2$
        "noRefInterfaceMethod()" } });
        deployUsageTest(typename, inc);
    }
}
