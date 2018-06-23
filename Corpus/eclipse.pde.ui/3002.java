/*******************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 487943
 *******************************************************************************/
package org.eclipse.pde.internal.ui.wizards.templates;

import java.util.ArrayList;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.pde.internal.ui.parts.FormBrowser;
import org.eclipse.pde.internal.ui.parts.WizardCheckboxTablePart;
import org.eclipse.pde.ui.templates.ITemplateSection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class TemplateSelectionPage extends WizardPage {

    private ITemplateSection[] fCandidates;

    private ArrayList<WizardPage> fVisiblePages;

    private WizardCheckboxTablePart fTablePart;

    private FormBrowser fDescriptionBrowser;

    class TablePart extends WizardCheckboxTablePart {

        public  TablePart(String mainLabel) {
            super(mainLabel);
        }

        @Override
        protected StructuredViewer createStructuredViewer(Composite parent, int style, FormToolkit toolkit) {
            return super.createStructuredViewer(parent, style | SWT.FULL_SELECTION, toolkit);
        }

        @Override
        protected void updateCounter(int amount) {
            super.updateCounter(amount);
            if (getContainer() != null)
                getContainer().updateButtons();
        }
    }

    class ListContentProvider implements IStructuredContentProvider {

        @Override
        public Object[] getElements(Object parent) {
            return fCandidates;
        }
    }

    class ListLabelProvider extends LabelProvider implements ITableLabelProvider {

        @Override
        public String getColumnText(Object obj, int index) {
            ITemplateSection section = (ITemplateSection) obj;
            if (index == 0)
                return section.getLabel();
            return section.getUsedExtensionPoint();
        }

        @Override
        public Image getColumnImage(Object obj, int index) {
            if (index == 0)
                return PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_EXTENSION_OBJ);
            return PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_EXT_POINT_OBJ);
        }
    }

    /**
	 * Constructor for TemplateSelectionPage.
	 * @param pageName
	 */
    public  TemplateSelectionPage(ITemplateSection[] candidates) {
        //$NON-NLS-1$
        super("templateSelection");
        fCandidates = candidates;
        setTitle(PDEUIMessages.TemplateSelectionPage_title);
        setDescription(PDEUIMessages.TemplateSelectionPage_desc);
        initializeTemplates();
    }

    private void initializeTemplates() {
        fTablePart = new TablePart(PDEUIMessages.TemplateSelectionPage_table);
        fDescriptionBrowser = new FormBrowser(SWT.BORDER | SWT.V_SCROLL);
        //$NON-NLS-1$
        fDescriptionBrowser.setText("");
        PDEPlugin.getDefault().getLabelProvider().connect(this);
        fVisiblePages = new ArrayList();
    }

    @Override
    public void dispose() {
        super.dispose();
        PDEPlugin.getDefault().getLabelProvider().disconnect(this);
    }

    /**
	 * @see IDialogPage#createControl(Composite)
	 */
    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 2;
        fTablePart.createControl(container);
        CheckboxTableViewer viewer = fTablePart.getTableViewer();
        viewer.setContentProvider(new ListContentProvider());
        viewer.setLabelProvider(new ListLabelProvider());
        initializeTable(viewer.getTable());
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection sel = (IStructuredSelection) event.getSelection();
                handleSelectionChanged((ITemplateSection) sel.getFirstElement());
            }
        });
        fDescriptionBrowser.createControl(container);
        Control c = fDescriptionBrowser.getControl();
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
        gd.heightHint = 100;
        //gd.horizontalSpan = 2;
        c.setLayoutData(gd);
        viewer.setInput(PDEPlugin.getDefault());
        // add all wizard pages to wizard.  Just don't iniatilize them right away (bug 174457)
        initializeWizardPages();
        setControl(container);
        Dialog.applyDialogFont(container);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(container, IHelpContextIds.TEMPLATE_SELECTION);
    }

    private void initializeWizardPages() {
        for (int i = 0; i < fCandidates.length; i++) {
            ITemplateSection section = fCandidates[i];
            if (section.getPagesAdded() == false)
                section.addPages((Wizard) getWizard());
        }
    }

    public ITemplateSection[] getSelectedTemplates() {
        Object[] elements = fTablePart.getTableViewer().getCheckedElements();
        ITemplateSection[] result = new ITemplateSection[elements.length];
        System.arraycopy(elements, 0, result, 0, elements.length);
        return result;
    }

    private void initializeTable(Table table) {
        table.setHeaderVisible(true);
        TableColumn column = new TableColumn(table, SWT.NULL);
        column.setText(PDEUIMessages.TemplateSelectionPage_column_name);
        column.setResizable(true);
        column = new TableColumn(table, SWT.NULL);
        column.setText(PDEUIMessages.TemplateSelectionPage_column_point);
        column.setResizable(true);
        TableLayout layout = new TableLayout();
        layout.addColumnData(new ColumnWeightData(50));
        layout.addColumnData(new ColumnWeightData(50));
        table.setLayout(layout);
    }

    private void handleSelectionChanged(ITemplateSection section) {
        //$NON-NLS-1$
        String text = section != null ? section.getDescription() : "";
        if (text.length() > 0)
            //$NON-NLS-1$ //$NON-NLS-2$
            text = "<p>" + text + "</p>";
        fDescriptionBrowser.setText(text);
    }

    @Override
    public boolean canFlipToNextPage() {
        if (fTablePart.getSelectionCount() == 0)
            return false;
        return super.canFlipToNextPage();
    }

    @Override
    public IWizardPage getNextPage() {
        ITemplateSection[] sections = getSelectedTemplates();
        fVisiblePages.clear();
        for (int i = 0; i < sections.length; i++) {
            ITemplateSection section = sections[i];
            for (int j = 0; j < section.getPageCount(); j++) {
                fVisiblePages.add(section.getPage(j));
            }
        }
        if (fVisiblePages.size() > 0)
            return fVisiblePages.get(0);
        return null;
    }

    public IWizardPage getNextVisiblePage(IWizardPage page) {
        if (page == this)
            return page.getNextPage();
        int index = fVisiblePages.indexOf(page);
        if (index >= 0 && index < fVisiblePages.size() - 1)
            return fVisiblePages.get(index + 1);
        return null;
    }
}
