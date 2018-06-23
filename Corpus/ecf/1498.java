/*
 * $Header: /home/data/cvs/rt/org.eclipse.ecf/tests/bundles/org.eclipse.ecf.tests.apache.httpclient.server/src/org/apache/commons/httpclient/ExecuteMethodThread.java,v 1.1 2009/02/13 18:07:49 slewis Exp $
 * $Revision: 1.1 $
 * $Date: 2009/02/13 18:07:49 $
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
package org.apache.commons.httpclient;

/**
 * Executes a method from a new thread.
 * 
 * @author Michael Becke
 */
class ExecuteMethodThread extends Thread {

    private HttpMethod method;

    private HttpClient client;

    private Exception exception;

    public  ExecuteMethodThread(HttpMethod method, HttpClient client) {
        this.method = method;
        this.client = client;
    }

    public void run() {
        try {
            client.executeMethod(method);
            method.getResponseBodyAsString();
        } catch (Exception e) {
            this.exception = e;
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * @return HttpMethod
     */
    public HttpMethod getMethod() {
        return method;
    }

    /**
     * Gets the exception that occurred when executing the method.
     * 
     * @return an Exception or <code>null</code> if no exception occurred
     */
    public Exception getException() {
        return exception;
    }
}
