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
package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionListenerAction;

/**
 * Action used with a runtime classpath viewer.
 */
public abstract class RuntimeClasspathAction extends SelectionListenerAction {

    public static final int DEFAULT = 0;

    public static final int ADD = 1;

    public static final int REMOVE = 2;

    public static final int MOVE = 3;

    private IClasspathViewer fViewer;

    private Button fButton;

    private Shell fShell;

    public  RuntimeClasspathAction(String label, IClasspathViewer viewer) {
        super(label);
        setViewer(viewer);
    }

    /**
	 * Sets the viewer on which this action operates.
	 * 
	 * @param viewer the viewer on which this action operates
	 */
    public void setViewer(IClasspathViewer viewer) {
        if (fViewer != null) {
            fViewer.removeSelectionChangedListener(this);
        }
        fViewer = viewer;
        if (fViewer != null) {
            fViewer.addSelectionChangedListener(this);
            update();
        }
    }

    /**
	 * Returns the viewer on which this action operates.
	 * 
	 * @return the viewer on which this action operates
	 */
    protected IClasspathViewer getViewer() {
        return fViewer;
    }

    /**
	 * Returns the selected items in the list, in the order they are
	 * displayed.
	 * 
	 * @return targets for an action
	 */
    protected List<IRuntimeClasspathEntry> getOrderedSelection() {
        List<IRuntimeClasspathEntry> targets = new ArrayList<IRuntimeClasspathEntry>();
        List<?> selection = ((IStructuredSelection) getViewer().getSelection()).toList();
        IRuntimeClasspathEntry[] entries = getViewer().getEntries();
        for (int i = 0; i < entries.length; i++) {
            IRuntimeClasspathEntry target = entries[i];
            if (selection.contains(target)) {
                targets.add(target);
            }
        }
        return targets;
    }

    /**
	 * Returns a list (copy) of the entries in the viewer
	 * @return the complete listing of all of the entries in the viewer
	 */
    protected List<IRuntimeClasspathEntry> getEntriesAsList() {
        IRuntimeClasspathEntry[] entries = getViewer().getEntries();
        List<IRuntimeClasspathEntry> list = new ArrayList<IRuntimeClasspathEntry>(entries.length);
        for (int i = 0; i < entries.length; i++) {
            list.add(entries[i]);
        }
        return list;
    }

    /**
	 * Updates the entries to the entries in the given list
	 * @param list the list of {@link IRuntimeClasspathEntry}s to set
	 */
    protected void setEntries(List<IRuntimeClasspathEntry> list) {
        getViewer().setEntries(list.toArray(new IRuntimeClasspathEntry[list.size()]));
        // update all selection listeners
        getViewer().setSelection(getViewer().getSelection());
    }

    /**
	 * Returns whether the item at the given index in the list
	 * (visually) is selected.
	 * @param selection the current selection from the viewer
	 * @param index the index to ask about
	 * @return <code>true</code> if the given index is in the selection, <code>false</code> otherwise
	 */
    protected boolean isIndexSelected(IStructuredSelection selection, int index) {
        if (selection.isEmpty()) {
            return false;
        }
        Iterator<?> entries = selection.iterator();
        List<IRuntimeClasspathEntry> list = getEntriesAsList();
        while (entries.hasNext()) {
            Object next = entries.next();
            if (list.indexOf(next) == index) {
                return true;
            }
        }
        return false;
    }

    /**
	 * Sets the {@link Button} that invokes this action
	 * @param button the new {@link Button} to set as the backing widget for this action
	 */
    public void setButton(Button button) {
        fButton = button;
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent evt) {
                run();
            }
        });
        fButton.setEnabled(false);
    }

    /**
	 * @see IAction#setEnabled(boolean)
	 */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (fButton != null) {
            fButton.setEnabled(enabled);
        }
    }

    /**
	 * Updates the enabled state.
	 */
    protected void update() {
        selectionChanged((IStructuredSelection) getViewer().getSelection());
    }

    /**
	 * Returns the shell used to realize this action's dialog (if any).
	 * @return the current {@link Shell}
	 */
    protected Shell getShell() {
        if (fShell == null) {
            fShell = getViewer().getShell();
        }
        return fShell;
    }

    /**
	 * Sets the shell used to realize this action's dialog (if any).
	 * @param shell the current shell
	 */
    public void setShell(Shell shell) {
        fShell = shell;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ui.actions.SelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
    @Override
    protected boolean updateSelection(IStructuredSelection selection) {
        return getViewer().updateSelection(getActionType(), selection);
    }

    protected int getActionType() {
        return DEFAULT;
    }
}
