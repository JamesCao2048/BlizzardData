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
import org.eclipse.pde.api.tools.internal.problems.ApiProblemFactory;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IElementDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblem;

/**
 *
 */
public class InvalidInterfaceAnnotationTests extends AnnotationTest {

    /**
	 * @param name
	 */
    public  InvalidInterfaceAnnotationTests(String name) {
        super(name);
    }

    /**
	 * @return the tests for this class
	 */
    public static Test suite() {
        return buildTestSuite(InvalidInterfaceAnnotationTests.class);
    }

    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$
        return super.getTestSourcePath().append("interface");
    }

    @Override
    protected int getDefaultProblemId() {
        return ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_USAGE, IElementDescriptor.TYPE, IApiProblem.UNSUPPORTED_ANNOTATION_USE, IApiProblem.NO_FLAGS);
    }

    /**
	 * Tests @NoInstantiate on an interface
	 *
	 * @throws Exception
	 */
    public void testNoInstantiateF() throws Exception {
        x1(false);
    }

    /**
	 * Tests @NoInstantiate on an interface
	 *
	 * @throws Exception
	 */
    public void testNoInstantiateI() throws Exception {
        x1(true);
    }

    private void x1(boolean inc) {
        setExpectedProblemIds(getDefaultProblemIdSet(1));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface } });
        //$NON-NLS-1$
        deployAnnotationTest("test1.java", inc, false);
    }

    /**
	 * Tests @NoOverride on an interface
	 *
	 * @throws Exception
	 */
    public void testNoOverrideF() throws Exception {
        x2(false);
    }

    /**
	 * Tests @NoOvveride on an interface
	 *
	 * @throws Exception
	 */
    public void testNoOverrideI() throws Exception {
        x2(true);
    }

    private void x2(boolean inc) {
        setExpectedProblemIds(getDefaultProblemIdSet(1));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface } });
        //$NON-NLS-1$
        deployAnnotationTestWithErrors("test2.java", inc, false);
    }
}
