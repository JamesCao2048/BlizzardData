/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     EclipseSource Corporation - ongoing enhancements
 *******************************************************************************/
package org.eclipse.pde.internal.core.product;

import java.io.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.internal.core.IWorkspaceModel;
import org.eclipse.pde.internal.core.PDECore;

public class WorkspaceProductModel extends ProductModel implements IWorkspaceModel {

    private static final long serialVersionUID = 1L;

    private IFile fFile;

    private boolean fDirty;

    private boolean fEditable;

    public  WorkspaceProductModel(IFile file, boolean editable) {
        fFile = file;
        fEditable = editable;
    }

    @Override
    public void load() throws CoreException {
        if (fFile.exists()) {
            InputStream stream = null;
            try {
                stream = new BufferedInputStream(fFile.getContents(true));
                try {
                    if (stream.available() > 0)
                        load(stream, false);
                    else {
                        // if we have an empty file, then mark as loaded so users changes will be saved
                        setLoaded(true);
                    }
                } catch (IOException e) {
                    PDECore.logException(e);
                } finally {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        PDECore.logException(e);
                    }
                }
            } catch (CoreException e) {
                PDECore.logException(e);
            }
        }
    }

    @Override
    public boolean isInSync() {
        IPath path = fFile.getLocation();
        return path == null ? false : isInSync(path.toFile());
    }

    @Override
    public IResource getUnderlyingResource() {
        return fFile;
    }

    @Override
    public String getInstallLocation() {
        return fFile.getLocation().toOSString();
    }

    @Override
    public void save() {
        ByteArrayInputStream stream = null;
        try {
            String contents = fixLineDelimiter(getContents(), fFile);
            //$NON-NLS-1$
            stream = new ByteArrayInputStream(contents.getBytes("UTF8"));
            if (fFile.exists()) {
                fFile.setContents(stream, false, false, null);
            } else {
                fFile.create(stream, false, null);
            }
            stream.close();
        } catch (CoreException e) {
            PDECore.logException(e);
        } catch (IOException e) {
            PDECore.logException(e);
        } finally {
            try {
                if (stream != null)
                    stream.close();
            } catch (IOException e) {
                PDECore.logException(e);
            }
        }
    }

    public String getContents() {
        StringWriter swriter = new StringWriter();
        PrintWriter writer = new PrintWriter(swriter);
        setLoaded(true);
        save(writer);
        writer.flush();
        try {
            swriter.close();
        } catch (IOException e) {
            PDECore.logException(e);
        }
        return swriter.toString();
    }

    @Override
    public boolean isDirty() {
        return fDirty;
    }

    @Override
    public void save(PrintWriter writer) {
        if (isLoaded()) {
            //$NON-NLS-1$
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            //$NON-NLS-1$
            writer.println("<?pde version=\"3.5\"?>");
            writer.println();
            //$NON-NLS-1$
            getProduct().write("", writer);
        }
        setDirty(false);
    }

    @Override
    public void setDirty(boolean dirty) {
        fDirty = dirty;
    }

    @Override
    public void fireModelChanged(IModelChangedEvent event) {
        setDirty(true);
        super.fireModelChanged(event);
    }

    @Override
    public boolean isEditable() {
        return fEditable;
    }

    @Override
    public void reload() {
        // Underlying file has to exist in order to reload the model
        if (fFile.exists()) {
            InputStream stream = null;
            try {
                // Get the file contents
                stream = new BufferedInputStream(fFile.getContents(true));
                // Load the model using the last saved file contents
                reload(stream, false);
                // Remove the dirty (*) indicator from the editor window
                setDirty(false);
            } catch (CoreException e) {
            }
        }
    }
}
