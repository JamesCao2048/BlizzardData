/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.builder.tests.compatibility;

import java.io.File;
import junit.framework.Test;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.pde.api.tools.builder.tests.ApiBuilderTest;
import org.eclipse.pde.api.tools.builder.tests.ApiProblem;
import org.eclipse.pde.api.tools.builder.tests.ApiTestingEnvironment;
import org.eclipse.pde.api.tools.internal.model.ApiModelFactory;
import org.eclipse.pde.api.tools.internal.problems.ApiProblemFactory;
import org.eclipse.pde.api.tools.internal.provisional.ApiPlugin;
import org.eclipse.pde.api.tools.internal.provisional.IApiBaselineManager;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IElementDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiBaseline;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiComponent;
import org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblem;
import org.eclipse.pde.api.tools.tests.ApiTestsPlugin;

/**
 * Tests for splitting plug-ins across releases
 *
 * @since 1.0
 */
public class BundleVersionTests extends ApiBuilderTest {

    //$NON-NLS-1$
    private static final String API_BASELINE = "API-baseline";

    /**
	 * Workspace relative path
	 */
    //$NON-NLS-1$
    protected static String WORKSPACE_ROOT = "bundleversions";

    //$NON-NLS-1$
    public static final String WORKSPACE_PROFILE = "after";

    //$NON-NLS-1$
    public static final String BASELINE = "before";

    IApiBaseline baseline;

    /**
	 * Constructor
	 *
	 * @param name
	 */
    public  BundleVersionTests(String name) {
        super(name);
    }

    /*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.pde.api.tools.builder.tests.ApiBuilderTests#setBuilderOptions
	 * ()
	 */
    @Override
    protected void setBuilderOptions() {
        enableUnsupportedTagOptions(false);
        enableUnsupportedAnnotationOptions(false);
        enableBaselineOptions(true);
        enableCompatibilityOptions(true);
        enableLeakOptions(false);
        enableSinceTagOptions(false);
        enableUsageOptions(false);
        enableVersionNumberOptions(true);
    }

    /**
	 * @return the tests for this class
	 */
    public static Test suite() {
        return buildTestSuite(BundleVersionTests.class);
    }

    /*
	 * (non-Javadoc) Ensure a baseline has been created to compare against.
	 * @see org.eclipse.pde.api.tools.builder.tests.ApiBuilderTest#setUp()
	 */
    @Override
    protected void setUp() throws Exception {
        ApiTestingEnvironment env = getEnv();
        if (env != null) {
            env.setRevert(true);
        }
        super.setUp();
    }

    /*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.pde.api.tools.builder.tests.ApiBuilderTest#getDefaultProblemId
	 * ()
	 */
    @Override
    protected int getDefaultProblemId() {
        return 0;
    }

    /*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.pde.api.tools.builder.tests.ApiBuilderTest#getTestSourcePath
	 * ()
	 */
    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$
        return new Path("");
    }

    /*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.pde.api.tools.builder.tests.ApiBuilderTest#getTestingProjectName
	 * ()
	 */
    @Override
    protected String getTestingProjectName() {
        //$NON-NLS-1$
        return "bundleversions";
    }

    @Override
    protected void tearDown() throws Exception {
        IApiBaselineManager manager = ApiPlugin.getDefault().getApiBaselineManager();
        manager.setDefaultApiBaseline(null);
        manager.removeApiBaseline(API_BASELINE);
        this.baseline.dispose();
        manager.getWorkspaceBaseline().dispose();
        this.baseline = null;
        IProject[] projects = getEnv().getWorkspace().getRoot().getProjects();
        for (int i = 0, length = projects.length; i < length; i++) {
            getEnv().removeProject(projects[i].getFullPath());
        }
        super.tearDown();
        getEnv().setRevert(false);
    }

    private void performBundleVersion() throws CoreException {
        cleanBuild();
        fullBuild();
        expectingNoJDTProblems();
        //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        IPath manifestPath = new Path("deltatest").append("META-INF").append("MANIFEST.MF");
        ApiProblem[] problems = getEnv().getProblemsFor(manifestPath, null);
        assertProblems(problems);
    }

    private String getReferenceBaselineLocation(String testName) {
        return WORKSPACE_ROOT + File.separator + testName + File.separator + BASELINE;
    }

    private String getCurrentBaselineLocation(String testName) {
        return WORKSPACE_ROOT + File.separator + testName + File.separator + WORKSPACE_PROFILE;
    }

    private void setupTest(String testName) throws Exception {
        // build the baseline if not present
        IApiBaselineManager manager = ApiPlugin.getDefault().getApiBaselineManager();
        String referenceBaselineLocation = getReferenceBaselineLocation(testName);
        // import baseline projects
        createExistingProjects(referenceBaselineLocation, false, true, false);
        // create the API baseline
        cleanBuild();
        fullBuild();
        IProject[] projects = getEnv().getWorkspace().getRoot().getProjects();
        int length = projects.length;
        IPath baselineLocation = ApiTestsPlugin.getDefault().getStateLocation().append(referenceBaselineLocation);
        for (int i = 0; i < length; i++) {
            IProject currentProject = projects[i];
            IApiComponent apiComponent = manager.getWorkspaceComponent(currentProject.getName());
            //$NON-NLS-1$
            assertNotNull("The project was not found in the workspace baseline: " + currentProject.getName(), apiComponent);
            exportApiComponent(currentProject, apiComponent, baselineLocation);
        }
        this.baseline = ApiModelFactory.newApiBaseline(API_BASELINE);
        IApiComponent[] components = new IApiComponent[length];
        for (int i = 0; i < length; i++) {
            IProject project = projects[i];
            IPath location = baselineLocation.append(project.getName());
            components[i] = ApiModelFactory.newApiComponent(this.baseline, location.toOSString());
        }
        this.baseline.addApiComponents(components);
        manager.addApiBaseline(this.baseline);
        manager.setDefaultApiBaseline(this.baseline.getName());
        // delete the projects
        for (int i = 0; i < length; i++) {
            getEnv().removeProject(projects[i].getFullPath());
        }
        // populate the workspace with initial plug-ins/projects
        String currentBaselineLocation = getCurrentBaselineLocation(testName);
        createExistingProjects(currentBaselineLocation, false, true, false);
    }

    /**
	 * Tests that changing the version of a re-exported bundle has no impact on
	 * the version of the current bundle as long as the version is within the
	 * range and the range has not changed.
	 *
	 * @throws Exception
	 */
    public void test001() throws Exception {
        // setup the environment
        //$NON-NLS-1$
        setupTest("test1");
        // expecting no problems
        performBundleVersion();
    }

    /**
	 * Tests that decreasing the minor version of the lower bound of the
	 * re-exported bundle range triggers a major version change.
	 *
	 * @throws Exception
	 */
    public void test002() throws Exception {
        // setup the environment
        //$NON-NLS-1$
        setupTest("test2");
        // expecting no problems
        int[] ids = new int[] { ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_VERSION, IElementDescriptor.RESOURCE, IApiProblem.REEXPORTED_MAJOR_VERSION_CHANGE, IApiProblem.NO_FLAGS) };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { "1.0.0", "exportedbundle" };
        setExpectedMessageArgs(args);
        performBundleVersion();
    }

    /**
	 * Tests that decreasing the major version of the lower bound of the
	 * re-exported bundle range triggers a major version change.
	 *
	 * @throws Exception
	 */
    public void test003() throws Exception {
        // setup the environment
        //$NON-NLS-1$
        setupTest("test3");
        // expecting no problems
        int[] ids = new int[] { ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_VERSION, IElementDescriptor.RESOURCE, IApiProblem.REEXPORTED_MAJOR_VERSION_CHANGE, IApiProblem.NO_FLAGS) };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { "1.0.0", "exportedbundle" };
        setExpectedMessageArgs(args);
        performBundleVersion();
    }

    /**
	 * Tests that increasing the major version of the lower bound of the
	 * re-exported bundle range triggers a major version change.
	 *
	 * @throws Exception
	 */
    public void test004() throws Exception {
        // setup the environment
        //$NON-NLS-1$
        setupTest("test4");
        // expecting no problems
        int[] ids = new int[] { ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_VERSION, IElementDescriptor.RESOURCE, IApiProblem.REEXPORTED_MAJOR_VERSION_CHANGE, IApiProblem.NO_FLAGS) };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { "1.0.0", "exportedbundle" };
        setExpectedMessageArgs(args);
        performBundleVersion();
    }

    /**
	 * Tests that increasing the minor version of the lower bound of the
	 * re-exported bundle range triggers a minor version change.
	 *
	 * @throws Exception
	 */
    public void test005() throws Exception {
        // setup the environment
        //$NON-NLS-1$
        setupTest("test5");
        // expecting no problems
        int[] ids = new int[] { ApiProblemFactory.createProblemId(IApiProblem.CATEGORY_VERSION, IElementDescriptor.RESOURCE, IApiProblem.REEXPORTED_MINOR_VERSION_CHANGE, IApiProblem.NO_FLAGS) };
        setExpectedProblemIds(ids);
        String[][] args = new String[1][];
        //$NON-NLS-1$ //$NON-NLS-2$
        args[0] = new String[] { "1.0.0", "exportedbundle" };
        setExpectedMessageArgs(args);
        performBundleVersion();
    }
}
