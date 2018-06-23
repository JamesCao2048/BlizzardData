/*
 * $Header: /home/data/cvs/rt/org.eclipse.ecf/tests/bundles/org.eclipse.ecf.tests.apache.httpclient.server/src/org/apache/commons/httpclient/server/HttpRequestHandlerChain.java,v 1.1 2009/02/13 18:07:51 slewis Exp $
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */
package org.apache.commons.httpclient.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Maintains a chain of {@link HttpRequestHandler}s where new request-handlers
 * can be prepended/appended.
 * 
 * For each call to {@link #processRequest(ResponseWriter,SimpleHttpServerConnection,RequestLine,Header[])}
 * we iterate over the chain from the start to the end, stopping as soon as a handler
 * has claimed the output.
 * 
 * @author Christian Kohlschuetter
 */
public class HttpRequestHandlerChain implements HttpRequestHandler {

    private List subhandlers = new ArrayList();

    public  HttpRequestHandlerChain(final HttpRequestHandlerChain chain) {
        super();
        if (chain != null) {
            this.subhandlers.clear();
            this.subhandlers.addAll(chain.subhandlers);
        }
    }

    public  HttpRequestHandlerChain() {
        super();
    }

    public synchronized void clear() {
        subhandlers.clear();
    }

    public synchronized void prependHandler(HttpRequestHandler handler) {
        subhandlers.add(0, handler);
    }

    public synchronized void appendHandler(HttpRequestHandler handler) {
        subhandlers.add(handler);
    }

    public synchronized boolean processRequest(final SimpleHttpServerConnection conn, final SimpleRequest request) throws IOException {
        for (Iterator it = subhandlers.iterator(); it.hasNext(); ) {
            HttpRequestHandler h = (HttpRequestHandler) it.next();
            boolean stop = h.processRequest(conn, request);
            if (stop) {
                return true;
            }
        }
        return false;
    }
}
