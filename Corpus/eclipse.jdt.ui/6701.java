/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.preferences;

import java.util.Set;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaElementComparator;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;

public class ProjectSelectionDialog extends SelectionStatusDialog {

    // the visual selection widget group
    private TableViewer fTableViewer;

    private Set<IJavaProject> fProjectsWithSpecifics;

    // sizing constants
    private static final int SIZING_SELECTION_WIDGET_HEIGHT = 250;

    private static final int SIZING_SELECTION_WIDGET_WIDTH = 300;

    //$NON-NLS-1$
    private static final String DIALOG_SETTINGS_SHOW_ALL = "ProjectSelectionDialog.show_all";

    private ViewerFilter fFilter;

    public  ProjectSelectionDialog(Shell parentShell, Set<IJavaProject> projectsWithSpecifics) {
        super(parentShell);
        setTitle(PreferencesMessages.ProjectSelectionDialog_title);
        setMessage(PreferencesMessages.ProjectSelectionDialog_desciption);
        fProjectsWithSpecifics = projectsWithSpecifics;
        fFilter = new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                return fProjectsWithSpecifics.contains(element);
            }
        };
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        // page group
        Composite composite = (Composite) super.createDialogArea(parent);
        Font font = parent.getFont();
        composite.setFont(font);
        createMessageArea(composite);
        fTableViewer = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                doSelectionChanged(((IStructuredSelection) event.getSelection()).toArray());
            }
        });
        fTableViewer.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {
                okPressed();
            }
        });
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
        data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
        fTableViewer.getTable().setLayoutData(data);
        fTableViewer.setLabelProvider(new JavaElementLabelProvider());
        fTableViewer.setContentProvider(new StandardJavaElementContentProvider());
        fTableViewer.setComparator(new JavaElementComparator());
        fTableViewer.getControl().setFont(font);
        Button checkbox = new Button(composite, SWT.CHECK);
        checkbox.setText(PreferencesMessages.ProjectSelectionDialog_filter);
        checkbox.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
        checkbox.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateFilter(((Button) e.widget).getSelection());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                updateFilter(((Button) e.widget).getSelection());
            }
        });
        IDialogSettings dialogSettings = JavaPlugin.getDefault().getDialogSettings();
        boolean doFilter = !dialogSettings.getBoolean(DIALOG_SETTINGS_SHOW_ALL) && !fProjectsWithSpecifics.isEmpty();
        checkbox.setSelection(doFilter);
        updateFilter(doFilter);
        IJavaModel input = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
        fTableViewer.setInput(input);
        doSelectionChanged(new Object[0]);
        Dialog.applyDialogFont(composite);
        return composite;
    }

    protected void updateFilter(boolean selected) {
        if (selected) {
            fTableViewer.addFilter(fFilter);
        } else {
            fTableViewer.removeFilter(fFilter);
        }
        JavaPlugin.getDefault().getDialogSettings().put(DIALOG_SETTINGS_SHOW_ALL, !selected);
    }

    private void doSelectionChanged(Object[] objects) {
        if (objects.length != 1) {
            //$NON-NLS-1$
            updateStatus(new StatusInfo(IStatus.ERROR, ""));
            setSelectionResult(null);
        } else {
            updateStatus(new StatusInfo());
            setSelectionResult(objects);
        }
    }

    @Override
    protected void computeResult() {
    }
}
