/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Cabe <benjamin.cabe@anyware-tech.com> - bug 225047
 *******************************************************************************/
package org.eclipse.pde.ui.tests.nls;

import junit.framework.*;
import org.eclipse.pde.internal.ui.nls.StringHelper;

/**
 * Tests StringHelper.java convenience methods
 * @since 3.4
 */
public class StringHelperTestCase extends TestCase {

    private static final String newLine = "\r\n";

    public static Test suite() {
        return new TestSuite(StringHelperTestCase.class);
    }

    public void testSimpleLines() {
        String s1, s2;
        // one line
        s1 = "abc";
        s2 = StringHelper.preparePropertiesString(s1, newLine.toCharArray());
        assertEquals(s1, s2);
        // two lines
        s1 = "abc" + newLine + "def";
        s2 = StringHelper.preparePropertiesString(s1, newLine.toCharArray());
        assertEquals("abc\\r\\n\\" + newLine + "def", s2);
    }

    public void testSpaces() {
        String s1, s2;
        s1 = "ab  c    ";
        s2 = StringHelper.preparePropertiesString(s1, newLine.toCharArray());
        assertEquals(s1, s2);
        s1 = "ab  c   " + newLine + "    ";
        s2 = StringHelper.preparePropertiesString(s1, newLine.toCharArray());
        assertEquals("ab  c   \\r\\n    ", s2);
        s1 = "abc   " + newLine + "  d  ef";
        s2 = StringHelper.preparePropertiesString(s1, newLine.toCharArray());
        assertEquals("abc   \\r\\n  \\" + newLine + "d  ef", s2);
    }

    public void testSideEffects() {
        String s1, s2;
        s1 = "";
        s2 = StringHelper.preparePropertiesString(s1, newLine.toCharArray());
        assertEquals(s1, s2);
        s1 = newLine;
        s2 = StringHelper.preparePropertiesString(s1, newLine.toCharArray());
        assertEquals("\\r\\n", s2);
    }
}
