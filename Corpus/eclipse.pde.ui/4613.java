/*******************************************************************************
 * Copyright (c) 2008, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 487943
 *******************************************************************************/
package org.eclipse.pde.internal.ui.nls;

import java.util.*;
import java.util.List;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.core.IModelProviderEvent;
import org.eclipse.pde.core.IModelProviderListener;
import org.eclipse.pde.core.plugin.*;
import org.eclipse.pde.internal.core.ClasspathUtilCore;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.util.SWTUtil;
import org.eclipse.pde.internal.ui.wizards.ListUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * The first page of the InternationalizeWizard. This page allows the user to
 * select the desired plug-ins for internationalization. These could be plug-ins
 * in the user's workspace or external ones.
 */
public class InternationalizeWizardPluginPage extends InternationalizationWizardPage implements IModelProviderListener {

    //$NON-NLS-1$
    private static final String CREATE_INDIVIDUAL_FRAGMENTS = "create individual fragments";

    //$NON-NLS-1$
    private static final String TEMPLATE = "name_template";

    //$NON-NLS-1$
    private static final String OVERWRITE = "overwrite?";

    //$NON-NLS-1$
    public static final String PAGE_NAME = "InternationalizeWizardPluginPage";

    protected IPluginModelBase[] fModels = new IPluginModelBase[0];

    private boolean fRefreshNeeded = true;

    //Displays "x out of y selected"
    private Label fCountLabel;

    //All available plug-ins
    private TableViewer fAvailableViewer;

    //Selected plug-ins
    protected TableViewer fSelectedViewer;

    private WorkbenchJob fFilterJob;

    private Text fFilterText;

    private Text fTemplateText;

    private AvailableFilter fFilter;

    // Used to track the selection in a HashMap so as to filter
    // selected items out of the available item list
    private final Map<Object, ?> fSelected = new HashMap();

    // Used to block the selection listeners from updating button enablement
    // when programatically removing items
    private boolean fBlockSelectionListeners;

    private Button fAddButton;

    private Button fAddAllButton;

    private Button fRemoveButton;

    private Button fRemoveAllButton;

    // Used to store the plug-ins
    private InternationalizeModelTable fInternationalizeModelTable;

    private Button overwriteOption;

    private Button individualFragments;

    private class ContentProvider implements IStructuredContentProvider {

        /**
		 * @return the list of available non-selected plug-ins
		 */
        @Override
        public Object[] getElements(Object parent) {
            return fInternationalizeModelTable.getModels();
        }
    }

    private class SelectedContentProvider implements IStructuredContentProvider {

        /**
		 * @return the list of selected plug-ins
		 */
        @Override
        public Object[] getElements(Object parent) {
            return fInternationalizeModelTable.getPreSelected();
        }
    }

    public  InternationalizeWizardPluginPage(InternationalizeModelTable modelTable, String pageName) {
        super(pageName);
        setTitle(PDEUIMessages.InternationalizeWizard_PluginPage_pageTitle);
        setDescription(PDEUIMessages.InternationalizeWizard_PluginPage_pageDescription);
        PDEPlugin.getDefault().getLabelProvider().connect(this);
        PDECore.getDefault().getModelManager().getExternalModelManager().addModelProviderListener(this);
        fInternationalizeModelTable = modelTable;
        IWizardContainer container = getContainer();
        if (container != null)
            container.updateButtons();
    }

    /**
	 * Adds a filter to the list of available plug-ins
	 */
    private void addFilter() {
        fFilter = new AvailableFilter(fSelected, PDEPlugin.getDefault().getLabelProvider());
        fAvailableViewer.addFilter(fFilter);
        fFilterJob = new //$NON-NLS-1$
        WorkbenchJob(//$NON-NLS-1$
        "FilterJob") {

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                handleFilter();
                return Status.OK_STATUS;
            }
        };
        fFilterJob.setSystem(true);
    }

    /**
	 * Handles changes to the list based on changes to the text field.
	 */
    private void handleFilter() {
        boolean changed = false;
        String newFilter;
        if (fFilterText == null || (newFilter = fFilterText.getText().trim()).length() == 0)
            newFilter = AvailableFilter.WILDCARD;
        changed = fFilter.setPattern(newFilter);
        if (changed) {
            fAvailableViewer.getTable().setRedraw(false);
            fAvailableViewer.refresh();
            fAvailableViewer.getTable().setRedraw(true);
            updateButtonEnablement(false, false);
        }
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.makeColumnsEqualWidth = false;
        layout.horizontalSpacing = 5;
        layout.verticalSpacing = 20;
        container.setLayout(layout);
        createScrollArea(container);
        createAvailableList(container).setLayoutData(new GridData(GridData.FILL_BOTH));
        createButtonArea(container);
        createInternationalizeList(container).setLayoutData(new GridData(GridData.FILL_BOTH));
        updateCount();
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        Composite comp = new Composite(container, SWT.NONE);
        comp.setLayoutData(data);
        GridLayout fl = new GridLayout(2, false);
        comp.setLayout(fl);
        IDialogSettings settings = getDialogSettings();
        String template = settings.get(TEMPLATE);
        Label label = new Label(comp, SWT.NONE);
        label.setText(PDEUIMessages.InternationalizeWizard_PluginPage_templateLabel);
        fTemplateText = new Text(comp, SWT.BORDER);
        fTemplateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        //$NON-NLS-1$
        fTemplateText.setText(template != null ? template : NLSFragmentGenerator.PLUGIN_NAME_MACRO + ".nl1");
        fTemplateText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                pageChanged();
            }
        });
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        overwriteOption = new Button(comp, SWT.CHECK);
        overwriteOption.setText(PDEUIMessages.InternationalizeWizard_PluginPage_overwriteWithoutAsking);
        overwriteOption.setSelection(settings.getBoolean(OVERWRITE));
        overwriteOption.setLayoutData(gd);
        individualFragments = new Button(comp, SWT.CHECK);
        individualFragments.setText(PDEUIMessages.InternationalizeWizard_PluginPage_individualFragments);
        individualFragments.setSelection(settings.getBoolean(CREATE_INDIVIDUAL_FRAGMENTS));
        individualFragments.setLayoutData(gd);
        addViewerListeners();
        addFilter();
        initialize();
        setControl(container);
        Dialog.applyDialogFont(container);
    }

    /**
	 * @param parent
	 * @return the container holding the available plug-ins list
	 */
    private Composite createAvailableList(Composite parent) {
        Composite container = createViewerContainer(parent, PDEUIMessages.InternationalizeWizard_PluginPage_availableList);
        fAvailableViewer = createTableViewer(container, new ContentProvider(), PDECore.getDefault().getModelManager());
        return container;
    }

    protected Composite createInternationalizeList(Composite parent) {
        Composite container = createViewerContainer(parent, PDEUIMessages.InternationalizeWizard_PluginPage_internationalizeList);
        fSelectedViewer = createTableViewer(container, new SelectedContentProvider(), PDECore.getDefault().getModelManager().getExternalModelManager());
        return container;
    }

    private Composite createViewerContainer(Composite parent, String message) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        container.setLayout(layout);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        Label label = new Label(container, SWT.NONE);
        label.setText(message);
        return container;
    }

    private static TableViewer createTableViewer(Composite container, IContentProvider provider, Object manager) {
        Table table = new Table(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 200;
        gd.widthHint = 225;
        table.setLayoutData(gd);
        TableViewer viewer = new TableViewer(table);
        viewer.setLabelProvider(PDEPlugin.getDefault().getLabelProvider());
        viewer.setContentProvider(provider);
        viewer.setInput(manager);
        viewer.setComparator(ListUtil.PLUGIN_COMPARATOR);
        return viewer;
    }

    protected boolean isRefreshNeeded() {
        if (fRefreshNeeded) {
            fRefreshNeeded = false;
            return true;
        }
        return false;
    }

    private IPluginModelBase findModel(String id) {
        for (int i = 0; i < fModels.length; i++) {
            String modelId = fModels[i].getPluginBase().getId();
            if (modelId != null && modelId.equals(id))
                return fModels[i];
        }
        return null;
    }

    private IFragmentModel[] findFragments(IPlugin plugin) {
        ArrayList<IPluginModelBase> result = new ArrayList();
        for (int i = 0; i < fModels.length; i++) {
            if (fModels[i] instanceof IFragmentModel) {
                IFragment fragment = ((IFragmentModel) fModels[i]).getFragment();
                if (plugin.getId().equalsIgnoreCase(fragment.getPluginId())) {
                    result.add(fModels[i]);
                }
            }
        }
        return result.toArray(new IFragmentModel[result.size()]);
    }

    protected void addPluginAndDependencies(IPluginModelBase model, ArrayList<IPluginModelBase> selected, boolean addFragments) {
        boolean containsVariable = false;
        if (!selected.contains(model)) {
            selected.add(model);
            boolean hasextensibleAPI = ClasspathUtilCore.hasExtensibleAPI(model);
            if (!addFragments && !hasextensibleAPI && model instanceof IPluginModel) {
                IPluginLibrary[] libraries = model.getPluginBase().getLibraries();
                for (int i = 0; i < libraries.length; i++) {
                    if (ClasspathUtilCore.containsVariables(libraries[i].getName())) {
                        containsVariable = true;
                        break;
                    }
                }
            }
            addDependencies(model, selected, addFragments || containsVariable || hasextensibleAPI);
        }
    }

    protected void addDependencies(IPluginModelBase model, ArrayList<IPluginModelBase> selected, boolean addFragments) {
        IPluginImport[] required = model.getPluginBase().getImports();
        if (required.length > 0) {
            for (int i = 0; i < required.length; i++) {
                IPluginModelBase found = findModel(required[i].getId());
                if (found != null) {
                    addPluginAndDependencies(found, selected, addFragments);
                }
            }
        }
        if (addFragments) {
            if (model instanceof IPluginModel) {
                IFragmentModel[] fragments = findFragments(((IPluginModel) model).getPlugin());
                for (int i = 0; i < fragments.length; i++) {
                    addPluginAndDependencies(fragments[i], selected, addFragments);
                }
            } else {
                IFragment fragment = ((IFragmentModel) model).getFragment();
                IPluginModelBase found = findModel(fragment.getPluginId());
                if (found != null) {
                    addPluginAndDependencies(found, selected, addFragments);
                }
            }
        }
    }

    public List<Object> getModelsToInternationalize() {
        TableItem[] items = fSelectedViewer.getTable().getItems();
        List<Object> result = new ArrayList();
        for (int i = 0; i < items.length; i++) {
            result.add(items[i].getData());
        }
        return result;
    }

    public void storeSettings() {
        IDialogSettings settings = getDialogSettings();
        settings.put(OVERWRITE, overwriteWithoutAsking());
        settings.put(TEMPLATE, getTemplate());
        settings.put(CREATE_INDIVIDUAL_FRAGMENTS, createIndividualFragments());
    }

    public boolean createIndividualFragments() {
        return individualFragments.getSelection();
    }

    @Override
    public void modelsChanged(IModelProviderEvent event) {
        fRefreshNeeded = true;
    }

    private void initialize() {
        updateButtonEnablement(true, true);
        setPageComplete(false);
    }

    private void addViewerListeners() {
        fAvailableViewer.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {
                handleAdd();
            }
        });
        fSelectedViewer.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {
                handleRemove();
            }
        });
        fAvailableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (!fBlockSelectionListeners)
                    updateSelectionBasedEnablement(event.getSelection(), true);
            }
        });
        fSelectedViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (!fBlockSelectionListeners)
                    updateSelectionBasedEnablement(event.getSelection(), false);
            }
        });
        fFilterText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                fFilterJob.cancel();
                fFilterJob.schedule(200);
            }
        });
    }

    private Composite createButtonArea(Composite parent) {
        ScrolledComposite comp = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
        GridLayout layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = 0;
        comp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        Composite container = new Composite(comp, SWT.NONE);
        layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginTop = 50;
        container.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.verticalIndent = 15;
        container.setLayoutData(gd);
        fAddButton = new Button(container, SWT.PUSH);
        fAddButton.setText(PDEUIMessages.ImportWizard_DetailedPage_add);
        fAddButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fAddButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleAdd();
            }
        });
        SWTUtil.setButtonDimensionHint(fAddButton);
        fAddAllButton = new Button(container, SWT.PUSH);
        fAddAllButton.setText(PDEUIMessages.ImportWizard_DetailedPage_addAll);
        fAddAllButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fAddAllButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleAddAll();
            }
        });
        SWTUtil.setButtonDimensionHint(fAddAllButton);
        fRemoveButton = new Button(container, SWT.PUSH);
        fRemoveButton.setText(PDEUIMessages.ImportWizard_DetailedPage_remove);
        fRemoveButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fRemoveButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleRemove();
            }
        });
        SWTUtil.setButtonDimensionHint(fRemoveButton);
        fRemoveAllButton = new Button(container, SWT.PUSH);
        fRemoveAllButton.setText(PDEUIMessages.ImportWizard_DetailedPage_removeAll);
        fRemoveAllButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fRemoveAllButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleRemoveAll();
            }
        });
        SWTUtil.setButtonDimensionHint(fRemoveAllButton);
        fCountLabel = new Label(container, SWT.NONE);
        fCountLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        comp.setContent(container);
        comp.setMinHeight(250);
        comp.setExpandHorizontal(true);
        comp.setExpandVertical(true);
        return container;
    }

    private Composite createScrollArea(Composite parent) {
        Group container = createFilterContainer(parent, PDEUIMessages.InternationalizeWizard_PluginPage_filter, PDEUIMessages.ImportWizard_DetailedPage_search);
        //$NON-NLS-1$
        fFilterText = createFilterText(container, "");
        return container;
    }

    protected void refreshPage() {
        fSelectedViewer.getTable().removeAll();
        fSelected.clear();
        fAvailableViewer.refresh();
        pageChanged();
    }

    protected void pageChanged() {
        pageChanged(false, false);
    }

    protected void pageChanged(boolean doAddEnablement, boolean doRemoveEnablement) {
        if (fTemplateText.getText().length() == 0) {
            setErrorMessage(PDEUIMessages.InternationalizeWizard_PluginPage_templateError);
        } else if (fSelectedViewer.getTable().getItemCount() == 0) {
            setErrorMessage(PDEUIMessages.InternationalizeWizard_PluginPage_selectionError);
        } else {
            setErrorMessage(null);
        }
        updateCount();
        updateButtonEnablement(doAddEnablement, doRemoveEnablement);
        setPageComplete(fSelectedViewer.getTable().getItemCount() > 0);
    }

    private void updateCount() {
        fCountLabel.setText(NLS.bind(PDEUIMessages.ImportWizard_DetailedPage_count, (new String[] { Integer.valueOf(fSelectedViewer.getTable().getItemCount()).toString(), Integer.valueOf(fAvailableViewer.getTable().getItemCount() + fSelectedViewer.getTable().getItemCount()).toString() })));
        fCountLabel.getParent().layout();
    }

    private void updateButtonEnablement(boolean doAddEnablement, boolean doRemoveEnablement) {
        int availableCount = fAvailableViewer.getTable().getItemCount();
        int importCount = fSelectedViewer.getTable().getItemCount();
        if (doAddEnablement)
            updateSelectionBasedEnablement(fAvailableViewer.getSelection(), true);
        if (doRemoveEnablement)
            updateSelectionBasedEnablement(fSelectedViewer.getSelection(), false);
        fAddAllButton.setEnabled(availableCount > 0);
        fRemoveAllButton.setEnabled(importCount > 0);
    }

    private void updateSelectionBasedEnablement(ISelection theSelection, boolean available) {
        if (available)
            fAddButton.setEnabled(!theSelection.isEmpty());
        else
            fRemoveButton.setEnabled(!theSelection.isEmpty());
    }

    private void handleAdd() {
        IStructuredSelection ssel = (IStructuredSelection) fAvailableViewer.getSelection();
        if (ssel.size() > 0) {
            Table table = fAvailableViewer.getTable();
            int index = table.getSelectionIndices()[0];
            Object[] selection = ssel.toArray();
            setBlockSelectionListeners(true);
            setRedraw(false);
            for (int i = 0; i < selection.length; i++) {
                doAdd(selection[i]);
            }
            setRedraw(true);
            setBlockSelectionListeners(false);
            table.setSelection(index < table.getItemCount() ? index : table.getItemCount() - 1);
            pageChanged(true, false);
        }
    }

    private void handleAddAll() {
        TableItem[] items = fAvailableViewer.getTable().getItems();
        ArrayList<Object> data = new ArrayList();
        for (int i = 0; i < items.length; i++) {
            data.add(items[i].getData());
        }
        if (data.size() > 0) {
            Object[] datas = data.toArray();
            setBlockSelectionListeners(true);
            setRedraw(false);
            for (int i = 0; i < datas.length; i++) {
                doAdd(datas[i]);
            }
            setRedraw(true);
            setBlockSelectionListeners(false);
            pageChanged(true, false);
        }
    }

    private void handleRemove() {
        IStructuredSelection ssel = (IStructuredSelection) fSelectedViewer.getSelection();
        if (ssel.size() > 0) {
            Table table = fSelectedViewer.getTable();
            int index = table.getSelectionIndices()[0];
            Object[] selection = ssel.toArray();
            setBlockSelectionListeners(true);
            setRedraw(false);
            for (int i = 0; i < selection.length; i++) {
                doRemove(selection[i]);
            }
            setRedraw(true);
            setBlockSelectionListeners(false);
            table.setSelection(index < table.getItemCount() ? index : table.getItemCount() - 1);
            pageChanged(false, true);
        }
    }

    private void doAdd(Object o) {
        fInternationalizeModelTable.removeModel(o);
        fSelectedViewer.add(o);
        fAvailableViewer.remove(o);
        fSelected.put(o, null);
    }

    private void doRemove(Object o) {
        fInternationalizeModelTable.addModel(o);
        fSelected.remove(o);
        fSelectedViewer.remove(o);
        fAvailableViewer.add(o);
    }

    // used to prevent flicker during operations that move items between lists
    private void setRedraw(boolean redraw) {
        fAvailableViewer.getTable().setRedraw(redraw);
        fSelectedViewer.getTable().setRedraw(redraw);
    }

    private void handleRemoveAll() {
        TableItem[] items = fSelectedViewer.getTable().getItems();
        ArrayList<Object> data = new ArrayList();
        for (int i = 0; i < items.length; i++) {
            data.add(items[i].getData());
        }
        if (data.size() > 0) {
            Object[] datas = data.toArray();
            setBlockSelectionListeners(true);
            setRedraw(false);
            for (int i = 0; i < datas.length; i++) {
                doRemove(datas[i]);
            }
            setRedraw(true);
            setBlockSelectionListeners(false);
            pageChanged(false, true);
        }
    }

    @Override
    public void dispose() {
        PDEPlugin.getDefault().getLabelProvider().disconnect(this);
        PDECore.getDefault().getModelManager().getExternalModelManager().removeModelProviderListener(this);
    }

    private void setBlockSelectionListeners(boolean blockSelectionListeners) {
        fBlockSelectionListeners = blockSelectionListeners;
    }

    @Override
    public boolean isCurrentPage() {
        return super.isCurrentPage();
    }

    @Override
    public boolean canFlipToNextPage() {
        if (fSelectedViewer.getTable().getItems().length > 0 && getTemplate().length() > 0) {
            return true;
        }
        return false;
    }

    public String getTemplate() {
        return fTemplateText.getText();
    }

    public boolean overwriteWithoutAsking() {
        return overwriteOption.getSelection();
    }
}
