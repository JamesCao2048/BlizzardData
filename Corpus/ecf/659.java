/*
 * $Header: /home/data/cvs/rt/org.eclipse.ecf/tests/bundles/org.eclipse.ecf.tests.apache.httpclient.server/src/org/apache/commons/httpclient/server/SimpleHost.java,v 1.1 2009/02/13 18:07:51 slewis Exp $
 * $Revision: 1.1 $
 * $Date: 2009/02/13 18:07:51 $
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
 */
package org.apache.commons.httpclient.server;

/**
 * @author Oleg Kalnichevski
 */
public class SimpleHost implements Cloneable {

    private String hostname = null;

    private int port = -1;

    public  SimpleHost(final String hostname, int port) {
        super();
        if (hostname == null) {
            throw new IllegalArgumentException("Host name may not be null");
        }
        if (port < 0) {
            throw new IllegalArgumentException("Port may not be negative");
        }
        this.hostname = hostname;
        this.port = port;
    }

    public  SimpleHost(final SimpleHost httphost) {
        super();
        this.hostname = httphost.hostname;
        this.port = httphost.port;
    }

    public Object clone() {
        return new SimpleHost(this);
    }

    public String getHostName() {
        return this.hostname;
    }

    public int getPort() {
        return this.port;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer(50);
        buffer.append(this.hostname);
        buffer.append(':');
        buffer.append(this.port);
        return buffer.toString();
    }

    public boolean equals(final Object o) {
        if (o instanceof SimpleHost) {
            if (o == this) {
                return true;
            }
            SimpleHost that = (SimpleHost) o;
            if (!this.hostname.equalsIgnoreCase(that.hostname)) {
                return false;
            }
            if (this.port != that.port) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.hostname.hashCode() + this.port;
    }
}
