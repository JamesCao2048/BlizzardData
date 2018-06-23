/*
 * $Header: /home/data/cvs/rt/org.eclipse.ecf/tests/bundles/org.eclipse.ecf.tests.apache.httpclient.server/src/org/apache/commons/httpclient/TestHttpStatus.java,v 1.1 2009/02/13 18:07:50 slewis Exp $
 * $Revision: 1.1 $
 * $Date: 2009/02/13 18:07:50 $
 * ====================================================================
 *
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */
package org.apache.commons.httpclient;

import junit.framework.*;
import java.lang.reflect.*;

/**
 *
 * Unit tests for {@link HttpStatus}
 *
 * @author Sean C. Sullivan
 * @version $Id: TestHttpStatus.java,v 1.1 2009/02/13 18:07:50 slewis Exp $
 */
public class TestHttpStatus extends TestCase {

    // ------------------------------------------------------------ Constructor
    public  TestHttpStatus(String testName) {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestHttpStatus.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods
    public static Test suite() {
        return new TestSuite(TestHttpStatus.class);
    }

    // ----------------------------------------------------------- Test Methods
    public void testStatusText() throws IllegalAccessException {
        Field[] publicFields = HttpStatus.class.getFields();
        assertTrue(publicFields != null);
        assertTrue(publicFields.length > 0);
        for (int i = 0; i < publicFields.length; i++) {
            final Field f = publicFields[i];
            final int modifiers = f.getModifiers();
            if ((f.getType() == int.class) && Modifier.isPublic(modifiers) && Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers)) {
                final int iValue = f.getInt(null);
                final String text = HttpStatus.getStatusText(iValue);
                assertTrue("text is null for HttpStatus." + f.getName(), (text != null));
                assertTrue(text.length() > 0);
            }
        }
    }

    public void testStatusTextNegative() throws Exception {
        try {
            HttpStatus.getStatusText(-1);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testStatusTextAll() throws Exception {
        for (int i = 0; i < 600; i++) {
            HttpStatus.getStatusText(i);
        }
    }
}
