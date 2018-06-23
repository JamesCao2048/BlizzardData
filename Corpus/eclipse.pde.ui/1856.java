/*******************************************************************************
 * Copyright (c) 2008, 2014 IBM Corporation and others.
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

/**
 * Tests invalid Javadoc tags on interface methods
 *
 * @since 1.0
 */
public class InvalidInterfaceMethodTagTests extends InvalidMethodTagTests {

    /**
	 * Constructor
	 * @param name
	 */
    public  InvalidInterfaceMethodTagTests(String name) {
        super(name);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.ApiBuilderTests#getTestSourcePath()
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
        return buildTestSuite(InvalidInterfaceMethodTagTests.class);
    }

    public void testInvalidInterfaceMethodTag1I() {
        x1(true);
    }

    public void testInvalidInterfaceMethodTag1F() {
        x1(false);
    }

    /**
	 * Tests the unsupported @noextend tag on a variety of inner / outer interface methods
	 */
    private void x1(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(6));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method }, { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method }, { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method }, { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method }, { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method }, { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method } });
        //$NON-NLS-1$
        deployTagTest("test1.java", inc, false);
    }

    public void testInvalidInterfaceMethodTag2I() {
        x2(true);
    }

    public void testInvalidInterfaceMethodTag2F() {
        x2(false);
    }

    /**
	 * Tests the unsupported @noextend tag on interface methods in the default package
	 */
    private void x2(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(2));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method }, { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method } });
        //$NON-NLS-1$
        deployTagTest("test2.java", inc, true);
    }

    public void testInvalidInterfaceMethodTag3I() {
        x3(true);
    }

    public void testInvalidInterfaceMethodTag3F() {
        x3(false);
    }

    /**
	 * Tests the unsupported @noinstantiate tag on a variety of inner / outer interface methods
	 */
    private void x3(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(6));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method }, { //$NON-NLS-1$
        "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method }, { //$NON-NLS-1$
        "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method }, { //$NON-NLS-1$
        "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method }, { //$NON-NLS-1$
        "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method } });
        //$NON-NLS-1$
        deployTagTest("test3.java", inc, false);
    }

    public void testInvalidInterfaceMethodTag4I() {
        x4(true);
    }

    public void testInvalidInterfaceMethodTag4F() {
        x4(false);
    }

    /**
	 * Tests the unsupported @noinstantiate tag on interface methods in the default package
	 */
    private void x4(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(2));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method } });
        //$NON-NLS-1$
        deployTagTest("test4.java", inc, true);
    }

    public void testInvalidInterfaceMethodTag5I() {
        x5(true);
    }

    public void testInvalidInterfaceMethodTag5F() {
        x5(false);
    }

    /**
	 * Tests the unsupported @noimplement tag on a variety of inner / outer interface methods
	 */
    private void x5(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(6));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method }, { //$NON-NLS-1$
        "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method }, { //$NON-NLS-1$
        "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method }, { //$NON-NLS-1$
        "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method }, { //$NON-NLS-1$
        "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method }, { "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method } });
        //$NON-NLS-1$
        deployTagTest("test5.java", inc, false);
    }

    public void testInvalidInterfaceMethodTag6I() {
        x6(true);
    }

    public void testInvalidInterfaceMethodTag6F() {
        x6(false);
    }

    /**
	 * Tests the unsupported @noimplement tag on interface methods in the default package
	 */
    private void x6(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(2));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method }, { "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_method } });
        //$NON-NLS-1$
        deployTagTest("test6.java", inc, true);
    }

    public void testInvalidInterfaceMethodTag7I() {
        x7(true);
    }

    public void testInvalidInterfaceMethodTag7F() {
        x7(false);
    }

    /**
	 * Tests the unsupported @nooverride tag on a variety of inner / outer interface methods
	 */
    private void x7(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(6));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_nondefault_interface_method }, { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_nondefault_interface_method }, { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_nondefault_interface_method }, { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_nondefault_interface_method }, { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_nondefault_interface_method }, { "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_nondefault_interface_method } });
        //$NON-NLS-1$
        deployTagTest("test7.java", inc, false);
    }

    public void testInvalidInterfaceMethodTag8I() {
        x8(true);
    }

    public void testInvalidInterfaceMethodTag8F() {
        x8(false);
    }

    /**
	 * Tests the unsupported @nooverride tag on interface methods in the default package
	 */
    private void x8(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(2));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_nondefault_interface_method }, { "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_nondefault_interface_method } });
        //$NON-NLS-1$
        deployTagTest("test8.java", inc, true);
    }
}
