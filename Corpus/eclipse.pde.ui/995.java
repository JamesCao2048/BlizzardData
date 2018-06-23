/*******************************************************************************
 *  Copyright (c) 2006, 2007 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.templates.osgi;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.core.plugin.IPluginReference;
import org.eclipse.pde.internal.ui.templates.*;

public class OSGiPreferencesServiceTemplate extends PDETemplateSection {

    //$NON-NLS-1$
    public static final String COLOR = "color";

    public  OSGiPreferencesServiceTemplate() {
        setPageCount(1);
        addOption(COLOR, PDETemplateMessages.OSGiPreferencesServiceTemplate_label, PDETemplateMessages.OSGiPreferencesServiceTemplate_value, 0);
    }

    @Override
    public void addPages(Wizard wizard) {
        WizardPage page = createPage(0, IHelpContextIds.TEMPLATE_RCP_MAIL);
        page.setTitle(PDETemplateMessages.OSGiPreferencesServiceTemplate_pageTitle);
        page.setDescription(PDETemplateMessages.OSGiPreferencesServiceTemplate_pageDescription);
        wizard.addPage(page);
        markPagesAdded();
    }

    @Override
    public String getSectionId() {
        //$NON-NLS-1$
        return "OSGiPreferencesService";
    }

    @Override
    protected void updateModel(IProgressMonitor monitor) {
    // do nothing
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
}
