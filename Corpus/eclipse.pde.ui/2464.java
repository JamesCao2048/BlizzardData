/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     EclipseSource Corporation - ongoing enhancements
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 452487
 *******************************************************************************/
package org.eclipse.pde.internal.core.plugin;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.pde.core.IEditableModel;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.build.IBuildEntry;
import org.eclipse.pde.internal.core.*;
import org.eclipse.pde.internal.core.build.WorkspaceBuildModel;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModelBase;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModelProvider;
import org.eclipse.pde.internal.core.project.PDEProject;
import org.osgi.framework.Constants;

public class WorkspaceExtensionsModel extends AbstractExtensionsModel implements IEditableModel, IBundlePluginModelProvider {

    private static final long serialVersionUID = 1L;

    private IFile fUnderlyingResource;

    private boolean fDirty;

    private boolean fEditable = true;

    private transient IBundlePluginModelBase fBundleModel;

    @Override
    protected NLResourceHelper createNLResourceHelper() {
        return new NLResourceHelper(Constants.BUNDLE_LOCALIZATION_DEFAULT_BASENAME, getNLLookupLocations());
    }

    @Override
    public URL getNLLookupLocation() {
        try {
            //$NON-NLS-1$ //$NON-NLS-2$
            return new URL("file:" + getInstallLocation() + "/");
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public  WorkspaceExtensionsModel(IFile file) {
        fUnderlyingResource = file;
    }

    @Override
    public void fireModelChanged(IModelChangedEvent event) {
        fDirty = true;
        super.fireModelChanged(event);
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

    @Override
    public String getInstallLocation() {
        return fUnderlyingResource.getLocation().removeLastSegments(1).addTrailingSeparator().toOSString();
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
        getExtensions(true);
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
                adjustBuildPropertiesFile(fUnderlyingResource);
            }
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

    private void adjustBuildPropertiesFile(IFile underlyingResource) throws CoreException {
        IProject project = underlyingResource.getProject();
        IFile buildPropertiesFile = PDEProject.getBuildProperties(project);
        if (buildPropertiesFile.exists()) {
            WorkspaceBuildModel model = new WorkspaceBuildModel(buildPropertiesFile);
            IBuildEntry entry = model.getBuild().getEntry(IBuildEntry.BIN_INCLUDES);
            String relativePath = underlyingResource.getProjectRelativePath().toString();
            if (!entry.contains(relativePath)) {
                entry.addToken(relativePath);
                model.save();
            }
        }
    }

    @Override
    public void save(PrintWriter writer) {
        if (isLoaded()) {
            //$NON-NLS-1$
            fExtensions.write("", writer);
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

    @Override
    protected Extensions createExtensions() {
        Extensions extensions = super.createExtensions();
        extensions.setIsFragment(fUnderlyingResource.getName().equals(ICoreConstants.FRAGMENT_FILENAME_DESCRIPTOR));
        return extensions;
    }

    @Override
    public String toString() {
        return fUnderlyingResource.getName();
    }

    public void setBundleModel(IBundlePluginModelBase model) {
        fBundleModel = model;
    }

    @Override
    public IBundlePluginModelBase getBundlePluginModel() {
        return fBundleModel;
    }
}
