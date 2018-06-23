/*
 * $Header: /home/data/cvs/rt/org.eclipse.ecf/tests/bundles/org.eclipse.ecf.tests.apache.httpclient.server/src/org/apache/commons/httpclient/EchoService.java,v 1.1 2009/02/13 18:07:47 slewis Exp $
 * $Revision: 1.1 $
 * $Date: 2009/02/13 18:07:47 $
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
package org.apache.commons.httpclient;

import java.io.IOException;
import org.apache.commons.httpclient.server.HttpService;
import org.apache.commons.httpclient.server.SimpleRequest;
import org.apache.commons.httpclient.server.SimpleResponse;

/**
 * A service that echos the request body.
 */
public class EchoService implements HttpService {

    public  EchoService() {
        super();
    }

    public boolean process(final SimpleRequest request, final SimpleResponse response) throws IOException {
        HttpVersion httpversion = request.getRequestLine().getHttpVersion();
        response.setStatusLine(httpversion, HttpStatus.SC_OK);
        if (request.containsHeader("Content-Length")) {
            response.addHeader(request.getFirstHeader("Content-Length"));
        }
        if (request.containsHeader("Content-Type")) {
            response.addHeader(request.getFirstHeader("Content-Type"));
        }
        response.setBodyString(request.getBodyString());
        return true;
    }
}
