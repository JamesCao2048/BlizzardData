/*******************************************************************************
 * Copyright (c) 2008, 2015 Code 9 Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Code 9 Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor.product;

import org.eclipse.pde.internal.ui.*;
import org.eclipse.pde.internal.ui.editor.FormLayoutFactory;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class LicensingPage extends PDEFormPage {

    //$NON-NLS-1$
    public static final String PAGE_ID = "licensing";

    /**
	 * @param productEditor
	 * @param useFeatures
	 */
    public  LicensingPage(ProductEditor editor) {
        super(editor, PAGE_ID, PDEUIMessages.LicensingPage_title);
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);
        ScrolledForm form = managedForm.getForm();
        FormToolkit toolkit = managedForm.getToolkit();
        form.setImage(PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_LICENSE_OBJ));
        form.setText(PDEUIMessages.LicensingPage_title);
        fillBody(managedForm, toolkit);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(form.getBody(), IHelpContextIds.LICENSING_PAGE);
    }

    private void fillBody(IManagedForm managedForm, FormToolkit toolkit) {
        Composite body = managedForm.getForm().getBody();
        body.setLayout(FormLayoutFactory.createFormGridLayout(false, 2));
        managedForm.addPart(new LicenseSection(this, body));
    }
}
