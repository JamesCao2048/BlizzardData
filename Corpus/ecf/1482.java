/****************************************************************************
 * Copyright (c) 2007 IBM, Composent Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.filetransfer.httpclient4;

import org.eclipse.osgi.util.NLS;

/**
 * 
 */
public class Messages extends NLS {

    //$NON-NLS-1$
    private static final String BUNDLE_NAME = "org.eclipse.ecf.internal.provider.filetransfer.httpclient4.messages";

    public static String FileTransferNamespace_Http_Protocol;

    public static String FileTransferNamespace_Https_Protocol;

    public static String HttpClientRetrieveFileTransfer_CONNECTING_JOB_NAME;

    public static String HttpClientRetrieveFileTransfer_CONNECTING_TASK_NAME;

    public static String HttpClientRetrieveFileTransfer_Proxy_Auth_Required;

    public static String HttpClientRetrieveFileTransfer_Unauthorized;

    public static String HttpClientRetrieveFileTransfer_Username_Prefix;

    public static String HttpClientRetrieveFileTransfer_RESUME_START_POSITION_LESS_THAN_ZERO;

    public static String HttpClientRetrieveFileTransfer_ERROR_GENERAL_RESPONSE_CODE;

    public static String HttpClientRetrieveFileTransfer_EXCEPITION_INVALID_LAST_MODIFIED_FROM_SERVER;

    public static String HttpClientRetrieveFileTransfer_EXCEPTION_FILE_MODIFIED_SINCE_LAST_ACCESS;

    public static String HttpClientRetrieveFileTransfer_RESUME_ERROR_END_POSITION_LESS_THAN_START;

    public static String HttpClientRetrieveFileTransfer_EXCEPTION_COULD_NOT_CONNECT;

    public static String HttpClientRetrieveFileTransfer_INVALID_LAST_MODIFIED_TIME;

    public static String HttpClientRetrieveFileTransfer_INVALID_SERVER_RESPONSE_TO_PARTIAL_RANGE_REQUEST;

    public static String HttpClientRetrieveFileTransfer_RESUME_START_ERROR;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private  Messages() {
    // Generated
    }
}
