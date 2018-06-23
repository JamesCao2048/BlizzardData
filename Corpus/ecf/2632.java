/*
 * $Header: /home/data/cvs/rt/org.eclipse.ecf/tests/bundles/org.eclipse.ecf.tests.apache.httpclient.server/src/org/apache/commons/httpclient/HttpClientTestBase.java,v 1.1 2009/02/13 18:07:48 slewis Exp $
 * $Revision: 1.1 $
 * $Date: 2009/02/13 18:07:48 $
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
package org.apache.commons.httpclient;

import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.server.SimpleHttpServer;
import org.apache.commons.httpclient.server.SimplePlainSocketFactory;
import org.apache.commons.httpclient.server.SimpleProxy;
import org.apache.commons.httpclient.server.SimpleSocketFactory;

/**
 * Base class for test cases using 
 * {@link org.apache.commons.httpclient.server.SimpleHttpServer} based 
 * testing framework.
 *
 * @author Oleg Kalnichevski
 * 
 * @version $Id: HttpClientTestBase.java,v 1.1 2009/02/13 18:07:48 slewis Exp $
 */
public class HttpClientTestBase extends TestCase {

    protected HttpClient client = null;

    protected SimpleHttpServer server = null;

    protected SimpleProxy proxy = null;

    private boolean useProxy = false;

    private boolean useSSL = false;

    // ------------------------------------------------------------ Constructor
    public  HttpClientTestBase(final String testName) throws IOException {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { HttpClientTestBase.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods
    public static Test suite() {
        return new TestSuite(HttpClientTestBase.class);
    }

    public void setUseProxy(boolean useProxy) {
        this.useProxy = useProxy;
    }

    public void setUseSSL(boolean b) {
        this.useSSL = b;
    }

    public boolean isUseSSL() {
        return this.useSSL;
    }

    // ------------------------------------------------- TestCase setup/shutdown
    public void setUp() throws IOException {
        // Configure socket factories
        SimpleSocketFactory serversocketfactory = null;
        Protocol testhttp = null;
        /* XXX removed because ssl test code not included
        if (this.useSSL) {
            serversocketfactory = new SimpleSSLSocketFactory(); 
            testhttp = new Protocol("https", 
                    (ProtocolSocketFactory)new SimpleSSLTestProtocolSocketFactory(), 443);
        } else {
            serversocketfactory = new SimplePlainSocketFactory(); 
            testhttp = Protocol.getProtocol("http"); 
        }
		*/
        // use arbitrary port
        this.server = new SimpleHttpServer(serversocketfactory, 0);
        this.server.setTestname(getName());
        this.client = new HttpClient();
        this.client.getHostConfiguration().setHost(this.server.getLocalAddress(), this.server.getLocalPort(), testhttp);
        if (this.useProxy) {
            this.proxy = new SimpleProxy();
            client.getHostConfiguration().setProxy(proxy.getLocalAddress(), proxy.getLocalPort());
        }
    }

    public void tearDown() throws IOException {
        this.client = null;
        this.server.destroy();
        this.server = null;
        if (proxy != null) {
            proxy.destroy();
            proxy = null;
        }
    }
}
