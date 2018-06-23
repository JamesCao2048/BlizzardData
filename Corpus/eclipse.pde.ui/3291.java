/*******************************************************************************
 * Copyright (c) 2008, 2015 Code 9 Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Code 9 Corporation - initial API and implementation
 *     Bartosz Michalik <bartosz.michalik@gmail.com> - bug 240737
 *     Benjamin Cabe <benjamin.cabe@anyware-tech.com> - bug 265931
 *     Simon Scholz <simon.scholz@vogella.com> - bug 440275
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor.product;

import java.util.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.plugin.*;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;
import org.eclipse.pde.internal.core.iproduct.*;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.pde.internal.ui.dialogs.PluginSelectionDialog;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.pde.internal.ui.editor.TableSection;
import org.eclipse.pde.internal.ui.parts.TablePart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

public class PluginConfigurationSection extends TableSection {

    private class ContentProvider implements IStructuredContentProvider {

        private IProduct fProduct;

         ContentProvider() {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return fProduct.getPluginConfigurations();
        }

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            if (oldInput == newInput)
                return;
            fProduct = (IProduct) newInput;
        //TODO refresh
        }
    }

    private class LabelProvider extends PDELabelProvider {

        @Override
        public Image getColumnImage(Object obj, int index) {
            if (index == 0)
                return super.getColumnImage(PluginRegistry.findModel(((IPluginConfiguration) obj).getId()), index);
            return null;
        }

        @Override
        public String getColumnText(Object obj, int index) {
            IPluginConfiguration configuration = (IPluginConfiguration) obj;
            switch(index) {
                case 0:
                    return configuration.getId();
                //return super.getColumnText(PluginRegistry.findModel(configuration.getId()), index);
                case 1:
                    return (configuration.getStartLevel() == 0) ? "default" : Integer.toString(//$NON-NLS-1$
                    configuration.getStartLevel());
                case 2:
                    return Boolean.toString(configuration.isAutoStart());
            }
            return null;
        }
    }

    private TableViewer fConfigurationsTable;

    private TableEditor fLevelColumnEditor;

    private TableEditor fAutoColumnEditor;

    /**
	 * @param page
	 * @param parent
	 */
    public  PluginConfigurationSection(PDEFormPage page, Composite parent) {
        super(page, parent, Section.DESCRIPTION, getButtonLabels());
    }

    private static String[] getButtonLabels() {
        String[] labels = new String[4];
        labels[0] = PDEUIMessages.Product_PluginSection_add;
        labels[1] = PDEUIMessages.Product_PluginSection_recommended;
        labels[2] = PDEUIMessages.PluginSection_remove;
        labels[3] = PDEUIMessages.Product_PluginSection_removeAll;
        return labels;
    }

    /*
	 * Return a comma-separated list of bundles that typically require auto-start and optionally
	 * require a special startlevel. Each entry is of the form <bundleID>[@ [<startlevel>] [":start"]]
	 * If the startlevel is omitted then the framework will use the default start level for the bundle.
	 * The "start" tag indicates that the bundle is autostarted.
	 *
	 * This list loosely based on TargetPlatform.getBundleList and more specifically on
	 * TargetPlatformHelper.getDefaultBundleList(). Both of these implementations are
	 * problematic because they are out of date, and also leave out commonly used bundles.
	 *
	 * This list attempts to describe a typical set up on the assumption that an advanced user can
	 * further modify it. The list is hard-coded rather than walking the plugin requirements of
	 * the product and all required products. The reason for this is that there are some bundles,
	 * such as org.eclipse.equinox.ds, that are typically needed but users do not remember to
	 * add them, and they are not required by any bundle. The idea of this list is to suggest
	 * these commonly used bundles and start levels that clients typically do not remember.
	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=426529
	 *
	 * We use the same String format described in TargetPlatform so that in the future, we could
	 * obtain this list from another source that uses the same format.
	 */
    private static String getBundlesWithStartLevels() {
        StringBuffer buffer = new StringBuffer();
        //$NON-NLS-1$
        buffer.append("org.eclipse.core.runtime@start,");
        //$NON-NLS-1$
        buffer.append("org.eclipse.equinox.common@2:start,");
        //$NON-NLS-1$
        buffer.append("org.eclipse.equinox.ds@2:start,");
        //$NON-NLS-1$
        buffer.append("org.eclipse.equinox.event@2:start,");
        //$NON-NLS-1$
        buffer.append("org.eclipse.equinox.simpleconfigurator@1:start,");
        return buffer.toString();
    }

    private static final char VERSION_SEPARATOR = '*';

    @Override
    protected void createClient(Section section, FormToolkit toolkit) {
        section.setText(PDEUIMessages.ConfigurationPageMock_sectionTitle);
        section.setDescription(PDEUIMessages.ConfigurationPageMock_sectionDesc);
        GridData sectionData = new GridData(SWT.FILL, SWT.FILL, true, true);
        sectionData.horizontalSpan = 2;
        section.setLayoutData(sectionData);
        Composite container = createClientContainer(section, 3, toolkit);
        createViewerPartControl(container, SWT.SINGLE | SWT.FULL_SELECTION, 3, toolkit);
        fConfigurationsTable = getTablePart().getTableViewer();
        final Table table = fConfigurationsTable.getTable();
        final TableColumn column1 = new TableColumn(table, SWT.LEFT);
        column1.setText(PDEUIMessages.PluginConfigurationSection_tablePluginTitle);
        column1.setWidth(300);
        final TableColumn levelColumnEditor = new TableColumn(table, SWT.LEFT);
        levelColumnEditor.setText(PDEUIMessages.EquinoxPluginBlock_levelColumn);
        final TableColumn autoColumnEditor = new TableColumn(table, SWT.LEFT);
        autoColumnEditor.setText(PDEUIMessages.EquinoxPluginBlock_autoColumn);
        table.addControlListener(new ControlListener() {

            @Override
            public void controlMoved(ControlEvent e) {
            }

            @Override
            public void controlResized(ControlEvent e) {
                int size = table.getSize().x;
                column1.setWidth(size / 7 * 4);
                levelColumnEditor.setWidth(size / 7 * 2);
                autoColumnEditor.setWidth(size / 7 * 1);
            }
        });
        table.setHeaderVisible(true);
        toolkit.paintBordersFor(container);
        fConfigurationsTable.setLabelProvider(getLabelProvider());
        fConfigurationsTable.setContentProvider(new ContentProvider());
        fConfigurationsTable.setInput(getProduct());
        createEditors();
        section.setClient(container);
        getModel().addModelChangedListener(this);
        getTablePart().setButtonEnabled(0, isEditable());
        updateRemoveButtons(true, true);
    }

    @Override
    protected void buttonSelected(int index) {
        switch(index) {
            case 0:
                handleAdd();
                break;
            case 1:
                handleAddDefaults();
                break;
            case 2:
                handleRemove();
                break;
            case 3:
                handleRemoveAll();
                break;
        }
    }

    private void handleAdd() {
        Collection<IPluginModelBase> pluginModelBases = null;
        if (getProduct().useFeatures()) {
            pluginModelBases = getPluginModelBasesByFeature();
        } else {
            pluginModelBases = getPluginModelBasesByPlugin();
        }
        PluginSelectionDialog pluginSelectionDialog = new PluginSelectionDialog(PDEPlugin.getActiveWorkbenchShell(), pluginModelBases.toArray(new IPluginModelBase[pluginModelBases.size()]), true);
        if (pluginSelectionDialog.open() == Window.OK) {
            Object[] result = pluginSelectionDialog.getResult();
            for (Object object : result) {
                IPluginModelBase pluginModelBase = (IPluginModelBase) object;
                addPlugin(pluginModelBase.getPluginBase().getId());
            }
        }
    }

    private Collection<IPluginModelBase> getPluginModelBasesByFeature() {
        Collection<IPluginModelBase> pluginModelBases = new ArrayList();
        IProductFeature[] features = getProduct().getFeatures();
        for (IProductFeature feature : features) {
            FeatureModelManager manager = PDECore.getDefault().getFeatureModelManager();
            IFeatureModel fModel = manager.findFeatureModelRelaxed(feature.getId(), feature.getVersion());
            if (fModel == null) {
                fModel = manager.findFeatureModel(feature.getId());
            }
            if (fModel != null) {
                IFeaturePlugin[] fPlugins = fModel.getFeature().getPlugins();
                for (IFeaturePlugin fPlugin : fPlugins) {
                    if (!fPlugin.isFragment()) {
                        IPluginModelBase pluginModelBase = PluginRegistry.findModel(fPlugin.getId());
                        if (pluginModelBase != null) {
                            pluginModelBases.add(pluginModelBase);
                        }
                    }
                }
            }
        }
        return pluginModelBases;
    }

    private Collection<IPluginModelBase> getPluginModelBasesByPlugin() {
        Collection<IPluginModelBase> pluginModelBases = new ArrayList();
        IProductPlugin[] allPlugins = getProduct().getPlugins();
        IPluginConfiguration[] configs = getProduct().getPluginConfigurations();
        for (IProductPlugin productPlugin : allPlugins) {
            if (!pluginConfigurationContainsProductPlugin(configs, productPlugin)) {
                if (!(PluginRegistry.findModel(productPlugin.getId()) instanceof IFragmentModel)) {
                    IPluginModelBase pluginModelBase = PluginRegistry.findModel(productPlugin.getId());
                    pluginModelBases.add(pluginModelBase);
                }
            }
        }
        return pluginModelBases;
    }

    private boolean pluginConfigurationContainsProductPlugin(IPluginConfiguration[] configs, IProductPlugin productPlugin) {
        for (IPluginConfiguration pluginConfiguration : configs) {
            if (productPlugin.getId().equals(pluginConfiguration.getId())) {
                return true;
            }
        }
        return false;
    }

    private void handleRemove() {
        IStructuredSelection ssel = (IStructuredSelection) fConfigurationsTable.getSelection();
        if (ssel.size() > 0) {
            Object[] objects = ssel.toArray();
            IPluginConfiguration[] configurations = new IPluginConfiguration[objects.length];
            System.arraycopy(objects, 0, configurations, 0, objects.length);
            getProduct().removePluginConfigurations(configurations);
        }
        clearEditors();
    }

    private void handleRemoveAll() {
        IProduct product = getProduct();
        product.removePluginConfigurations(product.getPluginConfigurations());
        clearEditors();
    }

    private void handleAddDefaults() {
        //$NON-NLS-1$
        StringTokenizer tok = new StringTokenizer(getBundlesWithStartLevels(), ",");
        ArrayList<String[]> plugins = new ArrayList();
        IProduct product = getProduct();
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken();
            int index = token.indexOf('@');
            if (// there is a start level and/or autostart information
            index >= 0) {
                String idVersion = token.substring(0, index);
                int versionIndex = idVersion.indexOf(VERSION_SEPARATOR);
                String id = (versionIndex > 0) ? idVersion.substring(0, versionIndex) : idVersion;
                int endStartLevelIndex = token.indexOf(':', index);
                String //$NON-NLS-1$
                startLevel = //$NON-NLS-1$
                "";
                String autostart;
                if (// there is a start level
                endStartLevelIndex > 0) {
                    startLevel = token.substring(index + 1, endStartLevelIndex);
                    autostart = token.substring(endStartLevelIndex + 1);
                } else {
                    autostart = token.substring(index + 1);
                }
                // objects because that will change the model and we want confirmation.
                if (product.findPluginConfiguration(id) == null) {
                    plugins.add(new String[] { id, startLevel, autostart });
                }
            }
        }
        if (plugins.size() > 0) {
            // Build a user-presentable description of the plugins and start levels.
            StringBuffer bundlesList = new StringBuffer();
            bundlesList.append('\n');
            bundlesList.append('\n');
            for (int i = 0; i < plugins.size(); i++) {
                String[] config = plugins.get(i);
                bundlesList.append('\t');
                bundlesList.append(config[0]);
                //$NON-NLS-1$ // Not translated. This is bundle syntax, not a sentence
                bundlesList.append(", ");
                String startLevel = config[1];
                if (startLevel.length() > 0) {
                    bundlesList.append(PDEUIMessages.EquinoxPluginBlock_levelColumn);
                    bundlesList.append(' ');
                    bundlesList.append(startLevel);
                } else {
                    String defaultLevelColumn = //$NON-NLS-1$
                    NLS.bind(//$NON-NLS-1$
                    PDEUIMessages.EquinoxPluginBlock_defaultLevelColumn, //$NON-NLS-1$
                    "Default");
                    bundlesList.append(defaultLevelColumn);
                }
                if (//$NON-NLS-1$
                "start".equals(config[2])) {
                    //$NON-NLS-1$
                    bundlesList.append(//$NON-NLS-1$
                    ", ");
                    bundlesList.append(PDEUIMessages.EquinoxPluginBlock_autoColumn);
                }
                bundlesList.append('\n');
            }
            bundlesList.append('\n');
            // Confirm with user
            if (MessageDialog.openConfirm(PDEPlugin.getActiveWorkbenchShell(), PDEUIMessages.Product_PluginSection_RecommendedBundles_title, NLS.bind(PDEUIMessages.Product_PluginSection_RecommendedBundles_message, bundlesList.toString()))) {
                IPluginConfiguration[] pluginConfigs = new IPluginConfiguration[plugins.size()];
                // Build the model objects for the plugins and add to the product model.
                for (int i = 0; i < plugins.size(); i++) {
                    IProductModelFactory factory = product.getModel().getFactory();
                    IPluginConfiguration configuration = factory.createPluginConfiguration();
                    configuration.setId(plugins.get(i)[0]);
                    String startString = plugins.get(i)[1];
                    if (startString.length() > 0) {
                        configuration.setStartLevel(Integer.parseInt(startString));
                    }
                    configuration.setAutoStart("start".equals(//$NON-NLS-1$
                    plugins.get(i)[//$NON-NLS-1$
                    2]));
                    pluginConfigs[i] = configuration;
                }
                product.addPluginConfigurations(pluginConfigs);
                showControls();
            }
        } else {
            // The user already had all the recommended bundles
            MessageDialog.openInformation(PDEPlugin.getActiveWorkbenchShell(), PDEUIMessages.Product_PluginSection_RecommendedBundles_title, PDEUIMessages.Product_PluginSection_NoRecommendedBundles_message);
        }
    }

    @Override
    protected void selectionChanged(IStructuredSelection selection) {
        updateRemoveButtons(true, false);
    }

    private void addPlugin(String id) {
        IProduct product = getProduct();
        IProductModelFactory factory = product.getModel().getFactory();
        IPluginConfiguration configuration = factory.createPluginConfiguration();
        configuration.setId(id);
        product.addPluginConfigurations(new IPluginConfiguration[] { configuration });
        fConfigurationsTable.setSelection(new StructuredSelection(configuration));
        showControls();
    }

    private ILabelProvider getLabelProvider() {
        return new LabelProvider();
    }

    private void clearEditors() {
        Control oldEditor = fLevelColumnEditor.getEditor();
        if (oldEditor != null && !oldEditor.isDisposed())
            oldEditor.dispose();
        oldEditor = fAutoColumnEditor.getEditor();
        if (oldEditor != null && !oldEditor.isDisposed())
            oldEditor.dispose();
    }

    private void createEditors() {
        final Table table = fConfigurationsTable.getTable();
        fLevelColumnEditor = new TableEditor(table);
        fLevelColumnEditor.horizontalAlignment = SWT.CENTER;
        fLevelColumnEditor.minimumWidth = 40;
        fLevelColumnEditor.grabHorizontal = true;
        if (Util.isMac())
            fLevelColumnEditor.minimumHeight = 27;
        fAutoColumnEditor = new TableEditor(table);
        fAutoColumnEditor.horizontalAlignment = SWT.CENTER;
        fAutoColumnEditor.grabHorizontal = true;
        fAutoColumnEditor.minimumWidth = 50;
        table.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                showControls();
            }
        });
    }

    private void showControls() {
        // Clean up any previous editor control
        clearEditors();
        // Identify the selected row
        Table table = fConfigurationsTable.getTable();
        IStructuredSelection selection = (IStructuredSelection) fConfigurationsTable.getSelection();
        if (selection.isEmpty())
            return;
        final TableItem item = table.getSelection()[0];
        if (item != null && !isEditable())
            return;
        if (item != null) {
            final IPluginConfiguration ppc = (IPluginConfiguration) selection.getFirstElement();
            final Spinner spinner = new Spinner(table, SWT.BORDER);
            spinner.setMinimum(0);
            String level = item.getText(1);
            //$NON-NLS-1$
            int defaultLevel = level.length() == 0 || "default".equals(level) ? 0 : Integer.parseInt(level);
            spinner.setSelection(defaultLevel);
            spinner.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    int selection = spinner.getSelection();
                    item.setText(1, //$NON-NLS-1$
                    selection == 0 ? //$NON-NLS-1$
                    "default" : Integer.toString(selection));
                    ppc.setStartLevel(selection);
                }
            });
            fLevelColumnEditor.setEditor(spinner, item, 1);
            final CCombo combo = new CCombo(table, SWT.BORDER | SWT.READ_ONLY);
            //TODO is there need for the default options ??
            combo.setItems(new String[] { Boolean.toString(true), Boolean.toString(false) });
            combo.setText(item.getText(2));
            combo.pack();
            combo.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    item.setText(2, combo.getText());
                    ppc.setAutoStart(Boolean.valueOf(combo.getText()).booleanValue());
                }
            });
            fAutoColumnEditor.setEditor(combo, item, 2);
        }
    }

    private IProduct getProduct() {
        return getModel().getProduct();
    }

    private IProductModel getModel() {
        return (IProductModel) getPage().getPDEEditor().getAggregateModel();
    }

    @Override
    public void modelChanged(IModelChangedEvent e) {
        // No need to call super, handling world changed event here
        if (e.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
            handleGlobalRefresh();
            return;
        }
        Table table = fConfigurationsTable.getTable();
        int count = table.getItemCount();
        Object[] objects = e.getChangedObjects();
        boolean refreshRemove = false;
        boolean refreshRemoveAll = false;
        if (e.getChangeType() == IModelChangedEvent.INSERT) {
            if (count == 0) {
                refreshRemoveAll = true;
            }
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] instanceof IPluginConfiguration)
                    fConfigurationsTable.add(objects[i]);
            }
        } else if (e.getChangeType() == IModelChangedEvent.REMOVE) {
            refreshRemove = refreshRemoveAll = true;
            int index = table.getSelectionIndex();
            boolean global = false;
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] instanceof IPluginConfiguration)
                    fConfigurationsTable.remove(objects[i]);
                else if (objects[i] instanceof IProductPlugin) {
                    global = true;
                    break;
                }
            }
            if (global)
                handleGlobalRefresh();
            if (count == 0) {
                table.deselectAll();
                clearEditors();
            } else if (index < count) {
                table.setSelection(index);
            } else {
                table.setSelection(count - 1);
            }
        }
        getTablePart().setButtonEnabled(0, isEditable());
        updateRemoveButtons(refreshRemove, refreshRemoveAll);
    }

    private void handleGlobalRefresh() {
        fConfigurationsTable.setInput(getProduct());
        fConfigurationsTable.refresh();
    }

    private void updateRemoveButtons(boolean updateRemove, boolean updateRemoveAll) {
        TablePart tablePart = getTablePart();
        if (updateRemove) {
            ISelection selection = getViewerSelection();
            tablePart.setButtonEnabled(2, isEditable() && !selection.isEmpty());
        }
        int count = fConfigurationsTable.getTable().getItemCount();
        if (updateRemoveAll)
            tablePart.setButtonEnabled(3, isEditable() && count > 0);
    }
}
