/*******************************************************************************
 * Copyright (c) 2008, 2009 Code 9 Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Code 9 Corporation - initial API and implementation
 *     Rafael Oliveira Nobrega <rafael.oliveira@gmail.com> - bug 244558
 *******************************************************************************/
package org.eclipse.pde.internal.ui.templates.osgi;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginReference;
import org.eclipse.pde.internal.ui.templates.*;
import org.eclipse.pde.ui.IFieldData;

public class HelloServiceComponentTemplate extends PDETemplateSection {

    //$NON-NLS-1$
    public static final String COMMAND = "command";

    //$NON-NLS-1$
    public static final String WORD1 = "word1";

    //$NON-NLS-1$
    public static final String WORD2 = "word2";

    //$NON-NLS-1$
    public static final String WORD3 = "word3";

    private String packageName = null;

    public  HelloServiceComponentTemplate() {
        setPageCount(1);
        addOption(COMMAND, PDETemplateMessages.HelloServiceComponentTemplate_commandTitle, PDETemplateMessages.HelloServiceComponentTemplate_command, 0);
        //$NON-NLS-1$
        addOption(WORD1, PDETemplateMessages.HelloOSGiServiceTemplate_word1, "osgi", 0);
        //$NON-NLS-1$
        addOption(WORD2, PDETemplateMessages.HelloOSGiServiceTemplate_word2, "eclipse", 0);
        //$NON-NLS-1$
        addOption(WORD3, PDETemplateMessages.HelloOSGiServiceTemplate_word3, "equinox", 0);
    }

    @Override
    public void addPages(Wizard wizard) {
        WizardPage page = createPage(0, IHelpContextIds.TEMPLATE_RCP_MAIL);
        page.setTitle(PDETemplateMessages.DSTemplate_pageTitle);
        page.setDescription(PDETemplateMessages.DSTemplate_pageDescription);
        wizard.addPage(page);
        markPagesAdded();
    }

    @Override
    public String getSectionId() {
        //$NON-NLS-1$
        return "helloOSGiServiceComponent";
    }

    @Override
    protected // do nothing
    void updateModel(// do nothing
    IProgressMonitor monitor) {
        //$NON-NLS-1$ //$NON-NLS-2$]
        setManifestHeader("Service-Component", "OSGI-INF/*.xml");
    }

    @Override
    public String getUsedExtensionPoint() {
        return null;
    }

    @Override
    public boolean isDependentOnParentWizard() {
        return true;
    }

    @Override
    public int getNumberOfWorkUnits() {
        return super.getNumberOfWorkUnits() + 1;
    }

    @Override
    public IPluginReference[] getDependencies(String schemaVersion) {
        return new IPluginReference[0];
    }

    @Override
    protected void initializeFields(IFieldData data) {
        // In a new project wizard, we don't know this yet - the
        // model has not been created
        String packageName = getFormattedPackageName(data.getId());
        initializeOption(KEY_PACKAGE_NAME, packageName);
        this.packageName = getFormattedPackageName(data.getId());
    }

    @Override
    public void initializeFields(IPluginModelBase model) {
        String id = model.getPluginBase().getId();
        String packageName = getFormattedPackageName(id);
        initializeOption(KEY_PACKAGE_NAME, packageName);
        this.packageName = getFormattedPackageName(id);
    }

    @Override
    public String getStringOption(String name) {
        if (name.equals(KEY_PACKAGE_NAME)) {
            return packageName;
        }
        return super.getStringOption(name);
    }
}
