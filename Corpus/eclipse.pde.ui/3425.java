/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.builder.tests.compatibility;

import junit.framework.Test;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.pde.api.tools.internal.problems.ApiProblemFactory;
import org.eclipse.pde.api.tools.internal.provisional.comparator.IDelta;
import org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblem;

/**
 * Tests that the builder correctly reports compatibility problems
 * for constructors in classes.
 *
 * @since 1.0
 */
public class ClassCompatibilityConstructorTests extends ClassCompatibilityTests {

    /**
	 * Workspace relative path classes in bundle/project A
	 */
    //$NON-NLS-1$
    protected static IPath WORKSPACE_CLASSES_PACKAGE_A = new Path("bundle.a/src/a/classes/constructors");

    /**
	 * Package prefix for test classes
	 */
    //$NON-NLS-1$
    protected static String PACKAGE_PREFIX = "a.classes.constructors.";

    /**
	 * Constructor
	 * @param name
	 */
    public  ClassCompatibilityConstructorTests(String name) {
        super(name);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.ApiBuilderTests#getTestSourcePath()
	 */
    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$
        return super.getTestSourcePath().append("constructors");
    }

    /**
	 * @return the tests for this class
	 */
    public static Test suite() {
        return buildTestSuite(ClassCompatibilityConstructorTests.class);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.ApiBuilderTest#getDefaultProblemId()
	 */
    @Override
    protected int getDefaultProblemId() {
        return ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_COMPATIBILITY, IDelta.CLASS_ELEMENT_TYPE, IDelta.REMOVED, IDelta.CONSTRUCTOR);
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
	 * Tests the removal of a public constructor from an API class.
	 */
    private void xRemovePublicAPIConstructor(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemovePublicConstructor.java");
        int[] ids = new int[] { getDefaultProblemId() };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "RemovePublicConstructor", "RemovePublicConstructor(int)" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemovePublicAPIConstructorI() throws Exception {
        xRemovePublicAPIConstructor(true);
    }

    public void testRemovePublicAPIConstructorF() throws Exception {
        xRemovePublicAPIConstructor(false);
    }

    public void testRemoveTwoPublicAPIConstructorsI() throws Exception {
        xRemoveTwoPublicAPIConstructors(true);
    }

    public void testRemoveTwoPublicAPIConstructorsF() throws Exception {
        xRemoveTwoPublicAPIConstructors(false);
    }

    /**
	 * Tests the removal of a public constructors from an API class - incremental.
	 */
    private void xRemoveTwoPublicAPIConstructors(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemoveTwoPublicConstructors.java");
        int[] ids = new int[] { getDefaultProblemId(), getDefaultProblemId() };
        setExpectedProblemIds(ids);
        String[][] args = new String[2][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "RemoveTwoPublicConstructors", "RemoveTwoPublicConstructors(String)" };
        //$NON-NLS-1$ //$NON-NLS-2$
        args[1] = new String[] { PACKAGE_PREFIX + "RemoveTwoPublicConstructors", "RemoveTwoPublicConstructors(int)" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    /**
	 * Tests the removal of a protected constructor from an API class.
	 */
    private void xRemoveProtectedAPIConstructor(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemoveProtectedConstructor.java");
        int[] ids = new int[] { getDefaultProblemId() };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "RemoveProtectedConstructor", "RemoveProtectedConstructor(int)" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemoveProtectedAPIConstructorI() throws Exception {
        xRemoveProtectedAPIConstructor(true);
    }

    public void testRemoveProtectedAPIConstructorF() throws Exception {
        xRemoveProtectedAPIConstructor(false);
    }

    /**
	 * Tests the removal of a private constructor from an API class.
	 */
    private void xRemovePrivateAPIConstructor(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemovePrivateConstructor.java");
        // there are no expected problems
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemovePrivateAPIConstructorI() throws Exception {
        xRemovePrivateAPIConstructor(true);
    }

    public void testRemovePrivateAPIConstructorF() throws Exception {
        xRemovePrivateAPIConstructor(false);
    }

    /**
	 * Tests the removal of a package protected constructor from an API class.
	 */
    private void xRemovePackageConstructor(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemovePackageConstructor.java");
        // there are no expected problems
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemovePackageConstructorI() throws Exception {
        xRemovePackageConstructor(true);
    }

    public void testRemovePackageConstructorF() throws Exception {
        xRemovePackageConstructor(false);
    }

    /**
	 * Tests the removal of a public constructor from an API class annotated as noextend - incremental.
	 */
    private void xRemovePublicAPIConstructorNoExtend(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemovePublicConstructorNoExtend.java");
        int[] ids = new int[] { getDefaultProblemId() };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "RemovePublicConstructorNoExtend", "RemovePublicConstructorNoExtend(int)" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemovePublicAPIConstructorNoExtendI() throws Exception {
        xRemovePublicAPIConstructorNoExtend(true);
    }

    public void testRemovePublicAPIConstructorNoExtendF() throws Exception {
        xRemovePublicAPIConstructorNoExtend(false);
    }

    /**
	 * Tests the removal of a protected constructor from an API class annotated as noextend.
	 */
    private void xRemoveProtectedAPIConstructorNoExtend(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemoveProtectedConstructorNoExtend.java");
        // no problems expected since the constructor is not accessible
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemoveProtectedAPIConstructorNoExtendI() throws Exception {
        xRemoveProtectedAPIConstructorNoExtend(true);
    }

    public void testRemoveProtectedAPIConstructorNoExtendF() throws Exception {
        xRemoveProtectedAPIConstructorNoExtend(false);
    }

    /**
	 * Tests the removal of a public constructor from an API class annotated as noinstantiate - incremental.
	 */
    private void xRemovePublicAPIConstructorNoInstantiate(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemovePublicConstructorNoInstantiate.java");
        int[] ids = new int[] { getDefaultProblemId() };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "RemovePublicConstructorNoInstantiate", "RemovePublicConstructorNoInstantiate(int)" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemovePublicAPIConstructorNoInstantiateI() throws Exception {
        xRemovePublicAPIConstructorNoInstantiate(true);
    }

    public void testRemovePublicAPIConstructorNoInstantiateF() throws Exception {
        xRemovePublicAPIConstructorNoInstantiate(false);
    }

    /**
	 * Tests the removal of a protected constructor from an API class annotated as noinstantiate.
	 */
    private void xRemoveProtectedAPIConstructorNoInstantiate(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemoveProtectedConstructorNoInstantiate.java");
        int[] ids = new int[] { getDefaultProblemId() };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "RemoveProtectedConstructorNoInstantiate", "RemoveProtectedConstructorNoInstantiate(int)" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemoveProtectedAPIConstructorNoInstantiateI() throws Exception {
        xRemoveProtectedAPIConstructorNoInstantiate(true);
    }

    public void testRemoveProtectedAPIConstructorNoInstantiateF() throws Exception {
        xRemoveProtectedAPIConstructorNoInstantiate(false);
    }

    /**
	 * Tests the removal of a public constructor from an API class annotated as
	 * noextend and noinstantiate.
	 */
    private void xRemovePublicAPIConstructorNoExtendNoInstatiate(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemovePublicConstructorNoExtendNoInstantiate.java");
        // no problems expected
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemovePublicAPIConstructorNoExtendNoInstantiateI() throws Exception {
        xRemovePublicAPIConstructorNoExtendNoInstatiate(true);
    }

    public void testRemovePublicAPIConstructorNoExtendNoInstantiateF() throws Exception {
        xRemovePublicAPIConstructorNoExtendNoInstatiate(false);
    }

    /**
	 * Tests the removal of a protected constructor from an API class annotated as
	 * noextend and noinstantiate.
	 */
    private void xRemoveProtectedAPIConstructorNoExtendNoInstatiate(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemoveProtectedConstructorNoExtendNoInstantiate.java");
        // no problems expected due to noextend
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemoveProtectedAPIConstructorNoExtendNoInstantiateI() throws Exception {
        xRemoveProtectedAPIConstructorNoExtendNoInstatiate(true);
    }

    public void testRemoveProtectedAPIConstructorNoExtendNoInstantiateF() throws Exception {
        xRemoveProtectedAPIConstructorNoExtendNoInstatiate(false);
    }

    /**
	 * Tests the removal of a public constructor from an API class tagged noreference.
	 */
    private void xRemovePublicAPIConstructorNoReference(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemovePublicConstructorNoReference.java");
        // no problems since no references allowed
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemovePublicAPIConstructorNoReferenceI() throws Exception {
        xRemovePublicAPIConstructorNoReference(true);
    }

    public void testRemovePublicAPIConstructorNoReferenceF() throws Exception {
        xRemovePublicAPIConstructorNoReference(false);
    }

    /**
	 * Tests the removal of a protected constructor from an API class tagged noreference.
	 */
    private void xRemoveProtectedAPIConstructorNoReference(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemoveProtectedConstructorNoReference.java");
        // no problems since no references allowed
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemoveProtectedAPIConstructorNoReferenceI() throws Exception {
        xRemoveProtectedAPIConstructorNoReference(true);
    }

    public void testRemoveProtectedAPIConstructorNoReferencF() throws Exception {
        xRemoveProtectedAPIConstructorNoReference(false);
    }

    /**
	 * Tests the removal of a public constructor from an API class tagged no override.
	 */
    private void xRemovePublicAPIConstructorNoOverride(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemovePublicConstructorNoOverride.java");
        int[] ids = new int[] { getDefaultProblemId() };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "RemovePublicConstructorNoOverride", "RemovePublicConstructorNoOverride(int)" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemovePublicAPIConstructorNoOverrideI() throws Exception {
        xRemovePublicAPIConstructorNoOverride(true);
    }

    public void testRemovePublicAPIConstructorNoOverrideF() throws Exception {
        xRemovePublicAPIConstructorNoOverride(false);
    }

    /**
	 * Tests the removal of a protected constructor from an API class tagged no override.
	 */
    private void xRemoveProtectedAPIConstructorNoOverride(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemoveProtectedConstructorNoOverride.java");
        int[] ids = new int[] { getDefaultProblemId() };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "RemoveProtectedConstructorNoOverride", "RemoveProtectedConstructorNoOverride(int)" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemoveProtectedAPIConstructorNoOverrideI() throws Exception {
        xRemoveProtectedAPIConstructorNoOverride(true);
    }

    public void testRemoveProtectedAPIConstructorNoOverrideF() throws Exception {
        xRemoveProtectedAPIConstructorNoOverride(false);
    }

    /**
	 * Tests the addition of a private constructor in an API class.
	 */
    private void xAddPrivateAPIConstructor(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("AddPrivateConstructor.java");
        // there are no expected problems
        performCompatibilityTest(filePath, incremental);
    }

    public void testAddPrivateAPIConstructorI() throws Exception {
        xAddPrivateAPIConstructor(true);
    }

    public void testAddPrivateAPIConstructorF() throws Exception {
        xAddPrivateAPIConstructor(false);
    }

    /**
	 * Tests the addition of a protected constructor in an API class.
	 */
    private void xAddProtectedAPIConstructor(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("AddProtectedConstructor.java");
        // there are no expected problems
        performCompatibilityTest(filePath, incremental);
    }

    public void testAddProtectedAPIConstructorI() throws Exception {
        xAddProtectedAPIConstructor(true);
    }

    public void testAddProtectedAPIConstructorF() throws Exception {
        xAddProtectedAPIConstructor(false);
    }

    /**
	 * Tests the addition of a protected constructor in an API class.
	 */
    private void xAddPublicAPIConstructor(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("AddPublicConstructor.java");
        // there are no expected problems
        performCompatibilityTest(filePath, incremental);
    }

    public void testAddPublicAPIConstructorI() throws Exception {
        xAddPublicAPIConstructor(true);
    }

    public void testAddPublicAPIConstructorF() throws Exception {
        xAddPublicAPIConstructor(false);
    }

    /**
	 * Tests the addition of a singleton private constructor in an API class.
	 */
    private void xAddSingletonPrivateAPIConstructor(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("AddSinglePrivateConstructor.java");
        int[] ids = new int[] { getDefaultProblemId() };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "AddSinglePrivateConstructor", "AddSinglePrivateConstructor()" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testAddSingletonPrivateAPIConstructorI() throws Exception {
        xAddSingletonPrivateAPIConstructor(true);
    }

    public void testAddSingletonPrivateAPIConstructorF() throws Exception {
        xAddSingletonPrivateAPIConstructor(false);
    }

    /**
	 * Tests the addition of a singleton protected constructor in an API class.
	 */
    private void xAddSingletonProtectedAPIConstructor(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("AddSingleProtectedConstructor.java");
        int[] ids = new int[] { getDefaultProblemId() };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "AddSingleProtectedConstructor", "AddSingleProtectedConstructor()" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testAddSingletonProtectedAPIConstructorI() throws Exception {
        xAddSingletonProtectedAPIConstructor(true);
    }

    public void testAddSingletonProtectedAPIConstructorF() throws Exception {
        xAddSingletonProtectedAPIConstructor(false);
    }

    /**
	 * Tests the addition of a singleton protected constructor in an API class.
	 */
    private void xAddSingletonPublicAPIConstructor(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("AddSinglePublicConstructor.java");
        int[] ids = new int[] { getDefaultProblemId() };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "AddSinglePublicConstructor", "AddSinglePublicConstructor()" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testAddSingletonPublicAPIConstructorI() throws Exception {
        xAddSingletonPublicAPIConstructor(true);
    }

    public void testAddSingletonPublicAPIConstructorF() throws Exception {
        xAddSingletonPublicAPIConstructor(false);
    }
}
