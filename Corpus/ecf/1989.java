/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc., Peter Nehrer, Boris Bokowski. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.filetransfer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.util.ECFException;

/**
 * Exception class for user cancellation
 * 
 */
public class UserCancelledException extends ECFException {

    private static final long serialVersionUID = -1147166028435325320L;

    public  UserCancelledException() {
    // null constructor
    }

    public  UserCancelledException(IStatus status) {
        super(status);
    }

    public  UserCancelledException(String message) {
        super(message);
    }

    public  UserCancelledException(Throwable cause) {
        super(cause);
    }

    public  UserCancelledException(String message, Throwable cause) {
        super(message, cause);
    }
}
