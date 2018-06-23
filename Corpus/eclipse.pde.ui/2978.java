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

/**
 * Tests invalid tag use on interface fields
 *
 * @since 1.0
 */
public class InvalidInterfaceFieldTagTests extends InvalidFieldTagTests {

    /**
	 * Constructor
	 * @param name
	 */
    public  InvalidInterfaceFieldTagTests(String name) {
        super(name);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.tags.InvalidFieldTagTests#getTestSourcePath()
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
        return buildTestSuite(InvalidInterfaceFieldTagTests.class);
    }

    public void testInvalidInterfaceFieldTag1I() {
        x1(true);
    }

    public void testInvalidInterfaceFieldTag1F() {
    }

    /**
	 * Tests the unsupported @noextend tag on a variety of inner / outer interface fields
	 */
    private void x1(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(3));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_field }, { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_field }, { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_field } });
        //$NON-NLS-1$
        deployTagTest("test1.java", inc, false);
    }

    public void testInvalidInterfaceFieldTag2I() {
        x2(true);
    }

    public void testInvalidInterfaceFieldTag2F() {
        x2(false);
    }

    /**
	 * Tests the unsupported @noextend tag on an interface field in the default package
	 */
    private void x2(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(1));
        setExpectedMessageArgs(new String[][] { { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_field } });
        //$NON-NLS-1$
        deployTagTest("test2.java", inc, true);
    }

    public void testInvalidInterfaceFieldTag3I() {
        x3(true);
    }

    public void testInvalidInterfaceFieldTag3F() {
        x3(false);
    }

    /**
	 * Tests the unsupported @noinstantiate tag on a variety of inner / outer interface fields
	 */
    private void x3(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(3));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_field }, { //$NON-NLS-1$
        "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_field }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_field } });
        //$NON-NLS-1$
        deployTagTest("test3.java", inc, false);
    }

    public void testInvalidInterfaceFieldTag4I() {
        x4(true);
    }

    public void testInvalidInterfaceFieldTag4F() {
        x4(false);
    }

    /**
	 * Tests the unsupported @noinstantiate tag on an interface field in the default package
	 */
    private void x4(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(1));
        setExpectedMessageArgs(new String[][] { { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_field } });
        //$NON-NLS-1$
        deployTagTest("test4.java", inc, true);
    }

    public void testInvalidInterfaceFieldTag5I() {
        x5(true);
    }

    public void testInvalidInterfaceFieldTag5F() {
        x5(false);
    }

    /**
	 * Tests the unsupported @noimplement tag on a variety of inner / outer interface fields
	 */
    private void x5(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(3));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_field }, { //$NON-NLS-1$
        "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_field }, { "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_field } });
        //$NON-NLS-1$
        deployTagTest("test5.java", inc, false);
    }

    public void testInvalidInterfaceFieldTag6I() {
        x6(true);
    }

    public void testInvalidInterfaceFieldTag6F() {
        x6(false);
    }

    /**
	 * Tests the unsupported @noimplement tag on an interface field in the default package
	 */
    private void x6(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(1));
        setExpectedMessageArgs(new String[][] { { "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_field } });
        //$NON-NLS-1$
        deployTagTest("test6.java", inc, true);
    }

    public void testInvalidInterfaceFieldTag7I() {
        x7(true);
    }

    public void testInvalidInterfaceFieldTag7F() {
        x7(false);
    }

    /**
	 * Tests the unsupported @nooverride tag on a variety of inner / outer interface fields
	 */
    private void x7(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(3));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_field }, { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_field }, { "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_field } });
        //$NON-NLS-1$
        deployTagTest("test7.java", inc, false);
    }

    public void testInvalidInterfaceFieldTag8I() {
        x8(true);
    }

    public void testInvalidInterfaceFieldTag8F() {
        x8(false);
    }

    /**
	 * Tests the unsupported @nooverride tag on an interface field in the default package
	 */
    private void x8(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(1));
        setExpectedMessageArgs(new String[][] { { "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_field } });
        //$NON-NLS-1$
        deployTagTest("test8.java", inc, true);
    }

    public void testInvalidInterfaceFieldTag9I() {
        x9(true);
    }

    public void testInvalidInterfaceFieldTag9F() {
        x9(false);
    }

    /**
	 * Tests the supported @noreference tag on a variety of final inner / outer interface fields
	 */
    private void x9(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(1));
        setExpectedMessageArgs(new String[][] { { "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_not_visible_interface_field } });
        //$NON-NLS-1$
        deployTagTest("test9.java", inc, false);
    }

    public void testInvalidInterfaceFieldTag10I() {
        x10(true);
    }

    public void testInvalidInterfaceFieldTag10F() {
        x10(false);
    }

    /**
	 * Tests the supported @noreference tag on a final interface field in the default package
	 */
    private void x10(boolean inc) {
        //$NON-NLS-1$
        deployTagTest("test10.java", inc, true);
    }

    public void testInvalidInterfaceFieldTag11I() {
        x11(true);
    }

    public void testInvalidInterfaceFieldTag11F() {
        x11(false);
    }

    /**
	 * Tests the supported @noreference tag on a variety of static final inner / outer interface fields
	 */
    private void x11(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(1));
        setExpectedMessageArgs(new String[][] { { "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_not_visible_interface_field } });
        //$NON-NLS-1$
        deployTagTest("test11.java", inc, false);
    }

    public void testInvalidInterfaceFieldTag12I() {
        x12(true);
    }

    public void testInvalidInterfaceFieldTag12F() {
        x12(false);
    }

    /**
	 * Tests the supported @noreference tag on a static final interface field in the default package
	 */
    private void x12(boolean inc) {
        //$NON-NLS-1$
        deployTagTest("test12.java", inc, true);
    }
}
