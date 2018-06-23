/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Adapter factory for source lookup elemnets.
 * @since 3.0
 */
public class VariableClasspathEntryAdapterFactory implements IAdapterFactory {

    /* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (adapterType.equals(IWorkbenchAdapter.class)) {
            return (T) new VariableClasspathEntryWorkbenchAdapter();
        }
        return null;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
    @Override
    public Class<?>[] getAdapterList() {
        return new Class[] { IWorkbenchAdapter.class };
    }
}
