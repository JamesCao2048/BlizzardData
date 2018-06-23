/*
 * $Header: /home/data/cvs/rt/org.eclipse.ecf/tests/bundles/org.eclipse.ecf.tests.apache.httpclient.server/src/org/apache/commons/httpclient/FeedbackService.java,v 1.1 2009/02/13 18:07:49 slewis Exp $
 * $Revision: 1.1 $
 * $Date: 2009/02/13 18:07:49 $
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
import java.io.StringWriter;
import org.apache.commons.httpclient.server.HttpService;
import org.apache.commons.httpclient.server.RequestLine;
import org.apache.commons.httpclient.server.SimpleRequest;
import org.apache.commons.httpclient.server.SimpleResponse;

public class FeedbackService implements HttpService {

    public  FeedbackService() {
        super();
    }

    public boolean process(final SimpleRequest request, final SimpleResponse response) throws IOException {
        RequestLine requestline = request.getRequestLine();
        HttpVersion httpversion = requestline.getHttpVersion();
        StringWriter buffer = new StringWriter(100);
        buffer.write("Method type: ");
        buffer.write(requestline.getMethod());
        buffer.write("\r\n");
        buffer.write("Requested resource: ");
        buffer.write(requestline.getUri());
        buffer.write("\r\n");
        buffer.write("Protocol version: ");
        buffer.write(httpversion.toString());
        buffer.write("\r\n");
        String requestbody = request.getBodyString();
        if (requestbody != null && !requestbody.equals("")) {
            buffer.write("\r\n");
            buffer.write("Request body: ");
            buffer.write(requestbody);
            buffer.write("\r\n");
        }
        response.setStatusLine(httpversion, HttpStatus.SC_OK);
        response.setBodyString(buffer.toString());
        return true;
    }
}
