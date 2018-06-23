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
package org.eclipse.pde.internal.ui.wizards.site;

import java.io.File;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.pde.internal.ui.IHelpContextIds;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

public class NewSiteProjectCreationPage extends WizardNewProjectCreationPage {

    private Button fWebButton;

    protected Text fWebText;

    private Label fWebLabel;

    /**
	 * Creates a new project creation wizard page.
	 *
	 * @param pageName the name of this page
	 */
    public  NewSiteProjectCreationPage(String pageName) {
        super(pageName);
    }

    /** (non-Javadoc)
	 * Method declared on IDialogPage.
	 */
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        Composite control = (Composite) getControl();
        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 15;
        control.setLayout(layout);
        Group webGroup = new Group(control, SWT.NULL);
        webGroup.setText(PDEUIMessages.NewSiteProjectCreationPage_webTitle);
        initializeDialogUnits(parent);
        layout = new GridLayout();
        layout.numColumns = 2;
        webGroup.setLayout(layout);
        webGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fWebButton = new Button(webGroup, SWT.CHECK);
        fWebButton.setText(PDEUIMessages.SiteHTML_checkLabel);
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        fWebButton.setLayoutData(gd);
        fWebButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                fWebLabel.setEnabled(fWebButton.getSelection());
                fWebText.setEnabled(fWebButton.getSelection());
                setPageComplete(validatePage());
            }
        });
        fWebLabel = new Label(webGroup, SWT.NULL);
        fWebLabel.setText(PDEUIMessages.SiteHTML_webLabel);
        fWebLabel.setEnabled(false);
        fWebText = new Text(webGroup, SWT.BORDER);
        fWebText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        //$NON-NLS-1$
        fWebText.setText("web");
        fWebText.setEnabled(false);
        fWebText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                setPageComplete(validatePage());
            }
        });
        setPageComplete(validatePage());
        setControl(control);
        Dialog.applyDialogFont(control);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(control, IHelpContextIds.NEW_SITE_MAIN);
    }

    public String getWebLocation() {
        if (fWebButton == null)
            return null;
        if (!fWebButton.getSelection())
            return null;
        String text = fWebText.getText();
        if (//$NON-NLS-1$
        text.startsWith(File.separator) || text.startsWith("/"))
            text = text.substring(1);
        if (//$NON-NLS-1$
        text.endsWith(File.separator) || text.endsWith("/"))
            text = text.substring(0, text.length() - 1);
        return text.trim();
    }

    @Override
    protected boolean validatePage() {
        if (!super.validatePage())
            return false;
        String webLocation = getWebLocation();
        if (webLocation != null && webLocation.trim().length() == 0) {
            setErrorMessage(PDEUIMessages.SiteHTML_webError);
            return false;
        }
        return true;
    }
}
