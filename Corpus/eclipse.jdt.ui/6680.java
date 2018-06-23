/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.junit.tests;

import org.eclipse.jdt.junit.JUnitCore;
import org.eclipse.jdt.junit.TestRunListener;
import org.eclipse.jdt.junit.model.ITestElement.FailureTrace;
import org.eclipse.jdt.junit.model.ITestElement.ProgressState;
import org.eclipse.jdt.junit.model.ITestElement.Result;
import org.eclipse.jdt.testplugin.JavaProjectHelper;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.junit.launcher.TestKindRegistry;

public class TestRunFilteredStandardRunnerTest4 extends AbstractTestRunListenerTest {

    private IType fATestCase;

    private String[] runTreeTest(IType typeToLaunch, String testName, int step) throws Exception {
        TestRunLog log = new TestRunLog();
        final TestRunListener testRunListener = new TestRunListeners.TreeTest(log, step);
        JUnitCore.addTestRunListener(testRunListener);
        try {
            return launchJUnit(typeToLaunch, TestKindRegistry.JUNIT4_TEST_KIND_ID, testName, log);
        } finally {
            JUnitCore.removeTestRunListener(testRunListener);
        }
    }

    @Override
    protected void setUp() throws Exception {
        fProject = JavaProjectHelper.createJavaProject("TestRunListenerTest", "bin");
        JavaProjectHelper.addVariableEntry(fProject, new Path("JUNIT_HOME/junit.jar"), null, null);
        JavaProjectHelper.addToClasspath(fProject, JavaCore.newContainerEntry(JUnitCore.JUNIT4_CONTAINER_PATH));
        JavaProjectHelper.addRTJar15(fProject);
        String source = "package pack;\n" + "import org.junit.Test;\n" + "import org.junit.FixMethodOrder;\n" + "import org.junit.runners.MethodSorters;\n" + "import static org.junit.Assert.*;\n" + "\n" + // workaround for http://randomallsorts.blogspot.de/2012/12/junit-411-whats-new-test-execution-order.html
        "@FixMethodOrder(MethodSorters.NAME_ASCENDING)\n" + "public class ATestCase {\n" + "    @Test public void test1Succeed() { }\n" + "    @Test public void test2Fail() { fail(); }\n" + "}";
        fATestCase = createType(source, "pack", "ATestCase.java");
    }

    public void testFilterToTest1Succeed() throws Exception {
        String[] expectedSequence = new String[] { TestRunListeners.sessionAsString("ATestCase test1Succeed", ProgressState.COMPLETED, Result.OK, 0), TestRunListeners.testCaseAsString("test1Succeed", "pack.ATestCase", ProgressState.COMPLETED, Result.OK, null, 1) };
        String[] actual = runTreeTest(fATestCase, "test1Succeed", 4);
        assertEqualLog(expectedSequence, actual);
    }

    public void testFilterToTest2Fail() throws Exception {
        String[] expectedSequence = new String[] { TestRunListeners.sessionAsString("ATestCase test2Fail", ProgressState.COMPLETED, Result.FAILURE, 0), TestRunListeners.testCaseAsString("test2Fail", "pack.ATestCase", ProgressState.COMPLETED, Result.FAILURE, new FailureTrace("java.lang.AssertionError", null, null), 1) };
        String[] actual = runTreeTest(fATestCase, "test2Fail", 4);
        assertEqualLog(expectedSequence, actual);
    }

    public void testFilterToNoTestsRemain() throws Exception {
        String[] expectedSequence = new String[] { TestRunListeners.sessionAsString("ATestCase thisdoesnotexist", ProgressState.COMPLETED, Result.ERROR, 0), TestRunListeners.testCaseAsString("initializationError", "org.junit.runner.manipulation.Filter", ProgressState.COMPLETED, Result.ERROR, new FailureTrace("java.lang.Exception", null, null), 1) };
        String[] actual = runTreeTest(fATestCase, "thisdoesnotexist", 4);
        assertEqualLog(expectedSequence, actual);
    }
}
