/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.util;

public interface SuffixConstants {

    //$NON-NLS-1$
    public static final String EXTENSION_class = "class";

    //$NON-NLS-1$
    public static final String EXTENSION_CLASS = "CLASS";

    //$NON-NLS-1$
    public static final String EXTENSION_java = "java";

    //$NON-NLS-1$
    public static final String EXTENSION_JAVA = "JAVA";

    //$NON-NLS-1$
    public static final String SUFFIX_STRING_class = "." + EXTENSION_class;

    //$NON-NLS-1$
    public static final String SUFFIX_STRING_CLASS = "." + EXTENSION_CLASS;

    //$NON-NLS-1$
    public static final String SUFFIX_STRING_java = "." + EXTENSION_java;

    //$NON-NLS-1$
    public static final String SUFFIX_STRING_JAVA = "." + EXTENSION_JAVA;

    public static final char[] SUFFIX_class = SUFFIX_STRING_class.toCharArray();

    public static final char[] SUFFIX_CLASS = SUFFIX_STRING_CLASS.toCharArray();

    public static final char[] SUFFIX_java = SUFFIX_STRING_java.toCharArray();

    public static final char[] SUFFIX_JAVA = SUFFIX_STRING_JAVA.toCharArray();

    //$NON-NLS-1$
    public static final String EXTENSION_jar = "jar";

    //$NON-NLS-1$
    public static final String EXTENSION_JAR = "JAR";

    //$NON-NLS-1$
    public static final String EXTENSION_zip = "zip";

    //$NON-NLS-1$
    public static final String EXTENSION_ZIP = "ZIP";

    //$NON-NLS-1$
    public static final String SUFFIX_STRING_jar = "." + EXTENSION_jar;

    //$NON-NLS-1$
    public static final String SUFFIX_STRING_JAR = "." + EXTENSION_JAR;

    //$NON-NLS-1$
    public static final String SUFFIX_STRING_zip = "." + EXTENSION_zip;

    //$NON-NLS-1$
    public static final String SUFFIX_STRING_ZIP = "." + EXTENSION_ZIP;

    public static final char[] SUFFIX_jar = SUFFIX_STRING_jar.toCharArray();

    public static final char[] SUFFIX_JAR = SUFFIX_STRING_JAR.toCharArray();

    public static final char[] SUFFIX_zip = SUFFIX_STRING_zip.toCharArray();

    public static final char[] SUFFIX_ZIP = SUFFIX_STRING_ZIP.toCharArray();
}
