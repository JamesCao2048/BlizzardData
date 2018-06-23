/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.comparator.tests;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.api.tools.internal.provisional.VisibilityModifiers;
import org.eclipse.pde.api.tools.internal.provisional.comparator.ApiComparator;
import org.eclipse.pde.api.tools.internal.provisional.comparator.ApiScope;
import org.eclipse.pde.api.tools.internal.provisional.comparator.DeltaProcessor;
import org.eclipse.pde.api.tools.internal.provisional.comparator.IDelta;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiBaseline;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiComponent;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiMethod;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiScope;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiType;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiTypeContainer;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiTypeRoot;

/**
 * Delta tests using api scope
 */
public class ApiScopeDeltaTests extends DeltaTestSetup {

    public static Test suite() {
        return new TestSuite(ApiScopeDeltaTests.class);
    // TestSuite suite = new TestSuite(EnumDeltaTests.class.getName());
    // suite.addTest(new EnumDeltaTests("test13"));
    // return suite;
    }

    public  ApiScopeDeltaTests(String name) {
        super(name);
    }

    @Override
    public String getTestRoot() {
        //$NON-NLS-1$
        return "scope";
    }

    /**
	 * Use api scope
	 */
    public void test1() throws CoreException {
        //$NON-NLS-1$
        deployBundles("test1");
        IApiBaseline before = getBeforeState();
        IApiBaseline after = getAfterState();
        IApiComponent beforeApiComponent = before.getApiComponent(BUNDLE_NAME);
        //$NON-NLS-1$
        assertNotNull("no api component", beforeApiComponent);
        IApiComponent afterApiComponent = after.getApiComponent(BUNDLE_NAME);
        //$NON-NLS-1$
        assertNotNull("no api component", afterApiComponent);
        ApiScope scope = new ApiScope();
        scope.addElement(after);
        IApiElement[] apiElement = scope.getApiElements();
        //$NON-NLS-1$
        assertEquals("Empty", 1, apiElement.length);
        IDelta delta = ApiComparator.compare(scope, before, VisibilityModifiers.API, false, null);
        //$NON-NLS-1$
        assertNotNull("No delta", delta);
        IDelta[] allLeavesDeltas = collectLeaves(delta);
        //$NON-NLS-1$
        assertEquals("Wrong size", 1, allLeavesDeltas.length);
        IDelta child = allLeavesDeltas[0];
        //$NON-NLS-1$
        assertEquals("Wrong kind", IDelta.REMOVED, child.getKind());
        //$NON-NLS-1$
        assertEquals("Wrong flag", IDelta.METHOD, child.getFlags());
        //$NON-NLS-1$
        assertEquals("Wrong element type", IDelta.CLASS_ELEMENT_TYPE, child.getElementType());
        //$NON-NLS-1$
        assertFalse("Is compatible", DeltaProcessor.isCompatible(child));
    }

    /**
	 * Use api scope
	 */
    public void test2() throws CoreException {
        //$NON-NLS-1$
        deployBundles("test2");
        IApiBaseline before = getBeforeState();
        IApiBaseline after = getAfterState();
        IApiComponent beforeApiComponent = before.getApiComponent(BUNDLE_NAME);
        //$NON-NLS-1$
        assertNotNull("no api component", beforeApiComponent);
        IApiComponent afterApiComponent = after.getApiComponent(BUNDLE_NAME);
        //$NON-NLS-1$
        assertNotNull("no api component", afterApiComponent);
        ApiScope scope = new ApiScope();
        IApiComponent[] apiComponents = after.getApiComponents();
        for (int i = 0, max = apiComponents.length; i < max; i++) {
            scope.addElement(apiComponents[i]);
        }
        IDelta delta = ApiComparator.compare(scope, before, VisibilityModifiers.API, true, null);
        //$NON-NLS-1$
        assertNotNull("No delta", delta);
        IDelta[] allLeavesDeltas = collectLeaves(delta);
        //$NON-NLS-1$
        assertEquals("Wrong size", 1, allLeavesDeltas.length);
        IDelta child = allLeavesDeltas[0];
        //$NON-NLS-1$
        assertEquals("Wrong kind", IDelta.REMOVED, child.getKind());
        //$NON-NLS-1$
        assertEquals("Wrong flag", IDelta.METHOD, child.getFlags());
        //$NON-NLS-1$
        assertEquals("Wrong element type", IDelta.CLASS_ELEMENT_TYPE, child.getElementType());
        //$NON-NLS-1$
        assertFalse("Is compatible", DeltaProcessor.isCompatible(child));
    }

    /**
	 * Use api scope
	 */
    public void test3() throws CoreException {
        //$NON-NLS-1$
        deployBundles("test3");
        IApiBaseline before = getBeforeState();
        IApiBaseline after = getAfterState();
        IApiComponent beforeApiComponent = before.getApiComponent(BUNDLE_NAME);
        //$NON-NLS-1$
        assertNotNull("no api component", beforeApiComponent);
        IApiComponent afterApiComponent = after.getApiComponent(BUNDLE_NAME);
        //$NON-NLS-1$
        assertNotNull("no api component", afterApiComponent);
        ApiScope scope = new ApiScope();
        IApiComponent[] apiComponents = after.getApiComponents();
        for (int i = 0, max = apiComponents.length; i < max; i++) {
            IApiTypeContainer[] apiTypeContainers = apiComponents[i].getApiTypeContainers();
            for (int j = 0; j < apiTypeContainers.length; j++) {
                IApiTypeContainer iApiTypeContainer = apiTypeContainers[j];
                scope.addElement(iApiTypeContainer);
            }
        }
        IDelta delta = ApiComparator.compare(scope, before, VisibilityModifiers.API, true, null);
        //$NON-NLS-1$
        assertNotNull("No delta", delta);
        IDelta[] allLeavesDeltas = collectLeaves(delta);
        //$NON-NLS-1$
        assertEquals("Wrong size", 1, allLeavesDeltas.length);
        IDelta child = allLeavesDeltas[0];
        //$NON-NLS-1$
        assertEquals("Wrong kind", IDelta.REMOVED, child.getKind());
        //$NON-NLS-1$
        assertEquals("Wrong flag", IDelta.METHOD, child.getFlags());
        //$NON-NLS-1$
        assertEquals("Wrong element type", IDelta.CLASS_ELEMENT_TYPE, child.getElementType());
        //$NON-NLS-1$
        assertFalse("Is compatible", DeltaProcessor.isCompatible(child));
    }

    /**
	 * Use api scope
	 */
    public void test4() throws CoreException {
        //$NON-NLS-1$
        deployBundles("test4");
        IApiBaseline before = getBeforeState();
        IApiBaseline after = getAfterState();
        IApiComponent beforeApiComponent = before.getApiComponent(BUNDLE_NAME);
        //$NON-NLS-1$
        assertNotNull("no api component", beforeApiComponent);
        IApiComponent afterApiComponent = after.getApiComponent(BUNDLE_NAME);
        //$NON-NLS-1$
        assertNotNull("no api component", afterApiComponent);
        ApiScope scope = new ApiScope();
        IApiComponent[] apiComponents = after.getApiComponents();
        IApiTypeRoot root = null;
        for (int i = 0, max = apiComponents.length; i < max; i++) {
            //$NON-NLS-1$
            IApiTypeRoot findTypeRoot = apiComponents[i].findTypeRoot("p.X");
            if (findTypeRoot != null) {
                root = findTypeRoot;
                break;
            }
        }
        if (root != null) {
            scope.addElement(root);
        }
        IDelta delta = ApiComparator.compare(scope, before, VisibilityModifiers.API, true, null);
        //$NON-NLS-1$
        assertNotNull("No delta", delta);
        IDelta[] allLeavesDeltas = collectLeaves(delta);
        //$NON-NLS-1$
        assertEquals("Wrong size", 1, allLeavesDeltas.length);
        IDelta child = allLeavesDeltas[0];
        //$NON-NLS-1$
        assertEquals("Wrong kind", IDelta.REMOVED, child.getKind());
        //$NON-NLS-1$
        assertEquals("Wrong flag", IDelta.METHOD, child.getFlags());
        //$NON-NLS-1$
        assertEquals("Wrong element type", IDelta.CLASS_ELEMENT_TYPE, child.getElementType());
        //$NON-NLS-1$
        assertFalse("Is compatible", DeltaProcessor.isCompatible(child));
    }

    /**
	 * Use api scope
	 */
    public void test5() throws CoreException {
        //$NON-NLS-1$
        deployBundles("test5");
        IApiBaseline before = getBeforeState();
        IApiBaseline after = getAfterState();
        IApiComponent beforeApiComponent = before.getApiComponent(BUNDLE_NAME);
        //$NON-NLS-1$
        assertNotNull("no api component", beforeApiComponent);
        IApiComponent afterApiComponent = after.getApiComponent(BUNDLE_NAME);
        //$NON-NLS-1$
        assertNotNull("no api component", afterApiComponent);
        ApiScope scope = new ApiScope();
        IApiElement[] apiElement = scope.getApiElements();
        //$NON-NLS-1$
        assertEquals("Not empty", 0, apiElement.length);
        IApiComponent[] apiComponents = after.getApiComponents();
        IApiTypeRoot root = null;
        for (int i = 0, max = apiComponents.length; i < max; i++) {
            //$NON-NLS-1$
            IApiTypeRoot findTypeRoot = apiComponents[i].findTypeRoot("p.X");
            if (findTypeRoot != null) {
                root = findTypeRoot;
                break;
            }
        }
        if (root != null) {
            IApiType structure = root.getStructure();
            IApiMethod[] methods = structure.getMethods();
            for (int i = 0, max = methods.length; i < max; i++) {
                scope.addElement(methods[i]);
            }
        }
        try {
            ApiComparator.compare(scope, before, VisibilityModifiers.API, true, null);
            //$NON-NLS-1$
            assertFalse("Should not be there", true);
        } catch (CoreException e) {
        }
    }

    /**
	 * Use api scope
	 */
    public void test6() throws CoreException {
        //$NON-NLS-1$
        deployBundles("test6");
        IApiBaseline before = getBeforeState();
        IApiBaseline after = getAfterState();
        IApiComponent beforeApiComponent = before.getApiComponent(BUNDLE_NAME);
        //$NON-NLS-1$
        assertNotNull("no api component", beforeApiComponent);
        IApiComponent afterApiComponent = after.getApiComponent(BUNDLE_NAME);
        //$NON-NLS-1$
        assertNotNull("no api component", afterApiComponent);
        ApiScope scope = new ApiScope();
        scope.addElement(after);
        IDelta delta = ApiComparator.compare(scope, before, VisibilityModifiers.API, false, null);
        //$NON-NLS-1$
        assertNotNull("No delta", delta);
        //$NON-NLS-1$
        assertTrue("Not NO_DELTA", delta == ApiComparator.NO_DELTA);
    }

    /**
	 * Use api scope
	 */
    public void test7() throws CoreException {
        //$NON-NLS-1$
        deployBundles("test7");
        IApiBaseline before = getBeforeState();
        IApiBaseline after = getAfterState();
        IApiComponent beforeApiComponent = before.getApiComponent(BUNDLE_NAME);
        //$NON-NLS-1$
        assertNotNull("no api component", beforeApiComponent);
        IApiComponent afterApiComponent = after.getApiComponent(BUNDLE_NAME);
        //$NON-NLS-1$
        assertNotNull("no api component", afterApiComponent);
        ApiScope scope = new ApiScope();
        scope.addElement(after);
        try {
            ApiComparator.compare((IApiScope) null, before, VisibilityModifiers.API, false, null);
            //$NON-NLS-1$
            assertFalse("Should not be there", true);
        } catch (IllegalArgumentException e) {
        }
        try {
            ApiComparator.compare(scope, null, VisibilityModifiers.API, false, null);
            //$NON-NLS-1$
            assertFalse("Should not be there", true);
        } catch (IllegalArgumentException e) {
        }
    }
}
