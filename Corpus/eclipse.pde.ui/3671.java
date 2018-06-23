/*******************************************************************************
 *  Copyright (c) 2007, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.refactoring;

import org.eclipse.core.filebuffers.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.TextFileChange;

public class MovedTextFileChange extends TextFileChange {

    private IFile fCurrentFile;

    public  MovedTextFileChange(String name, IFile newFile, IFile currentFile) {
        super(name, newFile);
        fCurrentFile = currentFile;
    }

    @Override
    public IDocument getCurrentDocument(IProgressMonitor pm) throws CoreException {
        if (pm == null)
            pm = new NullProgressMonitor();
        IDocument result = null;
        //$NON-NLS-1$
        pm.beginTask("", 2);
        ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
        try {
            IPath path = fCurrentFile.getFullPath();
            manager.connect(path, LocationKind.NORMALIZE, pm);
            ITextFileBuffer buffer = manager.getTextFileBuffer(path, LocationKind.NORMALIZE);
            result = buffer.getDocument();
        } finally {
            if (result != null)
                manager.disconnect(fCurrentFile.getFullPath(), LocationKind.NORMALIZE, pm);
        }
        pm.done();
        return result;
    }
}
