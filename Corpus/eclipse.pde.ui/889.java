/*******************************************************************************
 * Copyright (c) 2014, 2015 Rapicorp Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rapicorp Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor.category;

import org.eclipse.pde.internal.ui.*;
import org.eclipse.pde.internal.ui.editor.FormLayoutFactory;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class RepositoryMetadataPage extends PDEFormPage {

    //$NON-NLS-1$
    public static final String PLUGIN_ID = "category-repository";

    public  RepositoryMetadataPage(FormEditor editor) {
        super(editor, PLUGIN_ID, PDEUIMessages.RepositoryPage_title);
    }

    @Override
    protected String getHelpResource() {
        return IHelpContextIds.UPDATES_PAGE;
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);
        ScrolledForm form = managedForm.getForm();
        FormToolkit toolkit = managedForm.getToolkit();
        form.setImage(PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_REPOSITORY_OBJ));
        form.setText(PDEUIMessages.RepositoryPage_title);
        fillBody(managedForm, toolkit);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(form.getBody(), IHelpContextIds.UPDATES_PAGE);
    }

    private void fillBody(IManagedForm managedForm, FormToolkit toolkit) {
        Composite body = managedForm.getForm().getBody();
        body.setLayout(FormLayoutFactory.createFormGridLayout(false, 1));
        managedForm.addPart(new RepositoryReferenceSection(this, body));
        managedForm.addPart(new DownloadStatsSection(this, body));
    }
}
