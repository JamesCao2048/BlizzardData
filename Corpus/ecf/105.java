/*
 * $Header: /home/data/cvs/rt/org.eclipse.ecf/tests/bundles/org.eclipse.ecf.tests.apache.httpclient.server/src/org/apache/commons/httpclient/server/SimpleConnManager.java,v 1.1 2009/02/13 18:07:51 slewis Exp $
 * $Revision: 1.1 $
 * $Date: 2009/02/13 18:07:51 $
 *
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
package org.apache.commons.httpclient.server;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A REALLY simple connection manager.
 * 
 * @author Oleg Kalnichevski
 */
public class SimpleConnManager {

    private Map connsets = new HashMap();

    public  SimpleConnManager() {
        super();
    }

    public synchronized SimpleHttpServerConnection openConnection(final SimpleHost host) throws IOException {
        if (host == null) {
            throw new IllegalArgumentException("Host may not be null");
        }
        SimpleHttpServerConnection conn = null;
        SimpleConnList connlist = (SimpleConnList) this.connsets.get(host);
        if (connlist != null) {
            conn = connlist.removeFirst();
            if (conn != null && !conn.isOpen()) {
                conn = null;
            }
        }
        if (conn == null) {
            Socket socket = new Socket(host.getHostName(), host.getPort());
            conn = new SimpleHttpServerConnection(socket);
        }
        return conn;
    }

    public synchronized void releaseConnection(final SimpleHost host, final SimpleHttpServerConnection conn) throws IOException {
        if (host == null) {
            throw new IllegalArgumentException("Host may not be null");
        }
        if (conn == null) {
            return;
        }
        if (!conn.isKeepAlive()) {
            conn.close();
        }
        if (conn.isOpen()) {
            SimpleConnList connlist = (SimpleConnList) this.connsets.get(host);
            if (connlist == null) {
                connlist = new SimpleConnList();
                this.connsets.put(host, connlist);
            }
            connlist.addConnection(conn);
        }
    }

    public synchronized void shutdown() {
        for (Iterator i = this.connsets.values().iterator(); i.hasNext(); ) {
            SimpleConnList connlist = (SimpleConnList) i.next();
            connlist.shutdown();
        }
        this.connsets.clear();
    }
}
