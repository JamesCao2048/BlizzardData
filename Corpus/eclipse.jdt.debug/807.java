/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.console;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.jdt.core.JavaCore;

/**
 * Resolves to Java-like file extensions for hyperlink matching.
 * 
 * @since 3.2
 */
public class JavaLikeExtensionsResolver implements IDynamicVariableResolver {

    @Override
    public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
        String[] javaLikeExtensions = JavaCore.getJavaLikeExtensions();
        StringBuffer buffer = new StringBuffer();
        if (javaLikeExtensions.length > 1) {
            //$NON-NLS-1$
            buffer.append("(");
        }
        for (int i = 0; i < javaLikeExtensions.length; i++) {
            String ext = javaLikeExtensions[i];
            //$NON-NLS-1$
            buffer.append("\\.");
            buffer.append(ext);
            //$NON-NLS-1$
            buffer.append(":");
            if (i < (javaLikeExtensions.length - 1)) {
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "|");
            }
        }
        if (javaLikeExtensions.length > 1) {
            //$NON-NLS-1$
            buffer.append(")");
        }
        return buffer.toString();
    }
}
