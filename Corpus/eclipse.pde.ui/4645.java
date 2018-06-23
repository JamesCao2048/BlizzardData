/*******************************************************************************
 * Copyright (c) Aug 22, 2013 IBM Corporation and others.
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
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.pde.api.tools.internal.problems.ApiProblemFactory;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IElementDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblem;

/**
 * Tests using restricted annotations
 *
 * @since 1.0.400
 */
public class AnnotationUsageTests extends UsageTest {

    //$NON-NLS-1$
    static final String RESTRICTED_ANNOTATION_NAME = "NoRefAnnotation";

    /**
	 * @param name
	 */
    public  AnnotationUsageTests(String name) {
        super(name);
    }

    /*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.pde.api.tools.builder.tests.ApiBuilderTest#getDefaultProblemId
	 * ()
	 */
    @Override
    protected int getDefaultProblemId() {
        return ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_USAGE, IElementDescriptor.TYPE, IApiProblem.ILLEGAL_REFERENCE, IApiProblem.ANNOTATION);
    }

    /*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.pde.api.tools.builder.tests.usage.UsageTest#getTestSourcePath
	 * ()
	 */
    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$
        return super.getTestSourcePath().append("annotation");
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

    public static Test suite() {
        return buildTestSuite(AnnotationUsageTests.class);
    }

    /**
	 * Tests using a restricted annotation on a type during a full build
	 *
	 * @throws Exception
	 */
    public void testAnnotationUsage1F() throws Exception {
        x1(false);
    }

    /**
	 * Tests using a restricted annotation on a type during an incremental build
	 *
	 * @throws Exception
	 */
    public void testAnnotationUsage1I() throws Exception {
        x1(true);
    }

    private void x1(boolean inc) {
        //$NON-NLS-1$
        String typename = "test1";
        int problemid = getDefaultProblemId();
        setExpectedProblemIds(new int[] { problemid });
        setExpectedLineMappings(new LineMapping[] { new LineMapping(15, problemid, new String[] { RESTRICTED_ANNOTATION_NAME }) });
        setExpectedMessageArgs(new String[][] { { RESTRICTED_ANNOTATION_NAME } });
        deployUsageTest(typename, inc);
    }

    /**
	 * Tests using a restricted annotation on a field during a full build
	 *
	 * @throws Exception
	 */
    public void testAnnotationUsage2F() throws Exception {
        x2(false);
    }

    /**
	 * Tests using a restricted annotation on a field during an incremental
	 * build
	 *
	 * @throws Exception
	 */
    public void testAnnotationUsage2I() throws Exception {
        x2(true);
    }

    private void x2(boolean inc) {
        //$NON-NLS-1$
        String typename = "test2";
        int problemid = getDefaultProblemId();
        setExpectedProblemIds(new int[] { problemid });
        setExpectedLineMappings(new LineMapping[] { new LineMapping(17, problemid, new String[] { RESTRICTED_ANNOTATION_NAME }) });
        setExpectedMessageArgs(new String[][] { { RESTRICTED_ANNOTATION_NAME } });
        deployUsageTest(typename, inc);
    }

    /**
	 * Tests using a restricted annotation on a method during a full build
	 *
	 * @throws Exception
	 */
    public void testAnnotationUsage3F() throws Exception {
        x3(false);
    }

    /**
	 * Tests using a restricted annotation on a method during an incremental
	 * build
	 *
	 * @throws Exception
	 */
    public void testAnnotationUsage3I() throws Exception {
        x3(true);
    }

    private void x3(boolean inc) {
        //$NON-NLS-1$
        String typename = "test3";
        int problemid = getDefaultProblemId();
        setExpectedProblemIds(new int[] { problemid });
        setExpectedLineMappings(new LineMapping[] { new LineMapping(17, problemid, new String[] { RESTRICTED_ANNOTATION_NAME }) });
        setExpectedMessageArgs(new String[][] { { RESTRICTED_ANNOTATION_NAME } });
        deployUsageTest(typename, inc);
    }

    /**
	 * Tests using a restricted annotation on a member type during a full build
	 *
	 * @throws Exception
	 */
    public void testAnnotationUsage4F() throws Exception {
        x4(false);
    }

    /**
	 * Tests using a restricted annotation on a member during an incremental
	 * build
	 *
	 * @throws Exception
	 */
    public void testAnnotationUsage4I() throws Exception {
        x4(true);
    }

    private void x4(boolean inc) {
        //$NON-NLS-1$
        String typename = "test4";
        int problemid = getDefaultProblemId();
        setExpectedProblemIds(new int[] { problemid });
        setExpectedLineMappings(new LineMapping[] { new LineMapping(17, problemid, new String[] { RESTRICTED_ANNOTATION_NAME }) });
        setExpectedMessageArgs(new String[][] { { RESTRICTED_ANNOTATION_NAME } });
        deployUsageTest(typename, inc);
    }

    /**
	 * Tests using a restricted annotation on a secondary type during a full
	 * build
	 *
	 * @throws Exception
	 */
    public void testAnnotationUsage5F() throws Exception {
        x5(false);
    }

    /**
	 * Tests using a restricted annotation on a secondary type during an
	 * incremental build
	 *
	 * @throws Exception
	 */
    public void testAnnotationUsage5I() throws Exception {
        x5(true);
    }

    private void x5(boolean inc) {
        //$NON-NLS-1$
        String typename = "test5";
        int problemid = getDefaultProblemId();
        setExpectedProblemIds(new int[] { problemid });
        setExpectedLineMappings(new LineMapping[] { new LineMapping(18, problemid, new String[] { RESTRICTED_ANNOTATION_NAME }) });
        setExpectedMessageArgs(new String[][] { { RESTRICTED_ANNOTATION_NAME } });
        deployUsageTest(typename, inc);
    }

    /**
	 * Tests using a restricted annotation on a field in a member type during a
	 * full build
	 *
	 * @throws Exception
	 */
    public void testAnnotationUsage6F() throws Exception {
        x6(false);
    }

    /**
	 * Tests using a restricted annotation on a field in a member type during an
	 * incremental build
	 *
	 * @throws Exception
	 */
    public void testAnnotationUsage6I() throws Exception {
        x6(true);
    }

    private void x6(boolean inc) {
        //$NON-NLS-1$
        String typename = "test6";
        int problemid = getDefaultProblemId();
        setExpectedProblemIds(new int[] { problemid });
        setExpectedLineMappings(new LineMapping[] { new LineMapping(18, problemid, new String[] { RESTRICTED_ANNOTATION_NAME }) });
        setExpectedMessageArgs(new String[][] { { RESTRICTED_ANNOTATION_NAME } });
        deployUsageTest(typename, inc);
    }

    /**
	 * Tests using a restricted annotation on a method in a member type during a
	 * full build
	 *
	 * @throws Exception
	 */
    public void testAnnotationUsage7F() throws Exception {
        x7(false);
    }

    /**
	 * Tests using a restricted annotation on a method in a member type during
	 * an incremental build
	 *
	 * @throws Exception
	 */
    public void testAnnotationUsage7I() throws Exception {
        x7(true);
    }

    private void x7(boolean inc) {
        //$NON-NLS-1$
        String typename = "test7";
        int problemid = getDefaultProblemId();
        setExpectedProblemIds(new int[] { problemid });
        setExpectedLineMappings(new LineMapping[] { new LineMapping(18, problemid, new String[] { RESTRICTED_ANNOTATION_NAME }) });
        setExpectedMessageArgs(new String[][] { { RESTRICTED_ANNOTATION_NAME } });
        deployUsageTest(typename, inc);
    }

    /**
	 * Tests using a restricted annotation on a member type of a secondary type
	 * during a full build
	 *
	 * @throws Exception
	 */
    public void testAnnotationUsage8F() throws Exception {
        x8(false);
    }

    /**
	 * Tests using a restricted annotation on a member type or a secondary type
	 * during an incremental build
	 *
	 * @throws Exception
	 */
    public void testAnnotationUsage8I() throws Exception {
        x8(true);
    }

    private void x8(boolean inc) {
        //$NON-NLS-1$
        String typename = "test8";
        int problemid = getDefaultProblemId();
        setExpectedProblemIds(new int[] { problemid });
        setExpectedLineMappings(new LineMapping[] { new LineMapping(20, problemid, new String[] { RESTRICTED_ANNOTATION_NAME }) });
        setExpectedMessageArgs(new String[][] { { RESTRICTED_ANNOTATION_NAME } });
        deployUsageTest(typename, inc);
    }

    /**
	 * Tests using a restricted annotation on a local type during a full build
	 *
	 * @throws Exception
	 */
    public void testAnnotationUsage9F() throws Exception {
        x9(false);
    }

    /**
	 * Tests using a restricted annotation on a local type during an incremental
	 * build
	 *
	 * @throws Exception
	 */
    public void testAnnotationUsage9I() throws Exception {
        x9(true);
    }

    private void x9(boolean inc) {
        //$NON-NLS-1$
        String typename = "test9";
        int problemid = getDefaultProblemId();
        setExpectedProblemIds(new int[] { problemid });
        setExpectedLineMappings(new LineMapping[] { new LineMapping(18, problemid, new String[] { RESTRICTED_ANNOTATION_NAME }) });
        setExpectedMessageArgs(new String[][] { { RESTRICTED_ANNOTATION_NAME } });
        deployUsageTest(typename, inc);
    }
}
