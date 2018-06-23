/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;

public class NewSourceFolderCreationWizard extends NewElementWizard {

    private NewSourceFolderWizardPage fPage;

    public  NewSourceFolderCreationWizard() {
        super();
        setDefaultPageImageDescriptor(JavaPluginImages.DESC_WIZBAN_NEWSRCFOLDR);
        setDialogSettings(JavaPlugin.getDefault().getDialogSettings());
        setWindowTitle(NewWizardMessages.NewSourceFolderCreationWizard_title);
    }

    /*
	 * @see Wizard#addPages
	 */
    @Override
    public void addPages() {
        super.addPages();
        fPage = new NewSourceFolderWizardPage();
        addPage(fPage);
        fPage.init(getSelection());
    }

    @Override
    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
        // use the full progress monitor
        fPage.createPackageFragmentRoot(monitor);
    }

    @Override
    public boolean performFinish() {
        boolean res = super.performFinish();
        if (res) {
            selectAndReveal(fPage.getCorrespondingResource());
        }
        return res;
    }

    @Override
    public IJavaElement getCreatedElement() {
        return fPage.getNewPackageFragmentRoot();
    }
}
