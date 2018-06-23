/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Code 9 Corporation - ongoing enhancements
 *     Benjamin Cabe <benjamin.cabe@anyware-tech.com> - bug 262885
 *     Alena Laskavaia - Bug 453392 - No debug options help
 *******************************************************************************/
package org.eclipse.pde.internal.ui.launcher;

import java.util.*;
import java.util.Map.Entry;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.*;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.TracingOptionsManager;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.util.SWTUtil;
import org.eclipse.pde.internal.ui.wizards.ListUtil;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.pde.ui.launcher.AbstractLauncherTab;
import org.eclipse.pde.ui.launcher.TracingTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;

public class TracingBlock {

    private TracingTab fTab;

    private Button fTracingCheck;

    private CheckboxTableViewer fPluginViewer;

    private IPluginModelBase[] fTraceableModels;

    private Properties fMasterOptions = new Properties();

    private Button fSelectAllButton;

    private Button fDeselectAllButton;

    private Hashtable<IPluginModelBase, TracingPropertySource> fPropertySources = new Hashtable();

    private FormToolkit fToolkit;

    private ScrolledPageBook fPageBook;

    /**
	 * The last selected item in the list is stored in the dialog settings.
	 */
    //$NON-NLS-1$
    private static final String TRACING_SETTINGS = "TracingTab";

    //$NON-NLS-1$
    private static final String SETTINGS_SELECTED_PLUGIN = "selectedPlugin";

    public  TracingBlock(TracingTab tab) {
        fTab = tab;
    }

    public AbstractLauncherTab getTab() {
        return fTab;
    }

    public void createControl(Composite parent) {
        fTracingCheck = new Button(parent, SWT.CHECK);
        fTracingCheck.setText(PDEUIMessages.TracingLauncherTab_tracing);
        fTracingCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fTracingCheck.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                masterCheckChanged(true);
                fTab.updateLaunchConfigurationDialog();
                if (fTracingCheck.getSelection()) {
                    IStructuredSelection selection = (IStructuredSelection) fPluginViewer.getSelection();
                    if (!selection.isEmpty()) {
                        pluginSelected((IPluginModelBase) selection.getFirstElement(), fPluginViewer.getChecked(selection.getFirstElement()));
                    }
                }
            }
        });
        createSashSection(parent);
        createButtonSection(parent);
    }

    private void createSashSection(Composite container) {
        SashForm sashForm = new SashForm(container, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        createPluginViewer(sashForm);
        createPropertySheetClient(sashForm);
    }

    private void createPluginViewer(Composite sashForm) {
        Composite composite = new Composite(sashForm, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = 1;
        composite.setLayout(layout);
        fPluginViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
        fPluginViewer.setContentProvider(ArrayContentProvider.getInstance());
        fPluginViewer.setLabelProvider(PDEPlugin.getDefault().getLabelProvider());
        fPluginViewer.setComparator(new ListUtil.PluginComparator());
        fPluginViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent e) {
                CheckboxTableViewer tableViewer = (CheckboxTableViewer) e.getSource();
                boolean selected = tableViewer.getChecked(getSelectedModel());
                pluginSelected(getSelectedModel(), selected);
                storeSelectedModel();
            }
        });
        fPluginViewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                CheckboxTableViewer tableViewer = (CheckboxTableViewer) event.getSource();
                tableViewer.setSelection(new StructuredSelection(event.getElement()));
                pluginSelected(getSelectedModel(), event.getChecked());
                fTab.updateLaunchConfigurationDialog();
            }
        });
        fPluginViewer.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {
                CheckboxTableViewer tableViewer = (CheckboxTableViewer) event.getSource();
                Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
                boolean addingCheck = !tableViewer.getChecked(selection);
                tableViewer.setChecked(selection, addingCheck);
                pluginSelected(getSelectedModel(), addingCheck);
                fTab.updateLaunchConfigurationDialog();
            }
        });
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 125;
        gd.heightHint = 100;
        fPluginViewer.getTable().setLayoutData(gd);
    }

    private void createPropertySheetClient(Composite sashForm) {
        Composite tableChild = new Composite(sashForm, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.marginHeight = layout.marginWidth = 0;
        tableChild.setLayout(layout);
        int margin = createPropertySheet(tableChild);
        layout.marginWidth = layout.marginHeight = margin;
    }

    private void createButtonSection(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        container.setLayout(layout);
        fSelectAllButton = new Button(container, SWT.PUSH);
        fSelectAllButton.setText(PDEUIMessages.TracingLauncherTab_selectAll);
        fSelectAllButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        SWTUtil.setButtonDimensionHint(fSelectAllButton);
        fSelectAllButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                fPluginViewer.setAllChecked(true);
                pluginSelected(getSelectedModel(), true);
                fTab.updateLaunchConfigurationDialog();
            }
        });
        fDeselectAllButton = new Button(container, SWT.PUSH);
        fDeselectAllButton.setText(PDEUIMessages.TracinglauncherTab_deselectAll);
        fDeselectAllButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        SWTUtil.setButtonDimensionHint(fDeselectAllButton);
        fDeselectAllButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                fPluginViewer.setAllChecked(false);
                pluginSelected(getSelectedModel(), false);
                fTab.updateLaunchConfigurationDialog();
            }
        });
    }

    protected int createPropertySheet(Composite parent) {
        fToolkit = new FormToolkit(parent.getDisplay());
        int toolkitBorderStyle = fToolkit.getBorderStyle();
        int style = toolkitBorderStyle == SWT.BORDER ? SWT.NULL : SWT.BORDER;
        Composite container = new Composite(parent, style);
        FillLayout flayout = new FillLayout();
        flayout.marginWidth = 1;
        flayout.marginHeight = 1;
        container.setLayout(flayout);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        fPageBook = new ScrolledPageBook(container, style | SWT.V_SCROLL | SWT.H_SCROLL);
        fToolkit.adapt(fPageBook, false, false);
        if (style == SWT.NULL) {
            fPageBook.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
            fToolkit.paintBordersFor(container);
        }
        return style == SWT.NULL ? 2 : 0;
    }

    public void initializeFrom(ILaunchConfiguration config) {
        fMasterOptions.clear();
        disposePropertySources();
        try {
            fTracingCheck.setSelection(config.getAttribute(IPDELauncherConstants.TRACING, false));
            Map<String, String> options = config.getAttribute(IPDELauncherConstants.TRACING_OPTIONS, (Map<String, String>) null);
            if (options == null) {
                fMasterOptions.putAll(PDECore.getDefault().getTracingOptionsManager().getTracingTemplateCopy());
            } else {
                fMasterOptions.putAll(PDECore.getDefault().getTracingOptionsManager().getTracingOptions(options));
            }
            masterCheckChanged(false);
            String checked = config.getAttribute(IPDELauncherConstants.TRACING_CHECKED, (String) null);
            if (checked == null) {
                fPluginViewer.setAllChecked(true);
            } else if (checked.equals(IPDELauncherConstants.TRACING_NONE)) {
                fPluginViewer.setAllChecked(false);
            } else {
                StringTokenizer tokenizer = new //$NON-NLS-1$
                StringTokenizer(//$NON-NLS-1$
                checked, //$NON-NLS-1$
                ",");
                ArrayList<IPluginModelBase> list = new ArrayList();
                while (tokenizer.hasMoreTokens()) {
                    String id = tokenizer.nextToken();
                    IPluginModelBase model = PluginRegistry.findModel(id);
                    model = PluginRegistry.findModel(id);
                    if (model != null) {
                        list.add(model);
                    }
                }
                fPluginViewer.setCheckedElements(list.toArray());
                IPluginModelBase model = getLastSelectedPlugin();
                if (model == null && !list.isEmpty()) {
                    model = list.get(0);
                }
                if (model != null) {
                    fPluginViewer.setSelection(new StructuredSelection(model), true);
                    if (fTracingCheck.getSelection()) {
                        pluginSelected(model, list.contains(model));
                    }
                } else {
                    pluginSelected(null, false);
                }
            }
        } catch (CoreException e) {
            PDEPlugin.logException(e);
        }
    }

    public void performApply(ILaunchConfigurationWorkingCopy config) {
        boolean tracingEnabled = fTracingCheck.getSelection();
        config.setAttribute(IPDELauncherConstants.TRACING, tracingEnabled);
        if (tracingEnabled) {
            boolean changes = false;
            for (Enumeration<TracingPropertySource> elements = fPropertySources.elements(); elements.hasMoreElements(); ) {
                TracingPropertySource source = elements.nextElement();
                if (source.isModified()) {
                    changes = true;
                    source.save();
                }
            }
            if (changes) {
                HashMap<String, String> atts = new HashMap(fMasterOptions.size());
                for (Entry<Object, Object> entry : fMasterOptions.entrySet()) {
                    String key = (String) entry.getKey();
                    // these are comment keys which we don't want to save
                    if (!//$NON-NLS-1$
                    key.startsWith(//$NON-NLS-1$
                    "#"))
                        atts.put(key, (String) entry.getValue());
                }
                config.setAttribute(IPDELauncherConstants.TRACING_OPTIONS, atts);
            }
        }
        Object[] checked = fPluginViewer.getCheckedElements();
        if (checked.length == 0) {
            config.setAttribute(IPDELauncherConstants.TRACING_CHECKED, IPDELauncherConstants.TRACING_NONE);
        } else if (checked.length == fPluginViewer.getTable().getItemCount()) {
            config.setAttribute(IPDELauncherConstants.TRACING_CHECKED, (String) null);
        } else {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < checked.length; i++) {
                IPluginModelBase model = (IPluginModelBase) checked[i];
                buffer.append(model.getPluginBase().getId());
                if (i < checked.length - 1)
                    buffer.append(',');
            }
            config.setAttribute(IPDELauncherConstants.TRACING_CHECKED, buffer.toString());
        }
    }

    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(IPDELauncherConstants.TRACING, false);
        configuration.setAttribute(IPDELauncherConstants.TRACING_CHECKED, IPDELauncherConstants.TRACING_NONE);
    }

    public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
        fPageBook.getParent().getParent().layout(true);
    }

    public void dispose() {
        if (fToolkit != null)
            fToolkit.dispose();
    }

    public FormToolkit getToolkit() {
        return fToolkit;
    }

    private IPluginModelBase getSelectedModel() {
        if (fTracingCheck.isEnabled()) {
            Object item = ((IStructuredSelection) fPluginViewer.getSelection()).getFirstElement();
            if (item instanceof IPluginModelBase)
                return ((IPluginModelBase) item);
        }
        return null;
    }

    private void pluginSelected(IPluginModelBase model, boolean checked) {
        TracingPropertySource source = getPropertySource(model);
        if (source == null) {
            fPageBook.showEmptyPage();
        } else {
            PageBookKey key = new PageBookKey(model, checked);
            if (!fPageBook.hasPage(key)) {
                Composite parent = fPageBook.createPage(key);
                source.createContents(parent, checked);
            }
            fPageBook.showPage(key);
        }
    }

    private IPluginModelBase[] getTraceableModels() {
        if (fTraceableModels == null) {
            IPluginModelBase[] models = PluginRegistry.getActiveModels();
            ArrayList<IPluginModelBase> result = new ArrayList();
            for (int i = 0; i < models.length; i++) {
                if (TracingOptionsManager.isTraceable(models[i]))
                    result.add(models[i]);
            }
            fTraceableModels = result.toArray(new IPluginModelBase[result.size()]);
        }
        return fTraceableModels;
    }

    /**
	 * Returns the last selected plug-in as stored in dialog settings or <code>null</code> if no
	 * previous selection is found.
	 *
	 * @return model for the last selected plug-in or <code>null</code>
	 */
    private IPluginModelBase getLastSelectedPlugin() {
        IDialogSettings settings = PDEPlugin.getDefault().getDialogSettings().getSection(TRACING_SETTINGS);
        if (settings != null) {
            String id = settings.get(SETTINGS_SELECTED_PLUGIN);
            if (id != null && id.trim().length() > 0) {
                return PluginRegistry.findModel(id);
            }
        }
        return null;
    }

    /**
	 * Stores the currently selected model in the dialog settings for later retrieval using
	 * {@link #getLastSelectedPlugin()}.  If no model is selected, the settings are cleared.
	 */
    private void storeSelectedModel() {
        IDialogSettings settings = PDEPlugin.getDefault().getDialogSettings().getSection(TRACING_SETTINGS);
        if (settings == null) {
            settings = PDEPlugin.getDefault().getDialogSettings().addNewSection(TRACING_SETTINGS);
        }
        IPluginModelBase model = getSelectedModel();
        if (model != null && fPluginViewer.getChecked(model)) {
            settings.put(SETTINGS_SELECTED_PLUGIN, model.getPluginBase().getId());
        } else {
            settings.put(SETTINGS_SELECTED_PLUGIN, (String) null);
        }
    }

    private TracingPropertySource getPropertySource(IPluginModelBase model) {
        if (model == null)
            return null;
        TracingPropertySource source = fPropertySources.get(model);
        if (source == null) {
            String id = model.getPluginBase().getId();
            Hashtable<?, ?> defaults = PDECore.getDefault().getTracingOptionsManager().getTemplateTable(id);
            source = new TracingPropertySource(model, fMasterOptions, defaults, this);
            fPropertySources.put(model, source);
        }
        return source;
    }

    private void masterCheckChanged(boolean userChange) {
        boolean enabled = fTracingCheck.getSelection();
        fPluginViewer.getTable().setEnabled(enabled);
        Control currentPage = fPageBook.getCurrentPage();
        if (currentPage != null && enabled == false) {
            fPageBook.showEmptyPage();
        }
        if (enabled) {
            fPluginViewer.setInput(getTraceableModels());
        }
        fSelectAllButton.setEnabled(enabled);
        fDeselectAllButton.setEnabled(enabled);
    }

    private void disposePropertySources() {
        Enumeration<TracingPropertySource> elements = fPropertySources.elements();
        while (elements.hasMoreElements()) {
            TracingPropertySource source = elements.nextElement();
            fPageBook.removePage(source.getModel());
        }
        fPropertySources.clear();
    }

    private class PageBookKey {

        IPluginModelBase fModel;

        boolean fEnabled;

         PageBookKey(IPluginModelBase model, boolean enabled) {
            fModel = model;
            fEnabled = enabled;
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof PageBookKey) {
                return fEnabled == ((PageBookKey) object).fEnabled && fModel.equals(((PageBookKey) object).fModel);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return fModel.hashCode() + (fEnabled ? 1 : 0);
        }
    }
}
