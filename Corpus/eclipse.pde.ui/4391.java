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
package org.eclipse.pde.ui.tests.util.xml;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ALLXMLUtilTests {

    public static Test suite() {
        //$NON-NLS-1$
        TestSuite suite = new TestSuite("XML Utilities Test Suite");
        suite.addTest(ParserWrapperTestCase.suite());
        return suite;
    }
}
