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
package org.eclipse.pde.ui.tests.wizards;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.pde.ui.tests.ee.ExecutionEnvironmentTests;

public class AllNewProjectTests {

    public static Test suite() {
        //$NON-NLS-1$
        TestSuite suite = new TestSuite("Test Suite to test project creation wizards.");
        suite.addTest(NewFeatureProjectTestCase.suite());
        suite.addTest(NewSiteProjectTestCase.suite());
        suite.addTest(ConvertProjectToPluginTestCase.suite());
        suite.addTest(ExecutionEnvironmentTests.suite());
        return suite;
    }
}
