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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.RuntimeClasspathAction;
import org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer;
import org.eclipse.jdt.internal.debug.ui.launcher.IEntriesChangedListener;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * A viewer that displays and manipulates runtime classpath entries.
 */
public class RuntimeClasspathViewer extends TreeViewer implements IClasspathViewer {

    /**
	 * Entry changed listeners
	 */
    private ListenerList<IEntriesChangedListener> fListeners = new ListenerList();

    private IClasspathEntry fCurrentParent = null;

    private IPreferenceChangeListener fPrefListeners = new IPreferenceChangeListener() {

        @Override
        public void preferenceChange(PreferenceChangeEvent event) {
            if (DebugUIPlugin.getStandardDisplay().getThread().equals(Thread.currentThread())) {
                refresh(true);
            } else {
                DebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {

                    @Override
                    public void run() {
                        refresh(true);
                    }
                });
            }
        }
    };

    /**
	 * Creates a runtime classpath viewer with the given parent.
	 *
	 * @param parent the parent control
	 */
    public  RuntimeClasspathViewer(Composite parent) {
        super(parent);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        data.heightHint = getTree().getItemHeight();
        getTree().setLayoutData(data);
        getTree().addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent event) {
                if (updateSelection(RuntimeClasspathAction.REMOVE, (IStructuredSelection) getSelection()) && event.character == SWT.DEL && event.stateMask == 0) {
                    List<?> selection = getSelectionFromWidget();
                    getClasspathContentProvider().removeAll(selection);
                    notifyChanged();
                }
            }
        });
        getTree().addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(LaunchingPlugin.ID_PLUGIN);
                if (prefs != null) {
                    prefs.removePreferenceChangeListener(fPrefListeners);
                }
            }
        });
        IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(LaunchingPlugin.ID_PLUGIN);
        if (prefs != null) {
            prefs.addPreferenceChangeListener(fPrefListeners);
        }
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#setEntries(org.eclipse.jdt.launching.IRuntimeClasspathEntry[])
	 */
    @Override
    public void setEntries(IRuntimeClasspathEntry[] entries) {
        getClasspathContentProvider().setRefreshEnabled(false);
        resolveCurrentParent(getSelection());
        getClasspathContentProvider().removeAll(fCurrentParent);
        getClasspathContentProvider().setEntries(entries);
        getClasspathContentProvider().setRefreshEnabled(true);
        notifyChanged();
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#getEntries()
	 */
    @Override
    public IRuntimeClasspathEntry[] getEntries() {
        return getClasspathContentProvider().getModel().getAllEntries();
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#addEntries(org.eclipse.jdt.launching.IRuntimeClasspathEntry[])
	 */
    @Override
    public void addEntries(IRuntimeClasspathEntry[] entries) {
        getClasspathContentProvider().setRefreshEnabled(false);
        IStructuredSelection sel = (IStructuredSelection) getSelection();
        Object beforeElement = sel.getFirstElement();
        resolveCurrentParent(getSelection());
        List<IClasspathEntry> existingEntries = Arrays.asList(fCurrentParent.getEntries());
        for (int i = 0; i < entries.length; i++) {
            if (!existingEntries.contains(entries[i])) {
                getClasspathContentProvider().add(fCurrentParent, entries[i], beforeElement);
            }
        }
        getClasspathContentProvider().setRefreshEnabled(true);
        notifyChanged();
    }

    private boolean resolveCurrentParent(ISelection selection) {
        fCurrentParent = null;
        Iterator<?> selected = ((IStructuredSelection) selection).iterator();
        while (selected.hasNext()) {
            Object element = selected.next();
            if (element instanceof ClasspathEntry) {
                IClasspathEntry parent = ((IClasspathEntry) element).getParent();
                if (fCurrentParent != null) {
                    if (!fCurrentParent.equals(parent)) {
                        return false;
                    }
                } else {
                    fCurrentParent = parent;
                }
            } else {
                if (fCurrentParent != null) {
                    if (!fCurrentParent.equals(element)) {
                        return false;
                    }
                } else {
                    fCurrentParent = (IClasspathEntry) element;
                }
            }
        }
        return true;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#isEnabled()
	 */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
	 * Sets the launch configuration context for this viewer, if any
	 * @param configuration the backing {@link ILaunchConfiguration}
	 */
    public void setLaunchConfiguration(ILaunchConfiguration configuration) {
        if (getLabelProvider() != null) {
            ((ClasspathLabelProvider) getLabelProvider()).setLaunchConfiguration(configuration);
        }
    }

    public void addEntriesChangedListener(IEntriesChangedListener listener) {
        fListeners.add(listener);
    }

    public void removeEntriesChangedListener(IEntriesChangedListener listener) {
        fListeners.remove(listener);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#notifyChanged()
	 */
    @Override
    public void notifyChanged() {
        for (IEntriesChangedListener listener : fListeners) {
            listener.entriesChanged(this);
        }
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#indexOf(org.eclipse.jdt.launching.IRuntimeClasspathEntry)
	 */
    @Override
    public int indexOf(IRuntimeClasspathEntry entry) {
        IClasspathEntry[] entries = getClasspathContentProvider().getBootstrapClasspathEntries();
        for (int i = 0; i < entries.length; i++) {
            IClasspathEntry existingEntry = entries[i];
            if (existingEntry.equals(entry)) {
                return 1;
            }
        }
        entries = getClasspathContentProvider().getUserClasspathEntries();
        for (int i = 0; i < entries.length; i++) {
            IClasspathEntry existingEntry = entries[i];
            if (existingEntry.equals(entry)) {
                return 1;
            }
        }
        return -1;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#getShell()
	 */
    @Override
    public Shell getShell() {
        return getControl().getShell();
    }

    private ClasspathContentProvider getClasspathContentProvider() {
        return (ClasspathContentProvider) super.getContentProvider();
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#updateSelection(int, org.eclipse.jface.viewers.IStructuredSelection)
	 */
    @Override
    public boolean updateSelection(int actionType, IStructuredSelection selection) {
        if (selection.isEmpty()) {
            return false;
        }
        switch(actionType) {
            case RuntimeClasspathAction.ADD:
                Iterator<IClasspathEntry> selected = selection.iterator();
                while (selected.hasNext()) {
                    IClasspathEntry entry = selected.next();
                    if (!entry.isEditable() && entry instanceof ClasspathEntry) {
                        return false;
                    }
                }
                return selection.size() > 0;
            case RuntimeClasspathAction.REMOVE:
            case RuntimeClasspathAction.MOVE:
                selected = selection.iterator();
                while (selected.hasNext()) {
                    IClasspathEntry entry = selected.next();
                    if (!entry.isEditable()) {
                        return false;
                    }
                }
                return selection.size() > 0;
            default:
                break;
        }
        return true;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#getSelectedEntries()
	 */
    public ISelection getSelectedEntries() {
        IStructuredSelection selection = (IStructuredSelection) getSelection();
        List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>(selection.size() * 2);
        Iterator<IClasspathEntry> itr = selection.iterator();
        while (itr.hasNext()) {
            IClasspathEntry element = itr.next();
            if (element.hasEntries()) {
                entries.addAll(Arrays.asList(element.getEntries()));
            } else {
                entries.add(element);
            }
        }
        return new StructuredSelection(entries);
    }
}
