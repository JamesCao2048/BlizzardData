/*
 * $Header: /home/data/cvs/rt/org.eclipse.ecf/tests/bundles/org.eclipse.ecf.tests.apache.httpclient.server/src/org/apache/commons/httpclient/TestEquals.java,v 1.1 2009/02/13 18:07:48 slewis Exp $
 * $Revision: 1.1 $
 * $Date: 2009/02/13 18:07:48 $
 *
 * ====================================================================
 *
 *  Copyright 2002-2004 The Apache Software Foundation
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

import org.apache.commons.httpclient.protocol.DefaultProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SSLProtocolSocketFactory;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 */
public class TestEquals extends TestCase {

    public static Test suite() {
        return new TestSuite(TestEquals.class);
    }

    /**
     * 
     */
    public  TestEquals() {
        super();
    }

    /**
     * @param arg0
     */
    public  TestEquals(String arg0) {
        super(arg0);
    }

    public void testProtocol() {
        Protocol p1 = new Protocol("test", new DefaultProtocolSocketFactory(), 123);
        Protocol p2 = new Protocol("test", new DefaultProtocolSocketFactory(), 123);
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));
    }

    public void testProtocolSocketFactory() {
        ProtocolSocketFactory p1 = new DefaultProtocolSocketFactory();
        ProtocolSocketFactory p2 = new DefaultProtocolSocketFactory();
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));
        p1 = new SSLProtocolSocketFactory();
        p2 = new SSLProtocolSocketFactory();
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));
    }

    public void testHostConfiguration() {
        HostConfiguration hc1 = new HostConfiguration();
        hc1.setHost("http", 80, "http");
        HostConfiguration hc2 = new HostConfiguration();
        hc2.setHost("http", 80, "http");
        assertTrue(hc1.equals(hc2));
        assertTrue(hc2.equals(hc1));
    }
}
