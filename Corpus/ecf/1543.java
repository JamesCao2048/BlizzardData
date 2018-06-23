/*
 * $Header: /home/data/cvs/rt/org.eclipse.ecf/tests/bundles/org.eclipse.ecf.tests.apache.httpclient.server/src/org/apache/commons/httpclient/cookie/TestCookieIgnoreSpec.java,v 1.1 2009/02/13 18:07:50 slewis Exp $
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
 */
package org.apache.commons.httpclient.cookie;

import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClientTestBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.server.HttpService;
import org.apache.commons.httpclient.server.SimpleRequest;
import org.apache.commons.httpclient.server.SimpleResponse;

/**
 * Test cases for ignore cookie apec
 *
 * @author Michael Becke
 * 
 * @version $Revision: 1.1 $
 */
public class TestCookieIgnoreSpec extends HttpClientTestBase {

    public  TestCookieIgnoreSpec(final String testName) throws IOException {
        super(testName);
    }

    // ------------------------------------------------------- TestCase Methods
    public static Test suite() {
        return new TestSuite(TestCookieIgnoreSpec.class);
    }

    private class BasicAuthService implements HttpService {

        public  BasicAuthService() {
            super();
        }

        public boolean process(final SimpleRequest request, final SimpleResponse response) throws IOException {
            HttpVersion ver = request.getRequestLine().getHttpVersion();
            response.setStatusLine(ver, HttpStatus.SC_OK);
            response.addHeader(new Header("Connection", "close"));
            response.addHeader(new Header("Set-Cookie", "custno = 12345; comment=test; version=1," + " name=John; version=1; max-age=600; secure; domain=.apache.org"));
            return true;
        }
    }

    public void testIgnoreCookies() throws Exception {
        this.server.setHttpService(new BasicAuthService());
        GetMethod httpget = new GetMethod("/");
        httpget.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        try {
            this.client.executeMethod(httpget);
        } finally {
            httpget.releaseConnection();
        }
        assertEquals("Cookie parsing should have been disabled", 0, this.client.getState().getCookies().length);
    }

    public void testKeepCloverHappy() throws Exception {
        CookieSpec cookiespec = new IgnoreCookiesSpec();
        cookiespec.parseAttribute(null, null);
        cookiespec.parse("host", 80, "/", false, (String) null);
        cookiespec.parse("host", 80, "/", false, (Header) null);
        cookiespec.validate("host", 80, "/", false, (Cookie) null);
        cookiespec.match("host", 80, "/", false, (Cookie) null);
        cookiespec.match("host", 80, "/", false, (Cookie[]) null);
        cookiespec.domainMatch(null, null);
        cookiespec.pathMatch(null, null);
        cookiespec.match("host", 80, "/", false, (Cookie[]) null);
        cookiespec.formatCookie(null);
        cookiespec.formatCookies(null);
        cookiespec.formatCookieHeader((Cookie) null);
        cookiespec.formatCookieHeader((Cookie[]) null);
    }
}
