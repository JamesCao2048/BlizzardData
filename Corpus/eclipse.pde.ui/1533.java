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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.pde.core.plugin.IPluginExtensionPoint;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class NewSchemaFileWizard extends Wizard implements INewWizard {

    private NewSchemaFileMainPage mainPage;

    private IContainer container;

    private IPluginExtensionPoint point;

    private boolean isPluginIdFinal;

    public  NewSchemaFileWizard() {
        this(null, null, false);
    }

    public  NewSchemaFileWizard(IProject project, IPluginExtensionPoint point, boolean isFinalPluginId) {
        initialize();
        this.container = project;
        this.point = point;
        this.isPluginIdFinal = isFinalPluginId;
    }

    public void initialize() {
        setDialogSettings(getSettingsSection());
        setDefaultPageImageDescriptor(PDEPluginImages.DESC_EXT_POINT_SCHEMA_WIZ);
        setWindowTitle(PDEUIMessages.NewSchemaFileWizard_wtitle);
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        mainPage = new NewSchemaFileMainPage(container, point, isPluginIdFinal);
        addPage(mainPage);
    }

    private IDialogSettings getSettingsSection() {
        IDialogSettings root = PDEPlugin.getDefault().getDialogSettings();
        //$NON-NLS-1$
        IDialogSettings section = root.getSection("newExtensionPointWizard");
        if (section == null)
            //$NON-NLS-1$
            section = root.addNewSection("newExtensionPointWizard");
        return section;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        Object sel = selection.getFirstElement();
        if (sel instanceof IJavaProject) {
            container = ((IJavaProject) sel).getProject();
        } else if (sel instanceof IContainer)
            container = (IContainer) sel;
    }

    @Override
    public boolean performFinish() {
        return mainPage.finish();
    }
}
