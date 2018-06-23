/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.correction;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModelBase;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.editor.plugin.ManifestEditor;
import org.eclipse.pde.internal.ui.util.SWTUtil;
import org.eclipse.pde.internal.ui.wizards.extension.NewExtensionPointWizard;
import org.eclipse.ui.IEditorPart;

public class AddNewExtensionPointResolution extends AbstractPDEMarkerResolution {

    public  AddNewExtensionPointResolution(int type) {
        super(type);
    }

    @Override
    public String getLabel() {
        return PDEUIMessages.AddNewExtensionPointResolution_description;
    }

    @Override
    protected void createChange(IBaseModel model) {
        IEditorPart part = PDEPlugin.getActivePage().getActiveEditor();
        if (part instanceof ManifestEditor) {
            ManifestEditor editor = (ManifestEditor) part;
            IBaseModel base = editor.getAggregateModel();
            if (base instanceof IBundlePluginModelBase) {
                IBundlePluginModelBase pluginModel = (IBundlePluginModelBase) base;
                NewExtensionPointWizard wizard = new NewExtensionPointWizard(pluginModel.getUnderlyingResource().getProject(), pluginModel, editor) {

                    @Override
                    public boolean performFinish() {
                        return super.performFinish();
                    }
                };
                WizardDialog dialog = new WizardDialog(PDEPlugin.getActiveWorkbenchShell(), wizard);
                dialog.create();
                SWTUtil.setDialogSize(dialog, 400, 450);
                dialog.open();
            }
        }
    }
}
