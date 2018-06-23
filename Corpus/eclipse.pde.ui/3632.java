/*******************************************************************************
* Copyright (c) 2009, 2015 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.pde.internal.ui.wizards.category;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.pde.internal.ui.IHelpContextIds;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.wizards.PDEWizardNewFileCreationPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

public class CategoryDefinitionWizardPage extends PDEWizardNewFileCreationPage {

    protected static final int USE_EMPTY = 0;

    protected static final int USE_DEFAULT = 1;

    protected static final int USE_CURRENT_TP = 2;

    protected static final int USE_EXISTING_TARGET = 3;

    //$NON-NLS-1$
    private static String EXTENSION = "xml";

    public  CategoryDefinitionWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName, selection);
        setTitle(PDEUIMessages.CategoryDefinitionWizardPage_title);
        setDescription(PDEUIMessages.CategoryDefinitionWizardPage_description);
        // Force the file name to be category.xml
        setFileExtension(EXTENSION);
        //$NON-NLS-1$
        setFileName("category.xml");
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IHelpContextIds.CATEGORY_FILE_PAGE);
    }

    @Override
    protected void createAdvancedControls(Composite parent) {
    // do nothing
    }
}
