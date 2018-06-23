/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jesper Steen Moller - Enhancement 254677 - filter getters/setters
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import java.util.ArrayList;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * The preference page for Java step filtering, located at the node Java > Debug > Step Filtering
 * 
 * @since 3.0
 */
public class JavaStepFilterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    //$NON-NLS-1$
    public static final String PAGE_ID = "org.eclipse.jdt.debug.ui.JavaStepFilterPreferencePage";

    /**
	 * Content provider for the table.  Content consists of instances of StepFilter.
	 * @since 3.2
	 */
    class StepFilterContentProvider implements IStructuredContentProvider {

        public  StepFilterContentProvider() {
            initTableState(false);
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return getAllFiltersFromTable();
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public void dispose() {
        }
    }

    //widgets
    private CheckboxTableViewer fTableViewer;

    private Button fUseStepFiltersButton;

    private Button fAddPackageButton;

    private Button fAddTypeButton;

    private Button fRemoveFilterButton;

    private Button fAddFilterButton;

    private Button fFilterSyntheticButton;

    private Button fFilterStaticButton;

    private Button fFilterGetterButton;

    private Button fFilterSetterButton;

    private Button fFilterConstructorButton;

    private Button fStepThruButton;

    private Button fSelectAllButton;

    private Button fDeselectAllButton;

    /**
	 * Constructor
	 */
    public  JavaStepFilterPreferencePage() {
        super();
        setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
        setTitle(DebugUIMessages.JavaStepFilterPreferencePage_title);
        setDescription(DebugUIMessages.JavaStepFilterPreferencePage_description);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
    @Override
    protected Control createContents(Composite parent) {
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_STEP_FILTER_PREFERENCE_PAGE);
        //The main composite
        Composite composite = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH, 0, 0);
        createStepFilterPreferences(composite);
        return composite;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
    @Override
    public void init(IWorkbench workbench) {
    }

    /**
	 * handles the filter button being clicked
	 * @param event the clicked event
	 */
    private void handleFilterViewerKeyPress(KeyEvent event) {
        if (event.character == SWT.DEL && event.stateMask == 0) {
            removeFilters();
        }
    }

    /**
	 * Create a group to contain the step filter related widgetry
	 */
    private void createStepFilterPreferences(Composite parent) {
        Composite container = SWTFactory.createComposite(parent, parent.getFont(), 2, 1, GridData.FILL_BOTH, 0, 0);
        fUseStepFiltersButton = SWTFactory.createCheckButton(container, DebugUIMessages.JavaStepFilterPreferencePage__Use_step_filters, null, DebugUITools.isUseStepFilters(), 2);
        fUseStepFiltersButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setPageEnablement(fUseStepFiltersButton.getSelection());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        SWTFactory.createLabel(container, DebugUIMessages.JavaStepFilterPreferencePage_Defined_step_fi_lters__8, 2);
        fTableViewer = CheckboxTableViewer.newCheckList(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        fTableViewer.getTable().setFont(container.getFont());
        fTableViewer.setLabelProvider(new FilterLabelProvider());
        fTableViewer.setComparator(new FilterViewerComparator());
        fTableViewer.setContentProvider(new StepFilterContentProvider());
        fTableViewer.setInput(getAllStoredFilters(false));
        fTableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
        fTableViewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                ((Filter) event.getElement()).setChecked(event.getChecked());
            }
        });
        fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                if (selection.isEmpty()) {
                    fRemoveFilterButton.setEnabled(false);
                } else {
                    fRemoveFilterButton.setEnabled(true);
                }
            }
        });
        fTableViewer.getControl().addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent event) {
                handleFilterViewerKeyPress(event);
            }
        });
        createStepFilterButtons(container);
        createStepFilterCheckboxes(container);
        setPageEnablement(fUseStepFiltersButton.getSelection());
    }

    /**
	 * initializes the checked state of the filters when the dialog opens
	 * @since 3.2
	 */
    private void initTableState(boolean defaults) {
        Filter[] filters = getAllStoredFilters(defaults);
        for (int i = 0; i < filters.length; i++) {
            fTableViewer.add(filters[i]);
            fTableViewer.setChecked(filters[i], filters[i].isChecked());
        }
    }

    /**
	 * Enables or disables the widgets on the page, with the 
	 * exception of <code>fUseStepFiltersButton</code> according
	 * to the passed boolean
	 * @param enabled the new enablement status of the page's widgets
	 * @since 3.2
	 */
    protected void setPageEnablement(boolean enabled) {
        fAddFilterButton.setEnabled(enabled);
        fAddPackageButton.setEnabled(enabled);
        fAddTypeButton.setEnabled(enabled);
        fDeselectAllButton.setEnabled(enabled);
        fSelectAllButton.setEnabled(enabled);
        fFilterConstructorButton.setEnabled(enabled);
        fStepThruButton.setEnabled(enabled);
        fFilterGetterButton.setEnabled(enabled);
        fFilterSetterButton.setEnabled(enabled);
        fFilterStaticButton.setEnabled(enabled);
        fFilterSyntheticButton.setEnabled(enabled);
        fTableViewer.getTable().setEnabled(enabled);
        fRemoveFilterButton.setEnabled(enabled & !fTableViewer.getSelection().isEmpty());
    }

    /**
	 * create the checked preferences for the page
	 * @param container the parent container
	 */
    private void createStepFilterCheckboxes(Composite container) {
        fFilterSyntheticButton = SWTFactory.createCheckButton(container, DebugUIMessages.JavaStepFilterPreferencePage_Filter_s_ynthetic_methods__requires_VM_support__17, null, getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_FILTER_SYNTHETICS), 2);
        fFilterStaticButton = SWTFactory.createCheckButton(container, DebugUIMessages.JavaStepFilterPreferencePage_Filter_static__initializers_18, null, getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_FILTER_STATIC_INITIALIZERS), 2);
        fFilterConstructorButton = SWTFactory.createCheckButton(container, DebugUIMessages.JavaStepFilterPreferencePage_Filter_co_nstructors_19, null, getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_FILTER_CONSTRUCTORS), 2);
        fFilterGetterButton = SWTFactory.createCheckButton(container, DebugUIMessages.JavaStepFilterPreferencePage_Filter_getters, null, getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_FILTER_GETTERS), 2);
        fFilterSetterButton = SWTFactory.createCheckButton(container, DebugUIMessages.JavaStepFilterPreferencePage_Filter_setters, null, getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_FILTER_SETTERS), 2);
        fStepThruButton = SWTFactory.createCheckButton(container, DebugUIMessages.JavaStepFilterPreferencePage_0, null, getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_STEP_THRU_FILTERS), 2);
    }

    /**
	 * Creates the button for the step filter options
	 * @param container the parent container
	 */
    private void createStepFilterButtons(Composite container) {
        initializeDialogUnits(container);
        // button container
        Composite buttonContainer = new Composite(container, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        buttonContainer.setLayoutData(gd);
        GridLayout buttonLayout = new GridLayout();
        buttonLayout.numColumns = 1;
        buttonLayout.marginHeight = 0;
        buttonLayout.marginWidth = 0;
        buttonContainer.setLayout(buttonLayout);
        //Add filter button
        fAddFilterButton = SWTFactory.createPushButton(buttonContainer, DebugUIMessages.JavaStepFilterPreferencePage_Add__Filter_9, DebugUIMessages.JavaStepFilterPreferencePage_Key_in_the_name_of_a_new_step_filter_10, null);
        fAddFilterButton.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                addFilter();
            }
        });
        //Add type button
        fAddTypeButton = SWTFactory.createPushButton(buttonContainer, DebugUIMessages.JavaStepFilterPreferencePage_Add__Type____11, DebugUIMessages.JavaStepFilterPreferencePage_Choose_a_Java_type_and_add_it_to_step_filters_12, null);
        fAddTypeButton.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                addType();
            }
        });
        //Add package button
        fAddPackageButton = SWTFactory.createPushButton(buttonContainer, DebugUIMessages.JavaStepFilterPreferencePage_Add__Package____13, DebugUIMessages.JavaStepFilterPreferencePage_Choose_a_package_and_add_it_to_step_filters_14, null);
        fAddPackageButton.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                addPackage();
            }
        });
        //Remove button
        fRemoveFilterButton = SWTFactory.createPushButton(buttonContainer, DebugUIMessages.JavaStepFilterPreferencePage__Remove_15, DebugUIMessages.JavaStepFilterPreferencePage_Remove_all_selected_step_filters_16, null);
        fRemoveFilterButton.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                removeFilters();
            }
        });
        fRemoveFilterButton.setEnabled(false);
        Label separator = new Label(buttonContainer, SWT.NONE);
        separator.setVisible(false);
        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.BEGINNING;
        gd.heightHint = 4;
        separator.setLayoutData(gd);
        //Select All button
        fSelectAllButton = SWTFactory.createPushButton(buttonContainer, DebugUIMessages.JavaStepFilterPreferencePage__Select_All_1, DebugUIMessages.JavaStepFilterPreferencePage_Selects_all_step_filters_2, null);
        fSelectAllButton.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                fTableViewer.setAllChecked(true);
            }
        });
        //De-Select All button
        fDeselectAllButton = SWTFactory.createPushButton(buttonContainer, DebugUIMessages.JavaStepFilterPreferencePage_Deselect_All_3, DebugUIMessages.JavaStepFilterPreferencePage_Deselects_all_step_filters_4, null);
        fDeselectAllButton.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                fTableViewer.setAllChecked(false);
            }
        });
    }

    /**
	 * Allows a new filter to be added to the listing
	 */
    private void addFilter() {
        Filter newfilter = CreateStepFilterDialog.showCreateStepFilterDialog(getShell(), getAllFiltersFromTable());
        if (newfilter != null) {
            fTableViewer.add(newfilter);
            fTableViewer.setChecked(newfilter, true);
            fTableViewer.refresh(newfilter);
        }
    }

    /**
	 * add a new type to the listing of available filters 
	 */
    private void addType() {
        try {
            SelectionDialog dialog = JavaUI.createTypeDialog(getShell(), PlatformUI.getWorkbench().getProgressService(), SearchEngine.createWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_CLASSES, false);
            dialog.setTitle(DebugUIMessages.JavaStepFilterPreferencePage_Add_type_to_step_filters_20);
            dialog.setMessage(DebugUIMessages.JavaStepFilterPreferencePage_Select_a_type_to_filter_when_stepping_23);
            if (dialog.open() == IDialogConstants.OK_ID) {
                Object[] types = dialog.getResult();
                if (types != null && types.length > 0) {
                    IType type = (IType) types[0];
                    addFilter(type.getFullyQualifiedName(), true);
                }
            }
        } catch (JavaModelException jme) {
            ExceptionHandler.handle(jme, DebugUIMessages.JavaStepFilterPreferencePage_Add_type_to_step_filters_20, DebugUIMessages.JavaStepFilterPreferencePage_Could_not_open_type_selection_dialog_for_step_filters_21);
        }
    }

    /**
	 * add a new package to the list of all available package filters
	 */
    private void addPackage() {
        try {
            ElementListSelectionDialog dialog = JDIDebugUIPlugin.createAllPackagesDialog(getShell(), null, false);
            dialog.setTitle(DebugUIMessages.JavaStepFilterPreferencePage_Add_package_to_step_filters_24);
            dialog.setMessage(DebugUIMessages.JavaStepFilterPreferencePage_Select_a_package_to_filter_when_stepping_27);
            dialog.setMultipleSelection(true);
            if (dialog.open() == IDialogConstants.OK_ID) {
                Object[] packages = dialog.getResult();
                if (packages != null) {
                    IJavaElement pkg = null;
                    for (int i = 0; i < packages.length; i++) {
                        pkg = (IJavaElement) packages[i];
                        String filter = //$NON-NLS-1$
                        pkg.getElementName() + ".*";
                        addFilter(filter, true);
                    }
                }
            }
        } catch (JavaModelException jme) {
            ExceptionHandler.handle(jme, DebugUIMessages.JavaStepFilterPreferencePage_Add_package_to_step_filters_24, DebugUIMessages.JavaStepFilterPreferencePage_Could_not_open_package_selection_dialog_for_step_filters_25);
        }
    }

    /**
	 * Removes the currently selected filters.
	 */
    protected void removeFilters() {
        fTableViewer.remove(((IStructuredSelection) fTableViewer.getSelection()).toArray());
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
    @Override
    public boolean performOk() {
        DebugUITools.setUseStepFilters(fUseStepFiltersButton.getSelection());
        IPreferenceStore store = getPreferenceStore();
        ArrayList<String> active = new ArrayList<String>();
        ArrayList<String> inactive = new ArrayList<String>();
        //$NON-NLS-1$
        String name = "";
        Filter[] filters = getAllFiltersFromTable();
        for (int i = 0; i < filters.length; i++) {
            name = filters[i].getName();
            if (filters[i].isChecked()) {
                active.add(name);
            } else {
                inactive.add(name);
            }
        }
        String pref = JavaDebugOptionsManager.serializeList(active.toArray(new String[active.size()]));
        store.setValue(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, pref);
        pref = JavaDebugOptionsManager.serializeList(inactive.toArray(new String[inactive.size()]));
        store.setValue(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST, pref);
        store.setValue(IJDIPreferencesConstants.PREF_FILTER_CONSTRUCTORS, fFilterConstructorButton.getSelection());
        store.setValue(IJDIPreferencesConstants.PREF_FILTER_STATIC_INITIALIZERS, fFilterStaticButton.getSelection());
        store.setValue(IJDIPreferencesConstants.PREF_FILTER_GETTERS, fFilterGetterButton.getSelection());
        store.setValue(IJDIPreferencesConstants.PREF_FILTER_SETTERS, fFilterSetterButton.getSelection());
        store.setValue(IJDIPreferencesConstants.PREF_FILTER_SYNTHETICS, fFilterSyntheticButton.getSelection());
        store.setValue(IJDIPreferencesConstants.PREF_STEP_THRU_FILTERS, fStepThruButton.getSelection());
        return super.performOk();
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
    @Override
    protected void performDefaults() {
        boolean stepenabled = DebugUITools.isUseStepFilters();
        fUseStepFiltersButton.setSelection(stepenabled);
        setPageEnablement(stepenabled);
        fFilterSyntheticButton.setSelection(getPreferenceStore().getDefaultBoolean(IJDIPreferencesConstants.PREF_FILTER_SYNTHETICS));
        fFilterStaticButton.setSelection(getPreferenceStore().getDefaultBoolean(IJDIPreferencesConstants.PREF_FILTER_STATIC_INITIALIZERS));
        fFilterConstructorButton.setSelection(getPreferenceStore().getDefaultBoolean(IJDIPreferencesConstants.PREF_FILTER_CONSTRUCTORS));
        fFilterGetterButton.setSelection(getPreferenceStore().getDefaultBoolean(IJDIPreferencesConstants.PREF_FILTER_GETTERS));
        fFilterSetterButton.setSelection(getPreferenceStore().getDefaultBoolean(IJDIPreferencesConstants.PREF_FILTER_SETTERS));
        fStepThruButton.setSelection(getPreferenceStore().getDefaultBoolean(IJDIPreferencesConstants.PREF_STEP_THRU_FILTERS));
        fTableViewer.getTable().removeAll();
        initTableState(true);
        super.performDefaults();
    }

    /**
	 * adds a single filter to the viewer
	 * @param filter the new filter to add
	 * @param checked the checked state of the new filter
	 * @since 3.2
	 */
    protected void addFilter(String filter, boolean checked) {
        if (filter != null) {
            Filter f = new Filter(filter, checked);
            fTableViewer.add(f);
            fTableViewer.setChecked(f, checked);
        }
    }

    /**
	 * returns all of the filters from the table, this includes ones that have not yet been saved
	 * @return a possibly empty lits of filters fron the table
	 * @since 3.2
	 */
    protected Filter[] getAllFiltersFromTable() {
        TableItem[] items = fTableViewer.getTable().getItems();
        Filter[] filters = new Filter[items.length];
        for (int i = 0; i < items.length; i++) {
            filters[i] = (Filter) items[i].getData();
            filters[i].setChecked(items[i].getChecked());
        }
        return filters;
    }

    /**
	 * Returns all of the committed filters
	 * @return an array of committed filters
	 * @since 3.2
	 */
    protected Filter[] getAllStoredFilters(boolean defaults) {
        Filter[] filters = null;
        String[] activefilters, inactivefilters;
        IPreferenceStore store = getPreferenceStore();
        if (defaults) {
            activefilters = JavaDebugOptionsManager.parseList(store.getDefaultString(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST));
            inactivefilters = JavaDebugOptionsManager.parseList(store.getDefaultString(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST));
        } else {
            activefilters = JavaDebugOptionsManager.parseList(store.getString(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST));
            inactivefilters = JavaDebugOptionsManager.parseList(store.getString(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST));
        }
        filters = new Filter[activefilters.length + inactivefilters.length];
        for (int i = 0; i < activefilters.length; i++) {
            filters[i] = new Filter(activefilters[i], true);
        }
        for (int i = 0; i < inactivefilters.length; i++) {
            filters[i + activefilters.length] = new Filter(inactivefilters[i], false);
        }
        return filters;
    }
}
