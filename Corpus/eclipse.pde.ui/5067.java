/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.wizards.extension;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.pde.internal.ui.elements.ElementList;

public class ExtensionEditorWizard extends Wizard {

    //$NON-NLS-1$
    public static final String PLUGIN_POINT = "newExtension";

    private ExtensionEditorSelectionPage pointPage;

    private IPluginModelBase model;

    private IProject project;

    private IStructuredSelection selection;

    private ElementList wizards;

    public  ExtensionEditorWizard(IProject project, IPluginModelBase model, IStructuredSelection selection) {
        setDialogSettings(PDEPlugin.getDefault().getDialogSettings());
        setDefaultPageImageDescriptor(PDEPluginImages.DESC_NEWEX_WIZ);
        this.model = model;
        this.project = project;
        this.selection = selection;
        setForcePreviousAndNextButtons(true);
        setWindowTitle(PDEUIMessages.ExtensionEditorWizard_wtitle);
        PDEPlugin.getDefault().getLabelProvider().connect(this);
        loadWizardCollection();
    }

    @Override
    public void addPages() {
        pointPage = new ExtensionEditorSelectionPage(wizards);
        pointPage.init(project, model.getPluginBase(), selection);
        addPage(pointPage);
    }

    private void loadWizardCollection() {
        NewExtensionRegistryReader reader = new NewExtensionRegistryReader(true);
        wizards = reader.readRegistry(PDEPlugin.getPluginId(), PLUGIN_POINT, true);
    }

    @Override
    public boolean performFinish() {
        return true;
    }

    @Override
    public void dispose() {
        super.dispose();
        PDEPlugin.getDefault().getLabelProvider().disconnect(this);
    }
}
