/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.tests;

import org.eclipse.pde.api.tools.builder.tests.OSGiLessAnalysisTests;
import org.eclipse.pde.api.tools.comparator.tests.AllDeltaTests;
import org.eclipse.pde.api.tools.model.tests.ApiBaselineTests;
import org.eclipse.pde.api.tools.model.tests.ApiDescriptionTests;
import org.eclipse.pde.api.tools.model.tests.ApiModelCacheTests;
import org.eclipse.pde.api.tools.model.tests.ApiTypeContainerTests;
import org.eclipse.pde.api.tools.model.tests.BadClassfileTests;
import org.eclipse.pde.api.tools.model.tests.CRCTests;
import org.eclipse.pde.api.tools.model.tests.ClassFileScannerTests;
import org.eclipse.pde.api.tools.model.tests.ComponentManifestTests;
import org.eclipse.pde.api.tools.model.tests.ElementDescriptorTests;
import org.eclipse.pde.api.tools.model.tests.Java8ClassfileScannerTests;
import org.eclipse.pde.api.tools.model.tests.TagScannerTests;
import org.eclipse.pde.api.tools.problems.tests.ApiFilterTests;
import org.eclipse.pde.api.tools.problems.tests.ApiProblemFactoryTests;
import org.eclipse.pde.api.tools.problems.tests.ApiProblemTests;
import org.eclipse.pde.api.tools.reference.tests.SearchScopeTests;
import org.eclipse.pde.api.tools.search.tests.SearchEngineTests;
import org.eclipse.pde.api.tools.search.tests.SkippedComponentTests;
import org.eclipse.pde.api.tools.search.tests.UseSearchTests;
import org.eclipse.pde.api.tools.tests.util.ProjectUtils;
import org.eclipse.pde.api.tools.util.tests.HeadlessApiBaselineManagerTests;
import org.eclipse.pde.api.tools.util.tests.SignaturesTests;
import org.eclipse.pde.api.tools.util.tests.TarEntryTests;
import org.eclipse.pde.api.tools.util.tests.TarExceptionTests;
import org.eclipse.pde.api.tools.util.tests.UtilTests;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for all of the API tools test
 *
 * The API Baseline Tests should be run as JUnit tests, not JUnit Plug-in Tests.
 * This means that there is no OSGi environment available. The vm argument
 * requiredBundles must be set to a valid baseline. In addition, rather than use
 * the EE profiles provided by OSGi, the baseline will resolve using EEs found
 * in the org.eclipse.pde.api.tools.internal.util.profiles inside the
 * org.eclipse.pde.api.tools bundle. "-DrequiredBundles=${eclipse_home}/plugins"
 *
 * @since 1.0.0
 */
public class ApiToolsTestSuite extends TestSuite {

    /**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 * @return the test
	 */
    public static Test suite() {
        return new ApiToolsTestSuite();
    }

    /**
	 * Constructor
	 */
    public  ApiToolsTestSuite() {
        addTest(new TestSuite(ApiDescriptionTests.class));
        addTest(new TestSuite(SearchEngineTests.class));
        addTest(new TestSuite(SkippedComponentTests.class));
        addTest(new TestSuite(UseSearchTests.class));
        addTest(new TestSuite(HeadlessApiBaselineManagerTests.class));
        addTest(new TestSuite(TagScannerTests.class));
        addTest(new TestSuite(ComponentManifestTests.class));
        addTest(new TestSuite(UtilTests.class));
        addTest(new TestSuite(SignaturesTests.class));
        addTest(new TestSuite(ApiBaselineTests.class));
        addTest(new TestSuite(ApiTypeContainerTests.class));
        addTest(ClassFileScannerTests.suite());
        if (ProjectUtils.isJava8Compatible()) {
            addTest(Java8ClassfileScannerTests.suite());
        }
        addTest(new TestSuite(ElementDescriptorTests.class));
        addTest(new TestSuite(SearchScopeTests.class));
        addTest(new TestSuite(ApiProblemTests.class));
        addTest(new TestSuite(ApiProblemFactoryTests.class));
        addTest(new TestSuite(ApiFilterTests.class));
        addTest(new TestSuite(TarEntryTests.class));
        addTest(new TestSuite(TarExceptionTests.class));
        addTest(new TestSuite(OSGiLessAnalysisTests.class));
        addTest(new TestSuite(ApiModelCacheTests.class));
        addTest(new TestSuite(BadClassfileTests.class));
        addTest(new TestSuite(CRCTests.class));
        addTest(new AllDeltaTests());
    }
}
