/*
 * $HeadURL: https://svn.apache.org/repos/asf/jakarta/commons/proper/httpclient/branches/HTTPCLIENT_3_0_BRANCH/src/test/org/apache/commons/httpclient/TestHostConfiguration.java $
 * $Revision: 1.1 $
 * $Date: 2009/02/13 18:07:47 $
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
 */
package org.apache.commons.httpclient;

import java.io.IOException;
import java.net.UnknownHostException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.server.SimpleProxy;

/**
 * Tests basic HostConfiguration functionality.
 *
 * @author Oleg Kalnichevski
 * 
 * @version $Id: TestHostConfiguration.java,v 1.1 2009/02/13 18:07:47 slewis Exp $
 */
public class TestHostConfiguration extends HttpClientTestBase {

    public  TestHostConfiguration(final String testName) throws IOException {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestHostConfiguration.class);
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestHostConfiguration.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    public void testRelativeURLHitWithDefaultHost() throws IOException {
        this.server.setHttpService(new EchoService());
        // Set default host
        this.client.getHostConfiguration().setHost(this.server.getLocalAddress(), this.server.getLocalPort(), Protocol.getProtocol("http"));
        GetMethod httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
            assertEquals(HttpStatus.SC_OK, httpget.getStatusCode());
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testRelativeURLHitWithoutDefaultHost() throws IOException {
        this.server.setHttpService(new EchoService());
        // reset default host configuration
        this.client.setHostConfiguration(new HostConfiguration());
        GetMethod httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException expected) {
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testAbsoluteURLHitWithoutDefaultHost() throws IOException {
        this.server.setHttpService(new EchoService());
        // reset default host configuration
        this.client.setHostConfiguration(new HostConfiguration());
        GetMethod httpget = new GetMethod("http://" + this.server.getLocalAddress() + ":" + this.server.getLocalPort() + "/test/");
        try {
            this.client.executeMethod(httpget);
            assertEquals(HttpStatus.SC_OK, httpget.getStatusCode());
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testAbsoluteURLOverridesClientDefaultHost() throws IOException {
        this.server.setHttpService(new EchoService());
        // Somewhere out there in pampa
        this.client.getHostConfiguration().setHost("somewhere.outthere.in.pampa", 9999);
        GetMethod httpget = new GetMethod("http://" + this.server.getLocalAddress() + ":" + this.server.getLocalPort() + "/test/");
        try {
            this.client.executeMethod(httpget);
            assertEquals(HttpStatus.SC_OK, httpget.getStatusCode());
        } finally {
            httpget.releaseConnection();
        }
        httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
            fail("UnknownHostException should have been thrown");
        } catch (UnknownHostException expected) {
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testAbsoluteURLOverridesDefaultHostParam() throws IOException {
        this.proxy = new SimpleProxy();
        this.server.setHttpService(new EchoService());
        // reset default host configuration
        HostConfiguration hostconfig = new HostConfiguration();
        hostconfig.setHost("somehwere.outthere.in.pampa", 9999);
        hostconfig.setProxy(this.proxy.getLocalAddress(), this.proxy.getLocalPort());
        GetMethod httpget = new GetMethod("http://" + this.server.getLocalAddress() + ":" + this.server.getLocalPort() + "/test/");
        try {
            this.client.executeMethod(hostconfig, httpget);
            assertEquals(HttpStatus.SC_OK, httpget.getStatusCode());
            assertNotNull(httpget.getResponseHeader("Via"));
        } finally {
            httpget.releaseConnection();
        }
        httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(hostconfig, httpget);
            assertEquals(HttpStatus.SC_NOT_FOUND, httpget.getStatusCode());
        } finally {
            httpget.releaseConnection();
        }
    }
}
