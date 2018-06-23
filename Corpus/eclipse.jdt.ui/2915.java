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
package org.eclipse.jdt.internal.ui.refactoring.nls.search;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

public class FileEntry implements IAdaptable {

    private IFile fPropertiesFile;

    private String fMessage;

    public  FileEntry(IFile propertiesFile, String message) {
        fPropertiesFile = propertiesFile;
        fMessage = message;
    }

    public IFile getPropertiesFile() {
        return fPropertiesFile;
    }

    public String getMessage() {
        return fMessage;
    }

    @Override
    public String toString() {
        return fMessage;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Class<T> adapter) {
        if (IResource.class.equals(adapter))
            return (T) fPropertiesFile;
        return null;
    }
}
