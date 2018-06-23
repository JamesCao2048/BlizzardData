/*
 * $Header: /home/data/cvs/rt/org.eclipse.ecf/tests/bundles/org.eclipse.ecf.tests.apache.httpclient.server/src/org/apache/commons/httpclient/server/HttpRequestHandler.java,v 1.1 2009/02/13 18:07:51 slewis Exp $
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

/**
 * Defines an HTTP request handler for the SimpleHttpServer
 * 
 * @author Christian Kohlschuetter
 * @author Oleg Kalnichevski
 */
public interface HttpRequestHandler {

    /**
     * The request handler is asked to process this request.
     * 
     * If it is not capable/interested in processing it, this call should
     * be simply ignored.
     * 
     * Any modification of the output stream (via <code>conn.getWriter()</code>)
     * by this request handler will stop the execution chain and return the output
     * to the client.
     * 
     * The handler may also rewrite the request parameters (this is useful in
     * {@link HttpRequestHandlerChain} structures).
     * 
     * @param conn          The Connection object to which this request belongs to.
     * @param request       The request object.
     * @return true if this handler handled the request and no other handlers in the 
     * chain should be called, false otherwise.
     * @throws IOException
     */
    public boolean processRequest(final SimpleHttpServerConnection conn, final SimpleRequest request) throws IOException;
}
