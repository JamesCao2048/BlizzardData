/*******************************************************************************
 *  Copyright (c) 2005, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.wizards.product;

import java.util.TreeSet;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.core.plugin.*;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.internal.ui.IHelpContextIds;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.dialogs.PluginSelectionDialog;
import org.eclipse.pde.internal.ui.search.ShowDescriptionAction;
import org.eclipse.pde.internal.ui.util.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class ProductIntroWizardPage extends WizardPage implements IHyperlinkListener {

    private Text fPluginText;

    private Text fIntroIdText;

    private TreeSet<String> fIntroIds;

    private IProduct fProduct;

    private ModifyListener fListener = new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
            validatePage();
        }
    };

    public  ProductIntroWizardPage(String pageName, IProduct product) {
        super(pageName);
        setTitle(PDEUIMessages.ProductIntroWizardPage_title);
        setDescription(PDEUIMessages.ProductIntroWizardPage_description);
        fIntroIds = getCurrentIntroIds();
        fProduct = product;
    }

    @Override
    public void createControl(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 20;
        comp.setLayout(layout);
        FormToolkit toolkit = new FormToolkit(parent.getDisplay());
        createProductGroup(toolkit, comp);
        toolkit.dispose();
        setControl(comp);
        setPageComplete(getPluginId() != null);
        Dialog.applyDialogFont(comp);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(comp, IHelpContextIds.PRODUCT_DEFINITIONS_WIZARD);
    }

    private void createProductGroup(FormToolkit toolkit, Composite comp) {
        Group group = new Group(comp, SWT.NONE);
        group.setText(PDEUIMessages.ProductIntroWizardPage_groupText);
        group.setLayout(new GridLayout(3, false));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        FormText text = toolkit.createFormText(group, false);
        text.setText(PDEUIMessages.ProductIntroWizardPage_formText, true, false);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        gd.widthHint = 400;
        text.setLayoutData(gd);
        text.setBackground(null);
        text.addHyperlinkListener(this);
        Label label = new Label(group, SWT.NONE);
        label.setText(PDEUIMessages.ProductIntroWizardPage_targetLabel);
        fPluginText = new Text(group, SWT.SINGLE | SWT.BORDER);
        fPluginText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Button button = new Button(group, SWT.PUSH);
        button.setText(PDEUIMessages.ProductIntroWizardPage_browse);
        SWTUtil.setButtonDimensionHint(button);
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleBrowse();
            }
        });
        label = new Label(group, SWT.NONE);
        label.setText(PDEUIMessages.ProductIntroWizardPage_introLabel);
        fIntroIdText = new Text(group, SWT.SINGLE | SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        fIntroIdText.setLayoutData(gd);
        String pluginId = getPluginId();
        if (pluginId != null) {
            fPluginText.setText(pluginId);
            fIntroIdText.setText(getAvailableIntroId(pluginId));
        }
        fPluginText.addModifyListener(fListener);
        fIntroIdText.addModifyListener(fListener);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            fPluginText.setVisible(visible);
            fPluginText.setFocus();
        }
    }

    private void validatePage() {
        String error = null;
        String pluginId = getDefiningPlugin();
        IPluginModelBase model = PluginRegistry.findModel(pluginId);
        if (model == null) {
            error = PDEUIMessages.ProductDefinitonWizardPage_noPlugin;
        } else if (model.getUnderlyingResource() == null) {
            error = PDEUIMessages.ProductDefinitonWizardPage_notInWorkspace;
        } else if (pluginId.length() == 0) {
            error = PDEUIMessages.ProductIntroWizardPage_targetNotSet;
        }
        validateId(error);
    }

    private void validateId(String error) {
        if (error == null) {
            String id = fIntroIdText.getText().trim();
            if (id.length() == 0)
                error = PDEUIMessages.ProductIntroWizardPage_introNotSet;
            if (error == null)
                for (int i = 0; i < id.length(); i++) if (!//$NON-NLS-1$
                id.substring(i, i + 1).matches(//$NON-NLS-1$
                "[a-zA-Z0-9.]"))
                    error = PDEUIMessages.ProductIntroWizardPage_invalidIntroId;
            if (error == null && fIntroIds.contains(id))
                error = PDEUIMessages.ProductIntroWizardPage_introIdExists;
        }
        setErrorMessage(error);
        setPageComplete(error == null);
    }

    private void handleBrowse() {
        PluginSelectionDialog dialog = new PluginSelectionDialog(getShell(), PluginRegistry.getWorkspaceModels(), false);
        if (dialog.open() == Window.OK) {
            IPluginModelBase model = (IPluginModelBase) dialog.getFirstResult();
            String id = model.getPluginBase().getId();
            fPluginText.setText(id);
            fIntroIdText.setText(getAvailableIntroId(id));
        }
    }

    private String getAvailableIntroId(String id) {
        //$NON-NLS-1$
        String introId = "intro";
        //$NON-NLS-1$
        String numString = "";
        int idNum = 1;
        while (//$NON-NLS-1$
        fIntroIds.contains(id + "." + introId + numString)) {
            numString = Integer.toString(idNum++);
        }
        //$NON-NLS-1$
        return id + "." + introId + numString;
    }

    private TreeSet<String> getCurrentIntroIds() {
        String introId;
        TreeSet<String> result = new TreeSet();
        //$NON-NLS-1$
        IExtension[] extensions = PDECore.getDefault().getExtensionsRegistry().findExtensions("org.eclipse.ui.intro", true);
        for (int i = 0; i < extensions.length; i++) {
            IConfigurationElement[] children = extensions[i].getConfigurationElements();
            for (int j = 0; j < children.length; j++) {
                if (//$NON-NLS-1$
                "intro".equals(children[j].getName())) {
                    introId = //$NON-NLS-1$
                    children[j].getAttribute(//$NON-NLS-1$
                    "id");
                    if (introId != null)
                        result.add(introId);
                }
            }
        }
        return result;
    }

    public String getDefiningPlugin() {
        return fPluginText.getText().trim();
    }

    public String getIntroId() {
        return fIntroIdText.getText().trim();
    }

    private String getPluginId() {
        IProject project = fProduct.getModel().getUnderlyingResource().getProject();
        IPluginModelBase model = PluginRegistry.findModel(project);
        return (model == null) ? null : model.getPluginBase().getId();
    }

    @Override
    public void linkEntered(HyperlinkEvent e) {
    }

    @Override
    public void linkExited(HyperlinkEvent e) {
    }

    @Override
    public void linkActivated(HyperlinkEvent e) {
        //$NON-NLS-1$
        String extPoint = "org.eclipse.ui." + e.getHref().toString();
        IPluginExtensionPoint point = PDECore.getDefault().getExtensionsRegistry().findExtensionPoint(extPoint);
        if (point != null)
            new ShowDescriptionAction(point, true).run();
    }
}
