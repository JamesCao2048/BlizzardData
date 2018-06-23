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
package org.eclipse.jdt.internal.debug.ui.classpath;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.internal.launching.DefaultProjectClasspathEntry;

/**
 * Adapter factory for runtime classpath entries
 */
public class ClasspathEntryAdapterFactory implements IAdapterFactory {

    /* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (IClasspathEditor.class.equals(adapterType)) {
            if (adaptableObject instanceof DefaultProjectClasspathEntry) {
                return (T) new DefaultClasspathEntryEditor();
            }
        }
        return null;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
    @Override
    public Class<?>[] getAdapterList() {
        return new Class[] { IClasspathEditor.class };
    }
}
