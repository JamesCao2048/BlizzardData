/**
 * Copyright (c) 2011, 2015 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.pde.internal.junit.runtime;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.testing.ITestHarness;
import org.eclipse.ui.testing.TestableObject;

/**
 * The {@link ITestHarness} implementation used to execute Platform UI tests.
 */
public class PlatformUITestHarness implements ITestHarness {

    private TestableObject fTestableObject;

    private final boolean fRunTestsInSeparateThread;

    /**
	 * Creates a new instance.
	 * @param testableObject the testable object
	 */
    public  PlatformUITestHarness(Object testableObject, boolean runTestsInSeparateThread) {
        this.fRunTestsInSeparateThread = runTestsInSeparateThread;
        fTestableObject = (TestableObject) testableObject;
        fTestableObject.setTestHarness(this);
    }

    @Override
    public void runTests() {
        try {
            // signal starting
            fTestableObject.testingStarting();
            // the test runner runnable
            Runnable testsRunner = () -> RemotePluginTestRunner.main(Platform.getCommandLineArgs());
            if (fRunTestsInSeparateThread) {
                // wrap into separate thread and run from there
                final Thread testRunnerThread = new //$NON-NLS-1$
                Thread(//$NON-NLS-1$
                testsRunner, //$NON-NLS-1$
                "Plug-in Tests Runner");
                fTestableObject.runTest(() -> testRunnerThread.start());
                // note, this has do be done outside #runTest method to not lock the UI
                try {
                    testRunnerThread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                // run directly
                fTestableObject.runTest(testsRunner);
            }
        } finally {
            // signal shutdown
            fTestableObject.testingFinished();
        }
    }
}
