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
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.pde.api.tools.internal.builder.BuilderMessages;

/**
 * Tests invalid tags on annotation methods.
 *
 * @since 1.0
 */
public class InvalidAnnotationMethodTagTests extends InvalidMethodTagTests {

    /**
	 * Constructor
	 *
	 * @param name
	 */
    public  InvalidAnnotationMethodTagTests(String name) {
        super(name);
    }

    /*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.pde.api.tools.builder.tests.ApiBuilderTest#getTestCompliance
	 * ()
	 */
    @Override
    protected String getTestCompliance() {
        return JavaCore.VERSION_1_5;
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
        return super.getTestSourcePath().append("annotation");
    }

    /**
	 * @return the test for this class
	 */
    public static Test suite() {
        return buildTestSuite(InvalidAnnotationMethodTagTests.class);
    }

    public void testInvalidAnnotationMethodTag1I() {
        x1(true);
    }

    public void testInvalidAnnotationMethodTag1F() {
        x1(false);
    }

    /**
	 * Tests the unsupported @noextend tag on a variety of inner / outer
	 * annotation methods
	 */
    private void x1(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(6));
        //$NON-NLS-1$
        setExpectedMessageArgs("@noextend", BuilderMessages.TagValidator_an_annotation_method, 6);
        //$NON-NLS-1$
        String typename = "test1.java";
        deployTagTest(typename, inc, false);
    }

    public void testInvalidAnnotationMethodTag2I() {
        x2(true);
    }

    public void testInvalidAnnotationMethodTag2F() {
        x2(false);
    }

    /**
	 * Tests the unsupported @noextend tag on annotation methods in the default
	 * package
	 */
    private void x2(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(2));
        //$NON-NLS-1$
        setExpectedMessageArgs("@noextend", BuilderMessages.TagValidator_an_annotation_method, 2);
        //$NON-NLS-1$
        String typename = "test2.java";
        deployTagTest(typename, inc, true);
    }

    public void testInvalidAnnotationMethodTag3I() {
        x3(true);
    }

    public void testInvalidAnnotationMethodTag3F() {
        x3(false);
    }

    /**
	 * Tests the unsupported @noinstantiate tag on a variety of inner / outer
	 * annotation methods
	 */
    private void x3(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(6));
        //$NON-NLS-1$
        setExpectedMessageArgs("@noinstantiate", BuilderMessages.TagValidator_an_annotation_method, 6);
        //$NON-NLS-1$
        String typename = "test3.java";
        deployTagTest(typename, inc, false);
    }

    public void testInvalidAnnotationMethodTag4I() {
        x4(true);
    }

    public void testInvalidAnnotationMethodTag4F() {
        x4(false);
    }

    /**
	 * Tests the unsupported @noinstantiate tag on annotation methods in the
	 * default package
	 */
    private void x4(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(2));
        //$NON-NLS-1$
        setExpectedMessageArgs("@noinstantiate", BuilderMessages.TagValidator_an_annotation_method, 2);
        //$NON-NLS-1$
        String typename = "test4.java";
        deployTagTest(typename, inc, true);
    }

    public void testInvalidAnnotationMethodTag5I() {
        x5(true);
    }

    public void testInvalidAnnotationMethodTag5F() {
        x5(false);
    }

    /**
	 * Tests the unsupported @noimplement tag on a variety of inner / outer
	 * annotation methods
	 */
    private void x5(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(6));
        //$NON-NLS-1$
        setExpectedMessageArgs("@noimplement", BuilderMessages.TagValidator_an_annotation_method, 6);
        //$NON-NLS-1$
        String typename = "test5.java";
        deployTagTest(typename, inc, false);
    }

    public void testInvalidAnnotationMethodTag6I() {
        x6(true);
    }

    public void testInvalidAnnotationMethodTag6F() {
        x6(false);
    }

    /**
	 * Tests the unsupported @noimplement tag on annotation methods in the
	 * default package
	 */
    private void x6(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(2));
        //$NON-NLS-1$
        setExpectedMessageArgs("@noimplement", BuilderMessages.TagValidator_an_annotation_method, 2);
        //$NON-NLS-1$
        String typename = "test6.java";
        deployTagTest(typename, inc, true);
    }

    public void testInvalidAnnotationMethodTag7I() {
        x7(true);
    }

    public void testInvalidAnnotationMethodTag7F() {
        x7(false);
    }

    /**
	 * Tests the unsupported @nooverride tag on a variety of inner / outer
	 * annotation methods
	 */
    private void x7(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(6));
        //$NON-NLS-1$
        setExpectedMessageArgs("@nooverride", BuilderMessages.TagValidator_an_annotation_method, 6);
        //$NON-NLS-1$
        String typename = "test7.java";
        deployTagTest(typename, inc, false);
    }

    public void testInvalidAnnotationMethodTag8I() {
        x8(true);
    }

    public void testInvalidAnnotationMethodTag8F() {
        x8(false);
    }

    /**
	 * Tests the unsupported @nooverride tag on annotation methods in the
	 * default package
	 */
    private void x8(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(2));
        //$NON-NLS-1$
        setExpectedMessageArgs("@nooverride", BuilderMessages.TagValidator_an_annotation_method, 2);
        //$NON-NLS-1$
        String typename = "test8.java";
        deployTagTest(typename, inc, true);
    }

    public void testInvalidAnnotationMethodTag9I() {
        x9(true);
    }

    public void testInvalidAnnotationMethodTag9F() {
        x9(false);
    }

    /**
	 * Tests all the unsupported tags on a variety of annotation methods
	 */
    private void x9(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(30));
        setExpectedMessageArgs(new String[][] { { "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method } });
        //$NON-NLS-1$
        String typename = "test9.java";
        deployTagTest(typename, inc, false);
    }

    public void testInvalidAnnotationMethodTag10I() {
        x10(true);
    }

    public void testInvalidAnnotationMethodTag10F() {
        x10(false);
    }

    /**
	 * Tests the unsupported @noreference tag on a variety of inner / outer
	 * annotation methods
	 */
    private void x10(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(6));
        //$NON-NLS-1$
        setExpectedMessageArgs("@noreference", BuilderMessages.TagValidator_an_annotation_method, 6);
        //$NON-NLS-1$
        String typename = "test10.java";
        deployTagTest(typename, inc, false);
    }

    public void testInvalidAnnotationMethodTag11I() {
        x11(true);
    }

    public void testInvalidAnnotationMethodTag11F() {
        x11(false);
    }

    /**
	 * Tests the unsupported @noreference tag on annotation methods in the
	 * default package
	 */
    private void x11(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(2));
        //$NON-NLS-1$
        setExpectedMessageArgs("@noreference", BuilderMessages.TagValidator_an_annotation_method, 2);
        //$NON-NLS-1$
        String typename = "test11.java";
        deployTagTest(typename, inc, true);
    }

    public void testInvalidAnnotationMethodTag12I() {
        x12(true);
    }

    public void testInvalidAnnotationMethodTag12F() {
        x12(false);
    }

    /**
	 * Tests the unsupported tags on an annotation method with a default value
	 */
    private void x12(boolean inc) {
        setExpectedProblemIds(getDefaultProblemSet(15));
        setExpectedMessageArgs(new String[][] { { "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@nooverride", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noimplement", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noinstantiate", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noextend", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method }, { "@noreference", //$NON-NLS-1$
        BuilderMessages.TagValidator_an_annotation_method } });
        //$NON-NLS-1$
        String typename = "test12.java";
        deployTagTest(typename, inc, false);
    }
}
