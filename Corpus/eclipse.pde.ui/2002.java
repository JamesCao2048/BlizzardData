/*******************************************************************************
 *  Copyright (c) 2006, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.ui.tests.performance.parts;

import org.eclipse.pde.ui.tests.PDETestsPlugin;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.test.performance.PerformanceTestCase;
import org.osgi.framework.Bundle;

/**
 * AbstractSchemaPerfTest
 *
 */
public abstract class AbstractSchemaPerfTest extends PerformanceTestCase {

    protected int fTestIterations;

    protected int fWarmupIterations;

    protected int fRuns;

    //$NON-NLS-1$
    protected static final String F_FILENAME = "/tests/performance/schema/navigatorContent.exsd";

    protected static File fXSDFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setUpSchemaFile();
        setUpIterations();
    }

    /**
	 *
	 */
    protected void setUpIterations() {
        fTestIterations = 5;
        fWarmupIterations = 50;
        fRuns = 200;
    }

    /**
	 * @throws Exception
	 * @throws IOException
	 */
    private void setUpSchemaFile() throws Exception, IOException {
        PDETestsPlugin plugin = PDETestsPlugin.getDefault();
        if (plugin == null)
            //$NON-NLS-1$
            throw new Exception("ERROR:  Macro plug-in uninitialized");
        Bundle bundle = plugin.getBundle();
        if (bundle == null)
            //$NON-NLS-1$
            throw new Exception("ERROR:  Bundle uninitialized");
        URL url = bundle.getEntry(F_FILENAME);
        if (url == null)
            //$NON-NLS-1$
            throw new Exception("ERROR:  URL not found:  " + F_FILENAME);
        String path = FileLocator.resolve(url).getPath();
        if (//$NON-NLS-1$
        "".equals(path))
            //$NON-NLS-1$
            throw new Exception("ERROR:  URL unresolved:  " + F_FILENAME);
        fXSDFile = new File(path);
    }

    /**
	 * @throws Exception
	 */
    protected void executeTestRun() throws Exception {
        // Warm-up Iterations
        for (int i = 0; i < fWarmupIterations; i++) {
            executeTest();
        }
        // Test Iterations
        for (int j = 0; j < fRuns; j++) {
            startMeasuring();
            for (int i = 0; i < fTestIterations; i++) {
                executeTest();
            }
            stopMeasuring();
        }
        commitMeasurements();
        assertPerformance();
    }

    /**
	 * @throws Exception
	 */
    protected abstract void executeTest() throws Exception;
}
