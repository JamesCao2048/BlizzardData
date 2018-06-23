/*******************************************************************************
 * Copyright (c) 2008, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.model.tests;

import java.io.File;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.pde.api.tools.internal.ApiFilterStore;
import org.eclipse.pde.api.tools.internal.FilterStore;
import org.eclipse.pde.api.tools.internal.model.ApiModelFactory;
import org.eclipse.pde.api.tools.internal.problems.ApiProblemFactory;
import org.eclipse.pde.api.tools.internal.problems.ApiProblemFilter;
import org.eclipse.pde.api.tools.internal.provisional.ApiPlugin;
import org.eclipse.pde.api.tools.internal.provisional.IApiFilterStore;
import org.eclipse.pde.api.tools.internal.provisional.RestrictionModifiers;
import org.eclipse.pde.api.tools.internal.provisional.comparator.IDelta;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IElementDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiBaseline;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiComponent;
import org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblem;
import org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblemFilter;
import org.eclipse.pde.api.tools.tests.AbstractApiTest;
import org.eclipse.pde.api.tools.tests.util.FileUtils;

/**
 * Tests the {@link ApiFilterStore} and {@link ApiProblemFilter}s
 *
 * @since 1.0.0
 */
public class ApiFilterStoreTests extends AbstractApiTest {

    //$NON-NLS-1$
    private static final IPath SRC_LOC = TestSuiteHelper.getPluginDirectoryPath().append("test-source");

    //$NON-NLS-1$
    private static final IPath XML_LOC = TestSuiteHelper.getPluginDirectoryPath().append("test-xml");

    //$NON-NLS-1$
    private static final IPath PLUGIN_LOC = TestSuiteHelper.getPluginDirectoryPath().append("test-plugins");

    /* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
    @Override
    protected void setUp() throws Exception {
        createProject(TESTING_PLUGIN_PROJECT_NAME, null);
        File projectSrc = SRC_LOC.toFile();
        //$NON-NLS-1$
        assertTrue("the filter source dir must exist", projectSrc.exists());
        //$NON-NLS-1$
        assertTrue("the filter source dir must be a directory", projectSrc.isDirectory());
        IJavaProject project = getTestingJavaProject(TESTING_PLUGIN_PROJECT_NAME);
        //$NON-NLS-1$
        IPackageFragmentRoot srcroot = project.findPackageFragmentRoot(project.getProject().getFullPath().append("src"));
        //$NON-NLS-1$
        assertNotNull("the default src root must exist", srcroot);
        FileUtils.importFileFromDirectory(projectSrc, srcroot.getPath(), new NullProgressMonitor());
        // Import the test .api_filters file
        //$NON-NLS-1$
        File xmlsrc = XML_LOC.append(".api_filters").toFile();
        //$NON-NLS-1$
        assertTrue("the filter xml dir must exist", xmlsrc.exists());
        //$NON-NLS-1$
        assertTrue("the filter xml dir must be a file", !xmlsrc.isDirectory());
        //$NON-NLS-1$
        assertNotNull("no project", project);
        IProject project2 = project.getProject();
        //$NON-NLS-1$
        IPath settings = project2.getFullPath().append(".settings");
        FileUtils.importFileFromDirectory(xmlsrc, settings, new NullProgressMonitor());
        //$NON-NLS-1$
        IResource filters = project2.findMember("/.settings/.api_filters", true);
        //$NON-NLS-1$
        assertNotNull("the .api_filters file must exist in the testing project", filters);
    }

    /* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
    @Override
    protected void tearDown() throws Exception {
        deleteProject(TESTING_PLUGIN_PROJECT_NAME);
    }

    /**
	 * Runs a series of assertions against the loaded {@link IApiFilterStore}
	 * @param store the store to check
	 */
    private void assertFilterStore(IApiFilterStore store, int count) {
        //$NON-NLS-1$
        assertNotNull("the filter store for the testing project cannot be null", store);
        IResource[] resources = store.getResources();
        //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("there should be " + count + " resources with filters", count, resources.length);
        IJavaProject jproject = getTestingJavaProject(TESTING_PLUGIN_PROJECT_NAME);
        IProject project = jproject.getProject();
        //C4
        //$NON-NLS-1$
        IResource resource = project.findMember(new Path("src/x/y/z/C4.java"));
        //$NON-NLS-1$
        assertNotNull("the resource src/x/y/z/C4.java must exist", resource);
        IApiProblemFilter[] filters = store.getFilters(resource);
        //$NON-NLS-1$
        assertTrue("There should be 1 filter for src/x/y/z/C4.java", filters.length == 1);
        IApiProblem problem = ApiProblemFactory.newApiProblem(resource.getProjectRelativePath().toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_USAGE, IElementDescriptor.TYPE, IApiProblem.ILLEGAL_IMPLEMENT, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertTrue("the usage problem for src/x/y/z/C4.java should be filtered", store.isFiltered(problem));
        //C1
        //$NON-NLS-1$
        resource = project.findMember(new Path("src/x/C1.java"));
        //$NON-NLS-1$
        assertNotNull("the resource src/x/C1.java must exist", resource);
        filters = store.getFilters(resource);
        //$NON-NLS-1$
        assertTrue("there should be 2 filters for src/x/C1.java", filters.length == 2);
        problem = ApiProblemFactory.newApiProblem(resource.getProjectRelativePath().toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, 4, IDelta.REMOVED, IDelta.FIELD);
        //$NON-NLS-1$
        assertTrue("the removed binary problem for src/x/C1.java should be filtered", store.isFiltered(problem));
        problem = ApiProblemFactory.newApiProblem(resource.getProjectRelativePath().toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_COMPATIBILITY, 4, IDelta.CHANGED, IDelta.VARARGS_TO_ARRAY);
        //$NON-NLS-1$
        assertTrue("the changed binary problem for src/x/C1.java should be filtered", store.isFiltered(problem));
        //C3
        //$NON-NLS-1$
        resource = project.findMember(new Path("src/x/y/C3.java"));
        //$NON-NLS-1$
        assertNotNull("the resource src/x/y/C3.java must exist");
        filters = store.getFilters(resource);
        //$NON-NLS-1$
        assertTrue("there should be 2 filters for src/x/y/C3.java", filters.length == 2);
        problem = ApiProblemFactory.newApiProblem(resource.getProjectRelativePath().toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_VERSION, 7, IApiProblem.MAJOR_VERSION_CHANGE, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertTrue("the major version problem for src/x/y/C3.java should be filtered", store.isFiltered(problem));
        problem = ApiProblemFactory.newApiProblem(resource.getProjectRelativePath().toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_VERSION, 7, IApiProblem.MINOR_VERSION_CHANGE, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertTrue("the minor version problem for src/x/y/C3.java should be filtered", store.isFiltered(problem));
        //MANIFEST.MF
        //$NON-NLS-1$
        resource = project.findMember(new Path("META-INF/MANIFEST.MF"));
        //$NON-NLS-1$
        assertNotNull("the resource META-INF/MANIFEST.MF must exist", resource);
        filters = store.getFilters(resource);
        //$NON-NLS-1$
        assertTrue("there should be 3 filters for META-INF/MANIFEST.MF", filters.length == 3);
        problem = ApiProblemFactory.newApiProblem(resource.getProjectRelativePath().toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_SINCETAGS, 7, IApiProblem.SINCE_TAG_MISSING, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertTrue("the missing since tag problem should be filtered for META-INF/MANIFEST.MF", store.isFiltered(problem));
        problem = ApiProblemFactory.newApiProblem(resource.getProjectRelativePath().toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_SINCETAGS, 7, IApiProblem.SINCE_TAG_MALFORMED, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertTrue("the malformed since tag problem should be filtered for META-INF/MANIFEST.MF", store.isFiltered(problem));
        problem = ApiProblemFactory.newApiProblem(resource.getProjectRelativePath().toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_SINCETAGS, 7, IApiProblem.SINCE_TAG_INVALID, IApiProblem.NO_FLAGS);
        //$NON-NLS-1$
        assertTrue("the invalid since tag problem should be filterd for META-INF/MANIFEST.MF", store.isFiltered(problem));
    }

    /**
	 * Tests that a filter store can be correctly annotated from a persisted version
	 */
    public void testAnnotateStoreFromLocalFile() {
        IApiComponent component = getProjectApiComponent(TESTING_PLUGIN_PROJECT_NAME);
        //$NON-NLS-1$
        assertNotNull("the testing project api component must exist", component);
        try {
            assertFilterStore(component.getFilterStore(), 4);
        } catch (CoreException e) {
            fail(e.getMessage());
        }
    }

    /**
	 * Tests that asking the store if it filters an invalid problem will return 'false'
	 */
    public void testNonExistantProblem() {
        IApiComponent component = getProjectApiComponent(TESTING_PLUGIN_PROJECT_NAME);
        //$NON-NLS-1$
        assertNotNull("the testing project api component must exist", component);
        try {
            IApiFilterStore store = component.getFilterStore();
            IProject project = getTestingJavaProject(TESTING_PLUGIN_PROJECT_NAME).getProject();
            //$NON-NLS-1$
            IResource resource = project.findMember(new Path("src/x/y/z/C4.java"));
            //$NON-NLS-1$
            assertNotNull("the resource src/x/y/z/C4.java must exist", resource);
            IApiProblem problem = ApiProblemFactory.newApiProblem(resource.getProjectRelativePath().toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_USAGE, 0, IApiProblem.MINOR_VERSION_CHANGE, IDelta.ADDED);
            //$NON-NLS-1$
            assertFalse("the bogus problem should not be filtered", store.isFiltered(problem));
        } catch (CoreException e) {
            fail(e.getMessage());
        }
    }

    /**
	 * tests removing an api problem filter
	 */
    public void testRemoveFilter() {
        try {
            IApiComponent component = getProjectApiComponent(TESTING_PLUGIN_PROJECT_NAME);
            //$NON-NLS-1$
            assertNotNull("the testing project api component must exist", component);
            IProject project = getTestingJavaProject(TESTING_PLUGIN_PROJECT_NAME).getProject();
            //$NON-NLS-1$
            IResource resource = project.findMember(new Path("src/x/y/z/C4.java"));
            //$NON-NLS-1$
            assertNotNull("the resource src/x/y/z/C4.java must exist", resource);
            IApiProblem problem = ApiProblemFactory.newApiProblem(resource.getProjectRelativePath().toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_USAGE, 0, RestrictionModifiers.NO_IMPLEMENT, IApiProblem.NO_FLAGS);
            IApiFilterStore store;
            store = component.getFilterStore();
            store.removeFilters(new IApiProblemFilter[] { ApiProblemFactory.newProblemFilter(component.getSymbolicName(), problem, null) });
            //$NON-NLS-1$
            assertFalse("src/x/y/z/C4.java should not have a filter", store.isFiltered(problem));
        } catch (CoreException e) {
            fail(e.getMessage());
        }
    }

    /**
	 * tests adding a filter using the method that accepts a filter
	 */
    public void testAddFilterFromFilter() {
        try {
            IApiComponent component = getProjectApiComponent(TESTING_PLUGIN_PROJECT_NAME);
            //$NON-NLS-1$
            assertNotNull("the testing project api component must exist", component);
            IProject project = getTestingJavaProject(TESTING_PLUGIN_PROJECT_NAME).getProject();
            //$NON-NLS-1$
            IResource resource = project.findMember(new Path("src/x/y/z/C4.java"));
            //$NON-NLS-1$
            assertNotNull("the resource src/x/y/z/C4.java must exist", resource);
            IApiProblem problem = ApiProblemFactory.newApiProblem(resource.getProjectRelativePath().toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_USAGE, 0, RestrictionModifiers.NO_IMPLEMENT, IApiProblem.NO_FLAGS);
            IApiFilterStore store;
            store = component.getFilterStore();
            store.addFilters(new IApiProblemFilter[] { ApiProblemFactory.newProblemFilter(component.getSymbolicName(), problem, null) });
            //$NON-NLS-1$
            assertTrue("src/x/y/z/C4.java should have a filter", store.isFiltered(problem));
            store.removeFilters(new IApiProblemFilter[] { ApiProblemFactory.newProblemFilter(component.getSymbolicName(), problem, null) });
            //$NON-NLS-1$
            assertFalse("src/x/y/z/C4.java should not have a filter", store.isFiltered(problem));
        } catch (CoreException ce) {
            fail(ce.getMessage());
        }
    }

    /**
	 * tests adding a filter using the method that accepts an api problem
	 */
    public void testAddFilterFromProblem() {
        try {
            IApiComponent component = getProjectApiComponent(TESTING_PLUGIN_PROJECT_NAME);
            //$NON-NLS-1$
            assertNotNull("the testing project api component must exist", component);
            IProject project = getTestingJavaProject(TESTING_PLUGIN_PROJECT_NAME).getProject();
            //$NON-NLS-1$
            IResource resource = project.findMember(new Path("src/x/y/z/C4.java"));
            //$NON-NLS-1$
            assertNotNull("the resource src/x/y/z/C4.java must exist", resource);
            IApiProblem problem = ApiProblemFactory.newApiProblem(resource.getProjectRelativePath().toPortableString(), null, null, null, null, -1, -1, -1, IApiProblem.CATEGORY_USAGE, 0, RestrictionModifiers.NO_IMPLEMENT, IApiProblem.NO_FLAGS);
            IApiFilterStore store;
            store = component.getFilterStore();
            store.addFiltersFor(new IApiProblem[] { problem });
            //$NON-NLS-1$
            assertTrue("src/x/y/z/C4.java should have a filter", store.isFiltered(problem));
            store.removeFilters(new IApiProblemFilter[] { ApiProblemFactory.newProblemFilter(component.getSymbolicName(), problem, null) });
            //$NON-NLS-1$
            assertFalse("src/x/y/z/C4.java should not have a filter", store.isFiltered(problem));
        } catch (CoreException ce) {
            fail(ce.getMessage());
        }
    }

    /**
	 * Tests that a filter store will not be annotated from a bundle
	 */
    public void testAnnotateStoreFromBundle() {
        try {
            IProject project = getTestingJavaProject(TESTING_PLUGIN_PROJECT_NAME).getProject();
            //$NON-NLS-1$
            FileUtils.importFileFromDirectory(PLUGIN_LOC.append("component_c_1.0.0.jar").toFile(), project.getFullPath(), new NullProgressMonitor());
            //$NON-NLS-1$
            IResource res = project.findMember("component_c_1.0.0.jar");
            //$NON-NLS-1$
            assertNotNull("the jar should exist in the project dir", res);
            //$NON-NLS-1$
            IResource jar = project.findMember("component_c_1.0.0.jar");
            //$NON-NLS-1$
            assertNotNull("the component_c jar cannot be null", jar);
            IApiBaseline profile = ApiPlugin.getDefault().getApiBaselineManager().getWorkspaceBaseline();
            IApiComponent component = ApiModelFactory.newApiComponent(profile, jar.getLocation().toOSString());
            profile.addApiComponents(new IApiComponent[] { component });
            //$NON-NLS-1$
            assertNotNull("the new component cannot be null", component);
            IApiFilterStore store = component.getFilterStore();
            //$NON-NLS-1$
            assertFalse("the new filter store must not be an instance of ApiFilterStore", store instanceof ApiFilterStore);
            //$NON-NLS-1$
            assertTrue("the new filter store must be an instance of FilterStore", store instanceof FilterStore);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
