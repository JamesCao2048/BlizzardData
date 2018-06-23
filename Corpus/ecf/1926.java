/*
 * $Header: /home/data/cvs/rt/org.eclipse.ecf/tests/bundles/org.eclipse.ecf.tests.apache.httpclient.server/src/org/apache/commons/httpclient/TestHttpParser.java,v 1.1 2009/02/13 18:07:49 slewis Exp $
 * $Revision: 1.1 $
 * $Date: 2009/02/13 18:07:49 $
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import junit.framework.*;

/**
 * Simple tests for {@link HttpParser}.
 *
 * @author Oleg Kalnichevski
 * @version $Id: TestHttpParser.java,v 1.1 2009/02/13 18:07:49 slewis Exp $
 */
public class TestHttpParser extends TestCase {

    private static final String HTTP_ELEMENT_CHARSET = "US-ASCII";

    // ------------------------------------------------------------ Constructor
    public  TestHttpParser(String testName) {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestHeader.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods
    public static Test suite() {
        return new TestSuite(TestHttpParser.class);
    }

    public void testReadHttpLine() throws Exception {
        InputStream instream = new ByteArrayInputStream("\r\r\nstuff\r\n".getBytes(HTTP_ELEMENT_CHARSET));
        assertEquals("\r", HttpParser.readLine(instream, HTTP_ELEMENT_CHARSET));
        assertEquals("stuff", HttpParser.readLine(instream, HTTP_ELEMENT_CHARSET));
        assertEquals(null, HttpParser.readLine(instream, HTTP_ELEMENT_CHARSET));
        instream = new ByteArrayInputStream("\n\r\nstuff\r\n".getBytes("US-ASCII"));
        assertEquals("", HttpParser.readLine(instream, HTTP_ELEMENT_CHARSET));
        assertEquals("", HttpParser.readLine(instream, HTTP_ELEMENT_CHARSET));
        assertEquals("stuff", HttpParser.readLine(instream, HTTP_ELEMENT_CHARSET));
        assertEquals(null, HttpParser.readLine(instream, HTTP_ELEMENT_CHARSET));
    }

    public void testReadWellFormedHttpHeaders() throws Exception {
        InputStream instream = new ByteArrayInputStream("a: a\r\nb: b\r\n\r\nwhatever".getBytes(HTTP_ELEMENT_CHARSET));
        Header[] headers = HttpParser.parseHeaders(instream, HTTP_ELEMENT_CHARSET);
        assertNotNull(headers);
        assertEquals(2, headers.length);
        assertEquals("a", headers[0].getName());
        assertEquals("a", headers[0].getValue());
        assertEquals("b", headers[1].getName());
        assertEquals("b", headers[1].getValue());
    }

    public void testReadMalformedHttpHeaders() throws Exception {
        InputStream instream = new ByteArrayInputStream("a: a\r\nb b\r\n\r\nwhatever".getBytes(HTTP_ELEMENT_CHARSET));
        try {
            Header[] headers = HttpParser.parseHeaders(instream, HTTP_ELEMENT_CHARSET);
            fail("HttpException should have been thrown");
        } catch (HttpException expected) {
        }
    }

    public void testHeadersTerminatorLeniency1() throws Exception {
        InputStream instream = new ByteArrayInputStream("a: a\r\nb: b\r\n\r\r\nwhatever".getBytes(HTTP_ELEMENT_CHARSET));
        Header[] headers = HttpParser.parseHeaders(instream, HTTP_ELEMENT_CHARSET);
        assertNotNull(headers);
        assertEquals(2, headers.length);
        assertEquals("a", headers[0].getName());
        assertEquals("a", headers[0].getValue());
        assertEquals("b", headers[1].getName());
        assertEquals("b", headers[1].getValue());
    }

    public void testHeadersTerminatorLeniency2() throws Exception {
        InputStream instream = new ByteArrayInputStream("a: a\r\nb: b\r\n    \r\nwhatever".getBytes(HTTP_ELEMENT_CHARSET));
        Header[] headers = HttpParser.parseHeaders(instream, HTTP_ELEMENT_CHARSET);
        assertNotNull(headers);
        assertEquals(2, headers.length);
        assertEquals("a", headers[0].getName());
        assertEquals("a", headers[0].getValue());
        assertEquals("b", headers[1].getName());
        assertEquals("b", headers[1].getValue());
    }
}
