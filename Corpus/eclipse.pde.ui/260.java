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
 * Tests that the builder correctly finds and reports enum compatibility
 * problems
 *
 * @since 1.0
 */
public class EnumCompatibilityTests extends CompatibilityTest {

    /**
	 * Workspace relative path classes in bundle/project A
	 */
    //$NON-NLS-1$
    protected static IPath WORKSPACE_CLASSES_PACKAGE_A = new Path("bundle.a/src/a/enums");

    /**
	 * Package prefix for test classes
	 */
    //$NON-NLS-1$
    protected static String PACKAGE_PREFIX = "a.enums.";

    /**
	 * Constructor
	 * @param name
	 */
    public  EnumCompatibilityTests(String name) {
        super(name);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.ApiBuilderTests#getTestSourcePath()
	 */
    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$
        return super.getTestSourcePath().append("enums");
    }

    /**
	 * @return the tests for this class
	 */
    public static Test suite() {
        return buildTestSuite(EnumCompatibilityTests.class);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.ApiBuilderTest#getDefaultProblemId()
	 */
    @Override
    protected int getDefaultProblemId() {
        return 0;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.ApiBuilderTests#getTestingProjectName()
	 */
    @Override
    protected String getTestingProjectName() {
        //$NON-NLS-1$
        return "enumcompat";
    }

    /**
	 * Tests removing a member type
	 */
    private void xRemoveMemberType(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemoveMemberType.java");
        int[] ids = new int[] { ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_COMPATIBILITY, IDelta.ENUM_ELEMENT_TYPE, IDelta.REMOVED, IDelta.TYPE_MEMBER) };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "RemoveMemberType.MemberType", "bundle.a_1.0.0" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemoveMemberTypeI() throws Exception {
        xRemoveMemberType(true);
    }

    public void testRemoveMemberTypeF() throws Exception {
        xRemoveMemberType(false);
    }

    /**
	 * Tests removing a method
	 */
    private void xRemoveMethod(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemoveMethod.java");
        int[] ids = new int[] { ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_COMPATIBILITY, IDelta.ENUM_ELEMENT_TYPE, IDelta.REMOVED, IDelta.METHOD) };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "RemoveMethod", "method(int)" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemoveMethodI() throws Exception {
        xRemoveMethod(true);
    }

    public void testRemoveMethodF() throws Exception {
        xRemoveMethod(false);
    }

    /**
	 * Tests conversion to a class
	 */
    private void xConvertToClass(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("ToClass.java");
        int[] ids = new int[] { ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_COMPATIBILITY, IDelta.ENUM_ELEMENT_TYPE, IDelta.CHANGED, IDelta.TYPE_CONVERSION) };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        args[0] = new String[] { //$NON-NLS-1$
        PACKAGE_PREFIX + "ToClass", Integer.toString(IDelta.ENUM_ELEMENT_TYPE), Integer.toString(IDelta.CLASS_ELEMENT_TYPE) };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testConvertToClassI() throws Exception {
        xConvertToClass(true);
    }

    public void testConvertToClassF() throws Exception {
        xConvertToClass(false);
    }

    /**
	 * Tests conversion to an annotation
	 */
    private void xConvertToAnnotation(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("ToAnnotation.java");
        int[] ids = new int[] { ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_COMPATIBILITY, IDelta.ENUM_ELEMENT_TYPE, IDelta.CHANGED, IDelta.TYPE_CONVERSION) };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        args[0] = new String[] { //$NON-NLS-1$
        PACKAGE_PREFIX + "ToAnnotation", Integer.toString(IDelta.ENUM_ELEMENT_TYPE), Integer.toString(IDelta.ANNOTATION_ELEMENT_TYPE) };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testConvertToAnnotationI() throws Exception {
        xConvertToAnnotation(true);
    }

    public void testConvertToAnnotationF() throws Exception {
        xConvertToAnnotation(false);
    }

    /**
	 * Tests conversion to an interface
	 */
    private void xConvertToInterface(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("ToInterface.java");
        int[] ids = new int[] { ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_COMPATIBILITY, IDelta.ENUM_ELEMENT_TYPE, IDelta.CHANGED, IDelta.TYPE_CONVERSION) };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        args[0] = new String[] { //$NON-NLS-1$
        PACKAGE_PREFIX + "ToInterface", Integer.toString(IDelta.ENUM_ELEMENT_TYPE), Integer.toString(IDelta.INTERFACE_ELEMENT_TYPE) };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testConvertToInterfaceI() throws Exception {
        xConvertToInterface(true);
    }

    public void testConvertToInterfaceF() throws Exception {
        xConvertToInterface(false);
    }

    /**
	 * Tests removing a field
	 */
    private void xRemoveField(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemoveField.java");
        int[] ids = new int[] { ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_COMPATIBILITY, IDelta.ENUM_ELEMENT_TYPE, IDelta.REMOVED, IDelta.FIELD) };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "RemoveField", "FIELD" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemoveFieldI() throws Exception {
        xRemoveField(true);
    }

    public void testRemoveFieldF() throws Exception {
        xRemoveField(false);
    }

    /**
	 * Tests removing an enum constant
	 */
    private void xRemoveEnumConstant(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemoveEnumConstant.java");
        int[] ids = new int[] { ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_COMPATIBILITY, IDelta.ENUM_ELEMENT_TYPE, IDelta.REMOVED, IDelta.ENUM_CONSTANT) };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { PACKAGE_PREFIX + "RemoveEnumConstant", "A" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemoveEnumConstantI() throws Exception {
        xRemoveEnumConstant(true);
    }

    public void testRemoveEnumConstantF() throws Exception {
        xRemoveEnumConstant(false);
    }

    /**
	 * Tests reducing super interface set
	 */
    private void xRemoveSuperInterface(boolean incremental) throws Exception {
        //$NON-NLS-1$
        IPath filePath = WORKSPACE_CLASSES_PACKAGE_A.append("RemoveSuperInterface.java");
        int[] ids = new int[] { ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_COMPATIBILITY, IDelta.ENUM_ELEMENT_TYPE, IDelta.CHANGED, IDelta.CONTRACTED_SUPERINTERFACES_SET) };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$
        args[0] = new String[] { PACKAGE_PREFIX + "RemoveSuperInterface" };
        setExpectedMessageArgs(args);
        performCompatibilityTest(filePath, incremental);
    }

    public void testRemoveSuperInterfaceI() throws Exception {
        xRemoveSuperInterface(true);
    }

    public void testRemoveSuperInterfaceF() throws Exception {
        xRemoveSuperInterface(false);
    }
}
