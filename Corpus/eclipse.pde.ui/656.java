/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars.Vogel <Lars.Vogel@vogella.com> - Bug 486247, 486261
 *******************************************************************************/
package org.eclipse.pde.internal.ui.templates.ide;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.core.plugin.*;
import org.eclipse.pde.internal.ui.templates.*;
import org.eclipse.pde.ui.IFieldData;
import org.eclipse.pde.ui.templates.PluginReference;

public class PreferencePageTemplate extends PDETemplateSection {

    //$NON-NLS-1$
    private static final String KEY_PAGE_NAME = "pageName";

    //$NON-NLS-1$
    private static final String KEY_PAGE_CLASS_NAME = "pageClassName";

    public  PreferencePageTemplate() {
        setPageCount(1);
        createOptions();
    }

    @Override
    public String getSectionId() {
        //$NON-NLS-1$
        return "preferences";
    }

    /*
	 * @see ITemplateSection#getNumberOfWorkUnits()
	 */
    @Override
    public int getNumberOfWorkUnits() {
        return super.getNumberOfWorkUnits() + 1;
    }

    private void createOptions() {
        // first page
        addOption(KEY_PACKAGE_NAME, PDETemplateMessages.PreferencePageTemplate_packageName, (String) null, 0);
        addOption(//$NON-NLS-1$
        KEY_PAGE_CLASS_NAME, //$NON-NLS-1$
        PDETemplateMessages.PreferencePageTemplate_className, //$NON-NLS-1$
        "SamplePreferencePage", 0);
        addOption(KEY_PAGE_NAME, PDETemplateMessages.PreferencePageTemplate_pageName, PDETemplateMessages.PreferencePageTemplate_defaultPageName, 0);
    }

    @Override
    protected void initializeFields(IFieldData data) {
        // In a new project wizard, we don't know this yet - the
        // model has not been created
        String id = data.getId();
        initializeOption(KEY_PACKAGE_NAME, getFormattedPackageName(id));
    }

    @Override
    public void initializeFields(IPluginModelBase model) {
        // In the new extension wizard, the model exists so
        // we can initialize directly from it
        String pluginId = model.getPluginBase().getId();
        initializeOption(KEY_PACKAGE_NAME, getFormattedPackageName(pluginId));
    }

    @Override
    protected String getTemplateDirectory() {
        String schemaVersion = model.getPluginBase().getSchemaVersion();
        //$NON-NLS-1$ //$NON-NLS-2$
        return "templates_" + (schemaVersion == null ? "3.0" : schemaVersion);
    }

    @Override
    public boolean isDependentOnParentWizard() {
        return true;
    }

    @Override
    public IPluginReference[] getDependencies(String schemaVersion) {
        PluginReference[] deps = new PluginReference[2];
        //$NON-NLS-1$
        deps[0] = new PluginReference("org.eclipse.core.runtime");
        //$NON-NLS-1$
        deps[1] = new PluginReference("org.eclipse.ui");
        return deps;
    }

    @Override
    public void addPages(Wizard wizard) {
        WizardPage page = createPage(0, IHelpContextIds.TEMPLATE_PREFERENCE_PAGE);
        page.setTitle(PDETemplateMessages.PreferencePageTemplate_title);
        page.setDescription(PDETemplateMessages.PreferencePageTemplate_desc);
        wizard.addPage(page);
        markPagesAdded();
    }

    @Override
    public String getUsedExtensionPoint() {
        //$NON-NLS-1$
        return "org.eclipse.ui.preferencePages";
    }

    @Override
    protected void updateModel(IProgressMonitor monitor) throws CoreException {
        IPluginBase plugin = model.getPluginBase();
        IPluginExtension extension = createExtension(getUsedExtensionPoint(), true);
        IPluginModelFactory factory = model.getPluginFactory();
        //$NON-NLS-1$
        String fullClassName = getStringOption(KEY_PACKAGE_NAME) + "." + getStringOption(KEY_PAGE_CLASS_NAME);
        IPluginElement pageElement = factory.createElement(extension);
        //$NON-NLS-1$
        pageElement.setName("page");
        //$NON-NLS-1$
        pageElement.setAttribute("id", fullClassName);
        //$NON-NLS-1$
        pageElement.setAttribute("name", getStringOption(KEY_PAGE_NAME));
        //$NON-NLS-1$
        pageElement.setAttribute("class", fullClassName);
        extension.add(pageElement);
        if (!extension.isInTheModel())
            plugin.add(extension);
        //$NON-NLS-1$
        IPluginExtension extension2 = createExtension("org.eclipse.core.runtime.preferences", true);
        IPluginElement prefElement = factory.createElement(extension);
        //$NON-NLS-1$
        prefElement.setName("initializer");
        //$NON-NLS-1$ //$NON-NLS-2$
        prefElement.setAttribute("class", getStringOption(KEY_PACKAGE_NAME) + ".PreferenceInitializer");
        extension2.add(prefElement);
        if (!extension2.isInTheModel())
            plugin.add(extension2);
    }

    @Override
    protected String getFormattedPackageName(String id) {
        String packageName = super.getFormattedPackageName(id);
        if (packageName.length() != 0)
            //$NON-NLS-1$
            return packageName + ".preferences";
        //$NON-NLS-1$
        return "preferences";
    }
}
