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
package org.eclipse.pde.api.tools.problems.tests;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.pde.api.tools.internal.problems.ApiProblem;
import org.eclipse.pde.api.tools.internal.problems.ApiProblemFactory;
import org.eclipse.pde.api.tools.internal.provisional.ApiPlugin;
import org.eclipse.pde.api.tools.internal.provisional.comparator.IDelta;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IElementDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblem;
import org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblemTypes;
import org.eclipse.pde.api.tools.internal.util.Util;
import org.eclipse.pde.api.tools.tests.AbstractApiTest;

/**
 * Tests aspects of a {@link ApiProblem} and {@link ApiProblemFactory}
 *
 * @since 1.0.0
 */
public class ApiProblemTests extends AbstractApiTest {

    /**
	 * Tests that two problems are equal (when they are known to be)
	 */
    public void testProblemsEqual() {
        //$NON-NLS-1$
        IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        IApiProblem problem2 = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem2);
        //$NON-NLS-1$
        assertTrue("the two problems should be equal", problem.equals(problem2));
        //$NON-NLS-1$
        assertTrue("the two problems should be equal", problem2.equals(problem));
    }

    /**
	 * Tests that two problems are not equal (when they are known not to be)
	 */
    public void testProblemsNotEqual() {
        IApiProblem problem = ApiProblemFactory.newApiProblem(null, null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        IApiProblem problem2 = ApiProblemFactory.newApiProblem(null, null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem2);
        //$NON-NLS-1$
        assertTrue("the two problems should not be equal", !problem.equals(problem2));
        //$NON-NLS-1$
        assertTrue("the two problems should not be equal", !problem2.equals(problem));
    }

    /**
	 * Tests that two problems are not equal if one has a resource path and the
	 * the other does not
	 */
    public void testProblemsNotEqualMissingResourcePath() {
        //$NON-NLS-1$
        IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z/").toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        IApiProblem problem2 = ApiProblemFactory.newApiProblem(null, null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem2);
        //$NON-NLS-1$
        assertTrue("the two problems should not be equal", !problem.equals(problem2));
        //$NON-NLS-1$
        assertTrue("the two problems should not be equal", !problem2.equals(problem));
        problem = ApiProblemFactory.newApiProblem(null, null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        problem2 = ApiProblemFactory.newApiProblem(new Path("x/y/z/").toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem2);
        //$NON-NLS-1$
        assertTrue("the two problems should not be equal", !problem.equals(problem2));
        //$NON-NLS-1$
        assertTrue("the two problems should not be equal", !problem2.equals(problem));
    }

    /**
	 * Tests that two problems are not equal if their resource paths differ but
	 * are not null
	 */
    public void testProblemsNotEqualDifferentPaths() {
        //$NON-NLS-1$
        IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/yy/z").toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        IApiProblem problem2 = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem2);
        //$NON-NLS-1$
        assertTrue("the two problems should not be equal", !problem.equals(problem2));
        //$NON-NLS-1$
        assertTrue("the two problems should not be equal", !problem2.equals(problem));
    }

    /**
	 * Tests that two problems are not equal if their type names differ because
	 * one is null
	 */
    public void testNotEqualDifferingTypeNames() {
        //$NON-NLS-1$ //$NON-NLS-2$
        IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), "x.y.z.foo", null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        IApiProblem problem2 = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem2);
        //$NON-NLS-1$
        assertTrue("the two problems should not be equal", !problem.equals(problem2));
        //$NON-NLS-1$
        assertTrue("the two problems should not be equal", !problem2.equals(problem));
    }

    /**
	 * Tests that two problems are not equal if the message arguments are not
	 * equals
	 */
    public void testNotEqualDifferingMessageArguments() {
        //$NON-NLS-1$ //$NON-NLS-2$
        IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, new String[] { "one" }, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        IApiProblem problem2 = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem2);
        //$NON-NLS-1$
        assertTrue("the two problems should not be equal", !problem.equals(problem2));
        //$NON-NLS-1$
        assertTrue("the two problems should not be equal", !problem2.equals(problem));
        //$NON-NLS-1$
        problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$ //$NON-NLS-2$
        problem2 = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, new String[] { "one" }, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem2);
        //$NON-NLS-1$
        assertTrue("the two problems should not be equal", !problem.equals(problem2));
        //$NON-NLS-1$
        assertTrue("the two problems should not be equal", !problem2.equals(problem));
    }

    /**
	 * Tests that two problems are not equal if the message arguments are not
	 * equals
	 */
    public void testNotEqualDifferingMessageArgumentsNumber() {
        //$NON-NLS-1$ //$NON-NLS-2$
        IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, new String[] { "one" }, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        IApiProblem problem2 = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, new String[] { "one", "two" }, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem2);
        //$NON-NLS-1$
        assertTrue("the two problems should not be equal", !problem.equals(problem2));
        //$NON-NLS-1$
        assertTrue("the two problems should not be equal", !problem2.equals(problem));
    }

    /**
	 * Tests that an object other than an {@link IApiProblem} is not equal
	 */
    public void testNotEqualDifferentObjects() {
        IApiProblem problem = ApiProblemFactory.newApiProblem(null, null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue("the two problems should not be equal", !problem.equals(new String("API Problem")));
    }

    /**
	 * Tests that a problem category is encoded and decoded properly from a
	 * problem id
	 */
    public void testGetCategory() {
        IApiProblem problem = ApiProblemFactory.newApiProblem(null, null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertEquals("the category should be CATEGORY_BINARY", IApiProblem.CATEGORY_COMPATIBILITY, problem.getCategory());
    }

    /**
	 * Tests that a problem id is encoded properly
	 */
    public void testGetId() {
        IApiProblem problem = ApiProblemFactory.newApiProblem(null, null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertEquals("the problemids should match", problem.getId(), ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS));
    }

    /**
	 * Tests that a problem kind is encoded and decoded properly from a problem
	 * id
	 */
    public void testGetKind() {
        IApiProblem problem = ApiProblemFactory.newApiProblem(null, null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertEquals("the kind should be ILLEGAL_IMPLEMENT", problem.getKind(), IApiProblem.ILLEGAL_IMPLEMENT);
    }

    /**
	 * Tests that problem flags are encoded and decoded properly
	 */
    public void testGetFlags() {
        IApiProblem problem = ApiProblemFactory.newApiProblem(null, null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertEquals("the kind should be ANNOTATION_DEFAULT_VALUE", problem.getFlags(), IDelta.ANNOTATION_DEFAULT_VALUE);
    }

    /**
	 * Tests that a problem element kind is encoded and decoded properly
	 */
    public void testGetElementKind() {
        IApiProblem problem = ApiProblemFactory.newApiProblem(null, null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertEquals("the element kind should be T_FIELD", problem.getElementKind(), IElementDescriptor.FIELD);
    }

    /**
	 * Tests getting the resource path attribute
	 */
    public void testGetResourcePath() {
        //$NON-NLS-1$
        IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertNotNull("there should be a path set on the problem", problem.getResourcePath());
    }

    /**
	 * Tests getting a localized message for the problem
	 */
    public void testGetMessage() {
        //$NON-NLS-1$
        IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertNotNull("the message should be null", problem.getMessage());
        //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue("the not found message should be displayed", problem.getMessage().startsWith("Message not found for id: "));
    }

    /**
	 * Tests getting the charstart attribute
	 */
    public void testGetCharStart() {
        //$NON-NLS-1$
        IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, null, null, null, -1, 57, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertEquals("the charstart should be 57", problem.getCharStart(), 57);
    }

    /**
	 * Tests getting the charend attribute
	 */
    public void testGetCharEnd() {
        //$NON-NLS-1$
        IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, null, null, null, -1, -1, 57, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertEquals("the charend should be 57", problem.getCharEnd(), 57);
    }

    /**
	 * Tests getting the line number attribute
	 */
    public void testGetLineNumber() {
        //$NON-NLS-1$
        IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, null, null, null, 57, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertEquals("the line number should be 57", problem.getLineNumber(), 57);
    }

    /**
	 * Tests that passing in null for problem arguments will return an empty
	 * array
	 */
    public void testGetExtraArgumentNamesNull() {
        //$NON-NLS-1$
        IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertNotNull("null passed in should return an emtpy array", problem.getExtraMarkerAttributeIds());
    }

    /**
	 * Tests that getting the extra argument ids will return an empty array when
	 * the number of ids does not match the number of arguments
	 */
    public void testGetExtraArgumentIdsNotNullNotEqualLength() {
        IApiProblem problem = //$NON-NLS-1$
        ApiProblemFactory.newApiProblem(//$NON-NLS-1$
        new Path("x/y/z").toPortableString(), null, null, new //$NON-NLS-1$
        String[] //$NON-NLS-1$
        { "one" }, //$NON-NLS-1$ //$NON-NLS-2$
        new String[] { "one", "two" }, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertNotNull("null passed in should return an emtpy array", problem.getExtraMarkerAttributeIds());
        String[] args = problem.getExtraMarkerAttributeIds();
        //$NON-NLS-1$
        assertNotNull("the argument ids array type should not be null", args);
        //$NON-NLS-1$
        assertTrue("there should be no arguments returned", args.length == 0);
    }

    /**
	 * Tests that getting the extra argument values will return an empty array
	 * when the number of ids does not match the number of arguments
	 */
    public void testGetExtraArgumentValuesNotNullNotEqualLength() {
        IApiProblem problem = //$NON-NLS-1$
        ApiProblemFactory.newApiProblem(//$NON-NLS-1$
        new Path("x/y/z").toPortableString(), null, null, new //$NON-NLS-1$
        String[] //$NON-NLS-1$
        { "one" }, //$NON-NLS-1$ //$NON-NLS-2$
        new String[] { "one", "two" }, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertNotNull("null passed in should return an emtpy array", problem.getExtraMarkerAttributeIds());
        Object[] args = problem.getExtraMarkerAttributeValues();
        //$NON-NLS-1$
        assertNotNull("the argument ids array type should not be null", args);
        //$NON-NLS-1$
        assertTrue("there should be no arguments returned", args.length == 0);
    }

    /**
	 * Tests that passing in null for problem arguments will return an empty
	 * array
	 */
    public void testGetExtraArgumentValuesNull() {
        //$NON-NLS-1$
        IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertNotNull("null passed in should return an emtpy array", problem.getExtraMarkerAttributeValues());
    }

    /**
	 * Tests that non-null argument names and null values will return an empty
	 * array from a call to getExtramarkerAttributeIds (we have to have matching
	 * arrays for valid returns)
	 */
    public void tesGetExtraArgumentIdsNotNullValuesNull() {
        //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, null, new String[] { "test1", "test2", "test3" }, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertNotNull("argument ids should not be null", problem.getExtraMarkerAttributeIds());
        //$NON-NLS-1$
        assertTrue("argument size should be 0", problem.getExtraMarkerAttributeIds().length == 0);
    }

    /**
	 * Tests that non-null argument values and null ids will return an empty
	 * array from a call to getExtraMarkerAttributeValues (we have to have
	 * matching arrays for valid returns)
	 */
    public void tesGetExtraArgumentNamesNotNullIdsNull() {
        //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, null, null, new String[] { "test1", "test2", "test3" }, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertNotNull("arguments should not be null", problem.getExtraMarkerAttributeValues());
        //$NON-NLS-1$
        assertTrue("argument size should be 0", problem.getExtraMarkerAttributeValues().length == 0);
    }

    /**
	 * Tests that non-null argument values and non-null ids will return an empty
	 * array from a call to getExtraMarkerAttributeValues when the arrays are
	 * not the same size (we have to have matching arrays for valid returns)
	 */
    public void tesGetExtraArgumentsNotSameSize() {
        //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, null, new String[] { "id1", "id2" }, new String[] { "test1", "test2", "test3" }, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertNotNull("argument ids should not be null", problem.getExtraMarkerAttributeValues());
        //$NON-NLS-1$
        assertNotNull("argument values should not be null", problem.getExtraMarkerAttributeValues());
        //$NON-NLS-1$
        assertTrue("argument size should be 0", problem.getExtraMarkerAttributeIds().length == 0);
        //$NON-NLS-1$
        assertTrue("argument size should be 0", problem.getExtraMarkerAttributeValues().length == 0);
    }

    /**
	 * Tests that non-null argument values and non-null ids will return an the
	 * passed in arrays from a call to getExtraMarkerAttributeValues when the
	 * arrays are the same size (we have to have matching arrays for valid
	 * returns)
	 */
    public void tesGetExtraArgumentsSameSize() {
        //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, null, new String[] { "id1", "id2", "id3" }, new String[] { "value1", "value2", "value3" }, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertNotNull("argument ids should not be null", problem.getExtraMarkerAttributeValues());
        //$NON-NLS-1$
        assertNotNull("argument values should not be null", problem.getExtraMarkerAttributeValues());
        //$NON-NLS-1$
        assertTrue("argument size should be 3", problem.getExtraMarkerAttributeIds().length == 3);
        //$NON-NLS-1$
        assertTrue("argument size should be 3", problem.getExtraMarkerAttributeValues().length == 3);
    }

    /**
	 * Tests that passing in null for message arguments will return an empty
	 * array
	 */
    public void testGetMessageArgumentsNull() {
        //$NON-NLS-1$
        IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertNotNull("null passed in should return an emtpy array", problem.getMessageArguments());
    }

    /**
	 * Tests that non-null message arguments passed a retrievable
	 */
    public void testGetMessageArgumentsNotNull() {
        //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, new String[] { "test1", "test2", "test3" }, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertNotNull("arguments should not be null", problem.getMessageArguments());
        //$NON-NLS-1$
        assertTrue("argument size should be 3", problem.getMessageArguments().length == 3);
    }

    /**
	 * Tests that toString does not return null
	 */
    public void testToString() {
        //$NON-NLS-1$ //$NON-NLS-2$
        IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, new String[] { "test1, test2, test3" }, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertNotNull("there should be a string", problem.toString());
        //$NON-NLS-1$
        problem = ApiProblemFactory.newApiProblem(null, null, new String[] { "test1, test2, test3" }, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        //$NON-NLS-1$
        assertNotNull("there should be a string", problem.toString());
    }

    /**
	 * Tests getting the severity attribute
	 */
    public void testGetSeverity() {
        if (ApiPlugin.isRunningInFramework()) {
            IEclipsePreferences inode = InstanceScope.INSTANCE.getNode(ApiPlugin.PLUGIN_ID);
            //$NON-NLS-1$
            assertNotNull("The instance preference node must exist", inode);
            inode.put(IApiProblemTypes.ILLEGAL_IMPLEMENT, ApiPlugin.VALUE_IGNORE);
            //$NON-NLS-1$ //$NON-NLS-2$
            IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, new String[] { "test1, test2, test3" }, null, null, -1, -1, -1, IApiProblem.CATEGORY_USAGE, IElementDescriptor.TYPE, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
            //$NON-NLS-1$
            assertNotNull("there should have been a new problem created", problem);
            //$NON-NLS-1$
            assertEquals("the severity should be IGNORE", IMarker.SEVERITY_INFO, problem.getSeverity());
            IEclipsePreferences dnode = DefaultScope.INSTANCE.getNode(ApiPlugin.PLUGIN_ID);
            //$NON-NLS-1$
            assertNotNull("the default pref node must exist", dnode);
            inode.put(IApiProblemTypes.ILLEGAL_IMPLEMENT, dnode.get(IApiProblemTypes.ILLEGAL_IMPLEMENT, ApiPlugin.VALUE_WARNING));
        } else {
            //$NON-NLS-1$ //$NON-NLS-2$
            IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, new String[] { "test1, test2, test3" }, null, null, -1, -1, -1, IApiProblem.CATEGORY_USAGE, IElementDescriptor.TYPE, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
            //$NON-NLS-1$
            assertNotNull("there should have been a new problem created", problem);
            //$NON-NLS-1$
            assertEquals("the severity should be WARNING (no framework running)", IMarker.SEVERITY_WARNING, problem.getSeverity());
        }
    }

    /**
	 * Tests the {@link ApiProblem#getDescriptorKind(int)} method
	 */
    public void testGetDescriptorKind() {
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the kind should be 'PACKAGE'", "PACKAGE", ApiProblem.getDescriptorKind(IElementDescriptor.PACKAGE));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the kind should be 'REFERENCE_TYPE'", "REFERENCE_TYPE", ApiProblem.getDescriptorKind(IElementDescriptor.TYPE));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the kind should be 'METHOD'", "METHOD", ApiProblem.getDescriptorKind(IElementDescriptor.METHOD));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the kind should be 'FIELD'", "FIELD", ApiProblem.getDescriptorKind(IElementDescriptor.FIELD));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the kind should be 'RESOURCE'", "RESOURCE", ApiProblem.getDescriptorKind(IElementDescriptor.RESOURCE));
        //$NON-NLS-1$
        assertEquals("the kind should be 'UNKOWN_ELEMENT_KIND'", Util.UNKNOWN_ELEMENT_KIND, ApiProblem.getDescriptorKind(-1));
    }

    /**
	 * Tests the {@link ApiProblem#getTagsProblemKindName(int)} method
	 */
    public void testGetTagsProblemKindName() {
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the tag problem kind should be 'INVALID_SINCE_TAGS'", "INVALID_SINCE_TAGS", ApiProblem.getTagsProblemKindName(IApiProblem.SINCE_TAG_INVALID));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the tag problem kind should be 'MALFORMED_SINCE_TAGS'", "MALFORMED_SINCE_TAGS", ApiProblem.getTagsProblemKindName(IApiProblem.SINCE_TAG_MALFORMED));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the tag problem kind should be 'MISSING_SINCE_TAGS'", "MISSING_SINCE_TAGS", ApiProblem.getTagsProblemKindName(IApiProblem.SINCE_TAG_MISSING));
        //$NON-NLS-1$
        assertEquals("the tag problem kind should be 'UNKNOWN_KIND'", Util.UNKNOWN_KIND, ApiProblem.getTagsProblemKindName(-1));
    }

    /**
	 * Tests the {@link ApiProblem#getUsageProblemKindName(int)} method
	 */
    public void testGetUsageProblemKindName() {
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the usage problem kind should be 'ILLEGAL_EXTEND'", "ILLEGAL_EXTEND", ApiProblem.getUsageProblemKindName(IApiProblem.ILLEGAL_EXTEND));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the usage problem kind should be 'ILLEGAL_IMPLEMENT'", "ILLEGAL_IMPLEMENT", ApiProblem.getUsageProblemKindName(IApiProblem.ILLEGAL_IMPLEMENT));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the usage problem kind should be 'ILLEGAL_INSTANTIATE'", "ILLEGAL_INSTANTIATE", ApiProblem.getUsageProblemKindName(IApiProblem.ILLEGAL_INSTANTIATE));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the usage problem kind should be 'ILLEGAL_OVERRIDE'", "ILLEGAL_OVERRIDE", ApiProblem.getUsageProblemKindName(IApiProblem.ILLEGAL_OVERRIDE));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the usage problem kind should be 'ILLEGAL_REFERENCE'", "ILLEGAL_REFERENCE", ApiProblem.getUsageProblemKindName(IApiProblem.ILLEGAL_REFERENCE));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the usage problem kind should be 'API_LEAK'", "API_LEAK", ApiProblem.getUsageProblemKindName(IApiProblem.API_LEAK));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the usage problem kind should be 'UNSUPPORTED_TAG_USE'", "UNSUPPORTED_TAG_USE", ApiProblem.getUsageProblemKindName(IApiProblem.UNSUPPORTED_TAG_USE));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the usage problem kind should be 'INVALID_REFERENCE_IN_SYSTEM_LIBRARIES'", "INVALID_REFERENCE_IN_SYSTEM_LIBRARIES", ApiProblem.getUsageProblemKindName(IApiProblem.INVALID_REFERENCE_IN_SYSTEM_LIBRARIES));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the usage problem kind should be 'UNUSED_PROBLEM_FILTERS'", "UNUSED_PROBLEM_FILTERS", ApiProblem.getUsageProblemKindName(IApiProblem.UNUSED_PROBLEM_FILTERS));
        //$NON-NLS-1$
        assertEquals("the usage problem kind should be 'UNKNOWN_KIND'", Util.UNKNOWN_KIND, ApiProblem.getUsageProblemKindName(-1));
    }

    /**
	 * Tests the {@link ApiProblem#getVersionProblemKindName(int)} method
	 */
    public void testGetVersionProblemKindName() {
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the version problem kind should be 'MINOR_VERSION_CHANGE'", "MINOR_VERSION_CHANGE", ApiProblem.getVersionProblemKindName(IApiProblem.MINOR_VERSION_CHANGE));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the version problem kind should be 'MAJOR_VERSION_CHANGE'", "MAJOR_VERSION_CHANGE", ApiProblem.getVersionProblemKindName(IApiProblem.MAJOR_VERSION_CHANGE));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the version problem kind should be 'MAJOR_VERSION_CHANGE_NO_BREAKAGE'", "MAJOR_VERSION_CHANGE_NO_BREAKAGE", ApiProblem.getVersionProblemKindName(IApiProblem.MAJOR_VERSION_CHANGE_NO_BREAKAGE));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the version problem kind should be 'MINOR_VERSION_CHANGE_NO_NEW_API'", "MINOR_VERSION_CHANGE_NO_NEW_API", ApiProblem.getVersionProblemKindName(IApiProblem.MINOR_VERSION_CHANGE_NO_NEW_API));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the version problem kind should be 'REEXPORTED_MAJOR_VERSION_CHANGE'", "REEXPORTED_MAJOR_VERSION_CHANGE", ApiProblem.getVersionProblemKindName(IApiProblem.REEXPORTED_MAJOR_VERSION_CHANGE));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the version problem kind should be 'MINOR_VERSION_CHANGE'", "REEXPORTED_MINOR_VERSION_CHANGE", ApiProblem.getVersionProblemKindName(IApiProblem.REEXPORTED_MINOR_VERSION_CHANGE));
        //$NON-NLS-1$
        assertEquals("the version problem kind should be 'UNKNOWN_KIND'", Util.UNKNOWN_KIND, ApiProblem.getVersionProblemKindName(-1));
    }

    /**
	 * Tests the
	 * {@link ApiProblem#getApiComponentResolutionProblemKindName(int)} method
	 */
    public void testGetApiComponentResolutionProblemKindName() {
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the component resolution problem kind should be 'API_COMPONENT_RESOLUTION'", "API_COMPONENT_RESOLUTION", ApiProblem.getApiComponentResolutionProblemKindName(IApiProblem.API_COMPONENT_RESOLUTION));
        //$NON-NLS-1$
        assertEquals("the component resolution problem kind should be 'UNKNOWN_KIND'", Util.UNKNOWN_KIND, ApiProblem.getApiComponentResolutionProblemKindName(-1));
    }

    /**
	 * Tests the {@link ApiProblem#getApiBaselineProblemKindName(int)} method
	 */
    public void testGetApiBaselineProblemKindName() {
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the baseline problem kind should be 'API_BASELINE_MISSING'", "API_BASELINE_MISSING", ApiProblem.getApiBaselineProblemKindName(IApiProblem.API_BASELINE_MISSING));
        //$NON-NLS-1$
        assertEquals("the baseline problem kind should be 'UNKNOWN_KIND'", Util.UNKNOWN_KIND, ApiProblem.getApiBaselineProblemKindName(-1));
    }

    /**
	 * Tests the {@link ApiProblem#getProblemKind(int, int)} method
	 */
    public void testGetProblemKind() {
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem kind should be 'API_COMPONENT_RESOLUTION'", "API_COMPONENT_RESOLUTION", ApiProblem.getProblemKind(IApiProblem.CATEGORY_API_COMPONENT_RESOLUTION, IApiProblem.API_COMPONENT_RESOLUTION));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem kind should be 'API_BASELINE_MISSING'", "API_BASELINE_MISSING", ApiProblem.getProblemKind(IApiProblem.CATEGORY_API_BASELINE, IApiProblem.API_BASELINE_MISSING));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem kind should be 'INVALID_SINCE_TAGS'", "INVALID_SINCE_TAGS", ApiProblem.getProblemKind(IApiProblem.CATEGORY_SINCETAGS, IApiProblem.SINCE_TAG_INVALID));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem kind should be 'ILLEGAL_EXTEND'", "ILLEGAL_EXTEND", ApiProblem.getProblemKind(IApiProblem.CATEGORY_USAGE, IApiProblem.ILLEGAL_EXTEND));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem kind should be 'MINOR_VERSION_CHANGE'", "MINOR_VERSION_CHANGE", ApiProblem.getProblemKind(IApiProblem.CATEGORY_VERSION, IApiProblem.MINOR_VERSION_CHANGE));
        //$NON-NLS-1$
        assertEquals("the problem kind should be 'UNKNOWN_KIND'", Util.UNKNOWN_KIND, ApiProblem.getProblemKind(-1, -1));
    }

    /**
	 * Tests the {@link ApiProblem#getProblemFlagsName(int, int)} method
	 */
    public void testGetProblemFlagsName() {
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem flags kind should be 'LEAK_CONSTRUCTOR_PARAMETER'", "LEAK_CONSTRUCTOR_PARAMETER", ApiProblem.getProblemFlagsName(IApiProblem.CATEGORY_USAGE, IApiProblem.LEAK_CONSTRUCTOR_PARAMETER));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem flags kind should be 'LEAK_EXTENDS'", "LEAK_EXTENDS", ApiProblem.getProblemFlagsName(IApiProblem.CATEGORY_USAGE, IApiProblem.LEAK_EXTENDS));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem flags kind should be 'LEAK_FIELD'", "LEAK_FIELD", ApiProblem.getProblemFlagsName(IApiProblem.CATEGORY_USAGE, IApiProblem.LEAK_FIELD));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem flags kind should be 'LEAK_IMPLEMENTS'", "LEAK_IMPLEMENTS", ApiProblem.getProblemFlagsName(IApiProblem.CATEGORY_USAGE, IApiProblem.LEAK_IMPLEMENTS));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem flags kind should be 'LEAK_METHOD_PARAMETER'", "LEAK_METHOD_PARAMETER", ApiProblem.getProblemFlagsName(IApiProblem.CATEGORY_USAGE, IApiProblem.LEAK_METHOD_PARAMETER));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem flags kind should be 'LEAK_RETURN_TYPE'", "LEAK_RETURN_TYPE", ApiProblem.getProblemFlagsName(IApiProblem.CATEGORY_USAGE, IApiProblem.LEAK_RETURN_TYPE));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem flags kind should be 'CONSTRUCTOR_METHOD'", "CONSTRUCTOR_METHOD", ApiProblem.getProblemFlagsName(IApiProblem.CATEGORY_USAGE, IApiProblem.CONSTRUCTOR_METHOD));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem flags kind should be 'NO_FLAGS'", "NO_FLAGS", ApiProblem.getProblemFlagsName(IApiProblem.CATEGORY_USAGE, IApiProblem.NO_FLAGS));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem flags kind should be 'INDIRECT_REFERENCE'", "INDIRECT_REFERENCE", ApiProblem.getProblemFlagsName(IApiProblem.CATEGORY_USAGE, IApiProblem.INDIRECT_REFERENCE));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem flags kind should be 'METHOD'", "METHOD", ApiProblem.getProblemFlagsName(IApiProblem.CATEGORY_USAGE, IApiProblem.METHOD));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem flags kind should be 'FIELD'", "FIELD", ApiProblem.getProblemFlagsName(IApiProblem.CATEGORY_USAGE, IApiProblem.FIELD));
        //$NON-NLS-1$
        assertEquals("the problem flags kind should be 'UNKNOWN_FLAGS'", Util.UNKNOWN_FLAGS, ApiProblem.getProblemFlagsName(-1, -1));
    }

    /**
	 * Tests the {@link ApiProblem#getProblemElementKind(int, int)} method
	 */
    public void testGetProblemElementKind() {
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem element kind should be 'METHOD'", "METHOD", ApiProblem.getProblemElementKind(IApiProblem.CATEGORY_USAGE, IElementDescriptor.METHOD));
        //$NON-NLS-1$
        assertEquals("the problem element kind should be 'UNKNOWN_KIND'", Util.UNKNOWN_KIND, ApiProblem.getProblemElementKind(-1, -1));
    }

    /**
	 * Tests the {@link ApiProblem#getProblemCategory(int)} method
	 */
    public void testgetProblemCategory() {
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem category kind should be 'API_BASELINE'", "API_BASELINE", ApiProblem.getProblemCategory(IApiProblem.CATEGORY_API_BASELINE));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem category kind should be 'API_COMPONENT_RESOLUTION'", "API_COMPONENT_RESOLUTION", ApiProblem.getProblemCategory(IApiProblem.CATEGORY_API_COMPONENT_RESOLUTION));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem category kind should be 'COMPATIBILITY'", "COMPATIBILITY", ApiProblem.getProblemCategory(IApiProblem.CATEGORY_COMPATIBILITY));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem category kind should be 'SINCETAGS'", "SINCETAGS", ApiProblem.getProblemCategory(IApiProblem.CATEGORY_SINCETAGS));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem category kind should be 'USAGE'", "USAGE", ApiProblem.getProblemCategory(IApiProblem.CATEGORY_USAGE));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem category kind should be 'VERSION'", "VERSION", ApiProblem.getProblemCategory(IApiProblem.CATEGORY_VERSION));
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("the problem category kind should be 'UNKNOWN_CATEGORY'", "UNKNOWN_CATEGORY", ApiProblem.getProblemCategory(-1));
    }

    /**
	 * Regression test for the hash code of an {@link IApiProblem}.
	 */
    public void testGetHashCode() {
        //$NON-NLS-1$ //$NON-NLS-2$
        IApiProblem problem = ApiProblemFactory.newApiProblem(new Path("x/y/z").toPortableString(), null, new String[] { "test1, test2, test3" }, null, null, 2, 2, 2, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        assertEquals(//$NON-NLS-1$
        "the hashcode should be equal to the sum of: id, resourcepath.hashCode", //$NON-NLS-1$
        problem.hashCode(), //$NON-NLS-1$
        (problem.getId() + problem.getResourcePath().hashCode() + argumentsHashcode(new String[] { "test1, test2, test3" })));
    }

    /**
	 * Regression test for the hash code of an {@link IApiProblem}.
	 */
    public void testGetHashCodeResourcePathNull() {
        //$NON-NLS-1$
        IApiProblem problem = ApiProblemFactory.newApiProblem(null, null, new String[] { "test1, test2, test3" }, null, null, 2, 2, 2, IApiProblem.CATEGORY_COMPATIBILITY, IElementDescriptor.FIELD, IApiProblem.ILLEGAL_IMPLEMENT, IDelta.ANNOTATION_DEFAULT_VALUE);
        //$NON-NLS-1$
        assertNotNull("there should have been a new problem created", problem);
        assertEquals(//$NON-NLS-1$
        "the hashcode should be equal to the sum of: id, resourcepath.hashCode", //$NON-NLS-1$
        problem.hashCode(), //$NON-NLS-1$
        (problem.getId() + 0 + argumentsHashcode(new String[] { "test1, test2, test3" })));
    }

    /**
	 * Helper method to get a hash code for problem arguments
	 *
	 * @param arguments
	 * @return
	 */
    private int argumentsHashcode(String[] arguments) {
        if (arguments == null) {
            return 0;
        }
        int hashcode = 0;
        for (int i = 0; i < arguments.length; i++) {
            hashcode += arguments[i].hashCode();
        }
        return hashcode;
    }
}
