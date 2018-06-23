/*
 * $Header: /home/data/cvs/rt/org.eclipse.ecf/tests/bundles/org.eclipse.ecf.tests.apache.httpclient.server/src/org/apache/commons/httpclient/server/SimpleConnectionThread.java,v 1.1 2009/02/13 18:07:50 slewis Exp $
 * $Revision: 1.1 $
 * $Date: 2009/02/13 18:07:50 $
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
import java.io.InterruptedIOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple HTTP connection thread.
 * 
 * @author Christian Kohlschuetter
 * @author Oleg Kalnichevski
 */
public class SimpleConnectionThread extends Thread {

    private static final Log LOG = LogFactory.getLog(SimpleConnectionThread.class);

    public static final String DEFAULT_CONTENT_CHARSET = "ISO-8859-1";

    private SimpleHttpServerConnection conn = null;

    private SimpleConnSet connpool = null;

    private HttpRequestHandler handler = null;

    transient boolean stopped;

    public  SimpleConnectionThread(final ThreadGroup tg, final String name, final SimpleHttpServerConnection conn, final SimpleConnSet connpool, final HttpRequestHandler handler) throws IOException {
        super(tg, name);
        if (conn == null) {
            throw new IllegalArgumentException("Connection may not be null");
        }
        if (connpool == null) {
            throw new IllegalArgumentException("Connection pool not be null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("Request handler may not be null");
        }
        this.conn = conn;
        this.connpool = connpool;
        this.handler = handler;
        this.stopped = false;
    }

    public synchronized void destroy() {
        if (this.stopped) {
            return;
        }
        this.stopped = true;
        if (conn != null) {
            conn.close();
            conn = null;
        }
        interrupt();
    }

    public void run() {
        try {
            do {
                this.conn.setKeepAlive(false);
                SimpleRequest request = this.conn.readRequest();
                if (request != null) {
                    this.handler.processRequest(this.conn, request);
                }
            } while (this.conn.isKeepAlive());
        } catch (InterruptedIOException e) {
        } catch (IOException e) {
            if (!this.stopped && !isInterrupted() && LOG.isWarnEnabled()) {
                LOG.warn("[" + getName() + "] I/O error: " + e.getMessage());
            }
        } finally {
            destroy();
            this.connpool.removeConnection(this.conn);
        }
    }
}
