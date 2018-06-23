/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.site;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.pde.core.IEditableModel;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.internal.core.NLResourceHelper;
import org.eclipse.pde.internal.core.PDECore;

public class WorkspaceSiteModel extends AbstractSiteModel implements IEditableModel {

    private static final long serialVersionUID = 1L;

    private boolean fDirty;

    private IFile fFile;

    private boolean fEditable = true;

    public  WorkspaceSiteModel(IFile file) {
        fFile = file;
    }

    @Override
    public void fireModelChanged(IModelChangedEvent event) {
        setDirty(event.getChangeType() != IModelChangedEvent.WORLD_CHANGED);
        super.fireModelChanged(event);
    }

    protected NLResourceHelper createNLResourceHelper() {
        try {
            IPath path = fFile.getLocation().removeLastSegments(1);
            String installLocation = path.toOSString();
            if (//$NON-NLS-1$
            installLocation.startsWith("file:") == false)
                installLocation = "file:" + installLocation//$NON-NLS-1$
                ;
            //$NON-NLS-1$
            URL url = new URL(installLocation + "/");
            //$NON-NLS-1$
            String name = "site";
            NLResourceHelper helper = new NLResourceHelper(name, new URL[] { url });
            return helper;
        } catch (MalformedURLException e) {
            return null;
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
        }
        return swriter.toString();
    }

    public IFile getFile() {
        return fFile;
    }

    @Override
    public String getInstallLocation() {
        return fFile.getParent().getLocation().toOSString();
    }

    @Override
    public IResource getUnderlyingResource() {
        return fFile;
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
    public boolean isInSync() {
        return isInSync(fFile.getLocation().toFile());
    }

    @Override
    protected void updateTimeStamp() {
        updateTimeStamp(fFile.getLocation().toFile());
    }

    @Override
    public void load() {
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
                }
            } catch (CoreException e) {
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        } else {
            this.site = new Site();
            site.model = this;
            setLoaded(true);
        }
    }

    @Override
    public void save() {
        try {
            String contents = fixLineDelimiter(getContents(), fFile);
            //$NON-NLS-1$
            ByteArrayInputStream stream = new ByteArrayInputStream(contents.getBytes("UTF8"));
            if (fFile.exists()) {
                fFile.setContents(stream, false, false, null);
            } else {
                fFile.create(stream, false, null);
            }
            stream.close();
        } catch (CoreException e) {
            PDECore.logException(e);
        } catch (IOException e) {
        }
    }

    @Override
    public void save(PrintWriter writer) {
        if (isLoaded()) {
            //$NON-NLS-1$
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            //$NON-NLS-1$
            site.write("", writer);
        }
        setDirty(false);
    }

    @Override
    public void setDirty(boolean dirty) {
        fDirty = dirty;
    }

    public void setEditable(boolean newEditable) {
        fEditable = newEditable;
    }
}
