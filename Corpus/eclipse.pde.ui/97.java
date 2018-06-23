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
package org.eclipse.pde.internal.core;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import javax.xml.parsers.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.pde.core.*;
import org.xml.sax.SAXException;

public abstract class AbstractModel extends PlatformObject implements IModel, IModelChangeProviderExtension, Serializable {

    private static final long serialVersionUID = 1L;

    private transient List<IModelChangedListener> fListeners;

    private boolean fLoaded;

    protected boolean fDisposed;

    private long fTimestamp;

    private Exception fException;

    protected static String getLineDelimiterPreference(IFile file) {
        IScopeContext[] scopeContext;
        if (file != null && file.getProject() != null) {
            // project preference
            scopeContext = new IScopeContext[] { new ProjectScope(file.getProject()) };
            String lineDelimiter = Platform.getPreferencesService().getString(Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, null, scopeContext);
            if (lineDelimiter != null)
                return lineDelimiter;
        }
        // workspace preference
        scopeContext = new IScopeContext[] { InstanceScope.INSTANCE };
        return Platform.getPreferencesService().getString(Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, null, scopeContext);
    }

    /**
	 * Replaces all line delimiters to the same characters based on preference settings.  If no project
	 * or workspace preference has been set then the string will not be modified.  If the
	 * delimiter matches the current system setting, the string will not be modified.
	 *
	 * @param string the string to replace line delimiters in
	 * @param file the file to lookup specific project preference settings for, can be <code>null</code> to use workspace settings
	 * @return the provided string with line delimiters replaced
	 */
    public static String fixLineDelimiter(String string, IFile file) {
        String lineDelimiter = getLineDelimiterPreference(file);
        if (lineDelimiter == null)
            return string;
        //$NON-NLS-1$
        String lineSeparator = System.getProperty("line.separator");
        if (lineDelimiter.equals(lineSeparator))
            return string;
        return string.replaceAll(lineSeparator, lineDelimiter);
    }

    public  AbstractModel() {
        fListeners = Collections.synchronizedList(new ArrayList<IModelChangedListener>());
    }

    @Override
    public void addModelChangedListener(IModelChangedListener listener) {
        fListeners.add(listener);
    }

    @Override
    public void transferListenersTo(IModelChangeProviderExtension target, IModelChangedListenerFilter filter) {
        ArrayList<IModelChangedListener> removed = new ArrayList();
        for (int i = 0; i < fListeners.size(); i++) {
            IModelChangedListener listener = fListeners.get(i);
            if (filter == null || filter.accept(listener)) {
                target.addModelChangedListener(listener);
                removed.add(listener);
            }
        }
        fListeners.removeAll(removed);
    }

    @Override
    public void dispose() {
        fDisposed = true;
    }

    @Override
    public void fireModelChanged(IModelChangedEvent event) {
        IModelChangedListener[] list = fListeners.toArray(new IModelChangedListener[fListeners.size()]);
        for (int i = 0; i < list.length; i++) {
            IModelChangedListener listener = list[i];
            listener.modelChanged(event);
        }
    }

    @Override
    public void fireModelObjectChanged(Object object, String property, Object oldValue, Object newValue) {
        fireModelChanged(new ModelChangedEvent(this, object, property, oldValue, newValue));
    }

    @Override
    public String getResourceString(String key) {
        return key;
    }

    @Override
    public IResource getUnderlyingResource() {
        return null;
    }

    protected boolean isInSync(File localFile) {
        return localFile.exists() && localFile.lastModified() == getTimeStamp();
    }

    @Override
    public boolean isValid() {
        return !isDisposed() && isLoaded();
    }

    @Override
    public final long getTimeStamp() {
        return fTimestamp;
    }

    protected abstract void updateTimeStamp();

    protected void updateTimeStamp(File localFile) {
        if (localFile.exists())
            fTimestamp = localFile.lastModified();
    }

    @Override
    public boolean isDisposed() {
        return fDisposed;
    }

    @Override
    public boolean isLoaded() {
        return fLoaded;
    }

    public void setLoaded(boolean loaded) {
        fLoaded = loaded;
    }

    public void setException(Exception e) {
        fException = e;
    }

    public Exception getException() {
        return fException;
    }

    @Override
    public void removeModelChangedListener(IModelChangedListener listener) {
        fListeners.remove(listener);
    }

    public void throwParseErrorsException(Throwable e) throws CoreException {
        Status status = new //$NON-NLS-1$
        Status(//$NON-NLS-1$
        IStatus.ERROR, //$NON-NLS-1$
        PDECore.PLUGIN_ID, //$NON-NLS-1$
        IStatus.OK, //$NON-NLS-1$
        "Error in the manifest file", e);
        throw new CoreException(status);
    }

    protected SAXParser getSaxParser() throws ParserConfigurationException, SAXException, FactoryConfigurationError {
        return SAXParserFactory.newInstance().newSAXParser();
    }

    @Override
    public boolean isReconcilingModel() {
        return false;
    }
}
