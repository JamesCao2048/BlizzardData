/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     EclipseSource Corporation - ongoing enhancements
 *******************************************************************************/
package org.eclipse.pde.internal.ui.templates.osgi;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.core.plugin.IPluginReference;
import org.eclipse.pde.internal.ui.templates.*;

public class HelloOSGiServiceTemplate extends PDETemplateSection {

    //$NON-NLS-1$
    public static final String LANGUAGE = "language";

    //$NON-NLS-1$
    public static final String WORD1 = "word1";

    //$NON-NLS-1$
    public static final String WORD2 = "word2";

    //$NON-NLS-1$
    public static final String WORD3 = "word3";

    //$NON-NLS-1$
    public static final String KEY_APPLICATION_CLASS = "applicationClass";

    public  HelloOSGiServiceTemplate() {
        setPageCount(1);
        addOption(LANGUAGE, PDETemplateMessages.HelloOSGiServiceTemplate_greeting, PDETemplateMessages.HelloOSGiServiceTemplate_howdy, 0);
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
        page.setTitle(PDETemplateMessages.HelloOSGiServiceTemplate_pageTitle);
        page.setDescription(PDETemplateMessages.HelloOSGiServiceTemplate_pageDescription);
        wizard.addPage(page);
        markPagesAdded();
    }

    @Override
    public String getSectionId() {
        //$NON-NLS-1$
        return "helloOSGiService";
    }

    @Override
    protected // do nothing
    void updateModel(// do nothing
    IProgressMonitor monitor) {
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
