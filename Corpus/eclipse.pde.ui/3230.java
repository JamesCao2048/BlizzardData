/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
import org.eclipse.pde.ui.tests.classpathcontributor.ClasspathContributorTest;
import org.eclipse.pde.ui.tests.classpathresolver.ClasspathResolverTest;
import org.eclipse.pde.ui.tests.ee.ExportBundleTests;
import org.eclipse.pde.ui.tests.imports.AllImportTests;
import org.eclipse.pde.ui.tests.launcher.AllLauncherTests;
import org.eclipse.pde.ui.tests.model.bundle.AllBundleModelTests;
import org.eclipse.pde.ui.tests.model.xml.AllXMLModelTests;
import org.eclipse.pde.ui.tests.nls.AllNLSTests;
import org.eclipse.pde.ui.tests.preferences.AllPreferenceTests;
import org.eclipse.pde.ui.tests.project.*;
import org.eclipse.pde.ui.tests.runtime.AllPDERuntimeTests;
import org.eclipse.pde.ui.tests.target.AllTargetTests;
import org.eclipse.pde.ui.tests.views.log.AllLogViewTests;
import org.eclipse.pde.ui.tests.wizards.AllNewProjectTests;

public class AllPDETests {

    public static Test suite() {
        //$NON-NLS-1$
        TestSuite suite = new TestSuite("Test Suite for org.eclipse.pde.ui");
        suite.addTest(AllTargetTests.suite());
        suite.addTest(AllNewProjectTests.suite());
        suite.addTest(AllPreferenceTests.suite());
        suite.addTest(AllImportTests.suite());
        suite.addTest(AllBundleModelTests.suite());
        suite.addTest(AllXMLModelTests.suite());
        suite.addTest(AllValidatorTests.suite());
        suite.addTest(AllNLSTests.suite());
        suite.addTest(AllPDERuntimeTests.suite());
        suite.addTest(ExportBundleTests.suite());
        suite.addTest(AllLauncherTests.suite());
        suite.addTest(AllLogViewTests.suite());
        suite.addTest(ProjectCreationTests.suite());
        suite.addTest(BundleRootTests.suite());
        suite.addTest(PluginRegistryTests.suite());
        suite.addTest(ClasspathResolverTest.suite());
        suite.addTest(ClasspathContributorTest.suite());
        return suite;
    }
}
