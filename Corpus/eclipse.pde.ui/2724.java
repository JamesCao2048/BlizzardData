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

/**
 *
 */
public class InvalidClassFieldAnnotationTests extends FieldAnnotationTest {

    /**
	 * Constructor
	 *
	 * @param name
	 */
    public  InvalidClassFieldAnnotationTests(String name) {
        super(name);
    }

    /**
	 * @return the tests for this class
	 */
    public static Test suite() {
        return buildTestSuite(InvalidClassFieldAnnotationTests.class);
    }

    /*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.pde.api.tools.builder.tests.annotations.FieldAnnotationTest
	 * #getTestSourcePath()
	 */
    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$
        return super.getTestSourcePath().append("class");
    }

    public void testInvalidClassFieldAnnotation1I() {
        x1(true);
    }

    public void testInvalidClassFieldAnnotation1F() {
        x1(false);
    }

    /**
	 * Tests an invalid @NoReference annotation on final fields in inner / outer
	 * classes using an incremental build
	 */
    private void x1(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(14));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_private_field }, { //$NON-NLS-1$
        "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_private_field }, { //$NON-NLS-1$
        "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_private_field }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field_that_is_not_visible }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field_that_is_not_visible }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field_that_is_not_visible }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field_that_is_not_visible }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field_that_is_not_visible }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field_that_is_not_visible }, { //$NON-NLS-1$
        "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_private_field }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_field }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_field }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_field }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_field } });
        //$NON-NLS-1$
        deployAnnotationTest("test1.java", inc, false);
    }

    public void testInvalidClassFieldAnnotation2I() {
        x2(true);
    }

    public void testInvalidClassFieldAnnotation2F() {
        x2(false);
    }

    /**
	 * Tests a valid @NoReference annotation on three final fields in a class in
	 * the default package using an incremental build
	 */
    private void x2(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(2));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_private_field }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_field } });
        //$NON-NLS-1$
        deployAnnotationTest("test2.java", inc, true);
    }

    public void testInvalidClassFieldAnnotation3I() {
        x3(true);
    }

    public void testInvalidClassFieldAnnotation3F() {
        x3(false);
    }

    /**
	 * Tests a invalid @NoReference annotation on static final fields in inner
	 * /outer class using a full build
	 */
    private void x3(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(14));
        setExpectedMessageArgs(new String[][] { { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field_that_is_not_visible }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field_that_is_not_visible }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field_that_is_not_visible }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field_that_is_not_visible }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field_that_is_not_visible }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field_that_is_not_visible }, { //$NON-NLS-1$
        "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_private_field }, { //$NON-NLS-1$
        "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_private_field }, { //$NON-NLS-1$
        "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_private_field }, { //$NON-NLS-1$
        "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_private_field }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_field }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_field }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_field }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_field } });
        //$NON-NLS-1$
        deployAnnotationTest("test3.java", inc, false);
    }

    public void testInvalidClassFieldAnnotation4I() {
        x4(true);
    }

    public void testInvalidClassFieldAnnotation4F() {
        x4(false);
    }

    /**
	 * Tests a valid @NoReference annotation on three static final fields in a
	 * class in the default package using a full build
	 */
    private void x4(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(2));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_private_field }, { "@NoReference", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_package_default_field } });
        //$NON-NLS-1$
        deployAnnotationTest("test4.java", inc, true);
    }

    public void testInvalidClassFieldAnnotation5I() {
        x5(true);
    }

    public void testInvalidClassFieldAnnotation5F() {
        x5(false);
    }

    /**
	 * Tests a invalid @NoExtend annotation on fields in inner /outer class
	 * using a full build
	 */
    private void x5(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(16));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field } });
        //$NON-NLS-1$
        deployAnnotationTestWithErrors("test5.java", inc, false);
    }

    public void testInvalidClassFieldAnnotation6I() {
        x6(true);
    }

    public void testInvalidClassFieldAnnotation6F() {
        x6(false);
    }

    /**
	 * Tests a invalid @NoExtend annotation on three fields in a class in the
	 * default package using an incremental build
	 */
    private void x6(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(4));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoExtend", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field } });
        //$NON-NLS-1$
        deployAnnotationTestWithErrors("test6.java", inc, true);
    }

    public void testInvalidClassFieldAnnotation7I() {
        x7(true);
    }

    public void testInvalidClassFieldAnnotation7F() {
        x7(false);
    }

    /**
	 * Tests an invalid @NoImplement annotation on fields in inner / outer
	 * classes using an incremental build
	 */
    private void x7(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(16));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field } });
        //$NON-NLS-1$
        deployAnnotationTestWithErrors("test7.java", inc, false);
    }

    public void testInvalidClassFieldAnnotation8I() {
        x8(true);
    }

    public void testInvalidClassFieldAnnotation8F() {
        x8(false);
    }

    /**
	 * Tests a invalid @NoImplement annotation on three fields in a class in the
	 * default package using a full build
	 */
    private void x8(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(4));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoImplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field } });
        //$NON-NLS-1$
        deployAnnotationTestWithErrors("test8.java", inc, true);
    }

    public void testInvalidClassFieldAnnotation9I() {
        x9(true);
    }

    public void testInvalidClassFieldAnnotation9F() {
        x9(false);
    }

    /**
	 * Tests a invalid @NoOverride annotation on fields in inner /outer class
	 * using a full build
	 */
    private void x9(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(16));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field } });
        //$NON-NLS-1$
        deployAnnotationTestWithErrors("test9.java", inc, false);
    }

    public void testInvalidClassFieldAnnotation10I() {
        x10(true);
    }

    public void testInvalidClassFieldAnnotation10F() {
        x10(false);
    }

    /**
	 * Tests a valid @NoOverride annotation on three fields in a class in the
	 * default package using a full build
	 */
    private void x10(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(4));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoOverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field } });
        //$NON-NLS-1$
        deployAnnotationTestWithErrors("test10.java", inc, true);
    }

    public void testInvalidClassFieldAnnotation11I() {
        x11(true);
    }

    public void testInvalidClassFieldAnnotation11F() {
        x11(false);
    }

    /**
	 * Tests a invalid @NoInstantiate annotation on fields in inner /outer class
	 * using a full build
	 */
    private void x11(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(16));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field } });
        //$NON-NLS-1$
        deployAnnotationTestWithErrors("test11.java", inc, false);
    }

    public void testInvalidClassFieldAnnotation12I() {
        x12(true);
    }

    public void testInvalidClassFieldAnnotation12F() {
        x12(false);
    }

    /**
	 * Tests a valid @NoInstantiate annotation on three fields in a class in the
	 * default package using an incremental build
	 */
    private void x12(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(4));
        setExpectedMessageArgs(new String[][] { { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field }, { //$NON-NLS-1$
        "@NoInstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_a_field } });
        //$NON-NLS-1$
        deployAnnotationTestWithErrors("test12.java", inc, true);
    }
}
