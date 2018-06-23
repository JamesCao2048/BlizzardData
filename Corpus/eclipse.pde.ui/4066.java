/*******************************************************************************
 * Copyright (c)  2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.ui.tests;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.pde.ui.tests.build.properties.AllValidatorTests;
import org.eclipse.pde.ui.tests.classpathresolver.ClasspathResolverTest;
import org.eclipse.pde.ui.tests.launcher.AllLauncherTests;
import org.eclipse.pde.ui.tests.model.bundle.AllBundleModelTests;
import org.eclipse.pde.ui.tests.model.xml.AllXMLModelTests;
import org.eclipse.pde.ui.tests.nls.AllNLSTests;
import org.eclipse.pde.ui.tests.preferences.AllPreferenceTests;
import org.eclipse.pde.ui.tests.project.*;
import org.eclipse.pde.ui.tests.runtime.AllPDERuntimeTests;
import org.eclipse.pde.ui.tests.target.AllTargetMinimalTests;
import org.eclipse.pde.ui.tests.views.log.AllLogViewTests;
import org.eclipse.pde.ui.tests.wizards.AllNewProjectMinimalTests;

/**
 * They run on minimal plugin bundles and dont require the whole SDK ( for
 * hudson gerrit). This class is refactored out of AllPDETests
 *
 */
public class AllPDEMinimalTests {

    public static Test suite() {
        //$NON-NLS-1$
        TestSuite suite = new TestSuite("Test Suite for org.eclipse.pde.ui");
        suite.addTest(AllTargetMinimalTests.suite());
        suite.addTest(AllNewProjectMinimalTests.suite());
        suite.addTest(AllPreferenceTests.suite());
        // suite.addTest(AllImportTests.suite());
        suite.addTest(AllBundleModelTests.suite());
        suite.addTest(AllXMLModelTests.suite());
        suite.addTest(AllValidatorTests.suite());
        suite.addTest(AllNLSTests.suite());
        suite.addTest(AllPDERuntimeTests.suite());
        // suite.addTest(ExportBundleTests.suite());
        suite.addTest(AllLauncherTests.suite());
        suite.addTest(AllLogViewTests.suite());
        suite.addTest(ProjectCreationTests.suite());
        suite.addTest(BundleRootTests.suite());
        suite.addTest(PluginRegistryTestsMinimal.suite());
        suite.addTest(ClasspathResolverTest.suite());
        // suite.addTest(ClasspathContributorTest.suite());
        return suite;
    }
}
