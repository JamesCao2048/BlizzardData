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
 * Tests for the builder to ensure it find and reports unsupported Javadoc tags
 * on classes properly.
 *
 * @since 1.0
 */
public class InvalidClassTagTests extends TagTest {

    /**
	 * Constructor
	 *
	 * @param name
	 */
    public  InvalidClassTagTests(String name) {
        super(name);
    }

    /**
	 * @return the tests for this class
	 */
    public static Test suite() {
        return buildTestSuite(InvalidClassTagTests.class);
    }

    /*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.pde.api.tools.builder.tests.ApiBuilderTests#getTestSourcePath
	 * ()
	 */
    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$
        return super.getTestSourcePath().append("class");
    }

    /*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.pde.api.tools.builder.tests.tags.TagTest#getDefaultProblemId
	 * ()
	 */
    @Override
    protected int getDefaultProblemId() {
        return ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_USAGE, IElementDescriptor.TYPE, IApiProblem.UNSUPPORTED_TAG_USE, IApiProblem.NO_FLAGS);
    }

    public void testInvalidClassTag1I() {
        x1(true);
    }

    public void testInvalidClassTag1F() {
        x1(false);
    }

    /**
	 * Tests having an @noreference tag on a variety of inner / outer /
	 * top-level classes in package a.b.c using a full build
	 */
    private void x1(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(3));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_private_class }, { "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_class }, { "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_class } });
        //$NON-NLS-1$
        deployTagTest("test1.java", inc, false);
    }

    public void testInvalidClassTag2I() {
        x2(true);
    }

    public void testInvalidClassTag2F() {
        x2(false);
    }

    /**
	 * Tests having an @noreference tag on a class in the default package using
	 * an incremental build
	 */
    private void x2(boolean inc) {
        /*
		 * setExpectedProblemIds(getDefaultProblemSet(1));
		 * setExpectedMessageArgs(new String[][] { {"@noreference",
		 * BuilderMessages.TagValidator_a_class} });
		 */
        //$NON-NLS-1$
        deployTagTest("test2.java", inc, true);
    }

    public void testInvalidClassTag3I() {
        x3(true);
    }

    public void testInvalidClassTag3F() {
        x3(false);
    }

    /**
	 * Tests having an @noimplement tag on a variety of inner / outer /
	 * top-level classes in package a.b.c using a full build
	 */
    private void x3(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(4));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_class }, { //$NON-NLS-1$
        "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_class }, { //$NON-NLS-1$
        "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_class }, { //$NON-NLS-1$
        "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_class } });
        //$NON-NLS-1$
        deployTagTest("test3.java", inc, false);
    }

    public void testInvalidClassTag4I() {
        x4(true);
    }

    public void testInvalidClassTag4F() {
        x4(false);
    }

    /**
	 * Tests having an @noimplement tag on a class in the default package using
	 * a full build
	 */
    private void x4(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(1));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_class } });
        //$NON-NLS-1$
        deployTagTest("test4.java", inc, true);
    }

    public void testInvalidClassTag5I() {
        x5(true);
    }

    public void testInvalidClassTag5F() {
        x5(false);
    }

    /**
	 * Tests having an @nooverride tag on a variety of inner / outer / top-level
	 * classes in package a.b.c using a full build
	 */
    private void x5(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(4));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_class }, { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_class }, { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_class }, { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_class } });
        //$NON-NLS-1$
        deployTagTest("test5.java", inc, false);
    }

    public void testInvalidClassTag6I() {
        x6(true);
    }

    public void testInvalidClassTag6F() {
        x6(false);
    }

    /**
	 * Tests having an @nooverride tag on a class in the default package using
	 * an incremental build
	 */
    private void x6(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(1));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_class } });
        //$NON-NLS-1$
        deployTagTest("test6.java", inc, true);
    }

    public void testInvalidClassTag7I() {
        x7(true);
    }

    public void testInvalidClassTag7F() {
        x7(false);
    }

    /**
	 * Tests having an @noextend tag on a variety of inner / outer final classes
	 * using an incremental build
	 */
    private void x7(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(3));
        setExpectedMessageArgs(new String[][] { { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_class }, { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_class }, { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_private_class } });
        //$NON-NLS-1$
        deployTagTest("test7.java", inc, false);
    }

    public void testInvalidClassTag8I() {
        x8(true);
    }

    public void testInvalidClassTag8F() {
        x8(false);
    }

    /**
	 * Test having an @noinstantiate tag on an abstract class
	 */
    private void x8(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(1));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_abstract_class } });
        //$NON-NLS-1$
        deployTagTest("test8.java", inc, false);
    }

    /**
	 * Tests that @noextend and @noinstantiate are invalid tags on an outer
	 * class in the the testing package a.b.c using an incremental build
	 */
    public void testInvalidClassTag9I() {
        x9(true);
    }

    /**
	 * Tests that @noextend and @noinstantiate are invalid tags on an outer
	 * class in the the testing package a.b.c using a full build
	 */
    public void testInvalidClassTag9F() {
        x9(false);
    }

    private void x9(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(2));
        setExpectedMessageArgs(new String[][] { { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_class }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_class } });
        //$NON-NLS-1$
        deployTagTest("test9.java", inc, false);
    }

    /**
	 * Tests that @noextend and @noinstantiate are invalid tags on an inner
	 * class in the the testing package a.b.c using an incremental build
	 */
    public void testInvalidClassTag10I() {
        x10(true);
    }

    /**
	 * Tests that @noextend and @noinstantiate are invalid tags on an inner
	 * class in the the testing package a.b.c using a full build
	 */
    public void testInvalidClassTag10F() {
        x10(false);
    }

    private void x10(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(4));
        setExpectedMessageArgs(new String[][] { { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_class }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_class }, { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_private_class }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_private_class } });
        //$NON-NLS-1$
        deployTagTest("test10.java", inc, false);
    }

    /**
	 * Tests that @noextend and @noinstantiate are invalid tags on a variety of
	 * inner / outer / top-level classes in the the testing package a.b.c using
	 * an incremental build
	 */
    public void testInvalidClassTag11I() {
        x11(true);
    }

    /**
	 * Tests that @noextend and @noinstantiate are invalid tags on a variety of
	 * inner / outer / top-level classes in the the testing package a.b.c using
	 * a full build
	 */
    public void testInvalidClassTag11F() {
        x11(false);
    }

    private void x11(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(12));
        setExpectedMessageArgs(new String[][] { { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_class }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_class }, { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_private_class }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_private_class }, { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_class }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_class }, { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_private_class }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_private_class }, { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_class }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_class }, { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_private_class }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_private_class } });
        //$NON-NLS-1$
        deployTagTest("test11.java", inc, false);
    }

    /**
	 * Tests all tags are invalid when parent class is private or package
	 * default (incremental build)
	 */
    public void testInvalidClassTag12I() {
        x12(true);
    }

    /**
	 * Tests all tags are invalid when parent class is private or package
	 * default (full build)
	 */
    public void testInvalidClassTag12F() {
        x12(false);
    }

    private void x12(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(13));
        setExpectedMessageArgs(new String[][] { { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_class_that_is_not_visible }, { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_that_is_not_visible }, { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_method }, { //$NON-NLS-1$
        "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_that_is_not_visible }, { "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_that_is_not_visible }, { "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field_that_is_not_visible }, { "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_class_that_is_not_visible }, { //$NON-NLS-1$
        "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_enum_not_visible }, { "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_annotation_not_visible }, { "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_method_that_is_not_visible }, { "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_method_that_is_not_visible }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_class_that_is_not_visible } });
        //$NON-NLS-1$
        deployTagTest("test12.java", inc, false);
    }
}
