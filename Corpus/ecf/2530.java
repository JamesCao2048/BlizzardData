/*
 * $Header: /home/data/cvs/rt/org.eclipse.ecf/tests/bundles/org.eclipse.ecf.tests.apache.httpclient.server/src/org/apache/commons/httpclient/TestHttpVersion.java,v 1.1 2009/02/13 18:07:50 slewis Exp $
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test cases for HTTP version class
 *
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @version $Revision: 1.1 $
 */
public class TestHttpVersion extends TestCase {

    public  TestHttpVersion(String name) {
        super(name);
    }

    // ------------------------------------------------------- TestCase Methods
    public static Test suite() {
        return new TestSuite(TestHttpVersion.class);
    }

    // ------------------------------------------------------------------ Tests
    public void testHttpVersionInvalidConstructorInput() throws Exception {
        try {
            HttpVersion ver = new HttpVersion(-1, -1);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            HttpVersion ver = new HttpVersion(0, -1);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testHttpVersionParsing() throws Exception {
        String s = "HTTP/1.1";
        HttpVersion version = HttpVersion.parse(s);
        assertEquals("HTTP major version number", 1, version.getMajor());
        assertEquals("HTTP minor version number", 1, version.getMinor());
        assertEquals("HTTP version number", s, version.toString());
        s = "HTTP/123.4567";
        version = HttpVersion.parse(s);
        assertEquals("HTTP major version number", 123, version.getMajor());
        assertEquals("HTTP minor version number", 4567, version.getMinor());
        assertEquals("HTTP version number", s, version.toString());
    }

    public void testInvalidHttpVersionParsing() throws Exception {
        try {
            HttpVersion.parse(null);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            HttpVersion.parse("crap");
            fail("ProtocolException should have been thrown");
        } catch (ProtocolException e) {
        }
        try {
            HttpVersion.parse("HTTP/crap");
            fail("ProtocolException should have been thrown");
        } catch (ProtocolException e) {
        }
        try {
            HttpVersion.parse("HTTP/1");
            fail("ProtocolException should have been thrown");
        } catch (ProtocolException e) {
        }
        try {
            HttpVersion.parse("HTTP/1234   ");
            fail("ProtocolException should have been thrown");
        } catch (ProtocolException e) {
        }
        try {
            HttpVersion.parse("HTTP/1.");
            fail("ProtocolException should have been thrown");
        } catch (ProtocolException e) {
        }
        try {
            HttpVersion.parse("HTTP/1.1 crap");
            fail("ProtocolException should have been thrown");
        } catch (ProtocolException e) {
        }
        try {
            HttpVersion.parse("HTTP/whatever.whatever whatever");
            fail("ProtocolException should have been thrown");
        } catch (ProtocolException e) {
        }
        try {
            HttpVersion.parse("HTTP/1.whatever whatever");
            fail("ProtocolException should have been thrown");
        } catch (ProtocolException e) {
        }
    }

    public void testHttpVersionEquality() throws Exception {
        HttpVersion ver1 = new HttpVersion(1, 1);
        HttpVersion ver2 = new HttpVersion(1, 1);
        assertEquals(ver1.hashCode(), ver2.hashCode());
        assertTrue(ver1.equals(ver1));
        assertTrue(ver1.equals(ver2));
        assertTrue(ver1.equals((Object) ver1));
        assertTrue(ver1.equals((Object) ver2));
        assertFalse(ver1.equals(new Float(1.1)));
        try {
            ver1.equals(null);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
        }
        assertTrue((new HttpVersion(0, 9)).equals(HttpVersion.HTTP_0_9));
        assertTrue((new HttpVersion(1, 0)).equals(HttpVersion.HTTP_1_0));
        assertTrue((new HttpVersion(1, 1)).equals(HttpVersion.HTTP_1_1));
        assertFalse((new HttpVersion(1, 1)).equals(HttpVersion.HTTP_1_0));
    }

    public void testHttpVersionComparison() {
        assertTrue(HttpVersion.HTTP_0_9.lessEquals(HttpVersion.HTTP_1_1));
        assertTrue(HttpVersion.HTTP_0_9.greaterEquals(HttpVersion.HTTP_0_9));
        assertFalse(HttpVersion.HTTP_0_9.greaterEquals(HttpVersion.HTTP_1_0));
        assertTrue(HttpVersion.HTTP_1_0.compareTo((Object) HttpVersion.HTTP_1_1) < 0);
        assertTrue(HttpVersion.HTTP_1_0.compareTo((Object) HttpVersion.HTTP_0_9) > 0);
        assertTrue(HttpVersion.HTTP_1_0.compareTo((Object) HttpVersion.HTTP_1_0) == 0);
    }
}
