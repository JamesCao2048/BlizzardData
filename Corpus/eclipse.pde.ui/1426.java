/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.builder.tests.usage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.pde.api.tools.builder.tests.ApiBuilderTest;
import org.eclipse.pde.api.tools.builder.tests.ApiProblem;
import org.eclipse.pde.api.tools.builder.tests.ApiTestingEnvironment;
import org.eclipse.pde.api.tools.model.tests.TestSuiteHelper;

/**
 * General test for Java 8 usage
 *
 * @since 1.0.600
 */
public class Java8UsageTest extends ApiBuilderTest {

    public  Java8UsageTest(String name) {
        super(name);
    }

    @Override
    protected String getTestCompliance() {
        return JavaCore.VERSION_1_8;
    }

    @Override
    protected IPath getTestSourcePath() {
        //$NON-NLS-1$ //$NON-NLS-2$
        return new Path("usage").append("java8");
    }

    @Override
    protected int getDefaultProblemId() {
        return -1;
    }

    @Override
    protected String getTestingProjectName() {
        //$NON-NLS-1$
        return "usageprojectjava8";
    }

    @Override
    protected void setUp() throws Exception {
        ApiTestingEnvironment env = getEnv();
        if (env != null) {
            env.setRevert(true);
            env.setRevertSourcePath(null);
        }
        super.setUp();
        //$NON-NLS-1$
        IProject project = getEnv().getWorkspace().getRoot().getProject("refproject");
        if (!project.exists()) {
            //$NON-NLS-1$ //$NON-NLS-2$
            IPath path = TestSuiteHelper.getPluginDirectoryPath().append(TEST_SOURCE_ROOT).append("usageprojects").append("refproject");
            createExistingProject(path.toFile(), true, false);
        }
        project = getEnv().getWorkspace().getRoot().getProject(getTestingProjectName());
        if (!project.exists()) {
            // populate the workspace with initial plug-ins/projects
            //$NON-NLS-1$
            createExistingProjects("usageprojectsjava8", true, true, false);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ApiTestingEnvironment env = getEnv();
        if (env != null) {
            env.setRevert(false);
        }
    }

    /**
	 * Deploys a standard API usage test with the test project being created and
	 * the given source is imported in the testing project into the given
	 * project.
	 *
	 * This method assumes that the reference and testing project have been
	 * imported into the workspace already.
	 *
	 * @param sourcename
	 * @param inc if an incremental build should be done
	 */
    protected void deployUsageTest(String typename, boolean inc) {
        try {
            //$NON-NLS-1$
            IPath typepath = new Path(getTestingProjectName()).append(UsageTest.SOURCE_PATH).append(typename).addFileExtension("java");
            //$NON-NLS-1$
            createWorkspaceFile(typepath, TestSuiteHelper.getPluginDirectoryPath().append(TEST_SOURCE_ROOT).append(getTestSourcePath()).append(typename).addFileExtension("java"));
            if (inc) {
                incrementalBuild();
            } else {
                fullBuild();
            }
            expectingNoJDTProblemsFor(typepath);
            ApiProblem[] problems = getEnv().getProblemsFor(typepath, null);
            assertProblems(problems);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
	 * @return the tests for this class
	 */
    public static Test suite() {
        TestSuite suite = new TestSuite(Java8UsageTest.class.getName());
        collectTests(suite);
        return suite;
    }

    /**
	 * Collects tests from the getAllTestClasses() method into the given suite
	 *
	 * @param suite
	 */
    private static void collectTests(TestSuite suite) {
        // Hack to load all classes before computing their suite of test cases
        // this allow to reset test cases subsets while running all Builder
        // tests...
        Class<?>[] classes = getAllTestClasses();
        // Reset forgotten subsets of tests
        TestCase.TESTS_PREFIX = null;
        TestCase.TESTS_NAMES = null;
        TestCase.TESTS_NUMBERS = null;
        TestCase.TESTS_RANGE = null;
        TestCase.RUN_ONLY_ID = null;
        /* tests */
        for (int i = 0, length = classes.length; i < length; i++) {
            Class<?> clazz = classes[i];
            Method suiteMethod;
            try {
                suiteMethod = //$NON-NLS-1$
                clazz.getDeclaredMethod(//$NON-NLS-1$
                "suite", //$NON-NLS-1$
                new Class[0]);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                continue;
            }
            Object test;
            try {
                test = suiteMethod.invoke(null, new Object[0]);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                continue;
            }
            suite.addTest((Test) test);
        }
    }

    /**
	 *
	 * @return all of the child test classes of this class
	 */
    private static Class<?>[] getAllTestClasses() {
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(Java8LambdaUsageTests.class);
        classes.add(Java8MethodConstRefUsageTests.class);
        classes.add(Java8ConsRefInstantiateUsageTests.class);
        classes.add(Java8DefaultMethodUsageTests.class);
        return classes.toArray(new Class[classes.size()]);
    }
}
