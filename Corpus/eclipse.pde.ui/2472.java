/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.internal.util;

import org.eclipse.osgi.util.NLS;

public class UtilMessages extends NLS {

    //$NON-NLS-1$
    private static final String BUNDLE_NAME = "org.eclipse.pde.api.tools.internal.util.utilmessages";

    public static String Util_0;

    public static String Util_4;

    public static String Util_5;

    public static String Util_6;

    public static String Util_builder_errorMessage;

    public static String Util_couldNotFindFilterFile;

    public static String Util_problemWithFilterFile;

    public static String comparison_invalidRegularExpression;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, UtilMessages.class);
    }

    private  UtilMessages() {
    }
}
