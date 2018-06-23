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
package org.eclipse.pde.api.tools.builder.tests.annotations;

import junit.framework.Test;
import org.eclipse.core.runtime.IPath;
import org.eclipse.pde.api.tools.internal.builder.BuilderMessages;
import org.eclipse.pde.api.tools.internal.problems.ApiProblemFactory;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IElementDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblem;

/**
 * Tests a variety of invalid annotation use on classes
 *
 * @since 1.0.400
 */
public class InvalidClassAnnotationsTests extends AnnotationTest {

    /**
	 * Constructor
	 *
	 * @param name
	 */
    public  InvalidClassAnnotationsTests(String name) {
        super(name);
    }

    /**
	 * @return the tests for this class
	 */
    public static Test suite() {
        return buildTestSuite(InvalidClassAnnotationsTests.class);
    }

    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$
        return super.getTestSourcePath().append("class");
    }

    @Override
    protected int getDefaultProblemId() {
        return ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_USAGE, IElementDescriptor.TYPE, IApiProblem.UNSUPPORTED_ANNOTATION_USE, IApiProblem.NO_FLAGS);
    }

    /**
	 * Tests annotations on members that are not visible
	 *
	 * @throws Exception
	 */
    public void testNotVisibleAnnotations1I() throws Exception {
        setExpectedProblemIds(getDefaultProblemSet(3));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_private_class }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_class }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_class } });
        //$NON-NLS-1$
        deployAnnotationTest("test1.java", true, false);
    }

    /**
	 * Tests annotations on members that are not visible
	 *
	 * @throws Exception
	 */
    public void testNotVisibleAnnotations1F() throws Exception {
        setExpectedProblemIds(getDefaultProblemSet(3));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_private_class }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_class }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_class } });
        //$NON-NLS-1$
        deployAnnotationTest("test1.java", false, false);
    }

    public void testInvalidClassAnnotation3I() {
        x3(true);
    }

    public void testInvalidClassAnnotation3F() {
        x3(false);
    }

    /**
	 * Tests having an @NoImplement annotation on a variety of inner / outer /
	 * top-level classes in package a.b.c using a full build
	 */
    private void x3(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(4));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_class }, { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_class }, { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_class }, { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_class } });
        //$NON-NLS-1$
        deployAnnotationTest("test3.java", inc, false);
    }

    public void testInvalidClassAnnotation7I() {
        x7(true);
    }

    public void testInvalidClassAnnotation7F() {
        x7(false);
    }

    /**
	 * Tests having an @NoExtend tag on a variety of inner / outer final classes
	 * using an incremental build
	 */
    private void x7(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(3));
        setExpectedMessageArgs(new String[][] { { "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_class }, { "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_class }, { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_private_class } });
        //$NON-NLS-1$
        deployAnnotationTest("test7.java", inc, false);
    }

    /**
	 * Tests all annotations are invalid when parent class is private or package
	 * default (incremental build)
	 */
    public void testInvalidClassAnnotation12I() {
        x12(true);
    }

    /**
	 * Tests all annotations are invalid when parent class is private or package
	 * default (full build)
	 */
    public void testInvalidClassAnnotation12F() {
        x12(false);
    }

    private void x12(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(11));
        setExpectedMessageArgs(new String[][] { { "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_class_that_is_not_visible }, { "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_that_is_not_visible }, { "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_that_is_not_visible }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_interface_that_is_not_visible }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field_that_is_not_visible }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_class_that_is_not_visible }, { //$NON-NLS-1$
        "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_enum_not_visible }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_annotation_not_visible }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_method_that_is_not_visible }, { "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_method_that_is_not_visible }, { "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_class_that_is_not_visible } });
        //$NON-NLS-1$
        deployAnnotationTest("test12.java", inc, false);
    }
}
