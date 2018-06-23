/*******************************************************************************
 * Copyright (c) 2008, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.builder.tests.compatibility;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.pde.api.tools.internal.problems.ApiProblemFactory;
import org.eclipse.pde.api.tools.internal.provisional.comparator.IDelta;
import org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblem;
import junit.framework.Test;

/**
 * Tests that the builder correctly reports compatibility problems
 * for classes related to fields.
 *
 * @since 1.0
 */
public class ClassCompatibilityFieldTests extends ClassCompatibilityTests {

    /**
	 * Workspace relative path classes in bundle/project A
	 */
    //$NON-NLS-1$
    protected static IPath WORKSPACE_CLASSES_PACKAGE_A = new Path("bundle.a/src/a/classes/fields");

    /**
	 * Package prefix for test classes
	 */
    //$NON-NLS-1$
    protected static String PACKAGE_PREFIX = "a.classes.fields.";

    /**
	 * Constructor
	 * @param name
	 */
    public  ClassCompatibilityFieldTests(String name) {
        super(name);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.ApiBuilderTests#getTestSourcePath()
	 */
    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$
        return super.getTestSourcePath().append("fields");
    }

    /**
	 * @return the tests for this class
	 */
    public static Test suite() {
        return buildTestSuite(ClassCompatibilityFieldTests.class);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.ApiBuilderTest#getDefaultProblemId()
	 */
    @Override
    protected int getDefaultProblemId() {
        return ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_COMPATIBILITY, IDelta.CLASS_ELEMENT_TYPE, IDelta.REMOVED, IDelta.FIELD);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.ApiBuilderTests#getTestingProjectName()
	 */
    @Override
    protected String getTestingProjectName() {
        //$NON-NLS-1$
        return "classcompat";
    }

    /**
	 * Tests the removal of a public field from an API class.
	 */
    private void xRemovePublicField(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemovePublicField.java");
        int[] ids = new int[] { getDefaultProblemId() };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "RemovePublicField", "PUBLIC_FIELD" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemovePublicFieldI() throws Exception {
        xRemovePublicField(true);
    }

    public void testRemovePublicFieldF() throws Exception {
        xRemovePublicField(false);
    }

    /**
	 * Tests the removal of 2 public fields from an API class - incremental.
	 */
    public void testRemoveTwoPublicFieldsI() throws Exception {
        xRemoveTwoPublicFields(true);
    }

    /**
	 * Tests the removal of 2 public methods from an API class - full.
	 */
    public void testRemoveTwoPublicFieldsF() throws Exception {
        xRemoveTwoPublicFields(false);
    }

    /**
	 * Tests the removal of a public method from an API class - incremental.
	 */
    private void xRemoveTwoPublicFields(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemoveTwoPublicFields.java");
        int[] ids = new int[] { getDefaultProblemId(), getDefaultProblemId() };
        setExpectedProblemIds(ids);
        String[][] args = new String[2][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "RemoveTwoPublicFields", "PUBLIC_FIELD1" };
        //$NON-NLS-1$ //$NON-NLS-2$
        args[1] = new String[] { PACKAGE_PREFIX + "RemoveTwoPublicFields", "PUBLIC_FIELD2" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    /**
	 * Tests the removal of a protected field from an API class.
	 */
    private void xRemoveProtectedField(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemoveProtectedField.java");
        int[] ids = new int[] { getDefaultProblemId() };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "RemoveProtectedField", "PROTECTED_FIELD" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemoveProtectedFieldI() throws Exception {
        xRemoveProtectedField(true);
    }

    public void testRemoveProtectedFieldF() throws Exception {
        xRemoveProtectedField(false);
    }

    /**
	 * Tests the removal of a private field from an API class.
	 */
    private void xRemovePrivateField(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemovePrivateField.java");
        // there are no expected problems
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemovePrivateFieldI() throws Exception {
        xRemovePrivateField(true);
    }

    public void testRemovePrivateFieldF() throws Exception {
        xRemovePrivateField(false);
    }

    /**
	 * Tests the removal of a package protected field from an API class.
	 */
    private void xRemovePackageField(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemovePackageField.java");
        // there are no expected problems
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemovePackageFieldI() throws Exception {
        xRemovePackageField(true);
    }

    public void testRemovePackageFieldF() throws Exception {
        xRemovePackageField(false);
    }

    /**
	 * Tests the removal of a public field from an API class annotated as noextend - incremental.
	 */
    private void xRemovePublicFieldNoExtend(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemovePublicFieldNoExtend.java");
        int[] ids = new int[] { getDefaultProblemId() };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "RemovePublicFieldNoExtend", "PUBLIC_FIELD" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemovePublicFieldNoExtendI() throws Exception {
        xRemovePublicFieldNoExtend(true);
    }

    public void testRemovePublicFieldNoExtendF() throws Exception {
        xRemovePublicFieldNoExtend(false);
    }

    /**
	 * Tests the removal of a protected field from an API class annotated as noextend.
	 */
    private void xRemoveProtectedFieldNoExtend(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemoveProtectedFieldNoExtend.java");
        // no problems expected since the method is not accessible
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemoveProtectedFieldNoExtendI() throws Exception {
        xRemoveProtectedFieldNoExtend(true);
    }

    public void testRemoveProtectedFieldNoExtendF() throws Exception {
        xRemoveProtectedFieldNoExtend(false);
    }

    /**
	 * Tests the removal of a public field from an API class annotated as noinstantiate - incremental.
	 */
    private void xRemovePublicFieldNoInstantiate(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemovePublicFieldNoInstantiate.java");
        int[] ids = new int[] { getDefaultProblemId() };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "RemovePublicFieldNoInstantiate", "PUBLIC_FIELD" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemovePublicFieldNoInstantiateI() throws Exception {
        xRemovePublicFieldNoInstantiate(true);
    }

    public void testRemovePublicFieldNoInstantiateF() throws Exception {
        xRemovePublicFieldNoInstantiate(false);
    }

    /**
	 * Tests the removal of a protected field from an API class annotated as noinstantiate.
	 */
    private void xRemoveProtectedFieldNoInstantiate(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemoveProtectedFieldNoInstantiate.java");
        int[] ids = new int[] { getDefaultProblemId() };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "RemoveProtectedFieldNoInstantiate", "PROTECTED_FIELD" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemoveProtectedFieldNoInstantiateI() throws Exception {
        xRemoveProtectedFieldNoInstantiate(true);
    }

    public void testRemoveProtectedFieldNoInstantiateF() throws Exception {
        xRemoveProtectedFieldNoInstantiate(false);
    }

    /**
	 * Tests the removal of a public field from an API class annotated as
	 * noextend and noinstantiate.
	 */
    private void xRemovePublicFieldNoExtendNoInstatiate(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemovePublicFieldNoExtendNoInstantiate.java");
        int[] ids = new int[] { getDefaultProblemId() };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "RemovePublicFieldNoExtendNoInstantiate", "PUBLIC_FIELD" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemovePublicFieldNoExtendNoInstantiateI() throws Exception {
        xRemovePublicFieldNoExtendNoInstatiate(true);
    }

    public void testRemovePublicFieldNoExtendNoInstantiateF() throws Exception {
        xRemovePublicFieldNoExtendNoInstatiate(false);
    }

    /**
	 * Tests the removal of a protected field from an API class annotated as
	 * noextend and noinstantiate.
	 */
    private void xRemoveProtectedFieldNoExtendNoInstatiate(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemoveProtectedFieldNoExtendNoInstantiate.java");
        // no problems expected due to noextend
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemoveProtectedFieldNoExtendNoInstantiateI() throws Exception {
        xRemoveProtectedFieldNoExtendNoInstatiate(true);
    }

    public void testRemoveProtectedFieldNoExtendNoInstantiateF() throws Exception {
        xRemoveProtectedFieldNoExtendNoInstatiate(false);
    }

    /**
	 * Tests the removal of a public field from an API class tagged noreference.
	 */
    private void xRemovePublicFieldNoReference(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemovePublicFieldNoReference.java");
        // no problems since no references allowed
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemovePublicFieldNoReferenceI() throws Exception {
        xRemovePublicFieldNoReference(true);
    }

    public void testRemovePublicFieldNoReferencF() throws Exception {
        xRemovePublicFieldNoReference(false);
    }

    /**
	 * Tests the removal of a protected field from an API class tagged noreference.
	 */
    private void xRemoveProtectedFieldNoReference(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemoveProtectedFieldNoReference.java");
        // no problems since no references allowed
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemoveProtectedFieldNoReferenceI() throws Exception {
        xRemoveProtectedFieldNoReference(true);
    }

    public void testRemoveProtectedFieldNoReferencF() throws Exception {
        xRemoveProtectedFieldNoReference(false);
    }

    /**
	 * Tests the addition of a private field in an API class.
	 */
    private void xAddPrivateField(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("AddPrivateField.java");
        // there are no expected problems
        performCompatibilityTest(filePath, incremental);
    }

    public void testAddPrivateFieldI() throws Exception {
        xAddPrivateField(true);
    }

    public void testAddPrivateFieldF() throws Exception {
        xAddPrivateField(false);
    }

    /**
	 * Tests the addition of a protected field in an API class.
	 */
    private void xAddProtectedField(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("AddProtectedField.java");
        // there is 1 expected problems
        int[] ids = new int[] { ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_COMPATIBILITY, IDelta.CLASS_ELEMENT_TYPE, IDelta.ADDED, IDelta.FIELD) };
        setExpectedProblemIds(ids);
        performCompatibilityTest(filePath, incremental);
    }

    public void testAddProtectedFieldI() throws Exception {
        xAddProtectedField(true);
    }

    public void testAddProtectedFieldF() throws Exception {
        xAddProtectedField(false);
    }

    /**
	 * Tests the addition of a public field in an API class.
	 */
    private void xAddPublicField(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("AddPublicField.java");
        // there is 1 expected problem
        int[] ids = new int[] { ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_COMPATIBILITY, IDelta.CLASS_ELEMENT_TYPE, IDelta.ADDED, IDelta.FIELD) };
        setExpectedProblemIds(ids);
        performCompatibilityTest(filePath, incremental);
    }

    public void testAddPublicFieldI() throws Exception {
        xAddPublicField(true);
    }

    public void testAddPublicFieldF() throws Exception {
        xAddPublicField(false);
    }
}
