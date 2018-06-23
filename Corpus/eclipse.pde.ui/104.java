/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.wizards;

import java.util.Iterator;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.pde.internal.ui.elements.ElementList;
import org.eclipse.pde.internal.ui.elements.ListContentProvider;
import org.eclipse.pde.ui.IPluginContentWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public abstract class WizardListSelectionPage extends BaseWizardSelectionPage implements IExecutableExtension {

    protected TableViewer wizardSelectionViewer;

    protected ElementList wizardElements;

    private WizardSelectedAction doubleClickAction = new WizardSelectedAction();

    private class WizardSelectedAction extends Action {

        public  WizardSelectedAction() {
            //$NON-NLS-1$
            super("wizardSelection");
        }

        @Override
        public void run() {
            selectionChanged(new SelectionChangedEvent(wizardSelectionViewer, wizardSelectionViewer.getSelection()));
            advanceToNextPage();
        }
    }

    public  WizardListSelectionPage(ElementList wizardElements, String message) {
        //$NON-NLS-1$
        super("ListSelection", message);
        this.wizardElements = wizardElements;
    }

    public void advanceToNextPage() {
        getContainer().showPage(getNextPage());
    }

    public ElementList getWizardElements() {
        return wizardElements;
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 10;
        container.setLayout(layout);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        createAbove(container, 1);
        Label label = new Label(container, SWT.NONE);
        label.setText(getLabel());
        GridData gd = new GridData();
        label.setLayoutData(gd);
        SashForm sashForm = new SashForm(container, SWT.HORIZONTAL);
        gd = new GridData(GridData.FILL_BOTH);
        // limit the width of the sash form to avoid the wizard
        // opening very wide. This is just preferred size -
        // it can be made bigger by the wizard
        // See bug #83356
        gd.widthHint = 300;
        sashForm.setLayoutData(gd);
        wizardSelectionViewer = new TableViewer(sashForm, SWT.BORDER);
        wizardSelectionViewer.setContentProvider(new ListContentProvider());
        wizardSelectionViewer.setLabelProvider(ListUtil.TABLE_LABEL_PROVIDER);
        wizardSelectionViewer.setComparator(ListUtil.NAME_COMPARATOR);
        wizardSelectionViewer.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
        createDescriptionIn(sashForm);
        createBelow(container, 1);
        initializeViewer();
        wizardSelectionViewer.setInput(wizardElements);
        wizardSelectionViewer.addSelectionChangedListener(this);
        Dialog.applyDialogFont(container);
        setControl(container);
    }

    protected void createAbove(Composite container, int span) {
    }

    protected void createBelow(Composite container, int span) {
    }

    protected void initializeViewer() {
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        setErrorMessage(null);
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        WizardElement currentWizardSelection = null;
        Iterator<?> iter = selection.iterator();
        if (iter.hasNext())
            currentWizardSelection = (WizardElement) iter.next();
        if (currentWizardSelection == null) {
            //$NON-NLS-1$
            setDescriptionText("");
            setSelectedNode(null);
            return;
        }
        final WizardElement finalSelection = currentWizardSelection;
        setSelectedNode(createWizardNode(finalSelection));
        setDescriptionText(finalSelection.getDescription());
        getContainer().updateButtons();
    }

    public IWizardPage getNextPage(boolean shouldCreate) {
        if (!shouldCreate)
            return super.getNextPage();
        IWizardNode selectedNode = getSelectedNode();
        selectedNode.dispose();
        IWizard wizard = selectedNode.getWizard();
        if (wizard == null) {
            super.setSelectedNode(null);
            return null;
        }
        if (shouldCreate)
            // Allow the wizard to create its pages
            wizard.addPages();
        return wizard.getStartingPage();
    }

    protected void focusAndSelectFirst() {
        Table table = wizardSelectionViewer.getTable();
        table.setFocus();
        TableItem[] items = table.getItems();
        if (items.length > 0) {
            TableItem first = items[0];
            Object obj = first.getData();
            wizardSelectionViewer.setSelection(new StructuredSelection(obj));
        }
    }

    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
    }

    public IPluginContentWizard getSelectedWizard() {
        IWizardNode node = getSelectedNode();
        if (node != null)
            return (IPluginContentWizard) node.getWizard();
        return null;
    }

    @Override
    public boolean canFlipToNextPage() {
        IStructuredSelection ssel = (IStructuredSelection) wizardSelectionViewer.getSelection();
        return ssel != null && !ssel.isEmpty();
    }
}
