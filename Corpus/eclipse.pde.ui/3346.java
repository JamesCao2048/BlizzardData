/*******************************************************************************
 *  Copyright (c) 2005, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.ui.tests.imports;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllImportTests {

    public static Test suite() {
        //$NON-NLS-1$
        TestSuite suite = new TestSuite("Test Suite to test the plug-in and feature import wizards.");
        suite.addTest(ImportWithLinksTestCase.suite());
        suite.addTest(ImportAsBinaryTestCase.suite());
        suite.addTest(ImportAsSourceTestCase.suite());
        // Temporarily disabled until git migration is complete and we have access to a stable cvs repo (bug 355873)
        //		suite.addTest(ImportFromRepoTestCase.suite());
        //		suite.addTest(BundleImporterTests.suite());
        suite.addTest(ImportFeatureProjectsTestCase.suite());
        return suite;
    }
}
