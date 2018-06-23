/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.ui.tests.performance;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.pde.ui.tests.performance.parts.*;

public class PDEPerformanceTests {

    public static Test suite() {
        //$NON-NLS-1$
        TestSuite suite = new TestSuite("Performance Test Suite for org.eclipse.pde.ui");
        suite.addTest(PDEModelManagerPerfTest.suite());
        suite.addTest(SchemaLoaderPerfTest.suite());
        suite.addTest(SchemaTraversePerfTest.suite());
        suite.addTest(OpenManifestEditorPerfTest.suite());
        suite.addTest(TargetPlatformPerfTest.suite());
        return suite;
    }
}
