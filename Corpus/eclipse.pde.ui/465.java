/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     EclipseSource Corporation - ongoing enhancements
 *******************************************************************************/
package org.eclipse.pde.internal.core.plugin;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.pde.core.IEditableModel;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.build.IBuildModel;
import org.eclipse.pde.internal.core.*;

/**
 * This class only represents 3.0 style plug-ins
 */
public abstract class WorkspacePluginModelBase extends AbstractPluginModelBase implements IEditableModel {

    private static final long serialVersionUID = 1L;

    private IFile fUnderlyingResource;

    private boolean fDirty;

    private boolean fEditable = true;

    @Override
    protected NLResourceHelper createNLResourceHelper() {
        //$NON-NLS-1$
        return new NLResourceHelper("plugin", PDEManager.getNLLookupLocations(this));
    }

    @Override
    @Deprecated
    public URL getNLLookupLocation() {
        try {
            //$NON-NLS-1$ //$NON-NLS-2$
            return new URL("file:" + getInstallLocation() + "/");
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public  WorkspacePluginModelBase(IFile file, boolean abbreviated) {
        fUnderlyingResource = file;
        fAbbreviated = abbreviated;
        setEnabled(true);
    }

    @Override
    public void fireModelChanged(IModelChangedEvent event) {
        fDirty = true;
        super.fireModelChanged(event);
    }

    @Override
    @Deprecated
    public IBuildModel getBuildModel() {
        return null;
    }

    public String getContents() {
        StringWriter swriter = new StringWriter();
        PrintWriter writer = new PrintWriter(swriter);
        save(writer);
        writer.flush();
        try {
            swriter.close();
        } catch (IOException e) {
            PDECore.logException(e);
        }
        return swriter.toString();
    }

    public IFile getFile() {
        return fUnderlyingResource;
    }

    @Override
    public String getInstallLocation() {
        IPath path = fUnderlyingResource.getLocation();
        return path == null ? null : path.removeLastSegments(1).addTrailingSeparator().toOSString();
    }

    @Override
    public IResource getUnderlyingResource() {
        return fUnderlyingResource;
    }

    @Override
    public boolean isInSync() {
        if (fUnderlyingResource == null)
            return true;
        IPath path = fUnderlyingResource.getLocation();
        if (path == null)
            return false;
        return super.isInSync(path.toFile());
    }

    @Override
    public boolean isDirty() {
        return fDirty;
    }

    @Override
    public boolean isEditable() {
        return fEditable;
    }

    @Override
    public void load() {
        if (fUnderlyingResource == null)
            return;
        if (fUnderlyingResource.exists()) {
            InputStream stream = null;
            try {
                stream = new BufferedInputStream(fUnderlyingResource.getContents(true));
                load(stream, false);
            } catch (CoreException e) {
                PDECore.logException(e);
            } finally {
                try {
                    if (stream != null)
                        stream.close();
                } catch (IOException e) {
                    PDECore.logException(e);
                }
            }
        } else {
            fPluginBase = createPluginBase();
            setLoaded(true);
        }
    }

    @Override
    protected void updateTimeStamp() {
        updateTimeStamp(fUnderlyingResource.getLocation().toFile());
    }

    @Override
    public void save() {
        if (fUnderlyingResource == null)
            return;
        ByteArrayInputStream stream = null;
        try {
            String contents = fixLineDelimiter(getContents(), fUnderlyingResource);
            //$NON-NLS-1$
            stream = new ByteArrayInputStream(contents.getBytes("UTF8"));
            if (fUnderlyingResource.exists()) {
                fUnderlyingResource.setContents(stream, false, false, null);
            } else {
                fUnderlyingResource.create(stream, false, null);
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

    @Override
    public void save(PrintWriter writer) {
        if (isLoaded()) {
            //$NON-NLS-1$
            fPluginBase.write("", writer);
        }
        fDirty = false;
    }

    @Override
    public void setDirty(boolean dirty) {
        fDirty = dirty;
    }

    public void setEditable(boolean editable) {
        fEditable = editable;
    }
}
