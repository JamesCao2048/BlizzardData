/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ua.ui.editor.toc.details;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.pde.internal.ua.core.toc.text.TocObject;
import org.eclipse.pde.internal.ua.core.toc.text.TocTopic;
import org.eclipse.pde.internal.ua.ui.editor.toc.TocInputContext;
import org.eclipse.pde.internal.ua.ui.editor.toc.TocTreeSection;
import org.eclipse.pde.internal.ui.editor.FormEntryAdapter;
import org.eclipse.pde.internal.ui.parts.FormEntry;
import org.eclipse.pde.internal.ui.util.FileValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class TocTopicDetails extends TocAbstractDetails {

    private TocTopic fDataTOCTopic;

    private FormEntry fNameEntry;

    private FormEntry fPageEntry;

    /**
	 * @param masterSection
	 */
    public  TocTopicDetails(TocTreeSection masterSection) {
        super(masterSection, TocInputContext.CONTEXT_ID);
        fDataTOCTopic = null;
        fNameEntry = null;
        fPageEntry = null;
    }

    /**
	 * @param object
	 */
    public void setData(TocTopic object) {
        // Set data
        fDataTOCTopic = object;
    }

    protected TocObject getDataObject() {
        return fDataTOCTopic;
    }

    protected FormEntry getPathEntryField() {
        return fPageEntry;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.toc.TocAbstractDetails#createDetails(org.eclipse.swt.widgets.Composite)
	 */
    public void createFields(Composite parent) {
        createNameWidget(parent);
        createSpace(parent);
        createPageWidget(parent);
    }

    /**
	 * @param parent
	 */
    private void createNameWidget(Composite parent) {
        createLabel(parent, getManagedForm().getToolkit(), TocDetailsMessages.TocTopicDetails_nameDesc);
        fNameEntry = new FormEntry(parent, getManagedForm().getToolkit(), TocDetailsMessages.TocTopicDetails_nameText, SWT.NONE);
    }

    /**
	 * @param parent
	 */
    private void createPageWidget(Composite parent) {
        createLabel(parent, getManagedForm().getToolkit(), TocDetailsMessages.TocTopicDetails_locationDesc);
        fPageEntry = new FormEntry(parent, getManagedForm().getToolkit(), TocDetailsMessages.TocTopicDetails_locationText, TocDetailsMessages.TocTopicDetails_browse, isEditable());
    }

    protected String getDetailsTitle() {
        return TocDetailsMessages.TocTopicDetails_title;
    }

    protected String getDetailsDescription() {
        return null;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.toc.TocAbstractDetails#hookListeners()
	 */
    public void hookListeners() {
        createNameEntryListeners();
        createPageEntryListeners();
    }

    /**
	 *
	 */
    private void createNameEntryListeners() {
        fNameEntry.setFormEntryListener(new FormEntryAdapter(this) {

            public void textValueChanged(FormEntry entry) {
                // Ensure data object is defined
                if (fDataTOCTopic != null) {
                    {
                        fDataTOCTopic.setFieldLabel(fNameEntry.getValue());
                    }
                }
            }
        });
    }

    /**
	 *
	 */
    private void createPageEntryListeners() {
        fPageEntry.setFormEntryListener(new FormEntryAdapter(this) {

            public // Ensure data object is defined
            void textValueChanged(// Ensure data object is defined
            FormEntry entry) {
                if (fDataTOCTopic != null) {
                    fDataTOCTopic.setFieldRef(fPageEntry.getValue());
                }
            }

            public void browseButtonSelected(FormEntry entry) {
                handleBrowse();
            }

            public void linkActivated(HyperlinkEvent e) {
                handleOpen();
            }
        });
    }

    private void handleBrowse() {
        ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getPage().getSite().getShell(), new WorkbenchLabelProvider(), new WorkbenchContentProvider());
        dialog.setValidator(new FileValidator());
        dialog.setAllowMultiple(false);
        dialog.setTitle(TocDetailsMessages.TocTopicDetails_dialogTitle);
        dialog.setMessage(TocDetailsMessages.TocTopicDetails_dialogMessage);
        dialog.addFilter(new HelpEditorFilter());
        dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
        if (dialog.open() == Window.OK) {
            IFile file = (IFile) dialog.getFirstResult();
            setPathEntry(file);
        }
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.toc.TocAbstractDetails#updateFields()
	 */
    public void updateFields() {
        // Ensure data object is defined
        if (// Update name entry
        fDataTOCTopic != null) {
            updateNameEntry(isEditableElement());
            updatePageEntry(isEditableElement());
        }
    }

    /**
	 * @param editable
	 */
    private void updateNameEntry(boolean editable) {
        fNameEntry.setValue(fDataTOCTopic.getFieldLabel(), true);
        fNameEntry.setEditable(editable);
    }

    /**
	 * @param editable
	 */
    private void updatePageEntry(boolean editable) {
        fPageEntry.setValue(fDataTOCTopic.getFieldRef(), true);
        fPageEntry.setEditable(editable);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#commit(boolean)
	 */
    public void commit(boolean onSave) {
        super.commit(onSave);
        // Only required for form entries
        fNameEntry.commit();
        fPageEntry.commit();
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IPartSelectionListener#selectionChanged(org.eclipse.ui.forms.IFormPart, org.eclipse.jface.viewers.ISelection)
	 */
    public void selectionChanged(IFormPart part, ISelection selection) {
        // Get the first selected object
        Object object = getFirstSelectedObject(selection);
        // Ensure we have the right type
        if (object != null && object instanceof TocTopic) {
            // Set data
            setData((TocTopic) object);
            // Update the UI given the new data
            updateFields();
        }
    }
}
