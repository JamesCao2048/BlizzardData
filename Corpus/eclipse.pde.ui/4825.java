/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.internal.comparator;

import org.eclipse.osgi.util.NLS;

/**
 *
 */
public class ComparatorMessages extends NLS {

    //$NON-NLS-1$
    private static final String BUNDLE_NAME = "org.eclipse.pde.api.tools.internal.comparator.comparatormessages";

    public static String ClassFileComparator_0;

    public static String ClassFileComparator_1;

    public static String ClassFileComparator_2;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, ComparatorMessages.class);
    }

    private  ComparatorMessages() {
    }
}
