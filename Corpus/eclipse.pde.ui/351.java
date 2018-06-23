/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Code 9 Corporation - ongoing enhancements
 *     Anyware Technologies - ongoing enhancements
 *******************************************************************************/
package org.eclipse.pde.internal.ui.preferences;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.preferences.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.*;
import org.eclipse.pde.core.plugin.IPluginExtensionPoint;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PDEPreferencesManager;
import org.eclipse.pde.internal.core.schema.SchemaRegistry;
import org.eclipse.pde.internal.launching.*;
import org.eclipse.pde.internal.launching.launcher.OSGiFrameworkManager;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.pde.internal.ui.search.ShowDescriptionAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Provides the preference page for managing the default OSGi framework to use.
 *
 * @since 3.3
 */
public class OSGiFrameworkPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private IPluginExtensionPoint fPluginExtensionPoint = null;

    /**
	 * Label provider for the table viewer. Annotates the default framework with bold text
	 */
    class FrameworkLabelProvider extends LabelProvider implements IFontProvider {

        private Font font = null;

        @Override
        public Image getImage(Object element) {
            return PDEPluginImages.get(PDEPluginImages.OBJ_DESC_BUNDLE);
        }

        @Override
        public String getText(Object element) {
            if (element instanceof IConfigurationElement) {
                String name = ((IConfigurationElement) element).getAttribute(OSGiFrameworkManager.ATT_NAME);
                String id = ((IConfigurationElement) element).getAttribute(OSGiFrameworkManager.ATT_ID);
                return //$NON-NLS-1$
                fDefaultFramework.equals(id) ? //$NON-NLS-1$
                name + " " + PDEUIMessages.OSGiFrameworkPreferencePage_default : //$NON-NLS-1$
                name;
            }
            return super.getText(element);
        }

        @Override
        public Font getFont(Object element) {
            if (element instanceof IConfigurationElement) {
                String id = ((IConfigurationElement) element).getAttribute(OSGiFrameworkManager.ATT_ID);
                if (fDefaultFramework.equals(id)) {
                    if (this.font == null) {
                        Font dialogFont = JFaceResources.getDialogFont();
                        FontData[] fontData = dialogFont.getFontData();
                        for (int i = 0; i < fontData.length; i++) {
                            FontData data = fontData[i];
                            data.setStyle(SWT.BOLD);
                        }
                        Display display = getControl().getShell().getDisplay();
                        this.font = new Font(display, fontData);
                    }
                    return this.font;
                }
            }
            return null;
        }

        @Override
        public void dispose() {
            if (this.font != null) {
                this.font.dispose();
            }
            super.dispose();
        }
    }

    private CheckboxTableViewer fTableViewer;

    private String fDefaultFramework;

    /**
	 * Constructor
	 */
    public  OSGiFrameworkPreferencePage() {
        setDefaultFramework();
    }

    /**
	 * Restores the default framework setting from the PDE preferences
	 */
    private void setDefaultFramework() {
        PDEPreferencesManager preferenceManager = PDELaunchingPlugin.getDefault().getPreferenceManager();
        fDefaultFramework = preferenceManager.getString(ILaunchingPreferenceConstants.DEFAULT_OSGI_FRAMEOWRK);
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite comp = SWTFactory.createComposite(parent, 2, 1, GridData.FILL_BOTH);
        Link text = new Link(comp, SWT.WRAP);
        if (PDECore.getDefault().areModelsInitialized())
            fPluginExtensionPoint = PDECore.getDefault().getExtensionsRegistry().findExtensionPoint(OSGiFrameworkManager.POINT_ID);
        text.setText((fPluginExtensionPoint != null && SchemaRegistry.getSchemaURL(fPluginExtensionPoint) != null) ? PDEUIMessages.OSGiFrameworkPreferencePage_installed : PDEUIMessages.OSGiFrameworkPreferencePage_installed_nolink);
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        text.setLayoutData(gd);
        text.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                new ShowDescriptionAction(fPluginExtensionPoint, true).run();
            }
        });
        fTableViewer = new CheckboxTableViewer(new Table(comp, SWT.CHECK | SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION));
        fTableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
        fTableViewer.setContentProvider(ArrayContentProvider.getInstance());
        fTableViewer.setLabelProvider(new FrameworkLabelProvider());
        fTableViewer.setInput(PDELaunchingPlugin.getDefault().getOSGiFrameworkManager().getSortedFrameworks());
        fTableViewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                IConfigurationElement element = (IConfigurationElement) event.getElement();
                fTableViewer.setCheckedElements(new Object[] { element });
                fDefaultFramework = element.getAttribute(OSGiFrameworkManager.ATT_ID);
                fTableViewer.refresh();
            }
        });
        if (fDefaultFramework != null) {
            IConfigurationElement element = PDELaunchingPlugin.getDefault().getOSGiFrameworkManager().getFramework(fDefaultFramework);
            if (element != null) {
                fTableViewer.setCheckedElements(new Object[] { element });
            }
        }
        Dialog.applyDialogFont(parent);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IHelpContextIds.OSGI_PREFERENCE_PAGE);
        return comp;
    }

    @Override
    public boolean performOk() {
        IEclipsePreferences instancePrefs = InstanceScope.INSTANCE.getNode(IPDEConstants.PLUGIN_ID);
        IEclipsePreferences defaultPrefs = DefaultScope.INSTANCE.getNode(IPDEConstants.PLUGIN_ID);
        if (//$NON-NLS-1$
        defaultPrefs.get(ILaunchingPreferenceConstants.DEFAULT_OSGI_FRAMEOWRK, "").equals(fDefaultFramework)) {
            instancePrefs.remove(ILaunchingPreferenceConstants.DEFAULT_OSGI_FRAMEOWRK);
        } else {
            instancePrefs.put(ILaunchingPreferenceConstants.DEFAULT_OSGI_FRAMEOWRK, fDefaultFramework);
        }
        try {
            instancePrefs.flush();
        } catch (BackingStoreException e) {
            PDEPlugin.log(e);
        }
        return super.performOk();
    }

    @Override
    protected void performDefaults() {
        setDefaultFramework();
        fTableViewer.refresh();
    }

    @Override
    public void init(IWorkbench workbench) {
    }
}
