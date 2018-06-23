/*******************************************************************************
 * Copyright (c) 2011, 2014 IBM Corporation and others.
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
import org.eclipse.pde.api.tools.internal.problems.ApiProblemFactory;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IElementDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblem;

/**
 * Test class usage for Java 7 code snippets
 *
 * @since 1.0.100
 */
public class Java7FieldUsageTests extends Java7UsageTest {

    private int pid = -1;

    /**
	 * Constructor
	 * @param name
	 */
    public  Java7FieldUsageTests(String name) {
        super(name);
    }

    /**
	 * @return the test class for this suite
	 */
    public static Test suite() {
        return buildTestSuite(Java7FieldUsageTests.class);
    }

    @Override
    protected int getDefaultProblemId() {
        if (pid == -1) {
            pid = ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_USAGE, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_REFERENCE, IApiProblem.FIELD);
        }
        return pid;
    }

    /**
	 * Tests illegal use of classes inside a string switch block
	 * (full)
	 */
    public void testStringSwitchF() {
        x1(false);
    }

    /**
	 * Tests illegal use of classes inside a string switch block
	 * (incremental)
	 */
    public void testStringSwitchI() {
        x1(true);
    }

    private void x1(boolean inc) {
        setExpectedProblemIds(getDefaultProblemIdSet(3));
        //$NON-NLS-1$
        String typename = "testFStringSwitch";
        // Note that since constants are inlined, we do not get markers for illegal use
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        FieldUsageTests.FIELD_CLASS_NAME, //$NON-NLS-1$
        typename, //$NON-NLS-1$
        "f1" }, { //$NON-NLS-1$
        FieldUsageTests.FIELD_CLASS_NAME, //$NON-NLS-1$
        typename, //$NON-NLS-1$
        "f1" }, { //$NON-NLS-1$
        FieldUsageTests.FIELD_CLASS_NAME, //$NON-NLS-1$
        typename, //$NON-NLS-1$
        "f1" } });
        deployUsageTest(typename, inc);
    }

    /**
	 * Tests illegal use of classes inside a multi catch block
	 * (full)
	 */
    public void testMultiCatchF() {
        x2(false);
    }

    /**
	 * Tests illegal use of classes inside a multi catch block
	 * (incremental)
	 */
    public void testMultiCatchI() {
        x2(true);
    }

    private void x2(boolean inc) {
        setExpectedProblemIds(getDefaultProblemIdSet(2));
        //$NON-NLS-1$
        String typename = "testFMultiCatch";
        setExpectedMessageArgs(new String[][] { //$NON-NLS-1$ //$NON-NLS-2$
        { "MultipleThrowableClass", typename, "f1" }, //$NON-NLS-1$ //$NON-NLS-2$
        { "MultipleThrowableClass", typename, "f1" } });
        deployUsageTest(typename, inc);
    }
}
