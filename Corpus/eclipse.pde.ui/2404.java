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
import org.eclipse.pde.api.tools.internal.JavadocTagManager;
import org.eclipse.pde.api.tools.internal.builder.BuilderMessages;

/**
 * Tests unsupported javadoc tags on class constructors
 *
 * @since 1.0
 */
public class InvalidClassConstructorTagTests extends InvalidMethodTagTests {

    /**
	 * Constructor
	 * @param name
	 */
    public  InvalidClassConstructorTagTests(String name) {
        super(name);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.ApiBuilderTests#getTestSourcePath()
	 */
    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$
        return super.getTestSourcePath().append("class");
    }

    /**
	 * @return the test for this class
	 */
    public static Test suite() {
        return buildTestSuite(InvalidClassConstructorTagTests.class);
    }

    public void testInvalidClassMethodTag1I() {
        x1(true);
    }

    public void testInvalidClassMethodTag1F() {
        x1(false);
    }

    /**
	 * Tests the unsupported @noreference Javadoc tag on private constructors in a variety of inner / outer classes
	 * is detected properly
	 */
    private void x1(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(8));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_private_constructor }, { //$NON-NLS-1$
        "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_private_constructor }, { //$NON-NLS-1$
        "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_private_constructor }, { //$NON-NLS-1$
        "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_private_constructor }, { //$NON-NLS-1$
        "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_private_constructor }, { //$NON-NLS-1$
        "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_private_constructor }, { //$NON-NLS-1$
        "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_private_constructor }, { "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_private_constructor } });
        //$NON-NLS-1$
        deployTagTest("test15.java", inc, false);
    }

    public void testInvalidClassMethodTag2I() {
        x2(true);
    }

    public void testInvalidClassMethodTag2F() {
        x2(false);
    }

    /**
	 * Tests the unsupported @noreference Javadoc tag on private constructors in a class in the default package
	 * is detected properly
	 */
    private void x2(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(2));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_private_constructor }, { "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_private_constructor } });
        //$NON-NLS-1$
        deployTagTest("test16.java", inc, true);
    }

    public void testInvalidClassMethodTag3I() {
        x3(true);
    }

    public void testInvalidClassMethodTag3F() {
        x3(false);
    }

    /**
	 * Tests the unsupported @noinstantiate Javadoc tag on constructors in a variety of inner / outer classes
	 * is detected properly
	 */
    private void x3(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(8));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor } });
        //$NON-NLS-1$
        deployTagTest("test17.java", inc, false);
    }

    public void testInvalidClassMethodTag4I() {
        x4(true);
    }

    public void testInvalidClassMethodTag4F() {
        x4(false);
    }

    /**
	 * Tests the unsupported @noinstantiate Javadoc tag on constructors in a class in the default package
	 * is detected properly
	 */
    private void x4(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(2));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor } });
        //$NON-NLS-1$
        deployTagTest("test18.java", inc, true);
    }

    public void testInvalidClassMethodTag5I() {
        x5(true);
    }

    public void testInvalidClassMethodTag5F() {
        x5(false);
    }

    /**
	 * Tests the unsupported @noextend Javadoc tag on constructors in a variety of inner / outer classes
	 * is detected properly
	 */
    private void x5(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(8));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor } });
        //$NON-NLS-1$
        deployTagTest("test19.java", inc, false);
    }

    public void testInvalidClassMethodTag6I() {
        x6(true);
    }

    public void testInvalidClassMethodTag6F() {
        x6(false);
    }

    /**
	 * Tests the unsupported @noextend Javadoc tag on constructors in a class in the default package
	 * is detected properly
	 */
    private void x6(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(2));
        setExpectedMessageArgs(new String[][] { //$NON-NLS-1$
        { "@noextend", BuilderMessages.TagValidator_a_constructor }, { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor } });
        //$NON-NLS-1$
        deployTagTest("test20.java", inc, true);
    }

    public void testInvalidClassMethodTag7I() {
        x7(true);
    }

    public void testInvalidClassMethodTag7F() {
        x7(false);
    }

    /**
	 * Tests the unsupported @nooverride Javadoc tag on constructors in a variety of inner / outer classes
	 * is detected properly
	 */
    private void x7(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(8));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor } });
        //$NON-NLS-1$
        deployTagTest("test21.java", inc, false);
    }

    public void testInvalidClassMethodTag8I() {
        x8(true);
    }

    public void testInvalidClassMethodTag8F() {
        x8(false);
    }

    /**
	 * Tests the unsupported @nooverride Javadoc tag on constructors in a class in the default package
	 * is detected properly
	 */
    private void x8(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(2));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor } });
        //$NON-NLS-1$
        deployTagTest("test22.java", inc, true);
    }

    public void testInvalidClassMethodTag9I() {
        x9(true);
    }

    public void testInvalidClassMethodTag9F() {
        x9(false);
    }

    /**
	 * Tests the unsupported @noimplement Javadoc tag on constructors in a variety of inner / outer classes
	 * is detected properly
	 */
    private void x9(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(8));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { //$NON-NLS-1$
        "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor } });
        //$NON-NLS-1$
        deployTagTest("test23.java", inc, false);
    }

    public void testInvalidClassMethodTag10I() {
        x10(true);
    }

    public void testInvalidClassMethodTag10F() {
        x10(false);
    }

    /**
	 * Tests the unsupported @noimplement Javadoc tag on constructors in a class in the default package
	 * is detected properly
	 */
    private void x10(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(2));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor }, { "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_constructor } });
        //$NON-NLS-1$
        deployTagTest("test24.java", inc, true);
    }

    public void testInvalidConstructorMethodTag1I() {
        x11(true);
    }

    public void testInvalidConstructorMethodTag1F() {
        x11(false);
    }

    /**
	 * Tests the unsupported @nooverride Javadoc tag on constructors in a class
	 * is detected properly
	 *
	 * @since 1.0.400
	 */
    private void x11(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(4));
        setExpectedMessageArgs(new String[][] { { JavadocTagManager.TAG_NOOVERRIDE, BuilderMessages.TagValidator_a_constructor }, { JavadocTagManager.TAG_NOOVERRIDE, BuilderMessages.TagValidator_a_constructor }, { JavadocTagManager.TAG_NOREFERENCE, BuilderMessages.TagValidator_private_constructor }, { JavadocTagManager.TAG_NOREFERENCE, BuilderMessages.TagValidator_a_package_default_constructor } });
        //$NON-NLS-1$
        deployTagTest("test25.java", inc, true);
    }
}
