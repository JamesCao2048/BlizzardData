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
import org.eclipse.pde.api.tools.internal.builder.BuilderMessages;
import org.eclipse.pde.api.tools.internal.problems.ApiProblemFactory;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IElementDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblem;

/**
 * Tests that unsupported Javadoc tags on interfaces are reported properly
 *
 * @since 1.0
 */
public class InvalidInterfaceTagTests extends TagTest {

    /**
	 * Constructor
	 * @param name
	 */
    public  InvalidInterfaceTagTests(String name) {
        super(name);
    }

    /**
	 * @return the tests for this class
	 */
    public static Test suite() {
        return buildTestSuite(InvalidInterfaceTagTests.class);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.tags.JavadocTagTest#getTestSourcePath()
	 */
    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$
        return super.getTestSourcePath().append("interface");
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.tags.TagTest#getDefaultProblemId()
	 */
    @Override
    protected int getDefaultProblemId() {
        return ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_USAGE, IElementDescriptor.TYPE, IApiProblem.UNSUPPORTED_TAG_USE, IApiProblem.NO_FLAGS);
    }

    public void testInvalidInterfaceTag1I() {
        x1(true);
    }

    public void testInvalidInterfaceTag1F() {
        x1(false);
    }

    /**
	 * Tests having an @noreference tag on a variety of inner / outer / top-level interfaces in package a.b.c
	 */
    private void x1(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(1));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_that_is_not_visible } });
        //$NON-NLS-1$
        deployTagTest("test1.java", inc, false);
    }

    public void testInvalidInterfaceTag2I() {
        x2(true);
    }

    public void testInvalidInterfaceTag2F() {
        x2(false);
    }

    /**
	 * Tests having an @noreference tag on an interface in the default package
	 */
    private void x2(boolean inc) {
        /*setExpectedProblemIds(getDefaultProblemSet(1));
		setExpectedMessageArgs(new String[][] {
				{"@noreference", BuilderMessages.TagValidator_an_interface}
		});*/
        //$NON-NLS-1$
        deployTagTest("test2.java", inc, true);
    }

    public void testInvalidInterfaceTag3I() {
        x3(true);
    }

    public void testInvalidInterfaceTag3F() {
        x3(false);
    }

    /**
	 * Tests having an @nooverride tag on a variety of inner / outer / top-level interfaces in package a.b.c
	 */
    private void x3(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(4));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface }, { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface }, { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface }, { "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface } });
        //$NON-NLS-1$
        deployTagTest("test3.java", inc, false);
    }

    public void testInvalidInterfaceTag4I() {
        x4(true);
    }

    public void testInvalidInterfaceTag4F() {
        x4(false);
    }

    /**
	 * Tests having an @nooverride tag on an interface in the default package
	 */
    private void x4(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(1));
        setExpectedMessageArgs(new String[][] { //$NON-NLS-1$
        { "@nooverride", BuilderMessages.TagValidator_an_interface } });
        //$NON-NLS-1$
        deployTagTest("test4.java", inc, true);
    }

    public void testInvalidInterfaceTag5I() {
        x5(true);
    }

    public void testInvalidInterfaceTag5F() {
        x5(false);
    }

    /**
	 * Tests having an @noinstantiate tag on a variety of inner / outer / top-level interfaces in package a.b.c
	 */
    private void x5(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(4));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface }, { //$NON-NLS-1$
        "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface }, { //$NON-NLS-1$
        "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface } });
        //$NON-NLS-1$
        deployTagTest("test5.java", inc, false);
    }

    public void testInvalidInterfaceTag6I() {
        x6(true);
    }

    public void testInvalidInterfaceTag6F() {
        x6(false);
    }

    /**
	 * Tests having an @noinstantiate tag on an interface in the default package
	 */
    private void x6(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(1));
        setExpectedMessageArgs(new String[][] { //$NON-NLS-1$
        { "@noinstantiate", BuilderMessages.TagValidator_an_interface } });
        //$NON-NLS-1$
        deployTagTest("test6.java", inc, true);
    }
}
