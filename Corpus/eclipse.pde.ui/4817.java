/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Cabe <benjamin.cabe@anyware-tech.com> - bug 201572
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 487943
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor.plugin;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.ibundle.IBundleModel;
import org.eclipse.pde.internal.core.text.bundle.ExportPackageObject;
import org.eclipse.pde.internal.core.text.bundle.PackageFriend;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.pde.internal.ui.dialogs.PluginSelectionDialog;
import org.eclipse.pde.internal.ui.editor.*;
import org.eclipse.pde.internal.ui.editor.context.InputContextManager;
import org.eclipse.pde.internal.ui.parts.EditableTablePart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IPartSelectionListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.osgi.framework.Constants;

public class ExportPackageVisibilitySection extends TableSection implements IPartSelectionListener {

    private static int ADD_INDEX = 0;

    private static int REMOVE_INDEX = 1;

    private TableViewer fFriendViewer;

    private Action fAddAction;

    private Action fRemoveAction;

    private Button fInternalButton;

    private boolean fBlockChanges;

    private ExportPackageObject[] fSelectedObjects;

    private Image fImage;

    private Button fVisibleButton;

    class TableContentProvider implements IStructuredContentProvider {

        @Override
        public Object[] getElements(Object parent) {
            ExportPackageObject object = (ExportPackageObject) parent;
            if (object == null || !object.isInternal())
                return new Object[0];
            return object.getFriends();
        }
    }

    class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

        @Override
        public String getColumnText(Object obj, int index) {
            return obj.toString();
        }

        @Override
        public Image getColumnImage(Object obj, int index) {
            return fImage;
        }
    }

    public  ExportPackageVisibilitySection(PDEFormPage formPage, Composite parent) {
        super(formPage, parent, Section.DESCRIPTION, new String[] { PDEUIMessages.ManifestEditor_ExportSection_add, PDEUIMessages.ManifestEditor_ExportSection_remove });
        fHandleDefaultButton = false;
    }

    @Override
    public void createClient(Section section, FormToolkit toolkit) {
        section.setText(PDEUIMessages.ExportPackageVisibilitySection_title);
        section.setDescription(PDEUIMessages.ExportPackageVisibilitySection_default);
        Composite comp = toolkit.createComposite(section);
        comp.setLayout(FormLayoutFactory.createSectionClientGridLayout(false, 1));
        fVisibleButton = toolkit.createButton(comp, PDEUIMessages.ExportPackageVisibilitySection_unconditional, SWT.RADIO);
        fInternalButton = toolkit.createButton(comp, PDEUIMessages.ExportPackageVisibilitySection_hideAll, SWT.RADIO);
        fInternalButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                if (!fBlockChanges) {
                    for (int i = 0; i < fSelectedObjects.length; i++) {
                        fSelectedObjects[i].setInternal(fInternalButton.getSelection());
                    }
                    getTablePart().setButtonEnabled(ADD_INDEX, fInternalButton.getSelection());
                    getTablePart().setButtonEnabled(REMOVE_INDEX, fInternalButton.getSelection());
                    fFriendViewer.refresh();
                }
            }
        });
        Composite container = toolkit.createComposite(comp);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 1;
        layout.marginHeight = 1;
        layout.numColumns = 2;
        container.setLayout(layout);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        EditableTablePart tablePart = getTablePart();
        tablePart.setEditable(getPage().getModel().isEditable());
        createViewerPartControl(container, SWT.MULTI, 2, toolkit);
        fFriendViewer = tablePart.getTableViewer();
        fFriendViewer.setContentProvider(new TableContentProvider());
        fFriendViewer.setLabelProvider(new TableLabelProvider());
        toolkit.paintBordersFor(container);
        makeActions();
        fImage = PDEPluginImages.DESC_PLUGIN_OBJ.createImage();
        update(null);
        getBundleModel().addModelChangedListener(this);
        section.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));
        section.setLayoutData(new GridData(GridData.FILL_BOTH));
        section.setClient(comp);
    }

    private void makeActions() {
        fAddAction = new Action(PDEUIMessages.ManifestEditor_ExportSection_add) {

            @Override
            public void run() {
                handleAdd();
            }
        };
        fAddAction.setEnabled(isEditable());
        fRemoveAction = new Action(PDEUIMessages.ManifestEditor_ExportSection_remove) {

            @Override
            public void run() {
                handleRemove();
            }
        };
        fRemoveAction.setEnabled(isEditable());
    }

    @Override
    protected void selectionChanged(IStructuredSelection selection) {
        // Update global selection
        getPage().getPDEEditor().setSelection(selection);
        Object item = selection.getFirstElement();
        getTablePart().setButtonEnabled(1, item != null);
    }

    @Override
    protected void buttonSelected(int index) {
        if (index == ADD_INDEX)
            handleAdd();
        else if (index == REMOVE_INDEX)
            handleRemove();
    }

    @Override
    public boolean doGlobalAction(String actionId) {
        if (!isEditable()) {
            return false;
        }
        if (actionId.equals(ActionFactory.DELETE.getId())) {
            handleRemove();
            return true;
        }
        if (actionId.equals(ActionFactory.CUT.getId())) {
            // delete here and let the editor transfer
            // the selection to the clipboard
            handleRemove();
            return false;
        }
        if (actionId.equals(ActionFactory.PASTE.getId())) {
            doPaste();
            return true;
        }
        return false;
    }

    @Override
    protected boolean canPaste(Object targetObject, Object[] sourceObjects) {
        // One export package object must be selected
        if (isOneObjectSelected() == false) {
            return false;
        }
        // be a friend of the selected export package object
        for (int i = 0; i < sourceObjects.length; i++) {
            // Only package friends allowed
            if ((sourceObjects[i] instanceof PackageFriend) == false) {
                return false;
            }
            // No duplicate package friends allowed
            PackageFriend friend = (PackageFriend) sourceObjects[i];
            if (fSelectedObjects[0].hasFriend(friend.getName())) {
                return false;
            }
        }
        return true;
    }

    private boolean isOneObjectSelected() {
        if ((fSelectedObjects == null) || (fSelectedObjects.length != 1)) {
            return false;
        }
        return true;
    }

    @Override
    protected void doPaste(Object targetObject, Object[] sourceObjects) {
        // Paste all source objects
        for (int i = 0; i < sourceObjects.length; i++) {
            Object sourceObject = sourceObjects[i];
            if ((sourceObject instanceof PackageFriend) && isOneObjectSelected()) {
                // Package friend object
                PackageFriend friend = (PackageFriend) sourceObject;
                // Adjust all the source object transient field values to
                // acceptable values
                friend.reconnect(fSelectedObjects[0]);
                // Add the package friend to the export package object
                fSelectedObjects[0].addFriend(friend);
            }
        }
    }

    @Override
    public void dispose() {
        IBundleModel model = getBundleModel();
        if (model != null)
            model.removeModelChangedListener(this);
        if (fImage != null)
            fImage.dispose();
        super.dispose();
    }

    @Override
    protected void fillContextMenu(IMenuManager manager) {
        getPage().getPDEEditor().getContributor().contextMenuAboutToShow(manager);
    }

    private void handleAdd() {
        PluginSelectionDialog dialog = new PluginSelectionDialog(PDEPlugin.getActiveWorkbenchShell(), getModels(), true);
        dialog.create();
        if (dialog.open() == Window.OK) {
            Object[] selected = dialog.getResult();
            for (int i = 0; i < selected.length; i++) {
                IPluginModelBase model = (IPluginModelBase) selected[i];
                for (int j = 0; j < fSelectedObjects.length; j++) {
                    fSelectedObjects[j].addFriend(new PackageFriend(fSelectedObjects[j], model.getPluginBase().getId()));
                }
            }
        }
    }

    private IPluginModelBase[] getModels() {
        ArrayList<IPluginModelBase> list = new ArrayList();
        IPluginModelBase[] models = PluginRegistry.getActiveModels(true);
        for (int i = 0; i < models.length; i++) {
            String id = models[i].getPluginBase().getId();
            if (!fSelectedObjects[0].hasFriend(id))
                list.add(models[i]);
        }
        return list.toArray(new IPluginModelBase[list.size()]);
    }

    private void handleRemove() {
        Object[] removed = ((IStructuredSelection) fFriendViewer.getSelection()).toArray();
        for (int i = 0; i < removed.length; i++) {
            for (int j = 0; j < fSelectedObjects.length; j++) {
                fSelectedObjects[j].removeFriend((PackageFriend) removed[i]);
            }
        }
    }

    @Override
    public void modelChanged(IModelChangedEvent event) {
        if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
            markStale();
            return;
        }
        if (Constants.EXPORT_PACKAGE.equals(event.getChangedProperty())) {
            refresh();
            return;
        }
        if (event.getChangeType() == IModelChangedEvent.INSERT && event.getChangedObjects()[0] instanceof PackageFriend) {
            fFriendViewer.refresh();
            fFriendViewer.setSelection(new StructuredSelection(event.getChangedObjects()[0]), true);
            fFriendViewer.getControl().setFocus();
            return;
        }
        int index = fFriendViewer.getTable().getSelectionIndex();
        fFriendViewer.refresh();
        fFriendViewer.getTable().setSelection(Math.min(index, fFriendViewer.getTable().getItemCount() - 1));
    }

    @Override
    public void refresh() {
        update(null);
        super.refresh();
    }

    @Override
    public void selectionChanged(IFormPart source, ISelection selection) {
        List<?> list = ((IStructuredSelection) selection).toList();
        if (list.size() > 0) {
            Object[] objects = list.toArray();
            ExportPackageObject first = null;
            for (int i = 0; i < objects.length; i++) {
                if (!(objects[i] instanceof ExportPackageObject)) {
                    update(null);
                    return;
                }
                if (first == null) {
                    first = (ExportPackageObject) objects[i];
                    continue;
                }
                if (!first.hasSameVisibility((ExportPackageObject) objects[i])) {
                    update(null);
                    return;
                }
            }
            update(list.toArray(new ExportPackageObject[list.size()]));
        } else {
            update(null);
        }
    }

    private void update(ExportPackageObject[] objects) {
        fBlockChanges = true;
        fSelectedObjects = objects;
        ExportPackageObject object = objects == null ? null : objects[0];
        fVisibleButton.setEnabled(object != null && isEditable());
        fVisibleButton.setSelection(objects != null && !object.isInternal());
        fInternalButton.setEnabled(object != null && isEditable());
        fInternalButton.setSelection(objects != null && object.isInternal());
        getTablePart().setButtonEnabled(0, fInternalButton.getSelection() && isEditable());
        getTablePart().setButtonEnabled(1, fInternalButton.getSelection() && isEditable());
        fFriendViewer.setInput(object);
        fBlockChanges = false;
    }

    private BundleInputContext getBundleContext() {
        InputContextManager manager = getPage().getPDEEditor().getContextManager();
        return (BundleInputContext) manager.findContext(BundleInputContext.CONTEXT_ID);
    }

    private IBundleModel getBundleModel() {
        BundleInputContext context = getBundleContext();
        return (context != null) ? (IBundleModel) context.getModel() : null;
    }
}
