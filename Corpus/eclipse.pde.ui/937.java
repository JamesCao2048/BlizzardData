/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.text;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.text.IDocument;
import org.eclipse.pde.core.*;
import org.eclipse.pde.internal.core.*;

public abstract class AbstractEditingModel extends PlatformObject implements IEditingModel, IModelChangeProviderExtension {

    private ArrayList<IModelChangedListener> fListeners = new ArrayList();

    protected boolean fReconciling;

    protected boolean fInSync = true;

    protected boolean fLoaded = false;

    protected boolean fDisposed;

    protected long fTimestamp;

    private transient NLResourceHelper fNLResourceHelper;

    private IDocument fDocument;

    private boolean fDirty;

    private String fCharset;

    private IResource fUnderlyingResource;

    private String fInstallLocation;

    private boolean fStale;

    public  AbstractEditingModel(IDocument document, boolean isReconciling) {
        fDocument = document;
        fReconciling = isReconciling;
    }

    @Override
    public void dispose() {
        if (fNLResourceHelper != null) {
            fNLResourceHelper.dispose();
            fNLResourceHelper = null;
        }
        fDisposed = true;
        fListeners.clear();
    }

    @Override
    public String getResourceString(String key) {
        if (key == null || key.length() == 0)
            //$NON-NLS-1$
            return "";
        return (getNLResourceHelper() == null) ? key : getNLResourceHelper().getResourceString(key);
    }

    protected abstract NLResourceHelper createNLResourceHelper();

    public NLResourceHelper getNLResourceHelper() {
        if (fNLResourceHelper == null)
            fNLResourceHelper = createNLResourceHelper();
        return fNLResourceHelper;
    }

    @Override
    public boolean isDisposed() {
        return fDisposed;
    }

    @Override
    public boolean isEditable() {
        return fReconciling;
    }

    @Override
    public boolean isLoaded() {
        return fLoaded;
    }

    /**
	 * @param loaded
	 */
    public void setLoaded(boolean loaded) {
        // TODO: MP: TEO: LOW: Set as API?
        fLoaded = loaded;
    }

    @Override
    public boolean isInSync() {
        return fInSync;
    }

    @Override
    public boolean isValid() {
        return isLoaded();
    }

    @Override
    public final long getTimeStamp() {
        return fTimestamp;
    }

    @Override
    public final void load() throws CoreException {
        try {
            load(getInputStream(getDocument()), false);
        } catch (UnsupportedEncodingException e) {
        }
    }

    @Override
    public final void reload(InputStream source, boolean outOfSync) throws CoreException {
        load(source, outOfSync);
        fireModelChanged(new ModelChangedEvent(this, IModelChangedEvent.WORLD_CHANGED, new Object[] { this }, null));
    }

    @Override
    public boolean isReconcilingModel() {
        return fReconciling;
    }

    @Override
    public IDocument getDocument() {
        return fDocument;
    }

    @Override
    public final void reconciled(IDocument document) {
        if (isReconcilingModel()) {
            try {
                if (isStale()) {
                    adjustOffsets(document);
                    setStale(false);
                } else {
                    reload(getInputStream(document), false);
                }
            } catch (UnsupportedEncodingException e) {
            } catch (CoreException e) {
            }
            if (isDirty())
                setDirty(false);
        }
    }

    public abstract void adjustOffsets(IDocument document) throws CoreException;

    protected InputStream getInputStream(IDocument document) throws UnsupportedEncodingException {
        return new BufferedInputStream(new ByteArrayInputStream(document.get().getBytes(getCharset())));
    }

    @Override
    public String getCharset() {
        //$NON-NLS-1$
        return fCharset != null ? fCharset : "UTF-8";
    }

    @Override
    public void setCharset(String charset) {
        fCharset = charset;
    }

    @Override
    public void addModelChangedListener(IModelChangedListener listener) {
        if (!fListeners.contains(listener))
            fListeners.add(listener);
    }

    @Override
    public void transferListenersTo(IModelChangeProviderExtension target, IModelChangedListenerFilter filter) {
        @SuppressWarnings("unchecked") List<IModelChangedListener> oldList = (List<IModelChangedListener>) fListeners.clone();
        for (int i = 0; i < oldList.size(); i++) {
            IModelChangedListener listener = oldList.get(i);
            if (filter == null || filter.accept(listener)) {
                // add the listener to the target
                target.addModelChangedListener(listener);
                // remove the listener from our list
                fListeners.remove(listener);
            }
        }
    }

    @Override
    public void fireModelChanged(IModelChangedEvent event) {
        if (event.getChangeType() == IModelChangedEvent.CHANGE && event.getOldValue() != null && event.getOldValue().equals(event.getNewValue()))
            return;
        setDirty(event.getChangeType() != IModelChangedEvent.WORLD_CHANGED);
        for (int i = 0; i < fListeners.size(); i++) {
            fListeners.get(i).modelChanged(event);
        }
    }

    @Override
    public void fireModelObjectChanged(Object object, String property, Object oldValue, Object newValue) {
        fireModelChanged(new ModelChangedEvent(this, object, property, oldValue, newValue));
    }

    @Override
    public void removeModelChangedListener(IModelChangedListener listener) {
        fListeners.remove(listener);
    }

    @Override
    public boolean isDirty() {
        return fDirty;
    }

    @Override
    public void save(PrintWriter writer) {
    }

    @Override
    public void setDirty(boolean dirty) {
        this.fDirty = dirty;
    }

    @Override
    public boolean isStale() {
        return fStale;
    }

    @Override
    public void setStale(boolean stale) {
        fStale = stale;
    }

    @Override
    public IResource getUnderlyingResource() {
        return fUnderlyingResource;
    }

    public void setUnderlyingResource(IResource resource) {
        fUnderlyingResource = resource;
    }

    public String getInstallLocation() {
        if (fInstallLocation == null && fUnderlyingResource != null) {
            IPath path = fUnderlyingResource.getProject().getLocation();
            return path != null ? path.addTrailingSeparator().toString() : null;
        }
        return fInstallLocation;
    }

    public void setInstallLocation(String location) {
        fInstallLocation = location;
    }

    public IModelTextChangeListener getLastTextChangeListener() {
        for (int i = fListeners.size() - 1; i >= 0; i--) {
            Object obj = fListeners.get(i);
            if (obj instanceof IModelTextChangeListener)
                return (IModelTextChangeListener) obj;
        }
        return null;
    }
}
