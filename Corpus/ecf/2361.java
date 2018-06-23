/*
 * $Header: /home/data/cvs/rt/org.eclipse.ecf/tests/bundles/org.eclipse.ecf.tests.apache.httpclient.server/src/org/apache/commons/httpclient/NoHostHttpConnectionManager.java,v 1.1 2009/02/13 18:07:50 slewis Exp $
 * $Revision: 1.1 $
 * $Date: 2009/02/13 18:07:50 $
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

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

/**
 */
public class NoHostHttpConnectionManager implements HttpConnectionManager {

    private HttpConnection connection;

    private boolean connectionReleased = false;

    private HttpConnectionManagerParams params = new HttpConnectionManagerParams();

    public  NoHostHttpConnectionManager() {
        super();
    }

    /**
     * This method currently does nothing.
     */
    public void closeIdleConnections(long idleTimeout) {
    }

    /**
     * @return
     */
    public boolean isConnectionReleased() {
        return connectionReleased;
    }

    /**
     * @param connection
     */
    public void setConnection(HttpConnection connection) {
        this.connection = connection;
        connection.setHttpConnectionManager(this);
        connection.getParams().setDefaults(this.params);
    }

    public HttpConnection getConnection(HostConfiguration hostConfiguration) {
        // close it and set the values if they are not
        if (!hostConfiguration.hostEquals(connection) || !hostConfiguration.proxyEquals(connection)) {
            if (connection.isOpen()) {
                connection.close();
            }
            connection.setHost(hostConfiguration.getHost());
            connection.setPort(hostConfiguration.getPort());
            connection.setProtocol(hostConfiguration.getProtocol());
            connection.setLocalAddress(hostConfiguration.getLocalAddress());
            connection.setProxyHost(hostConfiguration.getProxyHost());
            connection.setProxyPort(hostConfiguration.getProxyPort());
        } else {
            finishLastResponse(connection);
        }
        connectionReleased = false;
        return connection;
    }

    /**
     * @deprecated
     */
    public HttpConnection getConnection(HostConfiguration hostConfiguration, long timeout) throws HttpException {
        return getConnection(hostConfiguration);
    }

    public HttpConnection getConnectionWithTimeout(HostConfiguration hostConfiguration, long timeout) throws ConnectionPoolTimeoutException {
        return getConnection(hostConfiguration);
    }

    public void releaseConnection(HttpConnection conn) {
        if (conn != connection) {
            throw new IllegalStateException("Unexpected close on a different connection.");
        }
        connectionReleased = true;
        finishLastResponse(connection);
    }

    /**
     * Since the same connection is about to be reused, make sure the
     * previous request was completely processed, and if not
     * consume it now.
     * @param conn The connection
     */
    static void finishLastResponse(HttpConnection conn) {
        InputStream lastResponse = conn.getLastResponseInputStream();
        if (lastResponse != null) {
            conn.setLastResponseInputStream(null);
            try {
                lastResponse.close();
            } catch (IOException ioe) {
                conn.close();
            }
        }
    }

    public HttpConnectionManagerParams getParams() {
        return this.params;
    }

    public void setParams(final HttpConnectionManagerParams params) {
        if (params == null) {
            throw new IllegalArgumentException("Parameters may not be null");
        }
        this.params = params;
    }
}
