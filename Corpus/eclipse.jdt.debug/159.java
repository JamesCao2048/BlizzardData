/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.classpath;

import org.eclipse.osgi.util.NLS;

public class ClasspathMessages extends NLS {

    //$NON-NLS-1$
    private static final String BUNDLE_NAME = "org.eclipse.jdt.internal.debug.ui.classpath.ClasspathMessages";

    public static String ClasspathModel_0;

    public static String ClasspathModel_1;

    public static String DefaultClasspathEntryDialog_0;

    public static String DefaultClasspathEntryDialog_1;

    public static String DefaultClasspathEntryDialog_2;

    public static String DefaultClasspathEntryDialog_3;

    public static String DefaultClasspathEntryDialog_4;

    public static String DefaultClasspathEntryDialog_property_locked;

    public static String DefaultClasspathEntryDialog_show_preferences;

    static {
        // load message values from bundle file
        NLS.initializeMessages(BUNDLE_NAME, ClasspathMessages.class);
    }
}
